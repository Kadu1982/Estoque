import React from 'react';

interface CardProps {
  children: React.ReactNode;
  className?: string;
  title?: string;
}

export const Card: React.FC<CardProps> = ({ children, className = '', title }) => {
  return (
    <div className={`bg-surface rounded-lg border border-border shadow ${className}`}>
      {title && (
        <div className="px-4 py-3 border-b border-border">
          <h3 className="text-lg font-semibold text-text">{title}</h3>
        </div>
      )}
      <div className="p-4">{children}</div>
    </div>
  );
};