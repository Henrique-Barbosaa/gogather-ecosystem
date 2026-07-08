"use client";

import { useMemo, useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Plus, ListTodo, CheckCircle, Circle, Home, Users, Loader2 } from "lucide-react";
import { toast } from "sonner";
import { api } from "@/lib/api";
import { Chore, GroupSimpleData, GroupMemberResponse } from "@/app/types";
import { Header } from "@/components/area-components/Header";
import { SubHeader } from "@/components/area-components/SubHeader";
import { InfoCards } from "@/components/area-components/InfoCards";
import { Empty } from "@/components/area-components/Empty";
import { HouseSelector } from "@/components/area-components/HouseSelector";
import { CreateChoreDialog } from "@/components/groups-components/CreateChoreDialog";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

type ChoreFilter = "all" | "pending" | "done";

export default function ChoresPage() {
  const queryClient = useQueryClient();
  const [selectedInviteCode, setSelectedInviteCode] = useState<string | null>(null);
  const [filter, setFilter] = useState<ChoreFilter>("all");
  const [isDialogOpen, setIsDialogOpen] = useState(false);

  const { data: houses, isLoading: loadingHouses } = useQuery({
    queryKey: ["groups-list"],
    queryFn: async () => {
      const res = await api.get<GroupSimpleData[]>("/groups");
      return res.data;
    },
  });

  const effectiveInviteCode = selectedInviteCode ?? houses?.[0]?.inviteCode ?? null;

  const selectedHouse = useMemo(
    () => houses?.find((h) => h.inviteCode === effectiveInviteCode),
    [houses, effectiveInviteCode]
  );
  const groupId = selectedHouse?.id;

  const { data: chores, isLoading: loadingChores } = useQuery({
    queryKey: ["group-chores", groupId],
    queryFn: async () => {
      const res = await api.get<Chore[]>(`/api/households/${groupId}/chores`);
      return res.data;
    },
    enabled: !!groupId,
  });

  const { data: members } = useQuery({
    queryKey: ["group-members", effectiveInviteCode],
    queryFn: async () => {
      const res = await api.get<GroupMemberResponse[]>(`/groups/${effectiveInviteCode}/members`);
      return res.data;
    },
    enabled: !!effectiveInviteCode,
  });

  const toggleChoreMutation = useMutation({
    mutationFn: async (choreId: number) => {
      await api.put(`/api/households/${groupId}/chores/${choreId}/complete`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["group-chores", groupId] });
    },
    onError: () => toast.error("Não foi possível atualizar a tarefa."),
  });

  const assignChoreMutation = useMutation({
    mutationFn: async ({ choreId, userId }: { choreId: number; userId: number }) => {
      await api.put(`/api/households/${groupId}/chores/${choreId}/assign/${userId}`);
    },
    onSuccess: () => {
      toast.success("Responsável atualizado!");
      queryClient.invalidateQueries({ queryKey: ["group-chores", groupId] });
    },
    onError: () => toast.error("Não foi possível atribuir a tarefa."),
  });

  const total = chores?.length ?? 0;
  const doneCount = chores?.filter((c) => c.completed).length ?? 0;
  const pendingCount = total - doneCount;

  const visibleChores = useMemo(() => {
    if (!chores) return [];
    if (filter === "pending") return chores.filter((c) => !c.completed);
    if (filter === "done") return chores.filter((c) => c.completed);
    return chores;
  }, [chores, filter]);

  if (loadingHouses) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <Loader2 className="h-8 w-8 animate-spin text-ra-purple" />
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto pt-4 md:pt-6 pb-12 px-4 md:px-6">
      <div className="flex flex-wrap justify-between items-center gap-4 mb-8">
        <Header description="Divida as tarefas e regras de convivência entre os moradores">
          Tarefas
        </Header>
        {groupId && (
          <Button
            onClick={() => setIsDialogOpen(true)}
            className="bg-ra-purple hover:bg-ra-purple-dark text-white rounded-full px-6 py-5 font-bold shadow-md hover:-translate-y-0.5 transition-all"
          >
            <Plus className="w-5 h-5 mr-1" />
            Nova Tarefa
          </Button>
        )}
      </div>

      {!houses || houses.length === 0 ? (
        <Empty title="Você ainda não faz parte de nenhuma casa." Icon={Home}>
          Crie uma casa ou entre com um código de convite para começar a organizar as tarefas.
        </Empty>
      ) : (
        <>
          <div className="mb-8">
            <SubHeader className="mb-3">Selecione a casa</SubHeader>
            <HouseSelector
              houses={houses}
              selectedInviteCode={effectiveInviteCode}
              onSelect={(code) => {
                setSelectedInviteCode(code);
                setFilter("all");
              }}
            />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-10">
            <InfoCards info={total.toString()} color="purple" Icon={ListTodo}>
              Total de Tarefas
            </InfoCards>
            <InfoCards info={pendingCount.toString()} color="green" Icon={Circle}>
              Pendentes
            </InfoCards>
            <InfoCards info={doneCount.toString()} color="blue" Icon={CheckCircle}>
              Concluídas
            </InfoCards>
          </div>

          <div className="flex flex-wrap items-center justify-between gap-3 mb-4">
            <SubHeader>Mural de Tarefas</SubHeader>
            <div className="flex gap-1 bg-white/60 border border-gray-200 rounded-full p-1">
              {(
                [
                  { key: "all", label: "Todas" },
                  { key: "pending", label: "Pendentes" },
                  { key: "done", label: "Concluídas" },
                ] as { key: ChoreFilter; label: string }[]
              ).map((tab) => (
                <button
                  key={tab.key}
                  onClick={() => setFilter(tab.key)}
                  className={`px-4 py-1.5 rounded-full text-sm font-semibold transition-all ${
                    filter === tab.key
                      ? "bg-ra-purple text-white shadow-sm"
                      : "text-gray-500 hover:text-gray-800"
                  }`}
                >
                  {tab.label}
                </button>
              ))}
            </div>
          </div>

          {loadingChores ? (
            <div className="flex items-center justify-center py-16">
              <Loader2 className="h-6 w-6 animate-spin text-ra-purple" />
            </div>
          ) : visibleChores.length === 0 ? (
            <Empty
              title={
                filter === "done"
                  ? "Nenhuma tarefa concluída ainda."
                  : filter === "pending"
                    ? "Nenhuma tarefa pendente. A casa está em ordem!"
                    : "Nenhuma tarefa cadastrada."
              }
              Icon={ListTodo}
            >
              Use o botão “Nova Tarefa” para adicionar afazeres ou regras de convivência.
            </Empty>
          ) : (
            <div className="flex flex-col gap-3">
              {visibleChores.map((chore) => (
                <div
                  key={chore.id}
                  className={`flex flex-col sm:flex-row sm:items-center justify-between gap-3 p-4 rounded-2xl border transition-all ${
                    chore.completed
                      ? "bg-gray-50 border-gray-100"
                      : "bg-white border-gray-200 hover:shadow-sm"
                  }`}
                >
                  <div className="flex items-start gap-3 min-w-0">
                    <button
                      onClick={() => toggleChoreMutation.mutate(chore.id)}
                      disabled={toggleChoreMutation.isPending}
                      className="focus:outline-none disabled:opacity-50 mt-0.5 shrink-0"
                      title={chore.completed ? "Marcar como pendente" : "Marcar como concluída"}
                    >
                      {chore.completed ? (
                        <CheckCircle className="w-6 h-6 text-ra-green" />
                      ) : (
                        <Circle className="w-6 h-6 text-gray-300 hover:text-ra-purple transition-colors" />
                      )}
                    </button>
                    <div className="min-w-0">
                      <p
                        className={`font-semibold ${
                          chore.completed ? "text-gray-400 line-through" : "text-gray-800"
                        }`}
                      >
                        {chore.title}
                      </p>
                      {chore.description && (
                        <p className="text-sm text-gray-500 mt-0.5">{chore.description}</p>
                      )}
                      <div className="flex flex-wrap items-center gap-x-3 gap-y-1 mt-1.5">
                        {chore.dueDate && (
                          <span className="text-[11px] text-gray-400">
                            Prazo: {new Date(chore.dueDate).toLocaleDateString("pt-BR")}
                          </span>
                        )}
                        {chore.assigneeUsername ? (
                          <span className="text-[11px] font-medium text-ra-purple bg-ra-purple/10 px-2 py-0.5 rounded-full">
                            Responsável: {chore.assigneeUsername}
                          </span>
                        ) : (
                          <span className="text-[11px] text-gray-400 italic">Sem responsável</span>
                        )}
                      </div>
                    </div>
                  </div>

                  <div className="shrink-0 sm:pl-2">
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <button
                          disabled={assignChoreMutation.isPending || !members?.length}
                          className="flex items-center gap-1.5 text-xs font-semibold text-gray-600 border border-gray-200 hover:border-ra-purple hover:text-ra-purple px-3 py-1.5 rounded-lg transition-colors disabled:opacity-50"
                        >
                          <Users className="w-3.5 h-3.5" />
                          Atribuir
                        </button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end" className="w-52">
                        <DropdownMenuLabel>Definir responsável</DropdownMenuLabel>
                        <DropdownMenuSeparator />
                        {members && members.length > 0 ? (
                          members.map((member) => (
                            <DropdownMenuItem
                              key={member.id}
                              className="cursor-pointer"
                              onClick={() =>
                                assignChoreMutation.mutate({
                                  choreId: chore.id,
                                  userId: member.id,
                                })
                              }
                            >
                              {member.displayName || member.username}
                            </DropdownMenuItem>
                          ))
                        ) : (
                          <DropdownMenuItem disabled>Nenhum morador</DropdownMenuItem>
                        )}
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </div>
                </div>
              ))}
            </div>
          )}
        </>
      )}

      {groupId && (
        <CreateChoreDialog
          isOpen={isDialogOpen}
          onClose={() => setIsDialogOpen(false)}
          groupId={groupId}
          onSuccess={() =>
            queryClient.invalidateQueries({ queryKey: ["group-chores", groupId] })
          }
        />
      )}
    </div>
  );
}
