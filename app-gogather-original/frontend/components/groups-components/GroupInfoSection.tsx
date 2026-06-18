"use client";

import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { GroupData } from "@/app/types";
import { Calendar } from "lucide-react";
import { format } from "date-fns";
import { ptBR } from "date-fns/locale";

export function GroupInfoSection({ groupId }: { groupId: string }) {
  const { data: group, isLoading } = useQuery({
    queryKey: ['group', groupId],
    queryFn: async () => {
      const res = await api.get<GroupData>(`/groups/${groupId}`);
      return res.data;
    },
    enabled: !!groupId,
  });

  if (isLoading) {
    return <div className="animate-pulse h-24 bg-gray-100 rounded-md"></div>;
  }

  if (!group) {
    return null;
  }

  const firstStop = group.eventStops?.[0];
  const mapboxToken = process.env.NEXT_PUBLIC_MAPBOX_TOKEN;

  return (
    <div className="flex flex-col gap-3">
      <h2 className="text-2xl font-extrabold text-gray-900 tracking-tight leading-tight">{group.name}</h2>
      <p className="text-sm text-gray-600 leading-relaxed font-medium">
        {group.description || "Sem descrição disponível."}
      </p>
      <div className="flex items-center gap-2 mt-1 text-sm text-gray-700 font-semibold">
        <Calendar className="w-4 h-4 text-gg-cyan" />
        <span>
          {group.eventDate ? format(new Date(group.eventDate), "EEEE, d 'de' MMMM '•' HH'h'", { locale: ptBR }) : 'Data não definida'}
        </span>
      </div>

      {firstStop && mapboxToken && (
        <div className="mt-6 flex flex-col gap-3">
          <div className="w-full h-36 rounded-2xl overflow-hidden border border-gg-beige-dark/30 shadow-sm relative bg-gray-100 group">
            <img
              src={`https://api.mapbox.com/styles/v1/mapbox/streets-v12/static/pin-s+cc241a(${firstStop.longitude},${firstStop.latitude})/${firstStop.longitude},${firstStop.latitude},14/400x144?access_token=${mapboxToken}`}
              alt={`Mapa para ${firstStop.name}`}
              className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
            />
          </div>
          <div className="flex flex-col gap-0.5 px-1">
            <span className="text-sm font-bold text-gray-900">
              {`${firstStop.name}`}
            </span>
            {(firstStop.city || firstStop.state) && (
              <span className="text-xs font-medium text-gray-500 italic">
                {firstStop.city}{firstStop.city && firstStop.state ? ' / ' : ''}{firstStop.state}
              </span>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
