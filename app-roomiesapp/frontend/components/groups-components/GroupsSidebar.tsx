"use client"

import { GroupSimpleData } from "@/app/types";
import { api } from "@/lib/api";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Search, Plus } from "lucide-react";
import React, { useState } from "react";
import { toast } from "sonner";
import { AxiosError } from "axios";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import {
  Field,
  FieldError,
  FieldGroup,
  FieldLabel,
} from "@/components/ui/field";
import z from "zod";
import { Controller, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Input } from "../ui/input";
import { Button } from "../ui/button";

interface GroupsSidebarProps {
  selectedInviteCode?: string | null;
  onSelectGroup?: (inviteCode: string) => void;
}

const formSchema = z.object({
  code: z
    .string()
    .length(8, { error: "O código de convite deve ter 8 caracteres" })
});

export function GroupsSidebar({ selectedInviteCode, onSelectGroup }: GroupsSidebarProps) {
  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      code: ""
    },
  });

  const queryClient = useQueryClient();
  const [isDialogOpen, setIsDialogOpen] = useState(false);

  const joinGroupMutation = useMutation<void, AxiosError<{message: string}>, string>({
    mutationFn: async (inviteCode: string) => {
      await api.post(`/groups/join/${inviteCode}`);
    },
    onSuccess: () => {
      toast.success("Você entrou na casa com sucesso!");
      queryClient.invalidateQueries({ queryKey: ['groups-list'] });
      setIsDialogOpen(false);
      form.reset();
    },
    onError: (error) => {
      const backendMessage = error.response?.data?.message;
      toast.error(backendMessage || "Erro ao entrar na casa.");
    }
  });

  function onSubmit(data: z.infer<typeof formSchema>) {
    joinGroupMutation.mutate(data.code);
  }

  const [searchQuery, setSearchQuery] = useState("");

  const {data: groups, isLoading} = useQuery({
    queryKey: ['groups-list'],
    queryFn: async () => {
      const res = await api.get<GroupSimpleData[]>('/groups');
      return res.data;
    }
  })

  const avatarColors = [
    'bg-ra-green text-white',
    'bg-ra-purple text-white',
    'bg-ra-blue-extradark text-white',
    'bg-blue-600 text-white',
    'bg-purple-600 text-white',
  ];

  const filteredGroups = groups?.filter((group) =>
    group.name.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <aside className="h-full w-full bg-ra-blue-extralight flex flex-col p-4 border-r border-gray-100">
      <div className="flex items-center justify-between mb-4">
        <h1 className="font-bold text-xl text-gray-900">Minhas Casas</h1>
        <Dialog open={isDialogOpen} onOpenChange={(open) => {
          setIsDialogOpen(open);
          if (!open) {
            joinGroupMutation.reset();
            form.reset();
          }
        }}>
          <DialogTrigger asChild>
            <button className="p-1 hover:bg-ra-blue-light rounded-md transition-colors text-gray-500">
              <Plus className="w-5 h-5" />
            </button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Entrar em uma Casa</DialogTitle>
            </DialogHeader>
            <div>
              <form id="form-group" onSubmit={form.handleSubmit(onSubmit)}>
                {joinGroupMutation.isError && (
                  <div className="mb-4 p-3 bg-red-50 border border-red-100 rounded-lg text-sm text-red-600 animate-in fade-in slide-in-from-top-1">
                    {(joinGroupMutation.error)?.response?.data?.message || "Erro ao entrar na casa."}
                  </div>
                )}
                <FieldGroup>
                  <Controller
                    name="code"
                    control={form.control}
                    render={({ field, fieldState }) => (
                      <Field>
                        <FieldLabel htmlFor="form-group-code">
                          Código de Convite
                        </FieldLabel>
                        <Input
                          {...field}
                          onChange={(e) => {
                            const value = e.target.value.toUpperCase();
                            field.onChange(value);
                          }}
                          id="form-group-code"
                          aria-invalid={fieldState.invalid}
                          placeholder="Código de 8 dígitos..."
                          autoComplete="off"
                          disabled={joinGroupMutation.isPending}
                          className="uppercase"
                        />
                        {fieldState.invalid && (
                          <FieldError errors={[fieldState.error]} />
                        )}
                      </Field>
                    )}
                  />
                </FieldGroup>
              </form>
            </div>
            <DialogFooter>
              <Button type="submit" form="form-group" disabled={joinGroupMutation.isPending}>
                {joinGroupMutation.isPending ? "Entrando..." : "Entrar"}
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>

      <div className="relative mb-6">
        <Search className={`w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 transition-colors ${searchQuery ? 'text-ra-green' : 'text-gray-400'}`} />
        <input
          type="text"
          placeholder="Buscar casa..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="w-full pl-9 pr-4 py-2 bg-white border border-gray-100 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-ra-green/20 focus:border-ra-green transition-all"
        />
      </div>

      <div className="flex flex-col gap-2 overflow-y-auto flex-1 pr-1 -mr-1">
        {isLoading ? (
          <div className="animate-pulse flex flex-col gap-3">
            {[1, 2, 3].map(i => (
              <div key={i} className="flex gap-3 items-center">
                <div className="w-12 h-12 rounded-full bg-ra-blue-light"></div>
                <div className="flex-1 flex flex-col gap-2">
                  <div className="h-4 bg-ra-blue-light rounded w-24"></div>
                  <div className="h-3 bg-ra-blue-light rounded w-32"></div>
                </div>
              </div>
            ))}
          </div>
        ) : filteredGroups && filteredGroups.length > 0 ? (
          filteredGroups.map((group) => {
            const isSelected = selectedInviteCode === group.inviteCode;
            const initials = group.name.substring(0, 1).toUpperCase();
            const colorIndex = group.inviteCode.split('').reduce((acc, char) => acc + char.charCodeAt(0), 0) % avatarColors.length;
            const colorClass = avatarColors[colorIndex];

            // Subtítulo: número de moradores (se o backend informar) ou endereço.
            const subtitle = typeof group.memberAmount === "number"
              ? `${group.memberAmount} ${group.memberAmount === 1 ? 'morador' : 'moradores'}`
              : group.address || "República";

            return (
              <div
                key={group.inviteCode}
                onClick={() => onSelectGroup?.(group.inviteCode)}
                className={`
                  relative flex items-center gap-3 p-2 rounded-xl cursor-pointer transition-all duration-200
                  ${isSelected ? 'bg-white shadow-sm ring-1 ring-gray-100' : 'hover:bg-white/50'}
                `}
              >
                {isSelected && (
                  <div className="absolute left-0 top-1/2 -translate-y-1/2 w-1 h-8 bg-ra-green rounded-r-full" />
                )}

                <div className={`w-12 h-12 rounded-2xl flex items-center justify-center font-bold text-lg shrink-0 ${colorClass} ${isSelected ? 'ml-1' : ''} transition-all`}>
                  {initials}
                </div>

                <div className="flex-1 min-w-0">
                  <h2 className={`font-semibold truncate text-sm ${isSelected ? 'text-gray-900' : 'text-gray-700'}`}>
                    {group.name}
                  </h2>
                  <p className="text-xs text-gray-500 truncate mt-0.5">
                    {subtitle}
                  </p>
                </div>
              </div>
            )
          })
        ) : (
          <div className="text-center text-gray-400 text-sm py-4">
            Nenhuma casa encontrada.
          </div>
        )}
      </div>
    </aside>
  )
}
