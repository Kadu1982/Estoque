import React from 'react';

interface BadgeProps {
  status: string;
  urgency?: string;
}

export const Badge: React.FC<BadgeProps> = ({ status, urgency }) => {
  const getBadgeStyles = () => {
    const statusValue = urgency || status;

    // RASCUNHO / NORMAL -> cinza
    if (statusValue === 'RASCUNHO' || statusValue === 'NORMAL') {
      return 'bg-surface-2 text-text-muted';
    }

    // PENDENTE_APROVACAO / PENDENTE -> amarelo
    if (statusValue === 'PENDENTE_APROVACAO' || statusValue === 'PENDENTE') {
      return 'bg-[rgba(245,158,11,0.2)] text-warning';
    }

    // APROVADA / CONFIRMADO / ENTREGUE -> verde
    if (statusValue === 'APROVADA' || statusValue === 'CONFIRMADO' || statusValue === 'ENTREGUE') {
      return 'bg-[rgba(34,197,94,0.2)] text-success';
    }

    // REPROVADA / CANCELADO -> vermelho
    if (statusValue === 'REPROVADA' || statusValue === 'CANCELADO') {
      return 'bg-[rgba(239,68,68,0.2)] text-danger';
    }

    // URGENTE / EM_PRODUCAO -> laranja
    if (statusValue === 'URGENTE' || statusValue === 'EM_PRODUCAO') {
      return 'bg-[rgba(249,115,22,0.2)] text-[#f97316]';
    }

    // EMERGENCIAL -> vermelho pulsante
    if (statusValue === 'EMERGENCIAL') {
      return 'bg-[rgba(239,68,68,0.2)] text-danger animate-pulse';
    }

    // Default
    return 'bg-surface-2 text-text-muted';
  };

  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getBadgeStyles()}`}>
      {urgency || status}
    </span>
  );
};