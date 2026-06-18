"use client";

import { useState } from "react";
import { useForm, useFieldArray } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { GroupData } from "@/app/types";
import { Plus, Trash2 } from "lucide-react";

const expenseSchema = z.object({
  description: z.string().min(1, "A descrição é obrigatória"),
  totalValue: z.number().min(0.01, "O valor deve ser maior que zero"),
  isAuto: z.boolean(),
  contributions: z.array(z.object({
    payerExternalId: z.string().min(1, "Selecione quem pagou"),
    value: z.number().min(0.01, "Valor inválido"),
  })).min(1, "Deve haver pelo menos um pagamento"),
  distributions: z.array(z.object({
    debtorExternalId: z.string().min(1, "Selecione o devedor"),
    creditorExternalId: z.string().min(1, "Selecione o credor"),
    value: z.number().min(0.01, "Valor inválido"),
  })).optional(),
}).refine(data => {
  if (!data.isAuto && (!data.distributions || data.distributions.length === 0)) {
    return false;
  }
  return true;
}, {
  message: "Distribuições são obrigatórias na divisão manual",
  path: ["distributions"],
});

type ExpenseFormValues = z.infer<typeof expenseSchema>;

export function CreateExpenseDialog({ group }: { group: GroupData }) {
  const [open, setOpen] = useState(false);
  const queryClient = useQueryClient();

  const form = useForm<ExpenseFormValues>({
    resolver: zodResolver(expenseSchema),
    defaultValues: {
      description: "",
      totalValue: 0,
      isAuto: true,
      contributions: [{ payerExternalId: "", value: 0 }],
      distributions: [],
    },
  });

  const { fields: contributionFields, append: appendContribution, remove: removeContribution } = useFieldArray({
    control: form.control,
    name: "contributions",
  });

  const { fields: distributionFields, append: appendDistribution, remove: removeDistribution } = useFieldArray({
    control: form.control,
    name: "distributions" as never,
  });

  const createExpenseMutation = useMutation({
    mutationFn: async (data: ExpenseFormValues) => {
      if (data.isAuto) {
        await api.post(`/groups/${group.externalId}/expense/auto`, {
          description: data.description,
          totalValue: data.totalValue,
          contributions: data.contributions,
        });
      } else {
        await api.post(`/groups/${group.externalId}/expense/manual`, {
          description: data.description,
          totalValue: data.totalValue,
          contributions: data.contributions,
          distributions: data.distributions,
        });
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['group-expenses', group.externalId] });
      setOpen(false);
      form.reset();
    },
  });

  const onSubmit = (data: ExpenseFormValues) => {
    const totalContributions = data.contributions.reduce((acc, curr) => acc + curr.value, 0);
    if (Math.abs(totalContributions - data.totalValue) > 0.01) {
      form.setError("totalValue", { type: "manual", message: "A soma dos pagamentos deve ser igual ao valor total" });
      return;
    }

    if (!data.isAuto && data.distributions) {
      const totalDistributions = data.distributions.reduce((acc, curr) => acc + curr.value, 0);
      if (Math.abs(totalDistributions - data.totalValue) > 0.01) {
        form.setError("distributions", { type: "manual", message: "A soma das divisões deve ser igual ao valor total" });
        return;
      }
    }

    createExpenseMutation.mutate(data);
  };

  const isAuto = form.watch("isAuto");

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <button className="px-4 py-2 text-sm font-semibold text-white bg-gg-cyan hover:bg-cyan-500 rounded-xl transition-colors flex items-center gap-2">
          <Plus className="w-4 h-4" />
          Adicionar Dívida
        </button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-[500px] max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Nova Despesa</DialogTitle>
        </DialogHeader>

        <form onSubmit={form.handleSubmit(onSubmit)} className="flex flex-col gap-6 py-4">
          <div className="flex flex-col gap-4">
            <div className="flex flex-col gap-1.5">
              <label className="text-sm font-medium text-gray-700">Descrição</label>
              <input
                type="text"
                {...form.register("description")}
                className="px-3 py-2 border border-gray-300 rounded-md text-sm"
                placeholder="Ex: Cerveja, Aluguel da casa..."
              />
              {form.formState.errors.description && <span className="text-xs text-red-500">{form.formState.errors.description.message}</span>}
            </div>

            <div className="flex flex-col gap-1.5">
              <label className="text-sm font-medium text-gray-700">Valor Total (R$)</label>
              <input
                type="number"
                step="0.01"
                {...form.register("totalValue", { valueAsNumber: true })}
                className="px-3 py-2 border border-gray-300 rounded-md text-sm"
                placeholder="0.00"
              />
              {form.formState.errors.totalValue && <span className="text-xs text-red-500">{form.formState.errors.totalValue.message}</span>}
            </div>
          </div>

          <div className="flex items-center gap-2">
            <input
              type="checkbox"
              id="isAuto"
              {...form.register("isAuto")}
              className="rounded border-gray-300 text-gg-cyan focus:ring-gg-cyan"
            />
            <label htmlFor="isAuto" className="text-sm font-medium text-gray-700 cursor-pointer">
              Dividir automaticamente entre todos os membros
            </label>
          </div>

          <div className="flex flex-col gap-3">
            <div className="flex justify-between items-center">
              <h4 className="text-sm font-bold text-gray-900">Quem pagou?</h4>
              <button
                type="button"
                onClick={() => appendContribution({ payerExternalId: "", value: 0 })}
                className="text-xs text-gg-cyan hover:underline font-medium"
              >
                + Adicionar pagador
              </button>
            </div>
            {contributionFields.map((field, index) => (
              <div key={field.id} className="flex gap-2 items-start">
                <div className="flex-1 flex flex-col gap-1">
                  <select
                    {...form.register(`contributions.${index}.payerExternalId`)}
                    className="px-3 py-2 border border-gray-300 rounded-md text-sm"
                  >
                    <option value="">Selecione...</option>
                    {group.members.map(m => (
                      <option key={m.externalId} value={m.externalId}>{m.displayName || m.username}</option>
                    ))}
                  </select>
                  {form.formState.errors.contributions?.[index]?.payerExternalId && (
                    <span className="text-xs text-red-500">{form.formState.errors.contributions[index]?.payerExternalId?.message}</span>
                  )}
                </div>
                <div className="w-1/3 flex flex-col gap-1">
                  <input
                    type="number"
                    step="0.01"
                    {...form.register(`contributions.${index}.value`, { valueAsNumber: true })}
                    className="px-3 py-2 border border-gray-300 rounded-md text-sm"
                    placeholder="Valor"
                  />
                  {form.formState.errors.contributions?.[index]?.value && (
                    <span className="text-xs text-red-500">{form.formState.errors.contributions[index]?.value?.message}</span>
                  )}
                </div>
                {contributionFields.length > 1 && (
                  <button type="button" onClick={() => removeContribution(index)} className="p-2 text-gray-400 hover:text-red-500">
                    <Trash2 className="w-4 h-4" />
                  </button>
                )}
              </div>
            ))}
          </div>

          {!isAuto && (
            <div className="flex flex-col gap-3 pt-4 border-t border-gray-100">
              <div className="flex justify-between items-center">
                <h4 className="text-sm font-bold text-gray-900">Divisão (Quem deve para quem?)</h4>
                <button
                  type="button"
                  onClick={() => appendDistribution({ debtorExternalId: "", creditorExternalId: "", value: 0 })}
                  className="text-xs text-gg-cyan hover:underline font-medium"
                >
                  + Adicionar divisão
                </button>
              </div>
              
              {form.formState.errors.distributions && typeof form.formState.errors.distributions.message === 'string' && (
                <span className="text-xs text-red-500">{form.formState.errors.distributions.message}</span>
              )}

              {distributionFields.map((field, index) => (
                <div key={field.id} className="flex flex-col gap-2 p-3 bg-gray-50 rounded-lg border border-gray-200">
                  <div className="flex gap-2">
                    <div className="flex-1 flex flex-col gap-1">
                      <label className="text-xs text-gray-500">Devedor</label>
                      <select
                        {...form.register(`distributions.${index}.debtorExternalId`)}
                        className="px-2 py-1.5 border border-gray-300 rounded-md text-sm"
                      >
                        <option value="">Selecione...</option>
                        {group.members.map(m => (
                          <option key={m.externalId} value={m.externalId}>{m.displayName || m.username}</option>
                        ))}
                      </select>
                    </div>
                    <div className="flex-1 flex flex-col gap-1">
                      <label className="text-xs text-gray-500">Credor</label>
                      <select
                        {...form.register(`distributions.${index}.creditorExternalId`)}
                        className="px-2 py-1.5 border border-gray-300 rounded-md text-sm"
                      >
                        <option value="">Selecione...</option>
                        {group.members.map(m => (
                          <option key={m.externalId} value={m.externalId}>{m.displayName || m.username}</option>
                        ))}
                      </select>
                    </div>
                  </div>
                  <div className="flex gap-2 items-end">
                    <div className="flex-1 flex flex-col gap-1">
                      <label className="text-xs text-gray-500">Valor (R$)</label>
                      <input
                        type="number"
                        step="0.01"
                        {...form.register(`distributions.${index}.value`, { valueAsNumber: true })}
                        className="px-2 py-1.5 border border-gray-300 rounded-md text-sm"
                      />
                    </div>
                    <button type="button" onClick={() => removeDistribution(index)} className="p-2 text-gray-400 hover:text-red-500">
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}

          <div className="pt-4 border-t border-gray-100 flex justify-end">
            <button
              type="submit"
              disabled={createExpenseMutation.isPending}
              className="px-6 py-2 text-sm font-semibold text-white bg-gg-cyan hover:bg-cyan-500 rounded-xl transition-colors disabled:opacity-70"
            >
              {createExpenseMutation.isPending ? 'Salvando...' : 'Salvar Despesa'}
            </button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
}
