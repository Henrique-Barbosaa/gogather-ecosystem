"use client";

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { BillResponse, DebtResponse, DebtStatus, PixCodeResponse } from "@/app/types";
import { useAuth } from "@/context/AuthContext";
import { useState } from "react";
import { Check, Copy, Receipt, QrCode, CalendarDays } from "lucide-react";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";

function formatCents(cents: number): string {
  return (cents / 100).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

const BILL_TYPE_LABEL: Record<string, string> = {
  NORMAL: "Avulsa",
  RECURRING: "Recorrente",
};

function StatusBadge({ status }: { status: DebtStatus }) {
  const map: Record<DebtStatus, { label: string; cls: string }> = {
    PENDING: { label: "Pendente", cls: "bg-yellow-100 text-yellow-800" },
    AWAITING_CONFIRMATION: { label: "Aguardando confirmação", cls: "bg-blue-100 text-blue-800" },
    PAID: { label: "Pago", cls: "bg-green-100 text-green-800" },
    CANCELLED: { label: "Cancelado", cls: "bg-red-100 text-red-800" },
  };
  const { label, cls } = map[status] ?? { label: status, cls: "bg-gray-200 text-gray-700" };
  return <span className={`text-xs font-medium px-2 py-0.5 rounded-full w-fit ${cls}`}>{label}</span>;
}

/** Card de uma conta da casa, com as dívidas que envolvem o usuário logado. */
function BillCard({ bill }: { bill: BillResponse }) {
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const currentUserId = user?.id?.toString();

  const [pixInfo, setPixInfo] = useState<PixCodeResponse | null>(null);
  const [isPixDialogOpen, setIsPixDialogOpen] = useState(false);
  const [activeDebtId, setActiveDebtId] = useState<string | null>(null);

  const { data: debts } = useQuery({
    queryKey: ['bill-debts', bill.externalId],
    queryFn: async () => {
      const res = await api.get<DebtResponse[]>(`/billing/bills/${bill.externalId}/debts`);
      return res.data;
    },
    enabled: !!bill.externalId,
  });

  const getPixMutation = useMutation({
    mutationFn: async (debtId: string) => {
      const res = await api.get<PixCodeResponse>(`/billing/debts/${debtId}/pix`);
      return res.data;
    },
    onSuccess: (data, debtId) => {
      setPixInfo(data);
      setActiveDebtId(debtId);
      setIsPixDialogOpen(true);
    },
  });

  const updateStatusMutation = useMutation({
    mutationFn: async ({ debtId, status }: { debtId: string; status: DebtStatus }) => {
      await api.patch(`/billing/debts/${debtId}/status`, null, { params: { status } });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['bill-debts', bill.externalId] });
      setIsPixDialogOpen(false);
    },
  });

  const myDebts = (debts ?? []).filter(
    d => d.debtor?.id === currentUserId || d.creditor?.id === currentUserId
  );

  return (
    <div className="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
      <div className="p-4 border-b border-gray-50 bg-gray-50/50">
        <div className="flex justify-between items-start gap-2">
          <div className="min-w-0">
            <h4 className="font-semibold text-gray-900 truncate">{bill.title}</h4>
            <p className="text-xs text-gray-500 mt-1">
              Total: <span className="font-medium text-gray-900">{formatCents(bill.totalCents)}</span>
              {bill.contributor && (
                <> • Pago por <span className="font-medium">{bill.contributor.name ?? bill.contributor.username}</span></>
              )}
            </p>
            {bill.dueDate && (
              <p className="text-xs text-gray-400 mt-1 flex items-center gap-1">
                <CalendarDays className="w-3 h-3" />
                Vence em {new Date(bill.dueDate).toLocaleDateString("pt-BR")}
              </p>
            )}
          </div>
          <span className="text-[10px] font-bold uppercase tracking-wider text-ra-purple bg-ra-purple/10 px-2 py-0.5 rounded-full shrink-0">
            {BILL_TYPE_LABEL[bill.billType] ?? bill.billType}
          </span>
        </div>
      </div>

      <div className="p-4 bg-white flex flex-col gap-3">
        <h5 className="text-xs font-bold text-gray-500 uppercase tracking-wider">Divisão</h5>
        {myDebts.length === 0 ? (
          <p className="text-xs text-gray-400">Você não tem pendências nesta conta.</p>
        ) : (
          <div className="flex flex-col gap-2">
            {myDebts.map((debt) => {
              const isDebtor = debt.debtor?.id === currentUserId;
              const otherName = isDebtor
                ? (debt.creditor?.name ?? debt.creditor?.username ?? "alguém")
                : (debt.debtor?.name ?? debt.debtor?.username ?? "alguém");

              return (
                <div key={debt.externalId} className="flex flex-col justify-between gap-3 p-3 rounded-lg bg-gray-50 border border-gray-100">
                  <div className="flex flex-col gap-1 text-sm">
                    {isDebtor ? (
                      <span>Você deve a <strong>{otherName}</strong> <strong className="text-red-600">{formatCents(debt.amountInCents)}</strong></span>
                    ) : (
                      <span><strong>{otherName}</strong> te deve <strong className="text-green-600">{formatCents(debt.amountInCents)}</strong></span>
                    )}
                    <StatusBadge status={debt.status} />
                  </div>

                  <div className="flex items-center gap-2">
                    {isDebtor && debt.status === 'PENDING' && (
                      <button
                        onClick={() => getPixMutation.mutate(debt.externalId)}
                        disabled={getPixMutation.isPending}
                        className="w-full px-3 py-2 text-xs font-semibold text-white bg-ra-green hover:bg-ra-green-dark rounded-lg flex items-center justify-center gap-1.5 transition-colors"
                      >
                        <QrCode className="w-3.5 h-3.5" />
                        Pagar (Pix)
                      </button>
                    )}

                    {!isDebtor && debt.status === 'AWAITING_CONFIRMATION' && (
                      <button
                        onClick={() => updateStatusMutation.mutate({ debtId: debt.externalId, status: 'PAID' })}
                        disabled={updateStatusMutation.isPending}
                        className="w-full px-3 py-2 text-xs font-semibold text-white bg-green-500 hover:bg-green-600 rounded-lg flex items-center justify-center gap-1.5 transition-colors"
                      >
                        <Check className="w-3.5 h-3.5" />
                        Confirmar recebimento
                      </button>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Dialog de Pix */}
      <Dialog open={isPixDialogOpen} onOpenChange={setIsPixDialogOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Pagar via Pix</DialogTitle>
          </DialogHeader>
          <div className="flex flex-col items-center gap-6 py-4">
            {pixInfo?.recipientName && (
              <p className="text-sm text-gray-600 text-center">
                Pagar {formatCents(pixInfo.amountInCents)} para <strong>{pixInfo.recipientName}</strong>
              </p>
            )}
            <div className="w-full bg-gray-50 p-4 rounded-lg flex items-center justify-between gap-3 border border-gray-200">
              <code className="text-xs text-gray-600 break-all line-clamp-3">
                {pixInfo?.pixCopiaECola}
              </code>
              <button
                onClick={() => pixInfo && navigator.clipboard.writeText(pixInfo.pixCopiaECola)}
                className="shrink-0 p-2 text-gray-500 hover:text-ra-green hover:bg-ra-green/10 rounded-md transition-colors"
              >
                <Copy className="w-4 h-4" />
              </button>
            </div>

            {activeDebtId && (
              <button
                onClick={() => updateStatusMutation.mutate({ debtId: activeDebtId, status: 'AWAITING_CONFIRMATION' })}
                disabled={updateStatusMutation.isPending}
                className="w-full py-2.5 text-sm font-semibold text-white bg-ra-green hover:bg-ra-green-dark rounded-xl transition-colors disabled:opacity-70"
              >
                {updateStatusMutation.isPending ? 'Registrando...' : 'Já paguei'}
              </button>
            )}
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}

export function ExpensesList({ groupExternalId }: { groupExternalId?: string }) {
  const { data: bills, isLoading } = useQuery({
    queryKey: ['group-bills', groupExternalId],
    queryFn: async () => {
      const res = await api.get<BillResponse[]>(`/billing/groups/${groupExternalId}/bills`);
      return res.data;
    },
    enabled: !!groupExternalId,
  });

  if (isLoading) {
    return <div className="animate-pulse h-32 bg-gray-100 rounded-md"></div>;
  }

  if (!bills || bills.length === 0) {
    return (
      <div className="text-center p-6 bg-white rounded-xl border border-gray-100 shadow-sm">
        <Receipt className="w-12 h-12 text-gray-300 mx-auto mb-3" />
        <h3 className="text-sm font-semibold text-gray-900">Nenhuma conta na casa</h3>
        <p className="text-xs text-gray-500 mt-1">Esta casa ainda não possui contas cadastradas.</p>
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-3">
      {bills.map((bill) => (
        <BillCard key={bill.externalId} bill={bill} />
      ))}
    </div>
  );
}
