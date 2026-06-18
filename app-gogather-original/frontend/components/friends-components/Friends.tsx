'use client'

import { RotateCw, ThumbsDown, UserRound, UserRoundX, Eye, MessageSquareMore } from 'lucide-react';
import { api } from '@/lib/api';
import { Empty } from '../area-components/Empty';
import { FriendData } from '@/app/types';
import { useQuery } from '@tanstack/react-query';

export function Friends() {

  const { data: friends, isLoading } = useQuery({
    queryKey: ['friends-list'],
    queryFn: async () => {
      const res = await api.get<FriendData[]>('/friendship');
      return res.data;
    }
  });

  if(isLoading){
    return <Empty title='Carregando...' Icon={RotateCw}>Aguarde um pouco. Estamos coletando seus amigos em nosso banco!</Empty>
  } else if(!friends || friends.length === 0) {
    return <Empty title='Você não possui amigos...' Icon={ThumbsDown}>Clique no botão de adicionar amigos para começar suas conexões!</Empty>
  }

  return (
    <div className="w-full bg-gg-beige-light p-4 rounded-lg flex flex-row gap-4">
      {friends.map((obj) => (
        <div key={obj.friendExternalId} className='w-1/3'>
          <div className='flex flex-col'>
            <div className='flex flex-col items-center bg-gg-beige-extralight p-5 px-15 rounded-t-xl'>
              <UserRound className='w-12 h-12 bg-gg-beige p-2 rounded-full'/>
              <div className='flex flex-col items-center'>
                <h1 className='text-xl'>{obj.friendDisplayName}</h1>
                <p className='text-sm text-zinc-500'>@{obj.friendUsername}</p>
              </div>
            </div>
            <div className='w-full h-10 bg-gg-beige rounded-b-xl flex flex-row'>
              <div className='border-r-2 border-gg-beige-dark w-1/3 flex justify-center items-center rounded-bl-xl hover:bg-gg-beige-dark cursor-pointer'>
                <UserRoundX className='w-5 h-5'/>
              </div>
              <div className='border-r-2 border-gg-beige-dark w-1/3 flex justify-center items-center hover:bg-gg-beige-dark cursor-pointer'>
                <Eye className='w-5 h-5'/>
              </div>
              <div className='w-1/3 flex justify-center items-center rounded-br-xl hover:bg-gg-beige-dark cursor-pointer'>
                <MessageSquareMore className='w-5 h-5'/>
              </div>
            </div>
          </div>
        </div>
      ))}
    </div>
  )
}
