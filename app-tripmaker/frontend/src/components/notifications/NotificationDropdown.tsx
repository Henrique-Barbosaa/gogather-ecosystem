'use client';

import React, { useState } from 'react';
import { useNotifications } from '../../context/NotificationContext';
import { Bell, BellOff, CheckCircle2, Trash2, ShieldAlert, MessageSquare, DollarSign, MapPin } from 'lucide-react';
import { useQuery } from '@tanstack/react-query';
import { api } from '../../services/api';

export const NotificationDropdown: React.FC = () => {
  const {
    notifications,
    mutedGroupIds,
    toggleMuteGroup,
    isGroupMuted,
    markAsRead,
    markAllAsRead,
    clearAll,
  } = useNotifications();

  const { data: groups = [] } = useQuery<any[]>({
    queryKey: ['groups'],
    queryFn: async () => {
      const res = await api.get('/groups');
      return res.data || [];
    },
  });

  const [activeTab, setActiveTab] = useState<'all' | 'muted'>('all');

  const getIcon = (type: string) => {
    switch (type) {
      case 'rateio':
        return <DollarSign className="w-4 h-4 text-emerald-600" />;
      case 'chat':
        return <MessageSquare className="w-4 h-4 text-[var(--color-pastel-red-600)]" />;
      case 'roadmap':
        return <MapPin className="w-4 h-4 text-[var(--color-pastel-yellow-700)]" />;
      default:
        return <Bell className="w-4 h-4 text-blue-600" />;
    }
  };

  return (
    <div className="w-80 sm:w-96 bg-white rounded-3xl shadow-2xl border border-[var(--color-pastel-red-200)] overflow-hidden animate-scaleUp z-50">
      {/* Header */}
      <div className="p-4 bg-gradient-to-r from-[var(--color-pastel-red-50)] to-[var(--color-pastel-yellow-50)] border-b border-gray-100 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Bell className="w-5 h-5 text-[var(--color-pastel-red-600)]" />
          <h4 className="font-bold text-[#2d2327]">Notificações & Alertas</h4>
        </div>
        <div className="flex items-center gap-1">
          {notifications.length > 0 && (
            <button
              onClick={markAllAsRead}
              title="Marcar todas como lidas"
              className="p-1.5 text-xs text-gray-500 hover:text-[var(--color-pastel-red-600)] transition-colors rounded-lg hover:bg-white/60"
            >
              <CheckCircle2 className="w-4 h-4" />
            </button>
          )}
          {notifications.length > 0 && (
            <button
              onClick={clearAll}
              title="Limpar todas"
              className="p-1.5 text-xs text-gray-500 hover:text-rose-600 transition-colors rounded-lg hover:bg-white/60"
            >
              <Trash2 className="w-4 h-4" />
            </button>
          )}
        </div>
      </div>

      {/* Sub-tabs for Notifications vs Silenced Groups */}
      <div className="flex border-b border-gray-100 bg-gray-50/50 text-xs font-semibold">
        <button
          onClick={() => setActiveTab('all')}
          className={`flex-1 py-2 text-center transition-colors border-b-2 ${
            activeTab === 'all'
              ? 'border-[var(--color-pastel-red-600)] text-[var(--color-pastel-red-700)] bg-white'
              : 'border-transparent text-gray-500 hover:text-[#2d2327]'
          }`}
        >
          Recentes ({notifications.length})
        </button>
        <button
          onClick={() => setActiveTab('muted')}
          className={`flex-1 py-2 text-center transition-colors border-b-2 flex items-center justify-center gap-1 ${
            activeTab === 'muted'
              ? 'border-[var(--color-pastel-red-600)] text-[var(--color-pastel-red-700)] bg-white'
              : 'border-transparent text-gray-500 hover:text-[#2d2327]'
          }`}
        >
          <BellOff className="w-3.5 h-3.5" />
          Silenciar Grupos ({mutedGroupIds.length})
        </button>
      </div>

      {/* Content */}
      <div className="max-h-80 overflow-y-auto divide-y divide-gray-100">
        {activeTab === 'all' && (
          <>
            {notifications.length === 0 ? (
              <div className="p-8 text-center text-gray-400 text-sm">
                Nenhuma notificação por aqui! ✨
              </div>
            ) : (
              notifications.map((notif) => {
                const muted = isGroupMuted(notif.groupId);
                return (
                  <div
                    key={notif.id}
                    onClick={() => markAsRead(notif.id)}
                    className={`p-3.5 transition-colors cursor-pointer flex items-start gap-3 ${
                      notif.read ? 'bg-white opacity-70' : 'bg-[var(--color-pastel-red-50)]/40 font-medium'
                    } hover:bg-gray-50`}
                  >
                    <div className="p-2 rounded-full bg-white shadow-xs border border-gray-100 shrink-0 mt-0.5">
                      {getIcon(notif.type)}
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center justify-between gap-1">
                        <span className="text-xs font-bold text-[#2d2327] truncate">
                          {notif.title}
                        </span>
                        <span className="text-[10px] text-gray-400 shrink-0">{notif.timestamp}</span>
                      </div>
                      <p className="text-xs text-gray-600 mt-0.5 line-clamp-2">{notif.message}</p>
                      <div className="flex items-center justify-between mt-1.5">
                        <span className="text-[10px] font-semibold text-[var(--color-pastel-red-700)] bg-[var(--color-pastel-red-100)]/60 px-2 py-0.5 rounded-md">
                          {notif.groupTitle}
                        </span>
                        {muted && (
                          <span className="text-[10px] text-gray-400 flex items-center gap-0.5" title="Grupo silenciado">
                            <BellOff className="w-3 h-3" /> Silenciado
                          </span>
                        )}
                      </div>
                    </div>
                  </div>
                );
              })
            )}
          </>
        )}

        {activeTab === 'muted' && (
          <div className="p-4 space-y-3">
            <div className="flex items-center gap-2 p-2.5 rounded-xl bg-[var(--color-pastel-yellow-50)] border border-[var(--color-pastel-yellow-200)] text-xs text-[var(--color-pastel-yellow-700)]">
              <ShieldAlert className="w-4 h-4 shrink-0" />
              <span>Grupos silenciados não emitem alertas sonoros nem contam no selo de novas notificações.</span>
            </div>
              {groups.length === 0 ? (
                <div className="p-4 text-center text-gray-400 text-xs">
                  Você ainda não possui grupos para silenciar.
                </div>
              ) : (
                groups.map((grp) => {
                  const idStr = String(grp.id || grp.inviteCode);
                  const titleStr = grp.title || grp.name || 'Viagem';
                  const muted = isGroupMuted(idStr);
                  return (
                    <div
                      key={idStr}
                      className="flex items-center justify-between p-2.5 rounded-xl bg-gray-50 border border-gray-100 hover:border-gray-200 transition-colors"
                    >
                      <span className="text-xs font-semibold text-[#2d2327] truncate pr-2">
                        {titleStr}
                      </span>
                      <button
                        onClick={() => toggleMuteGroup(idStr)}
                        className={`px-3 py-1 rounded-lg text-xs font-bold transition-all flex items-center gap-1 shrink-0 ${
                          muted
                            ? 'bg-rose-500 text-white shadow-xs shadow-rose-200'
                            : 'bg-white border border-gray-200 text-gray-600 hover:bg-gray-100'
                        }`}
                      >
                        <BellOff className="w-3 h-3" />
                        {muted ? 'Silenciado' : 'Silenciar'}
                      </button>
                    </div>
                  );
                })
              )}
          </div>
        )}
      </div>
    </div>
  );
};
