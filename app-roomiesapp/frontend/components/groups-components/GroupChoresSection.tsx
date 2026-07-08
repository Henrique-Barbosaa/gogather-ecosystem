"use client";

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { GroupData, Chore } from "@/app/types";
import { CheckCircle, Circle, Plus } from "lucide-react";
import { Button } from "@/components/ui/button";
import { CreateChoreDialog } from "./CreateChoreDialog";

export function GroupChoresSection({ inviteCode }: { inviteCode: string }) {
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const queryClient = useQueryClient();

  const { data: group } = useQuery({
    queryKey: ["group", inviteCode],
    queryFn: async () => {
      const res = await api.get<GroupData>(`/groups/${inviteCode}`);
      return res.data;
    },
    enabled: !!inviteCode,
  });

  const groupId = group?.id;

  const { data: chores, isLoading } = useQuery({
    queryKey: ["group-chores", groupId],
    queryFn: async () => {
      const res = await api.get<Chore[]>(`/api/households/${groupId}/chores`);
      return res.data;
    },
    enabled: !!groupId,
  });

  const toggleChoreMutation = useMutation({
    mutationFn: async (choreId: number) => {
      await api.put(`/api/households/${groupId}/chores/${choreId}/complete`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["group-chores", groupId] });
    },
  });

  return (
    <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm flex flex-col h-full">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h3 className="text-lg font-bold text-gray-900">Mural de Tarefas</h3>
          <p className="text-sm text-gray-500">Regras e afazeres da casa</p>
        </div>
        <Button
          onClick={() => setIsDialogOpen(true)}
          disabled={!groupId}
          className="bg-[#8724df] hover:bg-[#6c1cb3] text-white"
        >
          <Plus className="w-4 h-4 mr-2" />
          Nova Tarefa
        </Button>
      </div>

      <div className="flex-1 overflow-y-auto space-y-3">
        {isLoading ? (
          <p className="text-center text-gray-500 py-4">Carregando tarefas...</p>
        ) : !chores || chores.length === 0 ? (
          <p className="text-center text-gray-500 py-4">Nenhuma tarefa cadastrada. A casa está em ordem!</p>
        ) : (
          chores.map((chore) => (
            <div
              key={chore.id}
              className={`flex items-center justify-between p-4 rounded-lg border ${
                chore.completed ? "bg-gray-50 border-gray-100" : "bg-white border-gray-200"
              }`}
            >
              <div className="flex items-center gap-3">
                <button
                  onClick={() => toggleChoreMutation.mutate(chore.id)}
                  disabled={toggleChoreMutation.isPending}
                  className="focus:outline-none disabled:opacity-50"
                >
                  {chore.completed ? (
                    <CheckCircle className="w-6 h-6 text-[#299227]" />
                  ) : (
                    <Circle className="w-6 h-6 text-gray-300" />
                  )}
                </button>
                <div>
                  <p className={`font-semibold ${chore.completed ? "text-gray-400 line-through" : "text-gray-800"}`}>
                    {chore.title}
                  </p>
                  {chore.description && <p className="text-xs text-gray-500">{chore.description}</p>}
                  {chore.dueDate && (
                    <p className="text-[11px] text-gray-400 mt-0.5">
                      Prazo: {new Date(chore.dueDate).toLocaleDateString("pt-BR")}
                    </p>
                  )}
                </div>
              </div>
            </div>
          ))
        )}
      </div>

      {groupId && (
        <CreateChoreDialog
          isOpen={isDialogOpen}
          onClose={() => setIsDialogOpen(false)}
          groupId={groupId}
          onSuccess={() => queryClient.invalidateQueries({ queryKey: ["group-chores", groupId] })}
        />
      )}
    </div>
  );
}
