'use client'

import { UserRoundPlus, Search, Loader2} from 'lucide-react'
import { api } from '@/lib/api';
import { PButton } from "@/components/area-components/Button";
import { Input } from '../ui/input';
import { FriendSearchResult } from './FriendSearchResult';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { useEffect, useState } from 'react';
import { useQuery } from '@tanstack/react-query';

interface ResultData {
  externalId: string;
  displayName: string;
  username: string;
}

export function AddFriendButton() {

  const [searchQuery, setSearchQuery] = useState('');
  const [debouncedQuery, setDebouncedQuery] = useState('');

  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedQuery(searchQuery);
    }, 500);

    return () => clearTimeout(timer);
  }, [searchQuery]);

  const { data: results, isFetching } = useQuery({
    queryKey: ['users-search', debouncedQuery],
    queryFn: async () => {
      const res = await api.get<ResultData[]>(`/user/${debouncedQuery}`);
      return res.data;
    },
    enabled: debouncedQuery.length >= 3,
  });

  return (
    <Dialog>
      <DialogTrigger asChild>
        <PButton variant='emerald' Icon={UserRoundPlus} className='h-10 text-md font-semibold' type='button'>Adicionar amigo</PButton>
      </DialogTrigger>
      <DialogContent className='max-h-100'>
        <DialogHeader>
          <DialogTitle>Adicionar amigos!</DialogTitle>
        </DialogHeader>
        <div className='flex flex-col gap-2'>
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-zinc-800"/>
            <Input
              placeholder='Username do usuário...'
              className="pl-8"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}/>
          </div>
          { searchQuery === '' ? (
              <p className="text-sm text-gray-500">Pesquise por um nome de usuário!</p>
            ) : searchQuery.length < 3 ? (
              <p className="text-sm text-gray-500">Escreva no mínimo 3 caracteres...</p>
            ) : isFetching ? (
              <div className="flex items-center gap-2 text-sm text-gray-500">
                <Loader2 className="w-4 h-4 animate-spin" /> Buscando...
              </div>
            ) : results && results.length > 0 ? (
              results.map((obj) => <FriendSearchResult
                externalId={obj.externalId}
                displayName={obj.displayName}
                username={obj.username}
                key={obj.externalId}/>)
            ) : (
              <p className="text-sm text-gray-500">Nenhum usuário encontrado.</p>
            )}
        </div>
      </DialogContent>
    </Dialog>
  )
}
