'use client';

import React, { HTMLAttributes, forwardRef } from 'react';
import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

export interface CardProps extends HTMLAttributes<HTMLDivElement> {
  variant?: 'glass' | 'solid' | 'outline' | 'pastel-red' | 'pastel-yellow';
  hoverEffect?: boolean;
}

export const Card = forwardRef<HTMLDivElement, CardProps>(
  ({ className, variant = 'glass', hoverEffect = false, children, ...props }, ref) => {
    const variants = {
      glass: 'glass-card shadow-sm',
      solid: 'bg-white rounded-2xl shadow-md border border-gray-100',
      outline: 'bg-transparent border-2 border-[var(--color-pastel-red-200)] rounded-2xl',
      'pastel-red': 'bg-[var(--color-pastel-red-50)] border border-[var(--color-pastel-red-200)] rounded-2xl shadow-sm',
      'pastel-yellow': 'bg-[var(--color-pastel-yellow-50)] border border-[var(--color-pastel-yellow-200)] rounded-2xl shadow-sm',
    };

    const hoverStyle = hoverEffect
      ? 'hover:shadow-lg hover:-translate-y-1 transition-all duration-300 cursor-pointer'
      : '';

    return (
      <div
        ref={ref}
        className={twMerge(clsx('p-5 rounded-2xl', variants[variant], hoverStyle, className))}
        {...props}
      >
        {children}
      </div>
    );
  }
);

Card.displayName = 'Card';
