import React, { Suspense, lazy } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuthStore } from './store/auth.store';

const LoginPage = lazy(() => import('./pages/LoginPage').then(m => ({ default: m.LoginPage })));
const DashboardPage = lazy(() => import('./pages/DashboardPage').then(m => ({ default: m.DashboardPage })));
const ItemsPage = lazy(() => import('./pages/items/ItemsPage').then(m => ({ default: m.ItemsPage })));
const ItemFormPage = lazy(() => import('./pages/items/ItemFormPage').then(m => ({ default: m.ItemFormPage })));
const OrdersPage = lazy(() => import('./pages/orders/OrdersPage').then(m => ({ default: m.OrdersPage })));
const OrderFormPage = lazy(() => import('./pages/orders/OrderFormPage').then(m => ({ default: m.OrderFormPage })));
const RequisitionsPage = lazy(() => import('./pages/requisitions/RequisitionsPage').then(m => ({ default: m.RequisitionsPage })));
const RequisitionFormPage = lazy(() => import('./pages/requisitions/RequisitionFormPage').then(m => ({ default: m.RequisitionFormPage })));
const SuppliersPage = lazy(() => import('./pages/suppliers/SuppliersPage').then(m => ({ default: m.SuppliersPage })));
const SupplierFormPage = lazy(() => import('./pages/suppliers/SupplierFormPage').then(m => ({ default: m.SupplierFormPage })));
const StockPage = lazy(() => import('./pages/stock/StockPage').then(m => ({ default: m.StockPage })));

const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
};

const App: React.FC = () => {
  return (
    <Suspense
      fallback={
        <div className="flex items-center justify-center h-screen bg-bg">
          <div className="text-text">Carregando...</div>
        </div>
      }
    >
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <DashboardPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <DashboardPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/items"
          element={
            <ProtectedRoute>
              <ItemsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/items/new"
          element={
            <ProtectedRoute>
              <ItemFormPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/items/:id/edit"
          element={
            <ProtectedRoute>
              <ItemFormPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/orders"
          element={
            <ProtectedRoute>
              <OrdersPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/orders/new"
          element={
            <ProtectedRoute>
              <OrderFormPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/orders/:id"
          element={
            <ProtectedRoute>
              <OrderFormPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/requisitions"
          element={
            <ProtectedRoute>
              <RequisitionsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/requisitions/new"
          element={
            <ProtectedRoute>
              <RequisitionFormPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/requisitions/:id"
          element={
            <ProtectedRoute>
              <RequisitionFormPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/suppliers"
          element={
            <ProtectedRoute>
              <SuppliersPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/suppliers/new"
          element={
            <ProtectedRoute>
              <SupplierFormPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/suppliers/:id/edit"
          element={
            <ProtectedRoute>
              <SupplierFormPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/stock"
          element={
            <ProtectedRoute>
              <StockPage />
            </ProtectedRoute>
          }
        />
      </Routes>
    </Suspense>
  );
};

export default App;