'use client';

import React, { ButtonHTMLAttributes, forwardRef } from 'react';
import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';
import { Loader2 } from 'lucide-react';

export interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'outline' | 'ghost' | 'danger';
  size?: 'sm' | 'md' | 'lg';
  isLoading?: boolean;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
}

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  (
    {
      className,
      variant = 'primary',
      size = 'md',
      isLoading = false,
      leftIcon,
      rightIcon,
      children,
      disabled,
      ...props
    },
    ref
  ) => {
    const baseStyles =
      'inline-flex items-center justify-center font-medium rounded-xl transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-60 disabled:cursor-not-allowed active:scale-[0.98]';

    const variants = {
      primary:
        'bg-gradient-to-r from-[var(--color-pastel-red-500)] to-[var(--color-pastel-red-600)] text-white shadow-md shadow-[var(--color-pastel-red-300)]/40 hover:brightness-105 focus:ring-[var(--color-pastel-red-400)]',
      secondary:
        'bg-gradient-to-r from-[var(--color-pastel-yellow-400)] to-[var(--color-pastel-yellow-500)] text-[#2d2327] font-semibold shadow-md shadow-[var(--color-pastel-yellow-300)]/40 hover:brightness-105 focus:ring-[var(--color-pastel-yellow-400)]',
      outline:
        'border-2 border-[var(--color-pastel-red-400)] text-[var(--color-pastel-red-600)] hover:bg-[var(--color-pastel-red-50)] focus:ring-[var(--color-pastel-red-300)]',
      ghost:
        'text-[#2d2327]/80 hover:bg-[#fff0f3] hover:text-[var(--color-pastel-red-600)] focus:ring-[var(--color-pastel-red-200)]',
      danger:
        'bg-rose-500 text-white shadow-md shadow-rose-300/40 hover:bg-rose-600 focus:ring-rose-400',
    };

    const sizes = {
      sm: 'px-3 py-1.5 text-xs gap-1.5',
      md: 'px-4 py-2.5 text-sm gap-2',
      lg: 'px-6 py-3 text-base gap-2.5',
    };

    return (
      <button
        ref={ref}
        disabled={disabled || isLoading}
        className={twMerge(clsx(baseStyles, variants[variant], sizes[size], className))}
        {...props}
      >
        {isLoading && <Loader2 className="w-4 h-4 animate-spin shrink-0" />}
        {!isLoading && leftIcon && <span className="shrink-0">{leftIcon}</span>}
        <span>{children}</span>
        {!isLoading && rightIcon && <span className="shrink-0">{rightIcon}</span>}
      </button>
    );
  }
);

Button.displayName = 'Button';
