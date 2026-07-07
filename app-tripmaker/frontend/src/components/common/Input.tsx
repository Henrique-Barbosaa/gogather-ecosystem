'use client';

import React, { InputHTMLAttributes, forwardRef } from 'react';
import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

export interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  helperText?: string;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
}

export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ className, label, error, helperText, leftIcon, rightIcon, id, ...props }, ref) => {
    const inputId = id || (label ? label.toLowerCase().replace(/\s+/g, '-') : undefined);

    return (
      <div className="w-full flex flex-col gap-1">
        {label && (
          <label htmlFor={inputId} className="text-sm font-semibold text-[#2d2327]">
            {label}
          </label>
        )}
        <div className="relative flex items-center">
          {leftIcon && (
            <div className="absolute left-3.5 text-gray-400 pointer-events-none flex items-center">
              {leftIcon}
            </div>
          )}
          <input
            ref={ref}
            id={inputId}
            className={twMerge(
              clsx(
                'w-full bg-white/90 border rounded-xl py-2.5 text-sm text-[#2d2327] placeholder:text-gray-400 transition-all duration-200 outline-none shadow-sm',
                'focus:border-[var(--color-pastel-red-500)] focus:ring-2 focus:ring-[var(--color-pastel-red-200)]',
                leftIcon ? 'pl-10' : 'pl-3.5',
                rightIcon ? 'pr-10' : 'pr-3.5',
                error ? 'border-rose-500 ring-1 ring-rose-300' : 'border-gray-200 hover:border-gray-300',
                className
              )
            )}
            {...props}
          />
          {rightIcon && (
            <div className="absolute right-3.5 text-gray-400 flex items-center">
              {rightIcon}
            </div>
          )}
        </div>
        {error && <span className="text-xs font-medium text-rose-500 animate-pulse">{error}</span>}
        {!error && helperText && <span className="text-xs text-gray-500">{helperText}</span>}
      </div>
    );
  }
);

Input.displayName = 'Input';
