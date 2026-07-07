"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { GroupData } from "@/app/types";
import { MapPin, Users, Wallet, Copy, Check, CalendarDays } from "lucide-react";
import { format } from "date-fns";
import { ptBR } from "date-fns/locale";
import { toast } from "sonner";

function formatCents(cents?: number | null): string | null {
  if (cents === null || cents === undefined) return null;
  return (cents / 100).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

export function GroupInfoSection({ inviteCode }: { inviteCode: string }) {
  const [copied, setCopied] = useState(false);

  const { data: group, isLoading } = useQuery({
    queryKey: ['group', inviteCode],
    queryFn: async () => {
      const res = await api.get<GroupData>(`/groups/${inviteCode}`);
      return res.data;
    },
    enabled: !!inviteCode,
  });

  if (isLoading) {
    return <div className="animate-pulse h-24 bg-gray-100 rounded-md"></div>;
  }

  if (!group) {
    return null;
  }

  const rent = formatCents(group.monthlyRentCents);

  const handleCopyCode = () => {
    navigator.clipboard.writeText(group.inviteCode);
    setCopied(true);
    toast.success("Código copiado!");
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="flex flex-col gap-3">
      <h2 className="text-2xl font-extrabold text-gray-900 tracking-tight leading-tight">{group.name}</h2>
      <p className="text-sm text-gray-600 leading-relaxed font-medium">
        {group.description || "Sem descrição disponível."}
      </p>

      <div className="flex flex-col gap-2 mt-1">
        {group.address && (
          <div className="flex items-center gap-2 text-sm text-gray-700 font-semibold">
            <MapPin className="w-4 h-4 text-ra-green shrink-0" />
            <span>{group.address}</span>
          </div>
        )}
        {rent && (
          <div className="flex items-center gap-2 text-sm text-gray-700 font-semibold">
            <Wallet className="w-4 h-4 text-ra-green shrink-0" />
            <span>Aluguel: {rent}/mês</span>
          </div>
        )}
        {typeof group.maxOccupants === "number" && (
          <div className="flex items-center gap-2 text-sm text-gray-700 font-semibold">
            <Users className="w-4 h-4 text-ra-green shrink-0" />
            <span>Até {group.maxOccupants} moradores</span>
          </div>
        )}
        {group.createdAt && (
          <div className="flex items-center gap-2 text-sm text-gray-700 font-semibold">
            <CalendarDays className="w-4 h-4 text-ra-green shrink-0" />
            <span>Criada em {format(new Date(group.createdAt), "d 'de' MMMM 'de' yyyy", { locale: ptBR })}</span>
          </div>
        )}
      </div>

      {/* Código de convite */}
      <div className="mt-4 bg-white rounded-2xl border border-dashed border-ra-green/50 p-4 flex items-center justify-between gap-3">
        <div className="flex flex-col gap-0.5 min-w-0">
          <span className="text-xs font-semibold text-gray-500 uppercase tracking-wider">Código de Convite</span>
          <span className="text-xl font-black tracking-[0.15em] text-ra-green truncate">{group.inviteCode}</span>
        </div>
        <button
          onClick={handleCopyCode}
          className="shrink-0 p-2 text-gray-500 hover:text-ra-green hover:bg-ra-green/10 rounded-lg transition-colors"
          title="Copiar código"
        >
          {copied ? <Check className="w-5 h-5 text-ra-green" /> : <Copy className="w-5 h-5" />}
        </button>
      </div>
    </div>
  );
}
