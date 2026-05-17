#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
LOGIN="${SMOKE_LOGIN:-admin}"
PASSWORD="${SMOKE_PASSWORD:-}"

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || { echo "Missing required command: $1"; exit 1; }
}

require_cmd curl
require_cmd jq

if [[ -z "$PASSWORD" ]]; then
  echo "SMOKE_PASSWORD is required"
  exit 1
fi

api_json() {
  local method="$1"
  local url="$2"
  local auth_header="${3:-}"
  local data="${4:-}"
  local body_file header_file http_code content_type
  body_file="$(mktemp)"
  header_file="$(mktemp)"

  if [[ -n "$data" && -n "$auth_header" ]]; then
    http_code="$(curl -sS -o "$body_file" -D "$header_file" -w "%{http_code}" -X "$method" "$url" \
      -H "$auth_header" -H "Content-Type: application/json" -d "$data")"
  elif [[ -n "$data" ]]; then
    http_code="$(curl -sS -o "$body_file" -D "$header_file" -w "%{http_code}" -X "$method" "$url" \
      -H "Content-Type: application/json" -d "$data")"
  elif [[ -n "$auth_header" ]]; then
    http_code="$(curl -sS -o "$body_file" -D "$header_file" -w "%{http_code}" -X "$method" "$url" \
      -H "$auth_header")"
  else
    http_code="$(curl -sS -o "$body_file" -D "$header_file" -w "%{http_code}" -X "$method" "$url")"
  fi

  if [[ ! "$http_code" =~ ^2 ]]; then
    echo "HTTP failure: $method $url -> $http_code"
    cat "$body_file"
    rm -f "$body_file" "$header_file"
    exit 1
  fi

  content_type="$(grep -i '^content-type:' "$header_file" | tail -n 1 | tr -d '\r' | cut -d':' -f2- | xargs || true)"
  if [[ "$content_type" != application/json* ]]; then
    echo "Invalid content-type for $method $url: ${content_type:-<empty>}"
    cat "$body_file"
    rm -f "$body_file" "$header_file"
    exit 1
  fi

  cat "$body_file"
  rm -f "$body_file" "$header_file"
}

echo "[1/9] Login"
AUTH_JSON="$(api_json "POST" "$BASE_URL/api/v1/auth/login" "" "{\"login\":\"$LOGIN\",\"password\":\"$PASSWORD\"}")"
TOKEN=$(echo "$AUTH_JSON" | jq -r '.accessToken')
USER_ID=$(echo "$AUTH_JSON" | jq -r '.userId')
if [[ -z "$TOKEN" || "$TOKEN" == "null" ]]; then
  echo "Login failed"; echo "$AUTH_JSON"; exit 1
fi
AUTH_HEADER="Authorization: Bearer $TOKEN"

RAND=$(date +%s)

echo "[2/9] Create supplier"
SUPPLIER_JSON="$(api_json "POST" "$BASE_URL/api/v1/suppliers" "$AUTH_HEADER" "{
    \"code\":\"SUP-$RAND\",
    \"name\":\"Supplier Smoke $RAND\",
    \"country\":\"AO\",
    \"currency\":\"USD\",
    \"active\":true
  }")"
SUPPLIER_ID=$(echo "$SUPPLIER_JSON" | jq -r '.id')
if [[ -z "$SUPPLIER_ID" || "$SUPPLIER_ID" == "null" ]]; then
  echo "Supplier creation failed"; echo "$SUPPLIER_JSON"; exit 1
fi

echo "[3/9] Create item"
ITEM_JSON="$(api_json "POST" "$BASE_URL/api/v1/items" "$AUTH_HEADER" "{
    \"code\":\"ITM-$RAND\",
    \"description\":\"Item Smoke $RAND\",
    \"unitOfMeasure\":\"UN\",
    \"criticality\":\"MEDIO\",
    \"minStock\":1,
    \"maxStock\":10,
    \"active\":true
  }")"
ITEM_ID=$(echo "$ITEM_JSON" | jq -r '.id')
if [[ -z "$ITEM_ID" || "$ITEM_ID" == "null" ]]; then
  echo "Item creation failed"; echo "$ITEM_JSON"; exit 1
fi

