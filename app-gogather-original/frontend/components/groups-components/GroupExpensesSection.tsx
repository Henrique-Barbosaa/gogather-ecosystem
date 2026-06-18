"use client";

import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { GroupData } from "@/app/types";
import { useAuth } from "@/context/AuthContext";
import { CreateExpenseDialog } from "./CreateExpenseDialog";
import { ExpensesList } from "./ExpensesList";

export function GroupExpensesSection({ groupId }: { groupId: string }) {
  const { user } = useAuth();
  
  const { data: group, isLoading } = useQuery({
    queryKey: ['group', groupId],
    queryFn: async () => {
      const res = await api.get<GroupData>(`/groups/${groupId}`);
      return res.data;
    },
    enabled: !!groupId,
  });

  if (isLoading) {
    return <div className="animate-pulse h-32 bg-gray-100 rounded-md"></div>;
  }

  if (!group) return null;

  const currentMember = group.members.find(m => m.externalId === user?.id?.toString());
  const isAdmin = currentMember?.role === "ADMIN";

  return (
    <div className="flex flex-col gap-4">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h3 className="text-lg font-bold text-gray-900 shrink-0">Financeiro</h3>
        {isAdmin && <CreateExpenseDialog group={group} />}
      </div>
      <ExpensesList groupId={groupId} members={group.members} />
    </div>
  );
}
