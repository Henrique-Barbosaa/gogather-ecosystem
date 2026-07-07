import React from "react"

interface SubHeaderProps {
  children: React.ReactNode;
  className?: string;
}

export function SubHeader({ children, className }: SubHeaderProps) {
  return (
    <h2 className={`text-xl font-bold text-gray-900 ${className}`}>{children}</h2>
  )
}
