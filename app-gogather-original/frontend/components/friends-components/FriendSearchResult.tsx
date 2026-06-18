'use client'

import { Plus, UserRound, UserRoundCheck, Forward, Check, Loader2 } from "lucide-react";
import { Button } from "../ui/button";
import { api } from "@/lib/api";
import { ResultSkeleton } from "./ResultSkeleton";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { FriendshipData } from "@/app/types";

interface FriendProps {
  externalId: string;
  displayName: string;
  username: string;
}

export function FriendSearchResult({ externalId, displayName, username}: FriendProps) {

  const queryClient = useQueryClient();

  const { data: friendship, isLoading } = useQuery({
    queryKey: ['friendship', externalId],
    queryFn: async () => {
      const res = await api.get<FriendshipData>(`/friendship/find/${externalId}`);
      return res.data;
    }
  });

  const acceptMutation = useMutation({
    mutationFn: async (fsId: string) => {
      await api.patch(`/friendship/accept/${fsId}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['friendship', externalId] });
      queryClient.invalidateQueries({ queryKey: ['friends-list'] });
      queryClient.invalidateQueries({ queryKey: ['pending-list'] });
    }
  });

  const sendFriendRequestMutation = useMutation({
    mutationFn: async (recId: string) => {
      await api.post(`/friendship/send/${recId}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['friendship'] });
    }
  });

  const renderAction = () => {
    if(!friendship || friendship.status === 'REJECTED') {
      return (
        <Button className='w-6 h-6 bg-emerald-500 cursor-pointer hover:bg-emerald-600'
          onClick={() => sendFriendRequestMutation.mutate(externalId)}
          disabled={sendFriendRequestMutation.isPending}>
            {sendFriendRequestMutation.isPending ? <Loader2 className="w-4 h-4 animate-spin"/> : <Plus className="w-4 h-4"/>}
        </Button>
      );
    }

    if(friendship.status === 'ACCEPTED') {
      return <UserRoundCheck className='w-6 h-6 bg-emerald-500 p-1 rounded-lg text-white' />;
    }

    if(friendship.status === 'PENDING') {
      if(friendship.receiverUsername === username) {
        return (
          <Button disabled className='w-6 h-6 bg-emerald-500/50 cursor-not-allowed'>
            <Forward className="w-4 h-4"/>
          </Button>
        )
      } else {
        return (
          <Button className='w-6 h-6 bg-emerald-500 cursor-pointer hover:bg-emerald-600'
            onClick={() => acceptMutation.mutate(friendship.fsExternalId)}
            disabled={acceptMutation.isPending}>
              {acceptMutation.isPending ? <Loader2 className="w-4 h-4 animate-spin"/> : <Check className="w-4 h-4"/>}
          </Button>
        );
      }
    }

    return null;
  }

  if(isLoading){
    return <ResultSkeleton/>
  }

  return (
    <div className='flex flex-row h-12 border border-dashed border-gg-beige-dark rounded-md items-center px-2 gap-2'>
      <UserRound className='w-8 h-8 p-1 bg-gg-beige-light rounded-full'/>
      <div className='flex flex-row w-full justify-between items-center'>
        <div className='flex flex-col'>
          <h1>{displayName}</h1>
          <p className='text-zinc-500 text-xs'>@{username}</p>
        </div>
        <div className='flex flex-row'>
          {renderAction()}
        </div>
      </div>
    </div>
  );
}
