"use client";

import { useEffect, useState } from 'react';
import { Plus, Sparkles, MapPin, CalendarDays, Ticket, Users, Loader2 } from 'lucide-react';
import { Header } from '@/components/area-components/Header';
import { SubHeader } from '@/components/area-components/SubHeader';
import { HeaderButton } from '@/components/area-components/HeaderButton';
import { InfoCards } from '@/components/area-components/InfoCards';
import { ActionCard } from '@/components/area-components/ActionCard';
import { Empty } from '@/components/area-components/Empty';
import { api } from '@/lib/api';
import { GroupDetails } from '@/types/chat';
import Map from '@/components/map/Map';

export default function DashboardHome() {
  const [groups, setGroups] = useState<GroupDetails[]>([]);
  const [friendsCount, setFriendsCount] = useState(0);
  const [pendingCount, setPendingCount] = useState(0);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    async function fetchDashboardData() {
      try {
        setIsLoading(true);
        const [groupsRes, friendsRes, pendingRes] = await Promise.all([
          api.get<GroupDetails[]>('/groups'),
          api.get('/friendship'),
          api.get('/friendship/pending')
        ]);

        setGroups(groupsRes.data);
        setFriendsCount(friendsRes.data.length);
        setPendingCount(pendingRes.data.length);
      } catch (error) {
        console.error("Erro ao carregar dados do dashboard:", error);
      } finally {
        setIsLoading(false);
      }
    }

    fetchDashboardData();
  }, []);

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto pt-4 md:pt-6 pb-12">
      <div className="flex justify-between items-center mb-8">
        <Header description='Acompanhe seus eventos e convites pendentes'>Visão Geral</Header>
        <HeaderButton variant='red' Icon={Plus} href="/group/create">Novo Rolê</HeaderButton>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-12">
        <InfoCards info={groups.length.toString()} color='cyan' Icon={CalendarDays}>Próximos Rolês</InfoCards>
        <InfoCards info={pendingCount.toString()} color='red' Icon={Ticket}>Convites Pendentes</InfoCards>
        <InfoCards info={friendsCount.toString()} color='beige' Icon={Users}>Amigos na rede</InfoCards>
      </div>

      <div className="mb-12">
        <SubHeader className='mb-4'>Ações rápidas</SubHeader>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <ActionCard title='Criar com IA' footer='Gerar roteiro mágico →' color='cyan' Icon={Sparkles}>
            Deixe a magia acontecer! Diga a vibe da galera e nós sugerimos os melhores lugares e roteiros.
          </ActionCard>

          <ActionCard title='Criar Manualmente' footer='Montar roteiro do zero →' color='red' Icon={MapPin} href='/group/create'>
            Você no controle! Escolha os locais, defina a data, trace a rota e convide seus amigos.
          </ActionCard>
        </div>
      </div>

      <div>
        <div className="flex justify-between items-center mb-4">
          <SubHeader>Próximos encontros</SubHeader>
          <button className="text-sm font-bold text-[#458588] hover:underline">Ver histórico</button>
        </div>

        {groups.length > 0 ? (
          <div className="flex flex-col gap-5">
            {groups.map((group) => {
              const stops = group.eventStops || [];
              const mapLocais = stops.map((stop, index) => ({
                id: index,
                name: stop.name,
                time: "00:00", 
                latitude: stop.latitude,
                longitude: stop.longitude,
                category: stop.category || "Local"
              }));

              return (
                <div 
                  key={group.externalId} 
                  className="flex flex-col md:flex-row border rounded-xl overflow-hidden bg-card hover:shadow-md transition-shadow w-full"
                >
                  <div className="p-6 flex-1 flex flex-col justify-between z-10 bg-card">
                    <div>
                      <h4 className="font-bold text-xl text-[#282828] mb-2">{group.name}</h4>
                      <p className="text-sm text-muted-foreground line-clamp-2">{group.description}</p>
                    </div>
                    
                    <div className="flex items-center text-sm text-primary font-semibold mt-6">
                      <CalendarDays className="h-5 w-5 mr-2" />
                      {new Date(group.eventDate).toLocaleDateString('pt-BR', { 
                        weekday: 'long', 
                        day: '2-digit', 
                        month: 'long', 
                        hour: '2-digit', 
                        minute: '2-digit' 
                      })}
                    </div>
                  </div>
                  <div className="w-full md:w-[35%] min-h-[220px] bg-muted relative border-t md:border-t-0 md:border-l overflow-hidden">
                    <div className="absolute inset-0">
                       <Map locais={mapLocais} />
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        ) : (
          <Empty title='Nenhum encontro agendado.' Icon={CalendarDays}>
            Crie um novo rolê acima para começar a preencher sua agenda.
          </Empty>
        )}
      </div>
    </div>
  );
}