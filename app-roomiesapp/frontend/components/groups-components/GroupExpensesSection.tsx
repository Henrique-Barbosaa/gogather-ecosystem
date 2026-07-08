"use client";

import { useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { GroupData, GroupMemberResponse } from "@/app/types";
import { ExpensesList } from "./ExpensesList";
import { CreateExpenseDialog } from "./CreateExpenseDialog";
import { Receipt } from "lucide-react";

export function GroupExpensesSection({ inviteCode }: { inviteCode: string }) {
  const queryClient = useQueryClient();

  const { data: group, isLoading } = useQuery({
    queryKey: ['group', inviteCode],
    queryFn: async () => {
      const res = await api.get<GroupData>(`/groups/${inviteCode}`);
      return res.data;
    },
    enabled: !!inviteCode,
  });

  const { data: members } = useQuery({
    queryKey: ['group-members', inviteCode],
    queryFn: async () => {
      const res = await api.get<GroupMemberResponse[]>(`/groups/${inviteCode}/members`);
      return res.data;
    },
    enabled: !!inviteCode,
  });

  if (isLoading) {
    return <div className="animate-pulse h-32 bg-gray-100 rounded-md"></div>;
  }

  if (!group) return null;

  return (
    <div className="flex flex-col gap-4">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h3 className="text-lg font-bold text-gray-900 shrink-0 flex items-center gap-2">
          <Receipt className="w-5 h-5 text-ra-green" />
          Financeiro
        </h3>
        <CreateExpenseDialog
          groupExternalId={group.externalId}
          members={members ?? []}
          onSuccess={() => queryClient.invalidateQueries({ queryKey: ['group-bills', group.externalId] })}
        />
      </div>
      <ExpensesList groupExternalId={group.externalId} />
    </div>
  );
}
