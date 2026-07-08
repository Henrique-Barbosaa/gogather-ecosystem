"use client";

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Home, Users, Settings, LayoutDashboard, UserRound, LogOut, UserRoundSearch, Bell, Wallet, ListTodo } from 'lucide-react';
import {
  HoverCard,
  HoverCardContent,
  HoverCardTrigger,
} from "@/components/ui/hover-card"
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
  const { user, logout } = useAuth();

  const menuItems = [
    { name: "Painel", desc: "Visão geral da casa!", icon: LayoutDashboard, href: "/area/dashboard" },
    { name: "Minhas Casas", desc: "Suas repúblicas e apartamentos!", icon: Home, href: "/area/groups" },
    { name: "Tarefas", desc: "Divisão de tarefas domésticas!", icon: ListTodo, href: "/area/chores" },
    { name: "Contas", desc: "Gestão de despesas mensais!", icon: Wallet, href: "/area/bills" },
  ];

  return (
    <aside className="w-14 fixed left-0 top-0 h-full bg-white flex flex-col justify-between py-6 z-40 border-r border-gray-100">
      <div className="space-y-2 flex flex-col items-center">
        {menuItems.map((item, index) => {
          const isActive = pathname === item.href;

          return (
            <HoverCard key={index} openDelay={50} closeDelay={0}>
              <HoverCardTrigger asChild>
                <Link
                  className={`text-ra-purple hover:text-ra-purple-dark hover:bg-ra-purple/10
                    rounded-xl p-2 transition-all ${isActive ? 'bg-ra-purple/10 text-ra-purple-dark' : ''}`}
                  href={item.href}>
                  <item.icon className='w-6 h-6' />
                </Link>
              </HoverCardTrigger>
              <HoverCardContent side='right' className='flex flex-col bg-white ml-2 border-gray-100 shadow-xl'>
                <h1 className='font-bold text-ra-purple-dark'>{item.name}</h1>
                <p className='font-light text-gray-500 text-xs'>{item.desc}</p>
              </HoverCardContent>
            </HoverCard>
          );
        })}
      </div>

      {/* rodapé de configurações */}
      <div className="flex flex-col items-center gap-4 px-2">
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <div className='bg-ra-purple/10 text-ra-purple hover:bg-ra-purple/20 hover:cursor-pointer transition-all rounded-full w-10 h-10 flex items-center justify-center'>
              <UserRound className='w-6 h-6'/>
            </div>
          </DropdownMenuTrigger>
          <DropdownMenuContent side='right' className="w-56">
            <DropdownMenuGroup>
              <DropdownMenuLabel className="font-bold">{user?.displayName || user?.username}</DropdownMenuLabel>
              <DropdownMenuItem className="cursor-pointer">
                <UserRoundSearch className="mr-2 h-4 w-4" />
                Perfil
              </DropdownMenuItem>
              <DropdownMenuItem className="cursor-pointer">
                <Bell className="mr-2 h-4 w-4" />
                Notificações
              </DropdownMenuItem>
            </DropdownMenuGroup>
            <DropdownMenuSeparator />
            <DropdownMenuGroup>
              <DropdownMenuItem className="text-red-600 cursor-pointer" onClick={logout}>
                <LogOut className="mr-2 h-4 w-4" />
                Sair
              </DropdownMenuItem>
            </DropdownMenuGroup>
          </DropdownMenuContent>
        </DropdownMenu>

        <HoverCard openDelay={50} closeDelay={0}>
          <HoverCardTrigger asChild>
            <Link
              href="/area/configuracoes"
              className={`p-2 rounded-xl transition-all ${pathname === "/area/configuracoes"
                ? 'bg-ra-purple text-white'
                : 'text-gray-400 hover:bg-gray-100 hover:text-gray-700'
                }`}
            >
              <Settings className="w-6 h-6" />
            </Link>
          </HoverCardTrigger>
          <HoverCardContent side='right' className='flex flex-col bg-white ml-2 border-gray-100 shadow-xl'>
            <h1 className='font-bold text-gray-900'>Configurações</h1>
            <p className='font-light text-gray-500 text-xs'>Ajuste sua conta.</p>
          </HoverCardContent>
        </HoverCard>
      </div>
    </aside>
  );
}