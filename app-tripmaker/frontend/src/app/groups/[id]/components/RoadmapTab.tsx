'use client';

import React, { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { TravelGroup, RoadmapItem } from '../../../../services/mockData';
import { Card, Button, Input, Modal, Badge } from '../../../../components/common';
import { Map, Plus, Clock, MapPin, Calendar, Compass, Sparkles, Check, Tag } from 'lucide-react';
import { useNotifications } from '../../../../context/NotificationContext';
import { api } from '../../../../services/api';

const roadmapSchema = z.object({
  day: z.string().min(1, 'Especifique o dia (ex: Dia 1, 28/12)'),
  title: z.string().min(3, 'Digite o título da atividade'),
  time: z.string().min(1, 'Horário é obrigatório'),
  location: z.string().min(2, 'Local ou endereço é obrigatório'),
  category: z.enum(['Chegada', 'Passeio', 'Refeição', 'Festa', 'Outros']),
  description: z.string().optional(),
});

type RoadmapFormValues = z.infer<typeof roadmapSchema>;

interface RoadmapTabProps {
  group: TravelGroup;
  onUpdateRoadmap: (newRoadmap: RoadmapItem[]) => void;
}

export const RoadmapTab: React.FC<RoadmapTabProps> = ({ group, onUpdateRoadmap }) => {
  const { addNotification } = useNotifications();
  const [roadmap, setRoadmap] = useState<RoadmapItem[]>(group.roadmap || []);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [successMsg, setSuccessMsg] = useState(false);

  useEffect(() => {
    setRoadmap(group.roadmap || []);
  }, [group.roadmap]);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<RoadmapFormValues>({
    resolver: zodResolver(roadmapSchema),
    defaultValues: {
      day: 'Dia 1',
      title: '',
      time: '14:00',
      location: '',
      category: 'Passeio',
      description: '',
    },
  });

  const onSubmit = async (data: RoadmapFormValues) => {
    try {
      const res = await api.post(`/groups/${group.id}/itinerary`, {
        title: data.title,
        description: data.description || '',
        location: data.location,
        day: data.day,
        time: data.time,
        category: data.category,
        costEstimateCents: 0
      });
      const newItem: RoadmapItem = res.data;
      const updated = [...roadmap, newItem];
      setRoadmap(updated);
      onUpdateRoadmap(updated);
      setIsModalOpen(false);
      reset();
      setSuccessMsg(true);

      addNotification({
        groupId: group.id,
        groupTitle: group.title,
        type: 'roadmap',
        title: `Roadmap Atualizado! 🗺️`,
        message: `Nova atividade adicionada em ${data.day}: "${data.title}" às ${data.time}.`,
      });

      setTimeout(() => setSuccessMsg(false), 3000);
    } catch (err) {
      console.error('Erro ao salvar item no roadmap:', err);
      alert('Não foi possível salvar o item no banco de dados.');
    }
  };

  const getCategoryBadge = (cat: RoadmapItem['category']) => {
    switch (cat) {
      case 'Chegada':
        return <Badge variant="purple">{cat}</Badge>;
      case 'Passeio':
        return <Badge variant="yellow">{cat}</Badge>;
      case 'Refeição':
        return <Badge variant="red">{cat}</Badge>;
      case 'Festa':
        return <Badge variant="green">{cat}</Badge>;
      default:
        return <Badge variant="gray">{cat}</Badge>;
    }
  };

  return (
    <div className="space-y-6 animate-fadeIn">
      
      {/* Header Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-gradient-to-r from-[var(--color-pastel-yellow-50)] via-[var(--color-pastel-red-50)] to-[#fffefe] p-6 rounded-3xl border border-[var(--color-pastel-yellow-300)] shadow-2xs">
        <div>
          <h3 className="text-xl font-extrabold text-[#2d2327] flex items-center gap-2">
            <Map className="w-6 h-6 text-[var(--color-pastel-yellow-700)]" />
            Roadmap & Roteiro da Viagem ({roadmap.length} atividades)
          </h3>
          <p className="text-xs text-gray-600 mt-0.5">
            Acompanhe a linha do tempo dia a dia para ninguém perder os passeios e horários marcados.
          </p>
        </div>
        <Button
          variant="primary"
          onClick={() => setIsModalOpen(true)}
          leftIcon={<Plus className="w-4 h-4" />}
          className="shadow-md"
        >
          Adicionar Atividade
        </Button>
      </div>

      {successMsg && (
        <div className="p-4 rounded-2xl bg-emerald-50 border border-emerald-200 text-emerald-800 text-sm font-semibold flex items-center gap-2 animate-fadeIn">
          <Check className="w-5 h-5 text-emerald-600" />
          <span>Atividade inserida no roteiro com sucesso! Os integrantes foram notificados. 🗺️</span>
        </div>
      )}

      {/* Timeline List */}
      {roadmap.length === 0 ? (
        <Card variant="glass" className="py-16 text-center space-y-3">
          <Compass className="w-16 h-16 text-gray-300 mx-auto animate-pulse" />
          <div>
            <h4 className="text-base font-bold text-[#2d2327]">O roadmap está vazio!</h4>
            <p className="text-xs text-gray-500 max-w-md mx-auto mt-1">
              Organize os horários de voos, check-ins, festas e passeios turísticos para facilitar a vida do grupo.
            </p>
          </div>
          <Button variant="primary" size="sm" onClick={() => setIsModalOpen(true)} leftIcon={<Plus className="w-4 h-4" />}>
            Criar Primeira Atividade
          </Button>
        </Card>
      ) : (
        <div className="relative border-l-2 border-[var(--color-pastel-red-300)] ml-4 sm:ml-8 pl-6 sm:pl-8 space-y-8 py-2">
          {roadmap.map((item, index) => (
            <div key={item.id} className="relative group animate-fadeIn">
              
              {/* Timeline dot icon */}
              <div className="absolute -left-[35px] sm:-left-[43px] top-1.5 w-7 h-7 sm:w-8 sm:h-8 rounded-full bg-gradient-to-br from-[var(--color-pastel-red-500)] to-[var(--color-pastel-yellow-500)] text-white flex items-center justify-center shadow-md group-hover:scale-110 transition-transform">
                <Clock className="w-4 h-4" />
              </div>

              {/* Event Card */}
              <Card variant="glass" className="p-5 border-2 border-white/90 shadow-sm group-hover:shadow-md transition-shadow">
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-gray-100 pb-3 mb-3">
                  <div className="flex items-center gap-2">
                    <span className="px-3 py-1 rounded-xl bg-[var(--color-pastel-red-100)] text-[var(--color-pastel-red-800)] font-extrabold text-xs">
                      {item.day}
                    </span>
                    <span className="text-sm font-extrabold text-[#2d2327]">
                      {item.time}
                    </span>
                    <span className="text-gray-300">•</span>
                    <h4 className="font-bold text-[#2d2327] text-base truncate">
                      {item.title}
                    </h4>
                  </div>
                  <div className="shrink-0">{getCategoryBadge(item.category)}</div>
                </div>

                <div className="space-y-2 text-xs">
                  <div className="flex items-center gap-1.5 font-semibold text-gray-700">
                    <MapPin className="w-4 h-4 text-[var(--color-pastel-red-500)] shrink-0" />
                    <span>{item.location}</span>
                  </div>

                  {item.description && (
                    <p className="text-gray-600 pl-5 leading-relaxed font-normal">
                      {item.description}
                    </p>
                  )}
                </div>
              </Card>
            </div>
          ))}
        </div>
      )}

      {/* Add Roadmap Item Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Inserir Atividade no Roadmap"
        maxWidth="md"
      >
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <Input
              label="Dia / Etapa"
              placeholder="Ex: Dia 1 (28/12)"
              error={errors.day?.message}
              {...register('day')}
            />
            <Input
              label="Horário Planejado"
              type="time"
              error={errors.time?.message}
              {...register('time')}
            />
          </div>

          <Input
            label="Título da Atividade"
            placeholder="Ex: Check-in no Resort e Brinde de Boas-vindas"
            error={errors.title?.message}
            {...register('title')}
          />

          <Input
            label="Local / Endereço"
            placeholder="Ex: Praia dos Ingleses ou Restaurante Mar e Sol"
            leftIcon={<MapPin className="w-4 h-4" />}
            error={errors.location?.message}
            {...register('location')}
          />

          <div className="flex flex-col gap-1">
            <label className="text-sm font-semibold text-[#2d2327]">Categoria da Atividade</label>
            <select
              className="w-full bg-white/90 border border-gray-200 rounded-xl p-2.5 text-sm text-[#2d2327] outline-none focus:border-[var(--color-pastel-red-500)] focus:ring-2 focus:ring-[var(--color-pastel-red-200)] shadow-xs"
              {...register('category')}
            >
              <option value="Passeio">Passeio / Excursão</option>
              <option value="Refeição">Refeição / Gastronomia</option>
              <option value="Chegada">Chegada / Transporte</option>
              <option value="Festa">Festa / Vida Noturna</option>
              <option value="Outros">Outros</option>
            </select>
          </div>

          <div className="flex flex-col gap-1">
            <label className="text-sm font-semibold text-[#2d2327]">Descrição ou Observações</label>
            <textarea
              rows={3}
              placeholder="Dica de traje, ponto de encontro exato, ingressos necessários..."
              className="w-full bg-white/90 border border-gray-200 rounded-xl p-3 text-sm text-[#2d2327] outline-none focus:border-[var(--color-pastel-red-500)] focus:ring-2 focus:ring-[var(--color-pastel-red-200)] shadow-xs"
              {...register('description')}
            />
          </div>

          <div className="pt-3 flex justify-end gap-2 border-t border-gray-100">
            <Button type="button" variant="ghost" onClick={() => setIsModalOpen(false)}>
              Cancelar
            </Button>
            <Button type="submit" variant="primary" leftIcon={<Sparkles className="w-4 h-4" />}>
              Adicionar ao Roteiro
            </Button>
          </div>
        </form>
      </Modal>

    </div>
  );
};
