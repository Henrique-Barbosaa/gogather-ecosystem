import Link from 'next/link';
import React, { ElementType } from 'react';

interface HeaderButtonProps {
  children : React.ReactNode;
  variant: 'dark' | 'light' | 'emerald' | 'red' | 'cyan' | 'beige';
  href?: string;
  Icon?: ElementType;
}

const variants = {
  dark: 'bg-zinc-800 hover:bg-zinc-950 text-zinc-50',
  light: 'bg-zinc-50 hover:bg-zinc-200 text-zinc-800',
  emerald: 'bg-emerald-500 hover:bg-emerald-600 text-zinc-50',
  red: 'bg-gg-red hover:bg-gg-red-dark text-zinc-50',
  cyan: 'bg-gg-cyan hover:bg-gg-cyan-dark text-zinc-50',
  beige: 'bg-gg-beige-midlight hover:bg-gg-beige-light text-gg-beige-extradark'
}

export function HeaderButton({ children, variant, href, Icon }: HeaderButtonProps) {
  return (
    <Link
      href={href ? href : '#'}
      className={
        `px-6 py-3 rounded-full font-bold hover:-translate-y-1
        transition-all shadow-md flex items-center gap-2
        ${variants[variant]}`
      }
    >
      {Icon && <Icon className="w-5 h-5"/>}
      {children}
    </Link>
  )
}
