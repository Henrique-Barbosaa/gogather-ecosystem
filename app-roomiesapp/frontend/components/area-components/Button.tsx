import React, { ButtonHTMLAttributes, ElementType } from "react";
import { Button } from "../ui/button";

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  children: React.ReactNode;
  variant: 'dark' | 'light' | 'emerald' | 'red' | 'cyan' | 'beige';
  className?: string;
  Icon?: ElementType;
}

const variants = {
  dark: 'bg-zinc-800 hover:bg-zinc-950 text-zinc-50',
  light: 'bg-zinc-50 hover:bg-zinc-200 text-zinc-800',
  emerald: 'bg-emerald-500 hover:bg-emerald-600 text-zinc-50',
  red: 'bg-ra-purple hover:bg-ra-purple-dark text-zinc-50',
  cyan: 'bg-ra-green hover:bg-ra-green-dark text-zinc-50',
  beige: 'bg-ra-blue-midlight hover:bg-ra-blue-light text-ra-blue-extradark'
}

export function PButton({ children, variant, className, Icon, ...props }: ButtonProps) {
  return (
    <Button className={`hover:cursor-pointer shadow ${variants[variant]} ${className}`} {...props}>
      {Icon && <Icon className='w-3 h-3'/>}
      {children}
    </Button>
  )
}
