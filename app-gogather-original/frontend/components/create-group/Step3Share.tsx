"use client";

import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Check, Copy, ExternalLink, PartyPopper, UserPlus, Loader2 } from "lucide-react";
import { useRouter } from "next/navigation";
import { api } from "@/lib/api";
import { FriendData } from "@/app/types";

interface StepShareProps {
  roleName: string;
  inviteCode: string;
  groupId: string;
}

export const Step3Share = ({ roleName, inviteCode, groupId }: StepShareProps) => {
  const router = useRouter();
  
  const [copied, setCopied] = useState<boolean>(false);
  const [friends, setFriends] = useState<FriendData[]>([]);
  const [invitedFriends, setInvitedFriends] = useState<Record<string, 'loading' | boolean>>({});
  const [loadingFriends, setLoadingFriends] = useState<boolean>(true);

  useEffect(() => {
    const fetchFriends = async () => {
      try {
        const res = await api.get("/friendship");
        const acceptedFriends = res.data.filter((f: FriendData) => f.status === 'ACCEPTED');
        setFriends(acceptedFriends);
      } catch (err) {
        console.error("Erro ao buscar amigos:", err);
      } finally {
        setLoadingFriends(false);
      }
    };
    fetchFriends();
  }, []);

  const handleInviteFriend = async (friendId: string) => {
    try {
      setInvitedFriends(prev => ({ ...prev, [friendId]: 'loading' }));
      await api.post(`/groups/${groupId}/invite/${friendId}`);
      setInvitedFriends(prev => ({ ...prev, [friendId]: true }));
    } catch (err) {
      console.error("Erro ao convidar amigo:", err);
      setInvitedFriends(prev => ({ ...prev, [friendId]: false }));
    }
  };

  const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

  const inviteLink = `${apiUrl}/join/${inviteCode}`;

  const handleCopy = (): void => {
    navigator.clipboard.writeText(inviteLink);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleWhatsApp = (): void => {
    const text = encodeURIComponent(
      `🎉 Bora pro rolê "${roleName}"! Entre com o código: ${inviteCode}\n\nAcesse: ${inviteLink}`
    );
    window.open(`https://wa.me/?text=${text}`, "_blank");
  };

  return (
    <div className="flex w-full items-start justify-center px-6 pt-10 pb-24">
      <div className="w-full max-w-2xl bg-white rounded-2xl shadow-xl overflow-hidden border border-gray-100">
        
        <div className="text-center pt-10 pb-6 px-8">
          <div className="inline-flex items-center justify-center h-16 w-16 rounded-full bg-red-50 text-[#cc241a] mx-auto mb-4">
            <PartyPopper className="h-8 w-8" />
          </div>
          <h2 className="text-2xl font-bold text-gray-900">Rolê criado com sucesso!</h2>
        </div>

        <div className="px-8 pb-8 space-y-6">
          
          <div className="space-y-4">
            <p className="text-center text-sm font-medium text-gray-500">Compartilhe o código para convidar pessoas para o rolê.</p>
            
            <div className="bg-gray-50 rounded-xl border-2 border-dashed border-gg-cyan p-5 text-center space-y-1">
              <p className="text-xs font-semibold text-gray-500 uppercase tracking-wider">Código de Convite</p>
              <p className="text-3xl font-black tracking-[0.2em] text-gg-cyan">{inviteCode}</p>
            </div>
            
            <div className="flex gap-3">
              <Button
                variant="outline"
                onClick={handleCopy}
                className="flex-1 h-11 rounded-xl gap-2 font-semibold transition-all"
              >
                {copied ? <Check className="h-4 w-4 text-green-600" /> : <Copy className="h-4 w-4" />}
                {copied ? "Copiado!" : "Copiar Link"}
              </Button>
              
              <Button
                onClick={handleWhatsApp}
                className="flex-1 h-11 rounded-xl gap-2 font-semibold bg-[#25D366] hover:bg-[#20bd5a] text-white transition-colors"
              >
                <svg viewBox="0 0 24 24" className="h-5 w-5 fill-current">
                  <path d="M17.472 14.382c-.297-.149-1.758-.867-2.03-.967-.273-.099-.471-.148-.67.15-.197.297-.767.966-.94 1.164-.173.199-.347.223-.644.075-.297-.15-1.255-.463-2.39-1.475-.883-.788-1.48-1.761-1.653-2.059-.173-.297-.018-.458.13-.606.134-.133.298-.347.446-.52.149-.174.198-.298.298-.497.099-.198.05-.371-.025-.52-.075-.149-.669-1.612-.916-2.207-.242-.579-.487-.5-.669-.51-.173-.008-.371-.01-.57-.01-.198 0-.52.074-.792.372-.272.297-1.04 1.016-1.04 2.479 0 1.462 1.065 2.875 1.213 3.074.149.198 2.096 3.2 5.077 4.487.709.306 1.262.489 1.694.625.712.227 1.36.195 1.871.118.571-.085 1.758-.719 2.006-1.413.248-.694.248-1.289.173-1.413-.074-.124-.272-.198-.57-.347m-5.421 7.403h-.004a9.87 9.87 0 01-5.031-1.378l-.361-.214-3.741.982.998-3.648-.235-.374a9.86 9.86 0 01-1.51-5.26c.001-5.45 4.436-9.884 9.888-9.884 2.64 0 5.122 1.03 6.988 2.898a9.825 9.825 0 012.893 6.994c-.003 5.45-4.437 9.884-9.885 9.884m8.413-18.297A11.815 11.815 0 0012.05 0C5.495 0 .16 5.335.157 11.892c0 2.096.547 4.142 1.588 5.945L.057 24l6.305-1.654a11.882 11.882 0 005.683 1.448h.005c6.554 0 11.89-5.335 11.893-11.893a11.821 11.821 0 00-3.48-8.413z" />
                </svg>
                WhatsApp
              </Button>
            </div>
          </div>

          <div className="flex items-center gap-4">
            <div className="flex-1 h-px bg-gray-200" />
            <span className="text-xs font-medium text-gray-400">ou convide seus amigos</span>
            <div className="flex-1 h-px bg-gray-200" />
          </div>

          <div className="bg-gray-50 rounded-xl border border-gray-100 p-4 max-h-60 overflow-y-auto space-y-3">
            {loadingFriends ? (
              <div className="flex justify-center items-center py-4">
                <Loader2 className="h-6 w-6 animate-spin text-[#cc241a]" />
              </div>
            ) : friends.length === 0 ? (
              <p className="text-center text-sm text-gray-500 py-2">Você ainda não tem amigos adicionados.</p>
            ) : (
              friends.map(friend => (
                <div key={friend.friendExternalId} className="flex items-center justify-between bg-white p-3 rounded-lg border border-gray-100 shadow-sm">
                  <div className="flex flex-col">
                    <span className="text-sm font-bold text-gray-900">{friend.friendDisplayName}</span>
                    <span className="text-xs text-gray-500">@{friend.friendUsername}</span>
                  </div>
                  <Button 
                    size="sm"
                    variant={invitedFriends[friend.friendExternalId] === true ? "outline" : "default"}
                    onClick={() => handleInviteFriend(friend.friendExternalId)}
                    disabled={!!invitedFriends[friend.friendExternalId]}
                    className={
                      invitedFriends[friend.friendExternalId] === true 
                        ? "text-green-600 border-green-200 bg-green-50"
                        : "bg-[#cc241a] hover:bg-gray-800 text-white"
                    }
                  >
                    {invitedFriends[friend.friendExternalId] === 'loading' ? (
                      <Loader2 className="h-4 w-4 animate-spin" />
                    ) : invitedFriends[friend.friendExternalId] === true ? (
                      <>
                        <Check className="h-4 w-4 mr-1" />
                        Convidado
                      </>
                    ) : (
                      <>
                        <UserPlus className="h-4 w-4 mr-1" />
                        Convidar
                      </>
                    )}
                  </Button>
                </div>
              ))
            )}
          </div>

          <div className="flex items-center gap-4">
            <div className="flex-1 h-px bg-gray-200" />
            <span className="text-xs font-medium text-gray-400">ou vá para o painel</span>
            <div className="flex-1 h-px bg-gray-200" />
          </div>

          <Button
            onClick={() => router.push("/area/dashboard")}
            className="w-full h-12 text-base font-semibold bg-[#cc241a] hover:bg-gray-800 text-white rounded-xl gap-2 transition-colors mt-2"
          >
            <ExternalLink className="h-5 w-5" />
            Ir para o painel do rolê
          </Button>

        </div>
      </div>
    </div>
  );
};