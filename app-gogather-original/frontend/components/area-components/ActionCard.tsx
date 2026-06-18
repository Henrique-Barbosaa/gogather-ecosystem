import Link from "next/link";
import React, { ElementType } from "react";

interface ActionCardProps {
  title: string;
  children: React.ReactNode;
  footer: string;
  color: 'cyan' | 'red' | 'beige';
  href?: string;
  className?: string;
  Icon?: ElementType;
}

const colors = {
  cyan: {
    hover: 'hover:border-gg-cyan',
    icon: 'bg-gg-cyan',
    text: 'text-gg-cyan'
  },
  red: {
    hover: 'hover:border-gg-red',
    icon: 'bg-gg-red',
    text: 'text-gg-red'
  },
  beige: {
    hover: 'hover:border-gg-beige-extradark',
    icon: 'bg-gg-beige',
    text: 'text-gg-beige-extradark'
  },
}

export function ActionCard({ title, children, footer, color, href, className, Icon }: ActionCardProps) {
  return (
    <Link
        href={href ? href : '#'}
        className={`bg-white p-8 rounded-3xl shadow-sm border-2 border-transparent ${colors[color]['hover']} transition-all text-left group block ${className}`}
      >
        <div className={`w-14 h-14 ${colors[color]['icon']} text-white rounded-2xl flex items-center justify-center mb-6 shadow-md group-hover:scale-110 transition-transform`}>
          {Icon && <Icon className="w-7 h-7" />}
        </div>
        <h3 className="text-2xl font-bold text-gray-900 mb-2">{title}</h3>
        <p className="text-gray-500 mb-6 line-clamp-2">
          {children}
        </p>
        <span className={`${colors[color]['text']} font-bold flex items-center gap-1 group-hover:gap-2 transition-all`}>
          {footer}
        </span>
    </Link>
  )
}
