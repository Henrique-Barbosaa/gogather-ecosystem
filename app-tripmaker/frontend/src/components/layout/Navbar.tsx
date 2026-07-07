'use client';

import React, { useState, useRef, useEffect } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useAuth } from '../../context/AuthContext';
import { useNotifications } from '../../context/NotificationContext';
import { NotificationDropdown } from '../notifications/NotificationDropdown';
import { Bell, Users, Compass, User as UserIcon, LogOut, Plane, Menu, X } from 'lucide-react';
import { clsx } from 'clsx';
import { getAvatarUrl } from '../../services/mockData';

export const Navbar: React.FC = () => {
  const { user, isAuthenticated, logout } = useAuth();
  const { unreadCount } = useNotifications();
  const pathname = usePathname();

  const [showNotifications, setShowNotifications] = useState(false);
  const [showMobileMenu, setShowMobileMenu] = useState(false);
  const notifRef = useRef<HTMLDivElement>(null);

  // Close notifications dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (notifRef.current && !notifRef.current.contains(event.target as Node)) {
        setShowNotifications(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  if (!isAuthenticated && (pathname === '/login' || pathname === '/register')) {
    return null;
  }

  const navLinks = [
    { href: '/groups', label: 'Grupos de Viagem', icon: <Compass className="w-4 h-4" /> },
    { href: '/friends', label: 'Amigos', icon: <Users className="w-4 h-4" /> },
    { href: '/profile', label: 'Perfil', icon: <UserIcon className="w-4 h-4" /> },
  ];

  return (
    <header className="sticky top-0 z-40 glass-navbar px-4 sm:px-8 py-3 shadow-2xs">
      <div className="max-w-7xl mx-auto flex items-center justify-between">
        
        {/* LOGO PLACEHOLDER AREA */}
        <Link href="/groups" className="group flex items-center gap-3">
          <div className="relative flex items-center gap-2 px-3 py-1.5 rounded-2xl bg-gradient-to-r from-[var(--color-pastel-red-100)] via-[var(--color-pastel-yellow-100)] to-[var(--color-pastel-red-100)] border-2 border-dashed border-[var(--color-pastel-red-400)] group-hover:border-[var(--color-pastel-red-600)] transition-all duration-300 shadow-xs">
            <div className="w-8 h-8 rounded-xl bg-gradient-to-br from-[var(--color-pastel-red-500)] to-[var(--color-pastel-red-600)] flex items-center justify-center text-white shadow-sm transform group-hover:rotate-12 transition-transform duration-300">
              <Plane className="w-5 h-5" />
            </div>
            <div className="flex flex-col">
              <span className="font-extrabold tracking-tight text-base sm:text-lg bg-gradient-to-r from-[var(--color-pastel-red-700)] to-[var(--color-pastel-yellow-700)] bg-clip-text text-transparent">
                TripMaker
              </span>
              <span className="text-[9px] font-semibold text-gray-400 -mt-1 uppercase tracking-wider">
                [ Espaço Logo ]
              </span>
            </div>
          </div>
        </Link>

        {/* Desktop Navigation Links */}
        <nav className="hidden md:flex items-center space-x-1 lg:space-x-2">
          {navLinks.map((link) => {
            const isActive = pathname.startsWith(link.href);
            return (
              <Link
                key={link.href}
                href={link.href}
                className={clsx(
                  'flex items-center gap-2 px-4 py-2 rounded-xl font-semibold text-sm transition-all duration-200',
                  isActive
                    ? 'bg-gradient-to-r from-[var(--color-pastel-red-500)] to-[var(--color-pastel-red-600)] text-white shadow-md shadow-[var(--color-pastel-red-300)]/30'
                    : 'text-[#2d2327]/80 hover:bg-[var(--color-pastel-red-50)] hover:text-[var(--color-pastel-red-700)]'
                )}
              >
                {link.icon}
                <span>{link.label}</span>
              </Link>
            );
          })}
        </nav>

        {/* Right Action Icons (Notifications & Profile Avatar) */}
        <div className="flex items-center space-x-3">
          
          {/* Notification Bell */}
          <div className="relative" ref={notifRef}>
            <button
              onClick={() => setShowNotifications(!showNotifications)}
              className="relative p-2.5 rounded-2xl bg-white/80 border border-gray-200 text-gray-700 hover:text-[var(--color-pastel-red-600)] hover:border-[var(--color-pastel-red-300)] hover:bg-[var(--color-pastel-red-50)]/50 transition-all shadow-2xs"
              aria-label="Ver notificações"
            >
              <Bell className="w-5 h-5" />
              {unreadCount > 0 && (
                <span className="absolute -top-1 -right-1 flex h-5 w-5 items-center justify-center rounded-full bg-[var(--color-pastel-red-600)] text-[10px] font-extrabold text-white shadow-sm ring-2 ring-white animate-bounce">
                  {unreadCount > 9 ? '9+' : unreadCount}
                </span>
              )}
            </button>

            {showNotifications && (
              <div className="absolute right-0 mt-3 z-50">
                <NotificationDropdown />
              </div>
            )}
          </div>

          {/* User Profile Avatar */}
          {user ? (
            <div className="flex items-center gap-3 pl-2 border-l border-gray-200">
              <Link href="/profile" className="flex items-center gap-2 group">
                <img
                  src={getAvatarUrl(user.name, user.avatar)}
                  alt={user.name}
                  className="w-10 h-10 rounded-2xl object-cover border-2 border-[var(--color-pastel-yellow-400)] group-hover:scale-105 transition-transform shadow-xs"
                />
                <div className="hidden lg:flex flex-col text-left">
                  <span className="text-sm font-bold text-[#2d2327] group-hover:text-[var(--color-pastel-red-600)] transition-colors leading-tight">
                    {user.name}
                  </span>
                  <span className="text-[11px] text-gray-400 truncate max-w-[120px]">
                    {user.email}
                  </span>
                </div>
              </Link>
              <button
                onClick={logout}
                title="Sair (Logout)"
                className="p-2 text-gray-400 hover:text-rose-600 hover:bg-rose-50 rounded-xl transition-all"
              >
                <LogOut className="w-4 h-4" />
              </button>
            </div>
          ) : (
            <Link
              href="/login"
              className="px-4 py-2 rounded-xl bg-[var(--color-pastel-red-500)] text-white font-semibold text-sm shadow-sm"
            >
              Entrar
            </Link>
          )}

          {/* Mobile Menu Toggle */}
          <button
            onClick={() => setShowMobileMenu(!showMobileMenu)}
            className="md:hidden p-2 text-gray-700 hover:bg-gray-100 rounded-xl"
          >
            {showMobileMenu ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
          </button>
        </div>
      </div>

      {/* Mobile Menu Dropdown */}
      {showMobileMenu && (
        <div className="md:hidden mt-3 pt-3 border-t border-gray-100 space-y-2 animate-fadeIn">
          {navLinks.map((link) => {
            const isActive = pathname.startsWith(link.href);
            return (
              <Link
                key={link.href}
                href={link.href}
                onClick={() => setShowMobileMenu(false)}
                className={clsx(
                  'flex items-center gap-3 px-4 py-3 rounded-xl font-bold text-sm',
                  isActive
                    ? 'bg-[var(--color-pastel-red-500)] text-white'
                    : 'text-[#2d2327] hover:bg-[var(--color-pastel-red-50)]'
                )}
              >
                {link.icon}
                <span>{link.label}</span>
              </Link>
            );
          })}
        </div>
      )}
    </header>
  );
};
