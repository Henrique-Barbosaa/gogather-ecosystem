import React, { ElementType } from "react"

interface EmptyProps {
  title: string;
  children: React.ReactNode;
  className?: string;
  Icon?: ElementType;
}

export function Empty({ title, children, className, Icon }: EmptyProps) {
  return (
    <div className={`bg-white/40 border-2 border-dashed border-gray-300 rounded-2xl p-8 text-center flex flex-col items-center justify-center ${className}`}>
      {Icon && <Icon className="w-10 h-10 text-gray-400 mb-3" />}
      <p className="text-gray-600 font-medium">{title}</p>
      <p className="text-gray-400 text-sm mt-1">{children}</p>
    </div>
  )
}
