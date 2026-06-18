'use client'

import { Book, UserRound } from "lucide-react"
import { PButton } from "../area-components/Button"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogDescription,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { FriendData } from "@/app/types"
import { api } from "@/lib/api"
import { ResultSkeleton } from "./ResultSkeleton"
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"

export function PendingButton() {

  const queryClient = useQueryClient();

  const { data: pending, isLoading: isLoadingPending } = useQuery({
    queryKey: ['pending-list'],
    queryFn: async () => {
      const res = await api.get<FriendData[]>('/friendship/pending');
      return res.data;
    }
  });

  const acceptMutation = useMutation({
    mutationFn: async (id: string) => {
      await api.patch(`/friendship/accept/${id}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pending-list'] });
      queryClient.invalidateQueries({ queryKey: ['friends-list'] });
    }
  });

  const refuseMutation = useMutation({
    mutationFn: async (id: string) => {
      await api.patch(`/friendship/refuse/${id}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pending-list'] });
    }
  });

  return (
    <Dialog>
      <DialogTrigger asChild>
        <PButton variant="beige" Icon={Book} className='h-10 text-md font-semibold'>Pedidos pendentes</PButton>
      </DialogTrigger>
      
      <DialogContent className='max-h-100'>
        <DialogHeader>
          <DialogTitle>Pedidos de amizade pendentes</DialogTitle>
          <DialogDescription>Aqui aparece as pessoas que querem começar uma amizade com você!</DialogDescription>
        </DialogHeader>
        <div>
          { isLoadingPending ?
            <ResultSkeleton/>
            : pending && pending.length > 0 ? (
            pending.map(obj => (
              <div key={obj.friendExternalId} className="border border-dashed rounded-sm p-2 flex flex-row justify-between items-center">
                <div className="flex flex-row items-center gap-2">
                  <UserRound className="w-8 h-8 p-1 bg-gg-beige rounded-full"/>
                  <div className="flex flex-col">
                    <h1 className="text-md">{obj.friendDisplayName}</h1>
                    <p className="text-xs text-zinc-400">@{obj.friendUsername}</p>
                  </div>
                </div>
                <div className="flex flex-row gap-2">
                  <span
                    className="bg-emerald-500 rounded-sm py-1 px-2 text-zinc-50 font-semibold
                      hover:bg-emerald-600 cursor-pointer transition-all"
                    onClick={() => acceptMutation.mutate(obj.fsExternalId)}>
                      Aceitar
                  </span>
                  <span
                    className="bg-gg-red-light rounded-sm py-1 px-2 text-zinc-50 font-semibold
                      hover:bg-gg-red cursor-pointer transition-all"
                    onClick={() => refuseMutation.mutate(obj.fsExternalId)}>
                      Recusar
                  </span>
                </div>
              </div>
            ))
          ) : <p>Sem pedidos pendentes...</p> }
        </div>
      </DialogContent>
    </Dialog>
  )
}