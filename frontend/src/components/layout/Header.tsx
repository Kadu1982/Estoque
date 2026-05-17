import React from 'react';
import { useAuthStore } from '../../store/auth.store';

export const Header: React.FC = () => {
  const user = useAuthStore((state) => state.user);

  return (
    <header className="h-16 bg-surface border-b border-border flex items-center justify-end px-6">
      <div className="flex items-center gap-3">
        <span className="text-text-muted">Olá, {user?.fullName?.split(' ')[0] || 'Usuário'}</span>
        <div className="w-8 h-8 rounded-full bg-primary flex items-center justify-center text-white font-medium">
          {user?.fullName?.charAt(0) || 'U'}
        </div>
      </div>
    </header>
  );
};