'use client';

import React from 'react';
import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

interface BadgeProps {
  variant?: 'red' | 'yellow' | 'green' | 'gray' | 'purple';
  children: React.ReactNode;
  className?: string;
}

export const Badge: React.FC<BadgeProps> = ({ variant = 'red', children, className }) => {
  const variants = {
    red: 'bg-[var(--color-pastel-red-100)] text-[var(--color-pastel-red-800)] border-[var(--color-pastel-red-300)]',
    yellow: 'bg-[var(--color-pastel-yellow-100)] text-[var(--color-pastel-yellow-700)] border-[var(--color-pastel-yellow-400)]',
    green: 'bg-emerald-100 text-emerald-800 border-emerald-300',
    gray: 'bg-gray-100 text-gray-700 border-gray-300',
    purple: 'bg-purple-100 text-purple-800 border-purple-300',
  };

  return (
    <span
      className={twMerge(
        clsx(
          'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold border shadow-2xs',
          variants[variant],
          className
        )
      )}
    >
      {children}
    </span>
  );
};
