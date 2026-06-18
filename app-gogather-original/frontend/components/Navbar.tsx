"use client";

import { Bell, LogOut, UserCircle } from "lucide-react";
import Image from "next/image";
import Link from "next/link";
import { useAuth } from "@/context/AuthContext";
import { HeaderButton } from "@/components/area-components/HeaderButton";
import { PButton } from "@/components/area-components/Button";
import logoImg from "../public/logo.svg";

export default function Navbar() {
  const { user, isLoading, logout } = useAuth();
  const displayName = user?.displayName?.trim() || user?.username || "Usuário";

  return (
    <nav className="w-full flex items-center justify-between p-4 md:p-6 fixed top-0 left-0 z-50 bg-black/20 backdrop-blur-md border-b border-white/10 shadow-sm transition-all">
      <div className="flex items-center">
        <Link href="/" className="flex items-center gap-1 hover:opacity-90 transition-opacity">
          <Image src={logoImg} alt="GoGather Logo" width={40} height={35} priority />
          <span className="text-white text-2xl md:text-3xl font-bold tracking-tight mt-1">gogather</span>
        </Link>
      </div>

      {isLoading ? (
        <div className="h-10 w-40 rounded-full border border-white/10 bg-white/5 animate-pulse" />
      ) : !user ? (
        <>
          <div className="hidden md:flex items-center gap-8 text-white/90 font-medium">
            <Link href="/#recursos" className="hover:text-white transition-colors">
              Recursos
            </Link>
            <Link href="/#como-funciona" className="hover:text-white transition-colors">
              Como Funciona
            </Link>
          </div>
          <div className="flex items-center gap-4">
            <HeaderButton href="/login" variant="dark">
              Entrar
            </HeaderButton>
            <HeaderButton href="/register" variant="beige">	
              Cadastre-se
            </HeaderButton>
          </div>		
        </>
      ) : (
        <>
          <div className="flex items-center gap-6 text-white">
            <button className="hover:text-[#fbf2c7] transition-colors relative">
              <Bell className="w-6 h-6" />
              <span className="absolute -top-1 -right-1 bg-[#cc241a] text-xs font-bold w-4 h-4 rounded-full flex items-center justify-center">
                3
              </span>
            </button>
            <div className="flex items-center gap-2 cursor-pointer hover:text-[#fbf2c7] transition-colors">
              <UserCircle className="w-8 h-8" />
              <span className="font-bold hidden md:block">{displayName}</span>
            </div>
            <PButton
              type="button"
              onClick={logout}
              variant="cyan"
              Icon={LogOut}
              className="rounded-full px-4 py-2 text-sm font-bold"
            >
              Sair
            </PButton>
          </div>
        </>
      )}
    </nav>
  );
}
