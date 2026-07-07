import "mapbox-gl/dist/mapbox-gl.css";
import type { Metadata } from "next";
import "./globals.css";
import { Geist } from "next/font/google";
import { cn } from "@/lib/utils";
import { Providers } from "@/components/providers";

const geist = Geist({subsets:['latin'],variable:'--font-sans'});

export const metadata: Metadata = {
  title: "GoGather",
  description: "Encontre e compartilhe os melhores rolês da cidade",
};
import localFont from 'next/font/local';
import './globals.css';
import { AuthProvider } from "@/context/AuthContext";

const jakartaSans = localFont({
  src: [
    {
      path: '../public/fonts/PlusJakartaSans-Regular.ttf',
      weight: '400',
      style: 'normal',
    },
    {
      path: '../public/fonts/PlusJakartaSans-Bold.ttf',
      weight: '700',
      style: 'normal',
    },
  ],
  variable: '--font-jakarta',
});

import { Toaster } from 'sonner';

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="pt-BR">
      <body className={`${jakartaSans.variable} font-sans`}>
        <Providers>
          <AuthProvider>
               	{children}
          </AuthProvider>
        </Providers>
        <Toaster richColors position="top-right" />
      </body>
    </html>
  );
}
