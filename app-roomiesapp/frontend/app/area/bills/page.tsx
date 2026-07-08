"use client";

import { useMemo, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Wallet, TrendingUp, TrendingDown, Home, Receipt, Loader2, KeyRound, AlertTriangle } from "lucide-react";
import { api } from "@/lib/api";
import { DebtResponse, GroupSimpleData, GroupMemberResponse } from "@/app/types";
import { Header } from "@/components/area-components/Header";
import { SubHeader } from "@/components/area-components/SubHeader";
import { InfoCards } from "@/components/area-components/InfoCards";
import { Empty } from "@/components/area-components/Empty";
import { HouseSelector } from "@/components/area-components/HouseSelector";
import { ExpensesList } from "@/components/groups-components/ExpensesList";
import { CreateExpenseDialog } from "@/components/groups-components/CreateExpenseDialog";
import { RegisterPixDialog } from "@/components/groups-components/RegisterPixDialog";

function formatCents(cents: number): string {
  return (cents / 100).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

const isOpen = (status: DebtResponse["status"]) => status === "PENDING" || status === "AWAITING_CONFIRMATION";

interface PixInfoResponse {
  externalId: string;
  pixKey: string;
  merchantName: string;
  merchantCity: string;
}

export default function BillsPage() {
  const [selectedInviteCode, setSelectedInviteCode] = useState<string | null>(null);
  const [pixDialogOpen, setPixDialogOpen] = useState(false);

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

  const { data: myDebts } = useQuery({
    queryKey: ["my-debts"],
    queryFn: async () => {
      const res = await api.get<DebtResponse[]>("/billing/debts/my");
      return res.data;
    },
  });

  const { data: myCredits } = useQuery({
    queryKey: ["my-credits"],
    queryFn: async () => {
      const res = await api.get<DebtResponse[]>("/billing/credits/my");
      return res.data;
    },
  });

  const { data: pixInfo, isLoading: loadingPix } = useQuery({
    queryKey: ["my-pix"],
    queryFn: async () => {
      const res = await api.get<PixInfoResponse>("/users/pix");
      return res.data;
    },
    retry: false,
  });
  const hasPixKey = !!pixInfo?.pixKey;

  const { data: members } = useQuery({
    queryKey: ["group-members", effectiveInviteCode],
    queryFn: async () => {
      const res = await api.get<GroupMemberResponse[]>(`/groups/${effectiveInviteCode}/members`);
      return res.data;
    },
    enabled: !!effectiveInviteCode,
  });

  const totalOwed = useMemo(
    () => (myDebts ?? []).filter((d) => isOpen(d.status)).reduce((sum, d) => sum + d.amountInCents, 0),
    [myDebts]
  );
  const totalCredit = useMemo(
    () => (myCredits ?? []).filter((d) => isOpen(d.status)).reduce((sum, d) => sum + d.amountInCents, 0),
    [myCredits]
  );
  const openCount = useMemo(
    () => (myDebts ?? []).filter((d) => d.status === "PENDING").length,
    [myDebts]
  );

  if (loadingHouses) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <Loader2 className="h-8 w-8 animate-spin text-ra-purple" />
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto pt-4 md:pt-6 pb-12 px-4 md:px-6">
      <div className="mb-8">
        <Header description="Rateie aluguel, água, luz, internet e outras despesas da casa">
          Contas
        </Header>
      </div>

      {!houses || houses.length === 0 ? (
        <Empty title="Você ainda não faz parte de nenhuma casa." Icon={Home}>
          Crie uma casa ou entre com um código de convite para começar a dividir as contas.
        </Empty>
      ) : (
        <>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-10">
            <InfoCards info={formatCents(totalOwed)} color="green" Icon={TrendingDown}>
              Você deve (no geral)
            </InfoCards>
            <InfoCards info={formatCents(totalCredit)} color="purple" Icon={TrendingUp}>
              Te devem (no geral)
            </InfoCards>
            <InfoCards info={openCount.toString()} color="blue" Icon={Wallet}>
              Pendências em aberto
            </InfoCards>
          </div>

          <div className="mb-6">
            <SubHeader className="mb-3">Selecione a casa</SubHeader>
            <HouseSelector
              houses={houses}
              selectedInviteCode={effectiveInviteCode}
              onSelect={setSelectedInviteCode}
            />
          </div>

          {/* Sem chave Pix, o backend recusa a criação de contas (você seria o pagador). */}
          {!loadingPix && !hasPixKey && (
            <div className="mb-6 flex flex-col sm:flex-row sm:items-center justify-between gap-3 p-4 rounded-2xl border border-amber-200 bg-amber-50">
              <div className="flex items-start gap-3">
                <AlertTriangle className="w-5 h-5 text-amber-500 shrink-0 mt-0.5" />
                <div>
                  <p className="text-sm font-semibold text-amber-900">
                    Cadastre sua chave Pix para criar contas
                  </p>
                  <p className="text-xs text-amber-700 mt-0.5">
                    Como você é o pagador das contas que criar, é obrigatório ter uma chave Pix
                    para que os moradores possam te pagar.
                  </p>
                </div>
              </div>
              <button
                onClick={() => setPixDialogOpen(true)}
                className="shrink-0 flex items-center justify-center gap-2 px-4 py-2 text-sm font-semibold text-white bg-amber-500 hover:bg-amber-600 rounded-xl transition-colors"
              >
                <KeyRound className="w-4 h-4" />
                Cadastrar chave Pix
              </button>
            </div>
          )}

          {selectedHouse && (
            <div>
              <div className="flex flex-wrap items-center justify-between gap-3 mb-4">
                <SubHeader className="flex items-center gap-2">
                  <Receipt className="w-5 h-5 text-ra-green" />
                  Contas de {selectedHouse.name}
                </SubHeader>
                <div className="flex items-center gap-2">
                  {hasPixKey && (
                    <button
                      onClick={() => setPixDialogOpen(true)}
                      className="flex items-center gap-1.5 text-sm font-semibold text-gray-600 border border-gray-200 hover:border-ra-purple hover:text-ra-purple px-3 py-2 rounded-xl transition-colors"
                    >
                      <KeyRound className="w-4 h-4" />
                      Chave Pix
                    </button>
                  )}
                  <CreateExpenseDialog
                    groupExternalId={selectedHouse.externalId}
                    members={members ?? []}
                  />
                </div>
              </div>

              <ExpensesList groupExternalId={selectedHouse.externalId} />
            </div>
          )}
        </>
      )}

      <RegisterPixDialog
        key={pixDialogOpen ? "pix-open" : "pix-closed"}
        isOpen={pixDialogOpen}
        onClose={() => setPixDialogOpen(false)}
        current={pixInfo ?? null}
      />
    </div>
  );
}
