'use client';

import React from 'react';
import { clsx } from 'clsx';

export interface TabItem {
  id: string;
  label: string;
  icon?: React.ReactNode;
  badgeCount?: number;
}

interface TabsProps {
  tabs: TabItem[];
  activeTab: string;
  onChange: (id: string) => void;
}

export const Tabs: React.FC<TabsProps> = ({ tabs, activeTab, onChange }) => {
  return (
    <div className="flex overflow-x-auto no-scrollbar border-b border-gray-200/80 bg-white/60 backdrop-blur-md rounded-t-2xl px-2 pt-2">
      <div className="flex space-x-1 min-w-full">
        {tabs.map((tab) => {
          const isActive = tab.id === activeTab;
          return (
            <button
              key={tab.id}
              onClick={() => onChange(tab.id)}
              className={clsx(
                'flex items-center gap-2 px-4 py-3 text-sm font-semibold transition-all duration-200 border-b-2 rounded-t-xl whitespace-nowrap',
                isActive
                  ? 'border-[var(--color-pastel-red-600)] text-[var(--color-pastel-red-700)] bg-[var(--color-pastel-red-50)]/60 shadow-xs'
                  : 'border-transparent text-gray-500 hover:text-[#2d2327] hover:bg-white/40'
              )}
            >
              {tab.icon && <span className={clsx('w-4 h-4', isActive ? 'text-[var(--color-pastel-red-600)]' : 'text-gray-400')}>{tab.icon}</span>}
              <span>{tab.label}</span>
              {tab.badgeCount !== undefined && tab.badgeCount > 0 && (
                <span
                  className={clsx(
                    'px-2 py-0.5 text-xs rounded-full font-bold',
                    isActive
                      ? 'bg-[var(--color-pastel-red-600)] text-white'
                      : 'bg-gray-200 text-gray-700'
                  )}
                >
                  {tab.badgeCount}
                </span>
              )}
            </button>
          );
        })}
      </div>
    </div>
  );
};
