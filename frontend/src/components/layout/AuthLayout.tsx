import React from 'react';

interface AuthLayoutProps {
  children: React.ReactNode;
}

export const AuthLayout: React.FC<AuthLayoutProps> = ({ children }) => {
  return (
    <div className="min-h-screen bg-bg flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-primary mb-2">Austral Estoque</h1>
          <p className="text-text-muted">Sistema de gestão de estoque</p>
        </div>
        <div className="bg-surface rounded-lg border border-border shadow p-6">
          {children}
        </div>
      </div>
    </div>
  )
}
