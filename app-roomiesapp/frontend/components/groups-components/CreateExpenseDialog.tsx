"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { Plus } from "lucide-react";
import { toast } from "sonner";
import { GroupMemberResponse } from "@/app/types";

const billSchema = z.object({
  title: z.string().min(1, "O título é obrigatório"),
  amount: z.number().min(0.01, "O valor deve ser maior que zero"),
  dueDate: z.string().min(1, "A data de vencimento é obrigatória"),
  splitAmongAll: z.boolean().default(true),
});

type BillFormValues = z.infer<typeof billSchema>;

interface CreateExpenseDialogProps {
  /** externalId (UUID) do grupo — o mesmo usado em GET/POST /billing/groups/{groupExternalId}/bills */
  groupExternalId: string;
  members: GroupMemberResponse[];
  onSuccess?: () => void;
}

export function CreateExpenseDialog({ groupExternalId, members, onSuccess }: CreateExpenseDialogProps) {
  const [open, setOpen] = useState(false);
  const queryClient = useQueryClient();

  const form = useForm<BillFormValues>({
    resolver: zodResolver(billSchema),
    defaultValues: {
      title: "",
      amount: 0,
      dueDate: new Date().toISOString().split("T")[0],
      splitAmongAll: true,
    },
  });

  const createBillMutation = useMutation({
    mutationFn: async (data: BillFormValues) => {
      const payload = {
        title: data.title,
        description: "Conta dividida da casa",
        totalCents: Math.round(data.amount * 100),
        billType: "NORMAL",
        recurrenceInterval: "NONE",
        dueDate: data.dueDate,
        // O backend espera os externalId (UUID) dos membros, não o id numérico.
        participantIds: data.splitAmongAll ? members.map((m) => m.externalId) : [],
      };

      await api.post(`/billing/groups/${groupExternalId}/bills`, payload);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["group-bills", groupExternalId] });
      toast.success("Conta adicionada com sucesso!");
      setOpen(false);
      form.reset();
      if (onSuccess) onSuccess();
    },
    onError: (error) => {
      console.error("Erro ao criar conta:", error);
      toast.error("Não foi possível registrar a conta.");
    },
  });

  const onSubmit = (data: BillFormValues) => {
    createBillMutation.mutate(data);
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <button className="px-4 py-2 text-sm font-semibold text-white bg-[#8724df] hover:bg-[#6c1cb3] rounded-xl transition-colors flex items-center gap-2">
          <Plus className="w-4 h-4" />
          Nova Conta
        </button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-[450px]">
        <DialogHeader>
          <DialogTitle>Registrar Conta da Casa</DialogTitle>
        </DialogHeader>

        <form onSubmit={form.handleSubmit(onSubmit)} className="flex flex-col gap-6 py-4">
          <div className="flex flex-col gap-4">
            <div className="flex flex-col gap-1.5">
              <label className="text-sm font-medium text-gray-700">O que foi pago? (Ex: Luz, Internet)</label>
              <input
                type="text"
                {...form.register("title")}
                className="px-3 py-2 border border-gray-300 rounded-md text-sm focus:ring-2 focus:ring-[#8724df] outline-none"
                placeholder="Ex: Conta de Luz"
              />
              {form.formState.errors.title && (
                <span className="text-xs text-red-500">{form.formState.errors.title.message}</span>
              )}
            </div>

            <div className="flex flex-col gap-1.5">
              <label className="text-sm font-medium text-gray-700">Valor Total (R$)</label>
              <input
                type="number"
                step="0.01"
                {...form.register("amount", { valueAsNumber: true })}
                className="px-3 py-2 border border-gray-300 rounded-md text-sm focus:ring-2 focus:ring-[#8724df] outline-none"
                placeholder="0.00"
              />
              {form.formState.errors.amount && (
                <span className="text-xs text-red-500">{form.formState.errors.amount.message}</span>
              )}
            </div>

            <div className="flex flex-col gap-1.5">
              <label className="text-sm font-medium text-gray-700">Data de Vencimento</label>
              <input
                type="date"
                {...form.register("dueDate")}
                className="px-3 py-2 border border-gray-300 rounded-md text-sm focus:ring-2 focus:ring-[#8724df] outline-none"
              />
              {form.formState.errors.dueDate && (
                <span className="text-xs text-red-500">{form.formState.errors.dueDate.message}</span>
              )}
            </div>

            <div className="flex items-center gap-2 mt-2">
              <input
                type="checkbox"
                id="splitAmongAll"
                {...form.register("splitAmongAll")}
                className="w-4 h-4 rounded border-gray-300 text-[#8724df] focus:ring-[#8724df]"
              />
              <label htmlFor="splitAmongAll" className="text-sm text-gray-700 cursor-pointer">
                Ratear em partes iguais entre todos os moradores.
              </label>
            </div>

            {!form.watch("splitAmongAll") && (
              <p className="text-xs text-gray-500 italic mt-[-10px]">
                Atenção: Contas não rateadas integralmente devem ser gerenciadas pontualmente na visualização de débitos.
              </p>
            )}
          </div>

          <div className="pt-4 border-t border-gray-100 flex justify-end gap-3">
            <button
              type="button"
              onClick={() => setOpen(false)}
              className="px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 rounded-xl transition-colors"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={createBillMutation.isPending}
              className="px-6 py-2 text-sm font-semibold text-white bg-[#8724df] hover:bg-[#6c1cb3] rounded-xl transition-colors disabled:opacity-70"
            >
              {createBillMutation.isPending ? "Registrando..." : "Registrar"}
            </button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
}
