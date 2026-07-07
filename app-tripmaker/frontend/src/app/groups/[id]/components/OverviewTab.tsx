'use client';

import React from 'react';
import { TravelGroup } from '../../../../services/mockData';
import { Card, Badge, Button } from '../../../../components/common';
import { Calendar, MapPin, Users, DollarSign, MessageSquare, Map, ShieldCheck, Sparkles, Clock, Compass } from 'lucide-react';

interface OverviewTabProps {
  group: TravelGroup;
  onSwitchTab: (tabId: string) => void;
}

export const OverviewTab: React.FC<OverviewTabProps> = ({ group, onSwitchTab }) => {
  const totalExpenses = group.expenses.reduce((acc, curr) => acc + curr.amount, 0);

  return (
    <div className="space-y-6 animate-fadeIn">
      
      {/* Quick Stats Banner */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <Card
          variant="pastel-red"
          hoverEffect
          onClick={() => onSwitchTab('rateio')}
          className="p-5 flex items-center justify-between"
        >
          <div>
            <span className="text-xs font-bold text-[var(--color-pastel-red-800)] uppercase tracking-wider">
              Rateio Acumulado
            </span>
            <p className="text-2xl font-extrabold text-[#2d2327] mt-1">
              R$ {totalExpenses.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
            </p>
            <span className="text-[11px] text-gray-600 font-medium">
              {group.expenses.length} despesas registradas
            </span>
          </div>
          <div className="p-3.5 rounded-2xl bg-white text-[var(--color-pastel-red-600)] shadow-xs">
            <DollarSign className="w-6 h-6" />
          </div>
        </Card>

        <Card
          variant="pastel-yellow"
          hoverEffect
          onClick={() => onSwitchTab('roadmap')}
          className="p-5 flex items-center justify-between"
        >
          <div>
            <span className="text-xs font-bold text-[var(--color-pastel-yellow-700)] uppercase tracking-wider">
              Roadmap da Viagem
            </span>
            <p className="text-2xl font-extrabold text-[#2d2327] mt-1">
              {group.roadmap.length} Atividades
            </p>
            <span className="text-[11px] text-gray-600 font-medium">
              Roteiro dia a dia planejado
            </span>
          </div>
          <div className="p-3.5 rounded-2xl bg-white text-[var(--color-pastel-yellow-700)] shadow-xs">
            <Map className="w-6 h-6" />
          </div>
        </Card>

        <Card
          variant="glass"
          hoverEffect
          onClick={() => onSwitchTab('chat')}
          className="p-5 flex items-center justify-between border-2 border-white/80"
        >
          <div>
            <span className="text-xs font-bold text-gray-500 uppercase tracking-wider">
              Chat do Grupo
            </span>
            <p className="text-2xl font-extrabold text-[#2d2327] mt-1">
              {group.messages.length} Mensagens
            </p>
            <span className="text-[11px] text-gray-500 font-medium">
              Comunicação em tempo real
            </span>
          </div>
          <div className="p-3.5 rounded-2xl bg-gray-100 text-[#2d2327] shadow-xs">
            <MessageSquare className="w-6 h-6" />
          </div>
        </Card>
      </div>

      {/* About Section & Guidelines */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <Card variant="glass" className="lg:col-span-2 p-6 space-y-4 border-2 border-white/80">
          <div className="flex items-center gap-2 border-b border-gray-100 pb-3">
            <Compass className="w-5 h-5 text-[var(--color-pastel-red-600)]" />
            <h3 className="text-lg font-bold text-[#2d2327]">Sobre a Viagem</h3>
          </div>
          <p className="text-sm text-gray-700 leading-relaxed font-normal">
            {group.description || 'Este grupo foi criado no TripMaker para organizar todas as etapas desta incrível jornada, desde a compra das passagens e reserva de hospedagem até a divisão dos custos do churrasco e passeios!'}
          </p>

          <div className="pt-2 flex flex-wrap gap-2">
            <Badge variant="red" className="text-xs px-3 py-1 flex items-center gap-1">
              <Calendar className="w-3.5 h-3.5" />
              Início: {new Date(group.startDate).toLocaleDateString('pt-BR')}
            </Badge>
            <Badge variant="yellow" className="text-xs px-3 py-1 flex items-center gap-1">
              <Clock className="w-3.5 h-3.5" />
              Término: {new Date(group.endDate).toLocaleDateString('pt-BR')}
            </Badge>
            <Badge variant="green" className="text-xs px-3 py-1 flex items-center gap-1">
              <Users className="w-3.5 h-3.5" />
              {group.members.length} Participantes ativos
            </Badge>
          </div>
        </Card>

        {/* Quick Tips / Announcements */}
        <Card variant="pastel-yellow" className="p-6 space-y-4">
          <div className="flex items-center gap-2">
            <Sparkles className="w-5 h-5 text-[var(--color-pastel-yellow-700)]" />
            <h3 className="text-base font-bold text-[#2d2327]">Dicas do TripMaker</h3>
          </div>
          <ul className="space-y-3 text-xs text-gray-700 font-medium divide-y divide-[var(--color-pastel-yellow-200)]">
            <li className="pt-2 first:pt-0 flex items-start gap-2">
              <span className="text-[var(--color-pastel-red-600)] font-bold">•</span>
              <span>Cadastre todos os gastos na aba <b>Rateio de Contas</b> para o app calcular automaticamente quem deve quem no final.</span>
            </li>
            <li className="pt-2 flex items-start gap-2">
              <span className="text-[var(--color-pastel-red-600)] font-bold">•</span>
              <span>Use a aba <b>Roadmap</b> para combinar horários de saídas e pontos de encontro com todos os integrantes.</span>
            </li>
            <li className="pt-2 flex items-start gap-2">
              <span className="text-[var(--color-pastel-red-600)] font-bold">•</span>
              <span>Caso queira silenciar alertas de novas mensagens ou gastos desta viagem, basta clicar no botão <b>Silenciar</b> no topo.</span>
            </li>
          </ul>
        </Card>
      </div>

    </div>
  );
};
