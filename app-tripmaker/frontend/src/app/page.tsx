'use client';

import React, { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '../context/AuthContext';
import { Plane, Loader2 } from 'lucide-react';

export default function Home() {
  const { isAuthenticated, isLoading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!isLoading) {
      if (isAuthenticated) {
        router.replace('/groups');
      } else {
        router.replace('/login');
      }
    }
  }, [isAuthenticated, isLoading, router]);

  return (
    <div className="min-h-screen w-full flex flex-col items-center justify-center bg-gradient-to-br from-[#fff0f3] via-[#fffbfa] to-[#fffeea]">
      <div className="flex flex-col items-center gap-4 p-10 rounded-3xl glass-card animate-scaleUp">
        <div className="w-16 h-16 rounded-3xl bg-gradient-to-tr from-[var(--color-pastel-red-500)] to-[var(--color-pastel-yellow-500)] text-white flex items-center justify-center shadow-lg animate-bounce">
          <Plane className="w-8 h-8" />
        </div>
        <h1 className="text-2xl font-extrabold text-[#2d2327]">TripMaker</h1>
        <div className="flex items-center gap-2 text-sm font-semibold text-gray-500">
          <Loader2 className="w-4 h-4 animate-spin text-[var(--color-pastel-red-600)]" />
          <span>Iniciando plataforma de viagens...</span>
        </div>
      </div>
    </div>
  );
}
