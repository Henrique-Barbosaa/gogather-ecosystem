"use client";

import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { GroupData } from "@/app/types";

export function GroupParticipantsSection({ groupId }: { groupId: string }) {
  const { data: group, isLoading } = useQuery({
    queryKey: ['group', groupId],
    queryFn: async () => {
      const res = await api.get<GroupData>(`/groups/${groupId}`);
      return res.data;
    },
    enabled: !!groupId,
  });

  if (isLoading || !group) {
    return (
      <div>
        <h3 className="text-xs font-bold text-gray-500 mb-4 tracking-wider">PARTICIPANTES</h3>
        <div className="flex flex-col gap-3 animate-pulse">
          {[1, 2, 3].map(i => (
            <div key={i} className="flex items-center gap-3">
              <div className="w-8 h-8 rounded-full bg-gray-200"></div>
              <div className="h-4 bg-gray-200 rounded w-24"></div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  const members = group.members || [];

  return (
    <div>
      <h3 className="text-xs font-extrabold text-gray-400 mb-5 tracking-[0.1em] uppercase">
        PARTICIPANTES ({members.length})
      </h3>
      <div className="flex flex-col gap-4">
        {members.map((member) => {
          const nameToUse = member.displayName || member.username || 'U';
          const initials = nameToUse.substring(0, 2).toUpperCase();
          
          return (
            <div key={member.externalId} className="flex items-center gap-3 group/member cursor-default">
              <div className="w-9 h-9 rounded-full bg-gg-cyan-extralight text-gg-cyan-dark flex items-center justify-center text-xs font-bold shrink-0 border border-gg-cyan/10 group-hover/member:bg-gg-cyan group-hover/member:text-white transition-colors duration-200">
                {initials}
              </div>
              <span className="text-sm font-semibold text-gray-700 truncate group-hover/member:text-gray-900 transition-colors">
                {nameToUse}
              </span>
            </div>
          );
        })}
      </div>
    </div>
  );
}
