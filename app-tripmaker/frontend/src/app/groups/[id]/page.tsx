'use client';

import React, { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { useQuery } from '@tanstack/react-query';
import { api } from '../../../services/api';
import { TravelGroup, getGroupCoverUrl, getAvatarUrl, mapExpenseFromApi } from '../../../services/mockData';
import { AppLayout } from '../../../components/layout';
import { Tabs, TabItem, Badge, Button } from '../../../components/common';
import { OverviewTab, MembersTab, ExpensesTab, ChatTab, RoadmapTab } from './components';
import { useNotifications } from '../../../context/NotificationContext';
import { Compass, Users, DollarSign, MessageSquare, Map, Bell, BellOff, ArrowLeft, Loader2, Calendar, MapPin } from 'lucide-react';

export default function GroupDetailPage() {
  const params = useParams();
  const router = useRouter();
  const groupId = params?.id as string;

  const { isGroupMuted, toggleMuteGroup } = useNotifications();
  const [activeTab, setActiveTab] = useState('overview');
  const [groupState, setGroupState] = useState<TravelGroup | null>(null);

  const { data: fetchedGroup, isLoading } = useQuery<TravelGroup>({
    queryKey: ['group', groupId],
    queryFn: async () => {
      const res = await api.get(`/groups/${groupId}`);
      const g = res.data;
      return {
        ...g,
        id: String(g.id || g.inviteCode || groupId),
        inviteCode: g.inviteCode || String(g.id || groupId),
        title: g.title || g.name || 'Viagem',
        coverUrl: getGroupCoverUrl(g.destination, g.coverUrl),
        startDate: (g.startDate || '2026-11-01').includes('T') ? g.startDate : `${g.startDate || '2026-11-01'}T12:00:00`,
        endDate: (g.endDate || '2026-11-08').includes('T') ? g.endDate : `${g.endDate || '2026-11-08'}T12:00:00`,
        members: (g.members || []).map((m: any) => ({
          id: String(m.id),
          name: m.name || 'Viajante',
          email: m.email || '',
          role: m.role || 'membro',
          avatar: m.avatar || getAvatarUrl(m.name),
        })),
        expenses: (g.expenses || []).map((exp: any) => mapExpenseFromApi(exp, String(g.id || groupId))),
        messages: (g.messages || []).map((m: any) => ({
          id: String(m.id),
          groupId: String(g.id || groupId),
          senderId: String(m.senderId || ''),
          senderName: m.senderName || 'Viajante',
          senderAvatar: m.senderAvatar || getAvatarUrl(m.senderName),
          content: m.content || '',
          timestamp: m.createdAt ? new Date(m.createdAt).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' }) : '',
          type: m.type ? m.type.toLowerCase() : 'text',
        })),
        activities: g.activities || [],
        roadmap: g.roadmap || [],
      };
    },
    enabled: !!groupId,
  });

  useEffect(() => {
    if (fetchedGroup) {
      setGroupState(fetchedGroup);
    }
  }, [fetchedGroup]);

  if (isLoading || !groupState) {
    return (
      <AppLayout>
        <div className="py-24 flex flex-col items-center justify-center gap-3">
          <Loader2 className="w-10 h-10 animate-spin text-[var(--color-pastel-red-600)]" />
          <span className="text-sm font-semibold text-gray-500">Carregando painel da viagem...</span>
        </div>
      </AppLayout>
    );
  }

  const muted = isGroupMuted(groupState.id);

  const tabsList: TabItem[] = [
    { id: 'overview', label: 'Visão Geral', icon: <Compass className="w-4 h-4" /> },
    { id: 'members', label: 'Integrantes', icon: <Users className="w-4 h-4" />, badgeCount: groupState.members?.length || 0 },
    { id: 'rateio', label: 'Rateio de Contas', icon: <DollarSign className="w-4 h-4" />, badgeCount: groupState.expenses?.length || 0 },
    { id: 'chat', label: 'Chat da Viagem', icon: <MessageSquare className="w-4 h-4" />, badgeCount: groupState.messages?.length || 0 },
    { id: 'roadmap', label: 'Roadmap', icon: <Map className="w-4 h-4" />, badgeCount: groupState.roadmap?.length || 0 },
  ];

  return (
    <AppLayout>
      <div className="space-y-6">
        
        {/* Back navigation */}
        <div className="flex items-center justify-between">
          <button
            onClick={() => router.push('/groups')}
            className="inline-flex items-center gap-2 text-sm font-bold text-gray-600 hover:text-[var(--color-pastel-red-600)] transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>Voltar para Grupos</span>
          </button>
        </div>

        {/* Hero Banner with Cover & Title */}
        <div className="relative rounded-3xl overflow-hidden shadow-xl bg-gray-900 h-64 sm:h-80 w-full flex flex-col justify-end p-6 sm:p-10 text-white group border-2 border-white/80">
          <img
            src={groupState.coverUrl || 'https://images.unsplash.com/photo-1519046904884-53103b34b206?auto=format&fit=crop&w=1200&q=80'}
            alt={groupState.title}
            className="absolute inset-0 w-full h-full object-cover group-hover:scale-105 transition-transform duration-700 opacity-60"
          />
          <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-black/40 to-transparent" />

          {/* Top Right Actions (Mute toggle) */}
          <div className="absolute top-6 right-6 z-10 flex items-center gap-3">
            <button
              onClick={() => toggleMuteGroup(groupState.id)}
              title={muted ? 'Grupo silenciado. Clique para reativar notificações' : 'Silenciar notificações deste grupo'}
              className={`px-4 py-2.5 rounded-2xl backdrop-blur-md font-extrabold text-xs transition-all shadow-md flex items-center gap-2 ${
                muted
                  ? 'bg-rose-500 text-white hover:bg-rose-600 shadow-rose-500/30'
                  : 'bg-white/90 text-gray-800 hover:bg-white hover:text-[var(--color-pastel-red-600)]'
              }`}
            >
              {muted ? <BellOff className="w-4 h-4" /> : <Bell className="w-4 h-4" />}
              <span>{muted ? 'Grupo Silenciado (Muted)' : 'Alertas Ativos'}</span>
            </button>
          </div>

          {/* Hero Content */}
          <div className="relative z-10 space-y-3">
            <div className="flex flex-wrap items-center gap-2">
              <Badge variant="yellow" className="bg-[var(--color-pastel-yellow-300)] text-[#2d2327] px-3 py-1 text-xs font-bold flex items-center gap-1 shadow-xs">
                <MapPin className="w-3.5 h-3.5 text-[var(--color-pastel-red-700)]" />
                {groupState.destination}
              </Badge>
              <Badge variant="red" className="bg-[var(--color-pastel-red-100)] text-[var(--color-pastel-red-800)] px-3 py-1 text-xs font-bold flex items-center gap-1 shadow-xs">
                <Calendar className="w-3.5 h-3.5" />
                {new Date(groupState.startDate).toLocaleDateString('pt-BR')} até {new Date(groupState.endDate).toLocaleDateString('pt-BR')}
              </Badge>
            </div>
            <h1 className="text-3xl sm:text-5xl font-extrabold tracking-tight drop-shadow-sm leading-tight">
              {groupState.title}
            </h1>
          </div>
        </div>

        {/* Tab Navigation */}
        <div className="sticky top-20 z-30">
          <Tabs tabs={tabsList} activeTab={activeTab} onChange={setActiveTab} />
        </div>

        {/* Tab Content Rendering */}
        <div className="pt-2">
          {activeTab === 'overview' && (
            <OverviewTab group={groupState} onSwitchTab={setActiveTab} />
          )}

          {activeTab === 'members' && (
            <MembersTab
              group={groupState}
              onUpdateMembers={(newMembers) =>
                setGroupState({ ...groupState, members: newMembers })
              }
            />
          )}

          {activeTab === 'rateio' && (
            <ExpensesTab
              group={groupState}
              members={groupState.members || []}
              expenses={groupState.expenses || []}
              onUpdateExpenses={(newExpenses) =>
                setGroupState({ ...groupState, expenses: newExpenses })
              }
            />
          )}

          {activeTab === 'chat' && (
            <ChatTab
              group={groupState}
              onUpdateMessages={(newMessages) =>
                setGroupState({ ...groupState, messages: newMessages })
              }
            />
          )}

          {activeTab === 'roadmap' && (
            <RoadmapTab
              group={groupState}
              onUpdateRoadmap={(newRoadmap) =>
                setGroupState({ ...groupState, roadmap: newRoadmap })
              }
            />
          )}
        </div>

      </div>
    </AppLayout>
  );
}
