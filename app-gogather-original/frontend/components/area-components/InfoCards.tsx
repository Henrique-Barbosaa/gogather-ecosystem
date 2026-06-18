import React, { ElementType } from "react"

interface InfoCardsProps {
  children: React.ReactNode;
  info: string;
  color: 'cyan' | 'red' | 'beige';
  className?: string;
  Icon?: ElementType;
}

const colors = {
  cyan: 'bg-gg-cyan-extralight text-gg-cyan',
  red: 'bg-gg-red-extralight text-gg-red',
  beige: 'bg-gg-beige text-gg-beige-extradark'
}

export function InfoCards({ children, info, color, className, Icon }: InfoCardsProps) {
  return (
    <div className={`bg-white p-6 rounded-2xl shadow-sm border border-gray-100 flex items-center gap-4 ${className}`}>
      <div className={`w-12 h-12 ${colors[color]} rounded-full flex items-center justify-center`}>
        {Icon && <Icon className="w-6 h-6"/>}
      </div>
      <div>
        <p className="text-gray-500 text-sm font-medium">{children}</p>
        <p className="text-2xl font-bold text-gray-900">{info}</p>
      </div>
    </div>
  )
}
