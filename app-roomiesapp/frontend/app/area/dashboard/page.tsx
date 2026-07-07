"use client";

import { useEffect, useState } from 'react';
import { Plus, Home, Wallet, ListTodo, Key, Users, Loader2 } from 'lucide-react';
import { Header } from '@/components/area-components/Header';
import { SubHeader } from '@/components/area-components/SubHeader';
import { HeaderButton } from '@/components/area-components/HeaderButton';
import { InfoCards } from '@/components/area-components/InfoCards';
import { ActionCard } from '@/components/area-components/ActionCard';
import { Empty } from '@/components/area-components/Empty';
import { api } from '@/lib/api';
import Link from 'next/link';

// Atualize esta tipagem no seu arquivo types/chat.ts depois
interface HouseDetails {
  inviteCode: string;
  name: string;
  description: string;
  createdAt: string;
}

export default function DashboardHome() {
  const [houses, setHouses] = useState<HouseDetails[]>([]);
  const [choresCount, setChoresCount] = useState(0); 
  const [billsCount, setBillsCount] = useState(0);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    async function fetchDashboardData() {
      try {
        setIsLoading(true);
        // Busca as casas que o usuário faz parte (usando a mesma rota herdada do framework)
        const groupsRes = await api.get<HouseDetails[]>('/groups');
        setHouses(groupsRes.data);

        // AFAZER: Quando os controllers de Chore e Bill estiverem prontos no back,
        // você pode descomentar e buscar as pendências reais do usuário aqui!
        // const [choresRes, billsRes] = await Promise.all([
        //   api.get('/chores/pending'),
        //   api.get('/bills/pending')
        // ]);
        setChoresCount(0); // Mock temporário
        setBillsCount(0);  // Mock temporário

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
        <Loader2 className="h-8 w-8 animate-spin text-ra-purple" />
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto pt-4 md:pt-6 pb-12">
      <div className="flex justify-between items-center mb-8">
        <Header description='Acompanhe suas contas, tarefas e convivência'>Visão Geral</Header>
        <HeaderButton variant='purple' Icon={Plus} href="/group/create">Nova Casa</HeaderButton>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-12">
        <InfoCards info={houses.length.toString()} color='purple' Icon={Home}>Minhas Casas</InfoCards>
        <InfoCards info={choresCount.toString()} color='green' Icon={ListTodo}>Tarefas Pendentes</InfoCards>
        <InfoCards info={billsCount.toString()} color='blue' Icon={Wallet}>Contas a Pagar</InfoCards>
      </div>

      <div className="mb-12">
        <SubHeader className='mb-4'>Ações rápidas</SubHeader>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <ActionCard title='Criar Nova Casa' footer='Configurar república →' color='purple' Icon={Home} href='/group/create'>
            Você é o administrador? Crie o espaço da sua casa, adicione as contas fixas e convide os moradores.
          </ActionCard>

          <ActionCard title='Entrar em uma Casa' footer='Usar código de convite →' color='green' Icon={Key}>
            Recebeu um link ou código (InviteCode) do seu colega de quarto? Cole aqui para se juntar à casa.
          </ActionCard>
        </div>
      </div>

      <div>
        <div className="flex justify-between items-center mb-4">
          <SubHeader>Minhas Casas</SubHeader>
        </div>

        {houses.length > 0 ? (
          <div className="flex flex-col gap-5">
            {houses.map((house) => (
              <Link href={`/area/groups/${house.inviteCode}`} key={house.inviteCode}>
                <div className="flex flex-col md:flex-row border rounded-xl overflow-hidden bg-card hover:shadow-md transition-all w-full cursor-pointer group">
                  
                  <div className="p-6 flex-1 flex flex-col justify-between z-10 bg-card">
                    <div>
                      <h4 className="font-bold text-xl text-gray-900 mb-2 group-hover:text-[#8724df] transition-colors">
                        {house.name}
                      </h4>
                      <p className="text-sm text-muted-foreground line-clamp-2">{house.description}</p>
                    </div>
                    
                    <div className="flex items-center text-sm text-[#8724df] font-semibold mt-6">
                      <Users className="h-5 w-5 mr-2" />
                      Gerenciar Moradores e Contas
                    </div>
                  </div>

                  {/* Substituímos o Mapa por um Banner com Gradiente e Ícone */}
                  <div className="w-full md:w-[35%] min-h-[160px] bg-roomies-gradient relative border-t md:border-t-0 md:border-l overflow-hidden flex items-center justify-center">
                    <Home className="w-20 h-20 text-white opacity-40 group-hover:opacity-60 group-hover:scale-110 transition-all duration-500" />
                  </div>

                </div>
              </Link>
            ))}
          </div>
        ) : (
          <Empty title='Nenhuma casa encontrada.' Icon={Home}>
            Crie uma nova casa acima ou peça o código de convite para os seus colegas de quarto.
          </Empty>
        )}
      </div>
    </div>
  );
}