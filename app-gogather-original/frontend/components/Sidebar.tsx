"use client";

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Map, Users, Compass, Mail, Settings, LayoutDashboard, UserRound, LogOut, UserRoundSearch, Bell } from 'lucide-react';
import {
  HoverCard,
  HoverCardContent,
  HoverCardTrigger,
} from "@/components/ui/hover-card"
import { Button } from "@/components/ui/button"
import {
  DropdownMenuLabel,
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { useAuth } from '@/context/AuthContext';

export default function Sidebar() {
  const pathname = usePathname();
  const { user, isLoading, logout } = useAuth();

  const menuItems = [
    { name: "Visão Geral", desc: "Dashboards e guias rápidos!", icon: LayoutDashboard, href: "/area/dashboard" },
    { name: "Meus Rolês", desc: "Os rolês que criou ou que participa!", icon: Map, href: "/area/groups" },
    { name: "Amigos", desc: "Suas amizades para animar o rolê!", icon: Users, href: "/area/friends" },
    // { name: "Explorar IA", desc: "O seu braço direito nas escolhas de rolês!", icon: Compass, href: "/area/explore" },
    // { name: "Convites", desc: "Rolês que querem sua presença!", icon: Mail, href: "/area/invites" },
  ];
  return (
    <aside className="w-14 fixed left-0 top-0 h-full bg-white flex flex-col justify-between py-6 z-40">
      <div className="space-y-2 flex flex-col items-center">
        {menuItems.map((item, index) => {
          const isActive = pathname === item.href; //faz a verificação se a rota atual é a do item

          return (
            <HoverCard key={index} openDelay={50} closeDelay={0}>
              <HoverCardTrigger asChild>
                <Link
                  className=
                  {`text-gg-cyan hover:text-gg-cyan-dark hover:bg-gg-cyan-extralight
                    rounded-xl p-2 transition-all ${isActive ? 'bg-gg-cyan-extralight text-gg-cyan-dark' : ''}`}
                  href={item.href}>
                  <item.icon className='w-6 h-6' />
                </Link>
              </HoverCardTrigger>
              <HoverCardContent side='right' className='flex flex-col bg-white ml-2'>
                <h1 className='font-bold text-gg-cyan-extradark'>{item.name}</h1>
                <p className='font-extralight text-gg-cyan-extradark'>{item.desc}</p>
              </HoverCardContent>
            </HoverCard>
          );
        })}
      </div>
      {/* rodapé de configurações */}
      <div className="flex flex-col items-center gap-2 px-2">
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <UserRound
              className='text-gg-cyan-dark bg-gg-cyan-extralight hover:text-gg-cyan-extradark
              hover:bg-gg-cyan-light hover:cursor-pointer transition-all rounded-full w-10 h-10 p-2'/>
          </DropdownMenuTrigger>
          <DropdownMenuContent side='right'>
            <DropdownMenuGroup>
              <DropdownMenuLabel>{user?.displayName || user?.username}</DropdownMenuLabel>
              <DropdownMenuItem>
                <UserRoundSearch />
                Perfil
              </DropdownMenuItem>
              <DropdownMenuItem>
                <Bell />
                Notificações
              </DropdownMenuItem>
            </DropdownMenuGroup>
            <DropdownMenuSeparator />
            <DropdownMenuGroup>
              <DropdownMenuItem variant='destructive' onClick={logout}>
                <LogOut />
                Sair
              </DropdownMenuItem>
            </DropdownMenuGroup>
          </DropdownMenuContent>
        </DropdownMenu>
        <HoverCard openDelay={50} closeDelay={0}>
          <HoverCardTrigger asChild>
            <Link
              href="/area/configuracoes"
              className={`flex items-center gap-3 px-4 py-3 rounded-xl font-bold transition-all ${pathname === "/dashboard/configuracoes"
                ? 'bg-[#fbf2c7] text-[#cc241a]'
                : 'text-gg-cyan hover:bg-gg-cyan-extralight hover:text-gg-cyan-dark'
                }`}
            >
              <Settings className="w-8 h-8" />
            </Link>
          </HoverCardTrigger>
          <HoverCardContent side='right' className='flex flex-col bg-white ml-2'>
            <h1 className='font-bold text-gg-cyan-extradark'>Configurações</h1>
            <p className='font-extralight text-gg-cyan-extradark'>Ajuste o que quiser por aqui!</p>
          </HoverCardContent>
        </HoverCard>
      </div>
    </aside>
  );
}
