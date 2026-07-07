'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { api } from '../../services/api';
import { TravelGroup, getGroupCoverUrl, getAvatarUrl, mapExpenseFromApi } from '../../services/mockData';
import { AppLayout } from '../../components/layout';
import { Card, Button, Input, Modal, Badge } from '../../components/common';
import { useNotifications } from '../../context/NotificationContext';
import { Compass, Plus, Calendar, MapPin, Users, Bell, BellOff, ArrowRight, Loader2, Sparkles, Image as ImageIcon } from 'lucide-react';

const createGroupSchema = z.object({
  title: z.string().min(3, 'O título deve ter no mínimo 3 caracteres'),
  destination: z.string().min(2, 'O destino é obrigatório'),
  startDate: z.string().min(1, 'Data de início é obrigatória'),
  endDate: z.string().min(1, 'Data de término é obrigatória'),
  description: z.string().optional(),
  coverUrl: z.string().url('Digite uma URL válida de imagem').optional().or(z.literal('')),
});

type CreateGroupFormValues = z.infer<typeof createGroupSchema>;

export default function GroupsListPage() {
  const queryClient = useQueryClient();
  const { isGroupMuted, toggleMuteGroup, addNotification } = useNotifications();
  const [isModalOpen, setIsModalOpen] = useState(false);

  // Fetch groups
  const { data: groups = [], isLoading } = useQuery<TravelGroup[]>({
    queryKey: ['groups'],
    queryFn: async () => {
      const res = await api.get('/groups');
      const raw = res.data || [];
      return raw.map((g: any) => ({
        ...g,
        id: String(g.id || g.inviteCode),
        inviteCode: g.inviteCode || String(g.id),
        title: g.title || g.name || 'Viagem Sem Título',
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
        expenses: (g.expenses || []).map((exp: any) => mapExpenseFromApi(exp, String(g.id || g.inviteCode))),
        messages: (g.messages || []).map((m: any) => ({
          id: String(m.id),
          groupId: String(g.id),
          senderId: String(m.senderId || ''),
          senderName: m.senderName || 'Viajante',
          senderAvatar: m.senderAvatar || getAvatarUrl(m.senderName),
          content: m.content || '',
          timestamp: m.createdAt ? new Date(m.createdAt).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' }) : '',
          type: m.type ? m.type.toLowerCase() : 'text',
        })),
        activities: g.activities || [],
        roadmap: g.roadmap || [],
      }));
    },
  });

  // Create group mutation
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<CreateGroupFormValues>({
    resolver: zodResolver(createGroupSchema),
    defaultValues: {
      title: '',
      destination: '',
      startDate: '2026-11-01',
      endDate: '2026-11-08',
      description: '',
      coverUrl: '',
    },
  });

  const createMutation = useMutation({
    mutationFn: async (data: CreateGroupFormValues) => {
      const payload = {
        name: data.title,
        description: data.description || '',
        destination: data.destination || '',
        startDate: data.startDate || '2026-11-01',
        endDate: data.endDate || '2026-11-08',
        coverUrl: data.coverUrl || 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1200&q=80',
        maxTravelers: 30,
      };
      const res = await api.post('/groups', payload);
      const g = res.data;
      return {
        ...g,
        id: String(g.id || g.inviteCode),
        inviteCode: g.inviteCode || String(g.id),
        title: g.title || g.name || data.title,
        coverUrl: getGroupCoverUrl(g.destination || data.destination, g.coverUrl || data.coverUrl),
        startDate: (g.startDate || data.startDate || '2026-11-01').includes('T') ? (g.startDate || data.startDate) : `${g.startDate || data.startDate || '2026-11-01'}T12:00:00`,
        endDate: (g.endDate || data.endDate || '2026-11-08').includes('T') ? (g.endDate || data.endDate) : `${g.endDate || data.endDate || '2026-11-08'}T12:00:00`,
        members: (g.members || []).map((m: any) => ({
          id: String(m.id),
          name: m.name || 'Viajante',
          email: m.email || '',
          role: m.role || 'membro',
          avatar: m.avatar || getAvatarUrl(m.name),
        })),
        expenses: (g.expenses || []).map((exp: any) => mapExpenseFromApi(exp, String(g.id || g.inviteCode))),
        messages: (g.messages || []).map((m: any) => ({
          id: String(m.id),
          groupId: String(g.id),
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
    onSuccess: (newGroup) => {
      queryClient.invalidateQueries({ queryKey: ['groups'] });
      setIsModalOpen(false);
      reset();
      addNotification({
        groupId: newGroup.id,
        groupTitle: newGroup.title,
        type: 'roadmap',
        title: 'Nova Viagem Criada! ✈️',
        message: `O grupo "${newGroup.title}" foi criado e está pronto para rateios e roteiros!`,
      });
    },
  });

  const onSubmit = (data: CreateGroupFormValues) => {
    createMutation.mutate(data);
  };

  return (
    <AppLayout>
      <div className="space-y-8">
        
        {/* Banner Section */}
        <div className="relative rounded-3xl overflow-hidden shadow-lg bg-gradient-to-r from-[var(--color-pastel-red-500)] via-[var(--color-pastel-red-400)] to-[var(--color-pastel-yellow-400)] p-8 sm:p-10 text-white flex flex-col sm:flex-row items-center justify-between gap-6">
          <div className="absolute -top-12 -left-12 w-64 h-64 bg-white/20 rounded-full blur-3xl pointer-events-none" />
          <div className="relative z-10 max-w-xl text-center sm:text-left">
            <span className="inline-flex items-center gap-1 px-3 py-1 rounded-full bg-white/20 backdrop-blur-md text-xs font-bold mb-2">
              <Sparkles className="w-3.5 h-3.5" /> Suas Próximas Jornadas
            </span>
            <h1 className="text-3xl sm:text-4xl font-extrabold tracking-tight drop-shadow-xs">
              Grupos de Viagem
            </h1>
            <p className="text-white/90 text-sm font-medium mt-1">
              Gerencie seus roteiros, converse no chat e calcule o rateio de despesas sem complicação.
            </p>
          </div>
          <div className="relative z-10 shrink-0">
            <Button
              variant="secondary"
              size="lg"
              onClick={() => setIsModalOpen(true)}
              leftIcon={<Plus className="w-5 h-5" />}
              className="shadow-xl font-bold text-[#2d2327] bg-white hover:bg-gray-50 border-0"
            >
              Criar Novo Grupo
            </Button>
          </div>
        </div>

        {/* Groups Grid */}
        {isLoading ? (
          <div className="py-20 flex flex-col items-center justify-center gap-3">
            <Loader2 className="w-10 h-10 animate-spin text-[var(--color-pastel-red-600)]" />
            <span className="text-sm font-semibold text-gray-500">Carregando grupos de viagem...</span>
          </div>
        ) : groups.length === 0 ? (
          <Card variant="glass" className="py-16 text-center space-y-4">
            <Compass className="w-16 h-16 text-gray-300 mx-auto animate-pulse" />
            <div>
              <h3 className="text-lg font-bold text-[#2d2327]">Nenhum grupo encontrado</h3>
              <p className="text-sm text-gray-500 max-w-md mx-auto mt-1">
                Você ainda não faz parte de nenhuma viagem. Clique em <b>Criar Novo Grupo</b> para começar sua primeira jornada!
              </p>
            </div>
            <Button variant="primary" onClick={() => setIsModalOpen(true)} leftIcon={<Plus className="w-4 h-4" />}>
              Criar Primeira Viagem
            </Button>
          </Card>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {groups.map((group) => {
              const muted = isGroupMuted(group.id);
              return (
                <div key={group.id} className="group relative flex flex-col">
                  <Card
                    variant="glass"
                    hoverEffect
                    className="p-0 overflow-hidden border-2 border-white/80 flex flex-col h-full shadow-md"
                  >
                    {/* Cover Image */}
                    <div className="relative h-48 w-full overflow-hidden bg-gray-100">
                      <img
                        src={group.coverUrl || 'https://images.unsplash.com/photo-1519046904884-53103b34b206?auto=format&fit=crop&w=1200&q=80'}
                        alt={group.title}
                        className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                      />
                      <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/20 to-transparent" />
                      
                      {/* Destination Badge */}
                      <div className="absolute top-3 left-3">
                        <Badge variant="yellow" className="bg-white/90 backdrop-blur-md text-[#2d2327] font-bold text-xs flex items-center gap-1 px-3 py-1 shadow-sm">
                          <MapPin className="w-3.5 h-3.5 text-[var(--color-pastel-red-600)]" />
                          {group.destination}
                        </Badge>
                      </div>

                      {/* Mute Button (Mini notification system requirement) */}
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          e.preventDefault();
                          toggleMuteGroup(group.id);
                        }}
                        title={muted ? 'Grupo silenciado. Clique para ativar alertas' : 'Silenciar notificações deste grupo'}
                        className={`absolute top-3 right-3 p-2 rounded-xl backdrop-blur-md transition-all shadow-sm flex items-center gap-1.5 text-xs font-bold ${
                          muted
                            ? 'bg-rose-500/90 text-white hover:bg-rose-600'
                            : 'bg-white/90 text-gray-700 hover:bg-white hover:text-[var(--color-pastel-red-600)]'
                        }`}
                      >
                        {muted ? <BellOff className="w-3.5 h-3.5" /> : <Bell className="w-3.5 h-3.5" />}
                        <span className="hidden sm:inline">{muted ? 'Silenciado' : 'Ativo'}</span>
                      </button>

                      {/* Title on image */}
                      <div className="absolute bottom-3 left-3 right-3">
                        <h3 className="text-xl font-extrabold text-white leading-tight drop-shadow-sm truncate">
                          {group.title}
                        </h3>
                      </div>
                    </div>

                    {/* Card Content */}
                    <div className="p-5 flex-1 flex flex-col justify-between space-y-4">
                      <p className="text-xs text-gray-600 line-clamp-2 font-normal">
                        {group.description || 'Sem descrição.'}
                      </p>

                      <div className="pt-2 border-t border-gray-100 flex items-center justify-between text-xs font-semibold text-gray-500">
                        <div className="flex items-center gap-1.5">
                          <Calendar className="w-4 h-4 text-[var(--color-pastel-red-500)]" />
                          <span>{new Date(group.startDate).toLocaleDateString('pt-BR', { day: '2-digit', month: 'short' })} a {new Date(group.endDate).toLocaleDateString('pt-BR', { day: '2-digit', month: 'short' })}</span>
                        </div>
                        <div className="flex items-center gap-1 text-[var(--color-pastel-yellow-700)] bg-[var(--color-pastel-yellow-100)] px-2 py-0.5 rounded-md">
                          <Users className="w-3.5 h-3.5" />
                          <span>{group.members?.length || 1} membros</span>
                        </div>
                      </div>

                      <Link
                        href={`/groups/${group.id}`}
                        className="w-full inline-flex items-center justify-center gap-2 py-2.5 px-4 rounded-xl font-bold text-sm bg-[var(--color-pastel-red-50)] text-[var(--color-pastel-red-700)] hover:bg-gradient-to-r hover:from-[var(--color-pastel-red-500)] hover:to-[var(--color-pastel-red-600)] hover:text-white transition-all shadow-2xs group-hover:shadow-md"
                      >
                        <span>Acessar Painel do Grupo</span>
                        <ArrowRight className="w-4 h-4 transform group-hover:translate-x-1 transition-transform" />
                      </Link>
                    </div>
                  </Card>
                </div>
              );
            })}
          </div>
        )}

        {/* Create Group Modal */}
        <Modal
          isOpen={isModalOpen}
          onClose={() => setIsModalOpen(false)}
          title="Planejar Nova Viagem"
          maxWidth="lg"
        >
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <Input
              label="Nome da Viagem"
              placeholder="Ex: Réveillon em Florianópolis 🏖️"
              leftIcon={<Compass className="w-4 h-4" />}
              error={errors.title?.message}
              {...register('title')}
            />

            <Input
              label="Destino / Cidade"
              placeholder="Ex: Florianópolis, SC"
              leftIcon={<MapPin className="w-4 h-4" />}
              error={errors.destination?.message}
              {...register('destination')}
            />

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <Input
                label="Data de Início"
                type="date"
                error={errors.startDate?.message}
                {...register('startDate')}
              />
              <Input
                label="Data de Término"
                type="date"
                error={errors.endDate?.message}
                {...register('endDate')}
              />
            </div>

            <Input
              label="URL da Imagem de Capa (Opcional)"
              placeholder="https://images.unsplash.com/..."
              leftIcon={<ImageIcon className="w-4 h-4" />}
              error={errors.coverUrl?.message}
              {...register('coverUrl')}
            />

            <div className="flex flex-col gap-1">
              <label className="text-sm font-semibold text-[#2d2327]">Descrição da Viagem</label>
              <textarea
                rows={3}
                placeholder="Detalhes, recomendações ou motivação da viagem..."
                className="w-full bg-white/90 border border-gray-200 rounded-xl p-3 text-sm text-[#2d2327] outline-none focus:border-[var(--color-pastel-red-500)] focus:ring-2 focus:ring-[var(--color-pastel-red-200)] shadow-xs"
                {...register('description')}
              />
            </div>

            <div className="pt-3 flex justify-end gap-3 border-t border-gray-100">
              <Button type="button" variant="ghost" onClick={() => setIsModalOpen(false)}>
                Cancelar
              </Button>
              <Button
                type="submit"
                variant="primary"
                isLoading={createMutation.isPending}
                leftIcon={<Sparkles className="w-4 h-4" />}
              >
                Criar Viagem e Convidar
              </Button>
            </div>
          </form>
        </Modal>

      </div>
    </AppLayout>
  );
}
