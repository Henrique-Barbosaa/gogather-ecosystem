'use client';

import React, { ReactNode } from 'react';
import { Navbar } from './Navbar';
import { useAuth } from '../../context/AuthContext';
import { useRouter } from 'next/navigation';
import { Loader2 } from 'lucide-react';
import { useEffect } from 'react';

export const AppLayout: React.FC<{ children: ReactNode }> = ({ children }) => {
  const { isLoading, isAuthenticated } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      router.push('/login');
    }
  }, [isLoading, isAuthenticated, router]);

  if (isLoading || !isAuthenticated) {
    return (
      <div className="min-h-screen w-full flex items-center justify-center bg-gradient-to-br from-[#fffbfa] via-[#fff7f3] to-[#fefcf3]">
        <div className="flex flex-col items-center gap-3 p-8 rounded-3xl glass-card">
          <Loader2 className="w-10 h-10 animate-spin text-[var(--color-pastel-red-600)]" />
          <span className="text-sm font-bold text-[#2d2327]">Carregando TripMaker...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex flex-col w-full">
      <Navbar />
      <main className="flex-1 max-w-7xl w-full mx-auto p-4 sm:p-6 lg:p-8 animate-fadeIn">
        {children}
      </main>
    </div>
  );
};
