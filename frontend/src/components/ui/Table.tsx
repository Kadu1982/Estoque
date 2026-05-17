import React from 'react';

interface Column<T> {
  key: keyof T | string;
  label: string;
  render?: (value: unknown, row: T) => React.ReactNode;
}

interface TableProps<T> {
  columns: Column<T>[];
  data: T[];
  isLoading?: boolean;
  emptyMessage?: string;
}

export const Table = <T extends Record<string, any>>({
  columns,
  data,
  isLoading = false,
  emptyMessage = 'Nenhum registro encontrado',
}: TableProps<T>) => {
  if (isLoading) {
    return (
      <div className="w-full">
        <table className="w-full border-collapse">
          <thead>
            <tr>
              {columns.map((column, idx) => (
                <th
                  key={`${String(column.key)}-${idx}`}
                  className="px-4 py-3 text-left text-sm font-medium text-text-muted border-b border-border"
                >
                  {column.label}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {[...Array(5)].map((_, i) => (
              <tr key={i}>
                {columns.map((column, idx) => (
                  <td key={`${String(column.key)}-${idx}-skeleton`} className="px-4 py-3 border-b border-border">
                    <div className="h-4 bg-surface-2 rounded pulse" />
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    );
  }

  if (!data || data.length === 0) {
    return (
      <div className="text-center py-8 text-text-muted">
        {emptyMessage}
      </div>
    );
  }

  return (
    <div className="w-full overflow-x-auto">
      <table className="w-full border-collapse">
        <thead>
          <tr>
            {columns.map((column, idx) => (
              <th
                key={`${String(column.key)}-${idx}-header`}
                className="px-4 py-3 text-left text-sm font-medium text-text-muted border-b border-border"
              >
                {column.label}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {data.map((row, rowIndex) => {
            const rowKey = row.id || row.code || row.key || `row-${rowIndex}`;
            return (
              <tr key={rowKey} className="hover:bg-surface-2 transition-colors">
                {columns.map((column, idx) => {
                  const value = row[column.key as keyof T];
                  return (
                    <td key={`${rowKey}-${String(column.key)}-${idx}`} className="px-4 py-3 border-b border-border text-sm">
                      {column.render ? column.render(value, row) : String(value ?? '')}
                    </td>
                  );
                })}
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
};