echo "[4/9] Create requisition"
REQ_JSON="$(api_json "POST" "$BASE_URL/api/v1/requisitions" "$AUTH_HEADER" "{
    \"urgency\":\"NORMAL\",
    \"justification\":\"Smoke test requisition $RAND\",
    \"notes\":\"auto\"
  }")"
REQ_ID=$(echo "$REQ_JSON" | jq -r '.id')
if [[ -z "$REQ_ID" || "$REQ_ID" == "null" ]]; then
  echo "Requisition creation failed"; echo "$REQ_JSON"; exit 1
fi

echo "[5/9] Approve requisition"
APPROVE_JSON="$(api_json "PATCH" "$BASE_URL/api/v1/requisitions/$REQ_ID/approve" "$AUTH_HEADER")"
REQ_STATUS=$(echo "$APPROVE_JSON" | jq -r '.status')
if [[ "$REQ_STATUS" != "APROVADA" ]]; then
  echo "Requisition approve failed"; echo "$APPROVE_JSON"; exit 1
fi

echo "[6/9] Create order"
ORDER_JSON="$(api_json "POST" "$BASE_URL/api/v1/orders" "$AUTH_HEADER" "{
    \"code\":\"PO-$RAND\",
    \"supplierId\":\"$SUPPLIER_ID\",
    \"requesterId\":\"$USER_ID\",
    \"requisitionId\":\"$REQ_ID\",
    \"notes\":\"auto\",
    \"items\":[
      {\"itemId\":\"$ITEM_ID\",\"quantity\":2,\"unitPrice\":5.5}
    ]
  }")"
ORDER_ID=$(echo "$ORDER_JSON" | jq -r '.id')
ORDER_ITEM_ID=$(echo "$ORDER_JSON" | jq -r '.items[0].id')
if [[ -z "$ORDER_ID" || "$ORDER_ID" == "null" || -z "$ORDER_ITEM_ID" || "$ORDER_ITEM_ID" == "null" ]]; then
  echo "Order creation failed"; echo "$ORDER_JSON"; exit 1
fi

echo "[7/9] Load lookup data"
WAREHOUSE_ID=$(api_json "GET" "$BASE_URL/api/v1/lookups/warehouses" "$AUTH_HEADER" | jq -r '.[0].id')
if [[ -z "$WAREHOUSE_ID" || "$WAREHOUSE_ID" == "null" ]]; then
  echo "Warehouse lookup failed"; exit 1
fi

echo "[8/9] Receive order"
RECEIVE_JSON="$(api_json "POST" "$BASE_URL/api/v1/orders/$ORDER_ID/receive" "$AUTH_HEADER" "{
    \"warehouseId\":\"$WAREHOUSE_ID\",
    \"receiverId\":\"$USER_ID\",
    \"items\":[
      {\"orderItemId\":\"$ORDER_ITEM_ID\",\"quantity\":2}
    ]
  }")"
ORDER_STATUS=$(echo "$RECEIVE_JSON" | jq -r '.status')
if [[ "$ORDER_STATUS" != "ENTREGUE" && "$ORDER_STATUS" != "PARCIALMENTE_RECEBIDO" ]]; then
  echo "Order receive failed"; echo "$RECEIVE_JSON"; exit 1
fi

echo "[9/9] Validate stock and dashboard"
STOCK_JSON="$(api_json "GET" "$BASE_URL/api/v1/stock" "$AUTH_HEADER")"
HAS_STOCK=$(echo "$STOCK_JSON" | jq --arg ITEM_ID "$ITEM_ID" '[.[] | select(.itemId == $ITEM_ID)] | length')
if [[ "$HAS_STOCK" -lt 1 ]]; then
  echo "Stock validation failed"; echo "$STOCK_JSON"; exit 1
fi

DASH_JSON="$(api_json "GET" "$BASE_URL/api/v1/dashboard/summary" "$AUTH_HEADER")"
ACTIVE_ITEMS=$(echo "$DASH_JSON" | jq -r '.activeItems')
if [[ -z "$ACTIVE_ITEMS" || "$ACTIVE_ITEMS" == "null" ]]; then
  echo "Dashboard validation failed"; echo "$DASH_JSON"; exit 1
fi

echo "Smoke test PASSED"
