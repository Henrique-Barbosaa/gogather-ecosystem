"use client";

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { ExpenseData } from "@/app/types";
import { useAuth } from "@/context/AuthContext";
import { useState } from "react";
import { Check, Copy, Receipt, QrCode } from "lucide-react";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";

export function ExpensesList({ groupId, members }: { groupId: string, members: { externalId: string, displayName?: string, username: string }[] }) {
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const [pixCode, setPixCode] = useState<string | null>(null);
  const [selectedSplitId, setSelectedSplitId] = useState<string | null>(null);
  const [isPixDialogOpen, setIsPixDialogOpen] = useState(false);

  const { data: expenses, isLoading } = useQuery({
    queryKey: ['group-expenses', groupId],
    queryFn: async () => {
      const res = await api.get<ExpenseData[]>(`/groups/${groupId}/expenses`);
      return res.data;
    },
    enabled: !!groupId,
  });

  const getPixCodeMutation = useMutation({
    mutationFn: async (splitId: string) => {
      const res = await api.get(`/expense/split/${splitId}/pix-code`);
      return res.data.pixCopyAndPaste;
    },
    onSuccess: (code, splitId) => {
      setPixCode(code);
      setSelectedSplitId(splitId);
      setIsPixDialogOpen(true);
    },
  });

  const markAsPaidMutation = useMutation({
    mutationFn: async (splitId: string) => {
      await api.patch(`/expense/split/${splitId}/mark-as-paid`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['group-expenses', groupId] });
      setIsPixDialogOpen(false);
    },
  });

  const confirmReceiptMutation = useMutation({
    mutationFn: async (splitId: string) => {
      await api.patch(`/expense/split/${splitId}/confirm-receipt`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['group-expenses', groupId] });
    },
  });

  if (isLoading) {
    return <div className="animate-pulse h-32 bg-gray-100 rounded-md"></div>;
  }

  if (!expenses || expenses.length === 0) {
    return (
      <div className="text-center p-6 bg-white rounded-xl border border-gray-100 shadow-sm">
        <Receipt className="w-12 h-12 text-gray-300 mx-auto mb-3" />
        <h3 className="text-sm font-semibold text-gray-900">Nenhuma dívida no grupo</h3>
        <p className="text-xs text-gray-500 mt-1">Este grupo ainda não possui despesas cadastradas.</p>
      </div>
    );
  }

  const handleCopyPix = () => {
    if (pixCode) {
      navigator.clipboard.writeText(pixCode);
    }
  };

  return (
    <div className="flex flex-col gap-4">
      <h3 className="text-lg font-bold text-gray-900 flex items-center gap-2">
        <Receipt className="w-5 h-5 text-gg-cyan" />
        Despesas do Grupo
      </h3>

      <div className="flex flex-col gap-3">
        {expenses.map((expense) => (
          <div key={expense.expenseExternalId} className="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
            <div className="p-4 border-b border-gray-50 bg-gray-50/50">
              <div className="flex justify-between items-start">
                <div>
                  <h4 className="font-semibold text-gray-900">{expense.description}</h4>
                  <p className="text-xs text-gray-500 mt-1">
                    Valor Total: <span className="font-medium text-gray-900">R$ {expense.totalValue.toFixed(2)}</span>
                  </p>
                </div>
              </div>
            </div>

            <div className="p-4 bg-white flex flex-col gap-3">
              <h5 className="text-xs font-bold text-gray-500 uppercase tracking-wider">Divisões</h5>
              {expense.distributions.length === 0 ? (
                <p className="text-xs text-gray-400">Nenhuma divisão registrada.</p>
              ) : (
                <div className="flex flex-col gap-2">
                  {expense.distributions.map((dist) => {
                    const isDebtor = dist.debtorExternalId === user?.id?.toString();
                    const isCreditor = dist.creditorExternalId === user?.id?.toString();

                    if (!isDebtor && !isCreditor) return null;

                    const debtorMember = members.find(m => m.externalId === dist.debtorExternalId);
                    const creditorMember = members.find(m => m.externalId === dist.creditorExternalId);
                    const debtorName = debtorMember ? (debtorMember.displayName || debtorMember.username) : "Alguém";
                    const creditorName = creditorMember ? (creditorMember.displayName || creditorMember.username) : "Alguém";

                    return (
                      <div key={dist.expenseDistributionExternalId} className="flex flex-col justify-between gap-3 p-3 rounded-lg bg-gray-50 border border-gray-100">
                        <div className="flex flex-col gap-1 text-sm">
                          {isDebtor ? (
                            <span>Você deve a <strong>{creditorName}</strong> <strong className="text-red-600">R$ {dist.value.toFixed(2)}</strong></span>
                          ) : (
                            <span><strong>{debtorName}</strong> te deve <strong className="text-green-600">R$ {dist.value.toFixed(2)}</strong></span>
                          )}
                          <span className="text-xs font-medium px-2 py-0.5 rounded-full w-fit bg-gray-200 text-gray-700">
                            Status: {dist.status}
                          </span>
                        </div>

                        <div className="flex items-center gap-2">
                          {isDebtor && dist.status === 'PENDING' && (
                            <button
                              onClick={() => getPixCodeMutation.mutate(dist.expenseDistributionExternalId)}
                              disabled={getPixCodeMutation.isPending}
                              className="w-full px-3 py-2 text-xs font-semibold text-white bg-gg-cyan hover:bg-cyan-500 rounded-lg flex items-center justify-center gap-1.5 transition-colors"
                            >
                              <QrCode className="w-3.5 h-3.5" />
                              Pagar (Pix)
                            </button>
                          )}
                          
                          {isCreditor && dist.status === 'AWAITING_CONFIRMATION' && (
                            <button
                              onClick={() => confirmReceiptMutation.mutate(dist.expenseDistributionExternalId)}
                              disabled={confirmReceiptMutation.isPending}
                              className="w-full px-3 py-2 text-xs font-semibold text-white bg-green-500 hover:bg-green-600 rounded-lg flex items-center justify-center gap-1.5 transition-colors"
                            >
                              <Check className="w-3.5 h-3.5" />
                              Confirmar Recebimento
                            </button>
                          )}
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          </div>
        ))}
      </div>

      <Dialog open={isPixDialogOpen} onOpenChange={setIsPixDialogOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Pagar Dívida via Pix</DialogTitle>
          </DialogHeader>
          <div className="flex flex-col items-center gap-6 py-4">
            <div className="w-full bg-gray-50 p-4 rounded-lg flex items-center justify-between gap-3 border border-gray-200">
              <code className="text-xs text-gray-600 break-all line-clamp-2">
                {pixCode}
              </code>
              <button 
                onClick={handleCopyPix}
                className="shrink-0 p-2 text-gray-500 hover:text-gg-cyan hover:bg-cyan-50 rounded-md transition-colors"
              >
                <Copy className="w-4 h-4" />
              </button>
            </div>
            
            {selectedSplitId && (
              <button
                onClick={() => markAsPaidMutation.mutate(selectedSplitId)}
                disabled={markAsPaidMutation.isPending}
                className="w-full py-2.5 text-sm font-semibold text-white bg-gg-cyan hover:bg-cyan-500 rounded-xl transition-colors disabled:opacity-70"
              >
                {markAsPaidMutation.isPending ? 'Marcando como pago...' : 'Marcar como Pago'}
              </button>
            )}
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}
