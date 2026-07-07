'use client';

import React, { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { TravelGroup, Expense, Member, getAvatarUrl, mapExpenseFromApi } from '../../../../services/mockData';
import { Card, Button, Input, Modal, Badge } from '../../../../components/common';
import { DollarSign, Plus, ArrowUpRight, ArrowDownLeft, PieChart, Calendar, Tag, User, Sparkles, Check, AlertCircle, QrCode, Copy, CheckCircle, Clock, Trash2, XCircle, Users } from 'lucide-react';
import { useNotifications } from '../../../../context/NotificationContext';
import { useAuth } from '../../../../context/AuthContext';
import { api } from '../../../../services/api';

const expenseSchema = z.object({
  title: z.string().min(3, 'Digite uma descrição (ex: Jantar, Aluguel)'),
  amount: z.string().refine((val) => !isNaN(Number(val)) && Number(val) > 0, {
    message: 'Digite um valor válido maior que zero',
  }),
  paidBy: z.string().min(1, 'Selecione quem pagou'),
  category: z.enum(['Hospedagem', 'Alimentação', 'Transporte', 'Lazer', 'Outros']),
});

type ExpenseFormValues = z.infer<typeof expenseSchema>;

interface ExpensesTabProps {
  group: TravelGroup;
  members: Member[];
  expenses: Expense[];
  onUpdateExpenses: (newExpenses: Expense[]) => void;
}

export const ExpensesTab: React.FC<ExpensesTabProps> = ({
  group,
  members,
  expenses: propExpenses,
  onUpdateExpenses,
}) => {
  const { user } = useAuth();
  const { addNotification } = useNotifications();
  const [expenses, setExpenses] = useState<Expense[]>(propExpenses);
  const [debts, setDebts] = useState<any[]>([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isMultiPayer, setIsMultiPayer] = useState(false);
  const [isCustomParticipants, setIsCustomParticipants] = useState(false);
  const [selectedParticipantIds, setSelectedParticipantIds] = useState<string[]>([]);
  const [contributionsList, setContributionsList] = useState<Array<{ memberId: string; amount: string }>>([
    { memberId: '', amount: '' }
  ]);
  const [successMsg, setSuccessMsg] = useState(false);

  // Pix Payment Modal states
  const [pixModalDebt, setPixModalDebt] = useState<any | null>(null);
  const [pixCodeData, setPixCodeData] = useState<{
    pixCopyAndPaste: string;
    pixKey: string;
    merchantName: string;
    merchantCity: string;
    amountInCents: number;
  } | null>(null);
  const [loadingPixCode, setLoadingPixCode] = useState(false);
  const [pixError, setPixError] = useState<string | null>(null);
  const [copiedPix, setCopiedPix] = useState(false);

  const handleOpenPixModal = async (debt: any) => {
    setPixModalDebt(debt);
    setPixCodeData(null);
    setPixError(null);
    setLoadingPixCode(true);

    try {
      const res = await api.get(`/groups/${group.id}/expenses/debts/${debt.id}/pix-code`);
      setPixCodeData(res.data);
    } catch (err: any) {
      console.error('Erro ao gerar código Pix:', err);
      const msg = err.response?.data?.message || 'Não foi possível gerar o código Pix. Verifique se o credor cadastrou a chave Pix no perfil.';
      setPixError(msg);
    } finally {
      setLoadingPixCode(false);
    }
  };

  const handleConfirmPayment = async () => {
    if (!pixModalDebt) return;
    try {
      const res = await api.patch(`/groups/${group.id}/expenses/debts/${pixModalDebt.id}/status`, { status: 'AWAITING_CONFIRMATION' });
      const updatedDebt = res?.data || { ...pixModalDebt, status: 'AWAITING_CONFIRMATION' };
      setDebts((prev) => prev.map((d) => (d.id === pixModalDebt.id ? { ...d, status: updatedDebt.status || 'AWAITING_CONFIRMATION' } : d)));
      setPixModalDebt(null);
      addNotification({
        groupId: group.id,
        groupTitle: group.title,
        type: 'rateio',
        title: 'Pagamento via Pix Informado! ⏳',
        message: `Você informou o pagamento de R$ ${(pixModalDebt.amountInCents / 100).toFixed(2)} para ${pixModalDebt.creditorDisplayName || pixModalDebt.creditorUsername}. Aguardando confirmação.`,
      });
    } catch (err) {
      console.error('Erro ao informar pagamento da dívida:', err);
      alert('Não foi possível informar o pagamento. Tente novamente.');
    }
  };

  const handleConfirmReceipt = async (debt: any) => {
    try {
      const res = await api.patch(`/groups/${group.id}/expenses/debts/${debt.id}/status`, { status: 'PAID' });
      const updatedDebt = res?.data || { ...debt, status: 'PAID' };
      setDebts((prev) => prev.map((d) => (d.id === debt.id ? { ...d, status: updatedDebt.status || 'PAID' } : d)));
      addNotification({
        groupId: group.id,
        groupTitle: group.title,
        type: 'rateio',
        title: 'Pagamento Confirmado! 🎉',
        message: `O pagamento de R$ ${(debt.amountInCents / 100).toFixed(2)} de ${debt.debtorDisplayName || debt.debtorUsername} foi confirmado!`,
      });
    } catch (err) {
      console.error('Erro ao confirmar recebimento:', err);
      alert('Não foi possível confirmar o recebimento. Tente novamente.');
    }
  };

  const handleDenyReceipt = async (debt: any) => {
    if (!confirm(`Deseja realmente negar a confirmação de pagamento de ${debt.debtorDisplayName || debt.debtorUsername}? A dívida voltará para o status pendente.`)) {
      return;
    }
    try {
      const res = await api.patch(`/groups/${group.id}/expenses/debts/${debt.id}/status`, { status: 'PENDING' });
      const updatedDebt = res?.data || { ...debt, status: 'PENDING' };
      setDebts((prev) => prev.map((d) => (d.id === debt.id ? { ...d, status: updatedDebt.status || 'PENDING' } : d)));
      addNotification({
        groupId: group.id,
        groupTitle: group.title,
        type: 'rateio',
        title: 'Pagamento Negado ⚠️',
        message: `A confirmação de pagamento de ${debt.debtorDisplayName || debt.debtorUsername} foi negada e retornou para pendente.`,
      });
    } catch (err) {
      console.error('Erro ao recusar confirmação do pagamento:', err);
      alert('Não foi possível recusar o pagamento. Tente novamente.');
    }
  };

  useEffect(() => {
    setExpenses(propExpenses);
  }, [propExpenses]);

  useEffect(() => {
    if (members.length > 0) {
      if (!contributionsList[0]?.memberId) {
        setContributionsList([{ memberId: String(user?.id || members[0].id), amount: '' }]);
      }
      setSelectedParticipantIds(members.map(m => String(m.id)));
    }
  }, [members, user]);

  const fetchExpensesAndDebts = async () => {
    try {
      const [expRes, debtRes] = await Promise.all([
        api.get(`/groups/${group.id}/expenses`).catch(() => null),
        api.get(`/groups/${group.id}/expenses/debts`).catch(() => null)
      ]);

      if (expRes && Array.isArray(expRes.data)) {
        const mappedExpenses: Expense[] = expRes.data.map((exp: any) => mapExpenseFromApi(exp, group.id));
        setExpenses(mappedExpenses);
        onUpdateExpenses(mappedExpenses);
      }

      if (debtRes && Array.isArray(debtRes.data)) {
        setDebts(debtRes.data);
      }
    } catch (e) {
      console.error('Failed to fetch expenses or debts', e);
    }
  };

  useEffect(() => {
    fetchExpensesAndDebts();
  }, [group.id]);

  const totalExpenses = expenses.reduce((acc, curr) => acc + curr.amount, 0);
  const averagePerPerson = members.length > 0 ? totalExpenses / members.length : 0;

  // Calculate balance per person: totalPaid - averagePerPerson adjusted by confirmed rateios
  const balances = members.map((member) => {
    const totalPaidByMember = expenses.reduce((acc, exp) => {
      if (exp.contributions && exp.contributions.length > 0) {
        const memberContrib = exp.contributions.find((c) => String(c.userId) === String(member.id) || c.name === member.name);
        return acc + (memberContrib ? memberContrib.amount : 0);
      }
      return (String(exp.paidById) === String(member.id) || exp.paidBy === member.name) ? acc + exp.amount : acc;
    }, 0);

    const paidAsDebtor = debts
      .filter((d) => d.status === 'PAID' && (String(d.debtorId) === String(member.id) || d.debtorUsername === member.name || d.debtorDisplayName === member.name))
      .reduce((acc, d) => acc + (d.amountInCents || 0) / 100, 0);

    const receivedAsCreditor = debts
      .filter((d) => d.status === 'PAID' && (String(d.creditorId) === String(member.id) || d.creditorUsername === member.name || d.creditorDisplayName === member.name))
      .reduce((acc, d) => acc + (d.amountInCents || 0) / 100, 0);

    const balance = totalPaidByMember - averagePerPerson + paidAsDebtor - receivedAsCreditor;
    return {
      member,
      totalPaid: totalPaidByMember,
      balance,
    };
  });

  const currentUserBalance = balances.find((b) => b.member.id === user?.id || b.member.name === user?.name);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<ExpenseFormValues>({
    resolver: zodResolver(expenseSchema),
    defaultValues: {
      title: '',
      amount: '',
      category: 'Alimentação',
      paidBy: user?.name || members[0]?.name || 'Pedro Henrique',
    },
  });

  const onSubmit = async (data: ExpenseFormValues) => {
    const totalVal = Number(data.amount);
    let payloadContributions: Array<{ userId: number; amountInCents: number }> = [];

    if (isMultiPayer) {
      const validContribs = contributionsList.filter(c => c.memberId && Number(c.amount) > 0);
      if (validContribs.length === 0) {
        alert('Adicione pelo menos um contribuidor com valor maior que zero.');
        return;
      }
      const sumContribs = validContribs.reduce((acc, curr) => acc + Number(curr.amount), 0);
      if (Math.abs(sumContribs - totalVal) > 0.05) {
        alert(`A soma das contribuições (R$ ${sumContribs.toFixed(2)}) deve ser igual ao valor total da despesa (R$ ${totalVal.toFixed(2)}).`);
        return;
      }
      payloadContributions = validContribs
        .map(c => ({
          userId: Number(c.memberId),
          amountInCents: Math.round(Number(c.amount) * 100)
        }))
        .filter(c => !isNaN(c.userId) && c.userId > 0 && !isNaN(c.amountInCents) && c.amountInCents > 0);
    } else {
      const selectedMember = members.find((m) => m.name === data.paidBy || String(m.id) === data.paidBy) || members[0];
      const payerId = Number(selectedMember?.id || user?.id || 1);
      payloadContributions = [{
        userId: !isNaN(payerId) && payerId > 0 ? payerId : 1,
        amountInCents: Math.round(totalVal * 100)
      }];
    }

    try {
      if (isCustomParticipants && selectedParticipantIds.length === 0) {
        alert('Selecione pelo menos um participante para a divisão do rateio.');
        return;
      }

      const validParticipantIds = members
        .filter((m) => !isCustomParticipants || selectedParticipantIds.includes(String(m.id)))
        .map((m) => Number(m.id))
        .filter((id) => !isNaN(id) && id > 0);

      const payload = {
        description: data.title,
        expenseDate: new Date().toISOString().split('T')[0],
        category: data.category,
        contributions: payloadContributions,
        participantIds: validParticipantIds
      };

      await api.post(`/groups/${group.id}/expenses`, payload);
      await fetchExpensesAndDebts();
    } catch (err: any) {
      console.error('Erro ao registrar despesa no banco de dados:', err);
      const errorMsg = err.response?.data?.message || err.response?.data?.error || (typeof err.response?.data === 'string' ? err.response?.data : err.message) || 'Erro ao registrar despesa.';
      alert(`Atenção: ${errorMsg}`);
      return;
    }

    setIsModalOpen(false);
    reset();
    setIsMultiPayer(false);
    setIsCustomParticipants(false);
    setSelectedParticipantIds(members.map(m => String(m.id)));
    setContributionsList([{ memberId: String(user?.id || members[0]?.id || ''), amount: '' }]);
    setSuccessMsg(true);

    addNotification({
      groupId: group.id,
      groupTitle: group.title,
      type: 'rateio',
      title: `Novo Rateio: R$ ${Number(data.amount).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}`,
      message: `${isMultiPayer ? 'Múltiplos contribuidores' : data.paidBy} registraram a despesa "${data.title}".`,
    });

    setTimeout(() => setSuccessMsg(false), 3000);
  };

  const getCategoryBadge = (cat: Expense['category']) => {
    switch (cat) {
      case 'Hospedagem':
        return <Badge variant="purple">{cat}</Badge>;
      case 'Alimentação':
        return <Badge variant="red">{cat}</Badge>;
      case 'Transporte':
        return <Badge variant="yellow">{cat}</Badge>;
      case 'Lazer':
        return <Badge variant="green">{cat}</Badge>;
      default:
        return <Badge variant="gray">{cat}</Badge>;
    }
  };

  return (
    <div className="space-y-6 animate-fadeIn">
      
      {/* Summary Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <Card variant="pastel-red" className="p-5 flex items-center justify-between">
          <div>
            <span className="text-xs font-bold text-[var(--color-pastel-red-800)] uppercase tracking-wider">
              Custo Total da Viagem
            </span>
            <p className="text-2xl font-extrabold text-[#2d2327] mt-1">
              R$ {totalExpenses.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
            </p>
            <span className="text-xs font-medium text-gray-600">
              {expenses.length} contas lançadas
            </span>
          </div>
          <div className="p-3.5 rounded-2xl bg-white text-[var(--color-pastel-red-600)] shadow-xs">
            <DollarSign className="w-6 h-6" />
          </div>
        </Card>

        <Card variant="pastel-yellow" className="p-5 flex items-center justify-between">
          <div>
            <span className="text-xs font-bold text-[var(--color-pastel-yellow-700)] uppercase tracking-wider">
              Média por Pessoa
            </span>
            <p className="text-2xl font-extrabold text-[#2d2327] mt-1">
              R$ {averagePerPerson.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
            </p>
            <span className="text-xs font-medium text-gray-600">
              Dividido em {members.length} integrantes
            </span>
          </div>
          <div className="p-3.5 rounded-2xl bg-white text-[var(--color-pastel-yellow-700)] shadow-xs">
            <PieChart className="w-6 h-6" />
          </div>
        </Card>

        <Card variant="glass" className="p-5 flex items-center justify-between border-2 border-white/80">
          <div>
            <span className="text-xs font-bold text-gray-500 uppercase tracking-wider">
              Seu Saldo Pessoal
            </span>
            <p
              className={`text-2xl font-extrabold mt-1 ${
                Math.abs(currentUserBalance?.balance || 0) < 0.05
                  ? 'text-gray-700'
                  : (currentUserBalance?.balance || 0) >= 0
                  ? 'text-emerald-600'
                  : 'text-rose-600'
              }`}
            >
              {Math.abs(currentUserBalance?.balance || 0) < 0.05
                ? 'R$ 0,00'
                : `${(currentUserBalance?.balance || 0) >= 0 ? '+' : ''} R$ ${Math.abs(currentUserBalance?.balance || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}`}
            </p>
            <span className="text-xs font-semibold text-gray-600 flex items-center gap-1">
              {Math.abs(currentUserBalance?.balance || 0) < 0.05 ? (
                <span className="text-emerald-600 font-bold flex items-center">
                  <CheckCircle className="w-3.5 h-3.5 mr-1" /> Acertos quitados / Em dia!
                </span>
              ) : (currentUserBalance?.balance || 0) >= 0 ? (
                <span className="text-emerald-600 font-bold flex items-center">
                  <ArrowUpRight className="w-3.5 h-3.5" /> A receber no acerto
                </span>
              ) : (
                <span className="text-rose-600 font-bold flex items-center">
                  <ArrowDownLeft className="w-3.5 h-3.5" /> A transferir no rateio
                </span>
              )}
            </span>
          </div>
          <div className="p-3.5 rounded-2xl bg-gray-100 text-[#2d2327] shadow-xs">
            <DollarSign className="w-6 h-6" />
          </div>
        </Card>
      </div>

      {successMsg && (
        <div className="p-4 rounded-2xl bg-emerald-50 border border-emerald-200 text-emerald-800 text-sm font-semibold flex items-center gap-2 animate-fadeIn">
          <Check className="w-5 h-5 text-emerald-600" />
          <span>Nova despesa adicionada e notificação de rateio disparada com sucesso! 💸</span>
        </div>
      )}

      {/* Detailed Balances & Actions */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        {/* Balances List (Quem deve para quem) */}
        <Card variant="glass" className="p-6 border-2 border-white/80 space-y-4">
          <div className="flex items-center gap-2 border-b border-gray-100 pb-3">
            <PieChart className="w-5 h-5 text-[var(--color-pastel-yellow-700)]" />
            <h3 className="text-lg font-bold text-[#2d2327]">Resumo de Saldos (Rateio)</h3>
          </div>
          <p className="text-xs text-gray-500">
            Veja quanto cada integrante pagou até o momento e o balanço para o acerto de contas final.
          </p>
          <div className="space-y-3 pt-1">
            {balances.map((b) => (
              <div key={b.member.id} className="flex items-center justify-between p-3 rounded-2xl bg-gray-50/80 border border-gray-100">
                <div className="flex items-center gap-3">
                  <img
                    src={getAvatarUrl(b.member.name, b.member.avatar)}
                    alt={b.member.name}
                    className="w-10 h-10 rounded-xl object-cover border border-gray-200"
                  />
                  <div>
                    <span className="text-sm font-bold text-[#2d2327] block leading-tight">
                      {b.member.name}
                    </span>
                    <span className="text-xs text-gray-500">
                      Pagou: R$ {b.totalPaid.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
                    </span>
                  </div>
                </div>
                <div className="text-right">
                  <span
                    className={`text-xs font-extrabold px-2.5 py-1 rounded-lg ${
                      Math.abs(b.balance) < 0.05
                        ? 'bg-gray-100 text-gray-700 border border-gray-200'
                        : b.balance >= 0
                        ? 'bg-emerald-100 text-emerald-800 border border-emerald-200'
                        : 'bg-rose-100 text-rose-800 border border-rose-200'
                    }`}
                  >
                    {Math.abs(b.balance) < 0.05
                      ? 'R$ 0,00'
                      : `${b.balance >= 0 ? '+' : '-'} R$ ${Math.abs(b.balance).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}`}
                  </span>
                </div>
              </div>
            ))}
          </div>

          {debts.length > 0 && (
            <div className="pt-4 border-t border-gray-200/80 space-y-2">
              <h4 className="text-xs font-bold text-[#2d2327] uppercase tracking-wider flex items-center gap-1.5">
                <Sparkles className="w-3.5 h-3.5 text-amber-500" /> Transferências de Acerto (Rateio Oficial)
              </h4>
              <div className="space-y-2">
                {debts.map((d) => {
                  const isDebtor = String(d.debtorId) === String(user?.id) || d.debtorUsername === user?.email || d.debtorUsername === user?.name || d.debtorDisplayName === user?.name;
                  const isCreditor = String(d.creditorId) === String(user?.id) || d.creditorUsername === user?.email || d.creditorUsername === user?.name || d.creditorDisplayName === user?.name;

                  return (
                    <div key={d.id} className="p-3 rounded-2xl bg-white/90 border border-gray-200/90 text-xs flex flex-col sm:flex-row sm:items-center justify-between gap-3 shadow-2xs">
                      <div className="space-y-1">
                        <div className="flex items-center gap-1.5 flex-wrap">
                          <span className="font-bold text-rose-700 text-sm">{d.debtorDisplayName || d.debtorUsername}</span>
                          <span className="text-gray-500 font-medium">deve transferir para</span>
                          <span className="font-bold text-emerald-700 text-sm">{d.creditorDisplayName || d.creditorUsername}</span>
                        </div>
                        <div className="text-[11px] text-gray-500 font-medium flex items-center gap-2">
                          <span>Ref: <b>{d.expenseDescription}</b></span>
                          <span>•</span>
                          <span className="font-extrabold text-[#2d2327]">
                            R$ {(d.amountInCents / 100).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
                          </span>
                        </div>
                      </div>

                      <div className="flex items-center justify-end gap-2 pt-2 sm:pt-0 border-t sm:border-0 border-gray-100 flex-wrap">
                        {d.status === 'PAID' ? (
                          <span className="inline-flex items-center gap-1 px-3 py-1.5 rounded-xl bg-emerald-100 text-emerald-800 font-bold text-[11px] border border-emerald-200 cursor-not-allowed select-none">
                            <Check className="w-3.5 h-3.5 text-emerald-600" /> Pagamento confirmado!
                          </span>
                        ) : d.status === 'AWAITING_CONFIRMATION' ? (
                          isCreditor ? (
                            <div className="flex items-center gap-2">
                              <Button
                                variant="primary"
                                size="sm"
                                className="!py-1.5 !px-2.5 !text-[11px] !bg-emerald-600 hover:!bg-emerald-700 !text-white !font-bold !rounded-xl flex items-center gap-1 shadow-xs"
                                onClick={() => handleConfirmReceipt(d)}
                                title="Confirmar que você recebeu o dinheiro"
                              >
                                <Check className="w-3.5 h-3.5" /> Confirmar
                              </Button>
                              <Button
                                variant="outline"
                                size="sm"
                                className="!py-1.5 !px-2.5 !text-[11px] !bg-rose-50 hover:!bg-rose-100 !text-rose-700 !border-rose-300 !font-bold !rounded-xl flex items-center gap-1 shadow-xs"
                                onClick={() => handleDenyReceipt(d)}
                                title="Negar recebimento e pedir para o devedor pagar novamente"
                              >
                                <XCircle className="w-3.5 h-3.5" /> Negar
                              </Button>
                            </div>
                          ) : isDebtor ? (
                            <span className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-amber-100 text-amber-800 font-bold text-[11px] border border-amber-200 cursor-not-allowed select-none">
                              <Clock className="w-3.5 h-3.5" /> Aguardando confirmação...
                            </span>
                          ) : (
                            <span className="inline-flex items-center gap-1 px-3 py-1.5 rounded-xl bg-amber-50 text-amber-700 font-bold text-[11px] border border-amber-200 cursor-not-allowed select-none">
                              <Clock className="w-3.5 h-3.5" /> Em análise pelo credor
                            </span>
                          )
                        ) : isDebtor ? (
                          <Button
                            variant="primary"
                            size="sm"
                            className="!py-1.5 !px-3 !text-xs !bg-gradient-to-r !from-emerald-600 !to-teal-600 hover:!from-emerald-700 hover:!to-teal-700 !text-white !font-bold !shadow-md !rounded-xl flex items-center gap-1.5 transform hover:scale-[1.02] transition-all"
                            onClick={() => handleOpenPixModal(d)}
                          >
                            <QrCode className="w-4 h-4 animate-pulse" /> Pagar com Pix
                          </Button>
                        ) : isCreditor ? (
                          <span className="inline-flex items-center gap-1 px-3 py-1.5 rounded-xl bg-gray-100 text-gray-500 font-bold text-[11px] border border-gray-200 cursor-not-allowed select-none">
                            <Clock className="w-3.5 h-3.5" /> Aguardando pagamento...
                          </span>
                        ) : (
                          <button
                            type="button"
                            disabled
                            className="inline-flex items-center gap-1 px-3 py-1.5 rounded-xl bg-gray-100 text-gray-400 font-bold text-[11px] border border-gray-200 cursor-not-allowed select-none"
                            title="Você não é o devedor desta conta"
                          >
                            Você não é o devedor!
                          </button>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          )}
        </Card>

        {/* Expenses List */}
        <Card variant="glass" className="lg:col-span-2 p-6 border-2 border-white/80 space-y-4">
          <div className="flex items-center justify-between border-b border-gray-100 pb-3">
            <div className="flex items-center gap-2">
              <DollarSign className="w-5 h-5 text-[var(--color-pastel-red-600)]" />
              <h3 className="text-lg font-bold text-[#2d2327]">Histórico de Despesas</h3>
            </div>
            <Button
              variant="primary"
              size="sm"
              onClick={() => {
                setIsCustomParticipants(false);
                setSelectedParticipantIds(members.map(m => String(m.id)));
                setIsModalOpen(true);
              }}
              leftIcon={<Plus className="w-4 h-4" />}
            >
              Adicionar Despesa
            </Button>
          </div>

          {expenses.length === 0 ? (
            <div className="py-12 text-center text-gray-400 text-sm bg-white/50 rounded-2xl border border-dashed border-gray-200">
              Nenhuma conta registrada ainda. Clique em <b>Adicionar Despesa</b> para começar o rateio!
            </div>
          ) : (
            <div className="space-y-3 max-h-[420px] overflow-y-auto pr-1">
              {expenses.map((exp) => (
                <div
                  key={exp.id}
                  className="p-4 rounded-2xl bg-white border border-gray-100 hover:border-[var(--color-pastel-red-200)] transition-all shadow-2xs flex items-center justify-between gap-4"
                >
                  <div className="flex items-center gap-3 min-w-0">
                    <div className="p-2.5 rounded-xl bg-[var(--color-pastel-red-50)] text-[var(--color-pastel-red-600)] shrink-0">
                      <Tag className="w-5 h-5" />
                    </div>
                    <div className="min-w-0">
                      <div className="flex items-center gap-2">
                        <span className="font-bold text-[#2d2327] text-sm truncate">{exp.title}</span>
                        {getCategoryBadge(exp.category)}
                      </div>
                      <div className="flex items-center gap-3 text-xs text-gray-500 mt-1">
                        <span className="flex items-center gap-1 font-semibold text-gray-700">
                          <User className="w-3 h-3 text-gray-400" /> Pago por {exp.paidBy}
                        </span>
                        <span className="flex items-center gap-1">
                          <Calendar className="w-3 h-3 text-gray-400" /> {new Date(exp.date).toLocaleDateString('pt-BR')}
                        </span>
                      </div>
                      {exp.contributions && exp.contributions.length > 1 && (
                        <div className="flex flex-wrap gap-1.5 mt-1.5">
                          {exp.contributions.map((c, i) => (
                            <span key={i} className="text-[11px] bg-red-50/80 text-red-700 px-2 py-0.5 rounded-md font-semibold border border-red-100">
                              {c.name}: R$ {c.amount.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
                            </span>
                          ))}
                        </div>
                      )}
                    </div>
                  </div>

                  <div className="text-right shrink-0">
                    <span className="text-base font-extrabold text-[#2d2327]">
                      R$ {exp.amount.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
                    </span>
                    <span className="block text-[10px] font-semibold text-gray-400">
                      {exp.participants.length} integrantes
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </Card>

      </div>

      {/* Add Expense Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Registrar Despesa para Rateio"
        maxWidth="md"
      >
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <Input
            label="Título da Despesa"
            placeholder="Ex: Supermercado e Bebidas"
            error={errors.title?.message}
            {...register('title')}
          />

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <Input
              label="Valor Pago (R$)"
              type="number"
              step="0.01"
              placeholder="Ex: 350.00"
              error={errors.amount?.message}
              {...register('amount')}
            />

            <div className="flex flex-col gap-1">
              <label className="text-sm font-semibold text-[#2d2327]">Categoria</label>
              <select
                className="w-full bg-white/90 border border-gray-200 rounded-xl p-2.5 text-sm text-[#2d2327] outline-none focus:border-[var(--color-pastel-red-500)] focus:ring-2 focus:ring-[var(--color-pastel-red-200)] shadow-xs"
                {...register('category')}
              >
                <option value="Alimentação">Alimentação</option>
                <option value="Hospedagem">Hospedagem</option>
                <option value="Transporte">Transporte</option>
                <option value="Lazer">Lazer</option>
                <option value="Outros">Outros</option>
              </select>
            </div>
          </div>

          <div className="flex flex-col gap-2 pt-1">
            <div className="flex items-center justify-between">
              <label className="text-sm font-semibold text-[#2d2327]">Quem realizou o pagamento?</label>
              <button
                type="button"
                onClick={() => {
                  const nextVal = !isMultiPayer;
                  setIsMultiPayer(nextVal);
                  if (nextVal && contributionsList.length === 1) {
                    setContributionsList([
                      { memberId: String(user?.id || members[0]?.id || ''), amount: '' },
                      { memberId: String(members[1]?.id || members[0]?.id || ''), amount: '' }
                    ]);
                  }
                }}
                className="text-xs font-bold text-[var(--color-pastel-red-600)] hover:underline flex items-center gap-1"
              >
                <Sparkles className="w-3.5 h-3.5" />
                {isMultiPayer ? 'Voltar para pagador único' : '+ Múltiplos contribuidores (Dividir pagamento)'}
              </button>
            </div>

            {!isMultiPayer ? (
              <>
                <select
                  className="w-full bg-white/90 border border-gray-200 rounded-xl p-2.5 text-sm text-[#2d2327] outline-none focus:border-[var(--color-pastel-red-500)] focus:ring-2 focus:ring-[var(--color-pastel-red-200)] shadow-xs"
                  {...register('paidBy')}
                >
                  {members.map((m) => (
                    <option key={m.id} value={m.name}>
                      {m.name} ({m.email})
                    </option>
                  ))}
                </select>
                <span className="text-xs text-gray-500 mt-0.5">
                  O valor será dividido igualmente entre todos os integrantes deste grupo.
                </span>
              </>
            ) : (
              <div className="space-y-2.5 bg-gray-50 p-3.5 rounded-2xl border border-gray-200">
                <div className="text-xs text-gray-600 font-medium mb-1">
                  Informe quais integrantes contribuíram para pagar esta conta e o valor de cada um. A soma deve fechar com o valor total.
                </div>
                {contributionsList.map((contrib, idx) => (
                  <div key={idx} className="flex items-center gap-2">
                    <select
                      value={contrib.memberId}
                      onChange={(e) => {
                        const newList = [...contributionsList];
                        newList[idx].memberId = e.target.value;
                        setContributionsList(newList);
                      }}
                      className="flex-1 bg-white border border-gray-200 rounded-xl p-2 text-xs font-medium text-[#2d2327] outline-none focus:border-red-400 shadow-2xs"
                    >
                      <option value="">Selecione...</option>
                      {members.map((m) => (
                        <option key={m.id} value={String(m.id)}>
                          {m.name}
                        </option>
                      ))}
                    </select>
                    <div className="relative w-28">
                      <span className="absolute left-2.5 top-1/2 -translate-y-1/2 text-xs font-bold text-gray-400">R$</span>
                      <input
                        type="number"
                        step="0.01"
                        placeholder="0.00"
                        value={contrib.amount}
                        onChange={(e) => {
                          const newList = [...contributionsList];
                          newList[idx].amount = e.target.value;
                          setContributionsList(newList);
                        }}
                        className="w-full pl-7 pr-2 py-2 bg-white border border-gray-200 rounded-xl text-xs font-bold text-[#2d2327] outline-none focus:border-red-400 shadow-2xs"
                      />
                    </div>
                    {contributionsList.length > 1 && (
                      <button
                        type="button"
                        onClick={() => {
                          const newList = contributionsList.filter((_, i) => i !== idx);
                          setContributionsList(newList);
                        }}
                        className="p-1.5 rounded-lg text-gray-400 hover:text-rose-600 hover:bg-rose-50"
                      >
                        ×
                      </button>
                    )}
                  </div>
                ))}
                <button
                  type="button"
                  onClick={() => setContributionsList([...contributionsList, { memberId: '', amount: '' }])}
                  className="text-xs font-bold text-[var(--color-pastel-red-600)] hover:text-[var(--color-pastel-red-dark)] pt-1 block"
                >
                  + Adicionar outro contribuinte
                </button>
              </div>
            )}
          </div>

          {/* Divisão de participantes do rateio */}
          <div className="flex flex-col gap-2 pt-2 border-t border-gray-100">
            <div className="flex items-center justify-between">
              <label className="text-sm font-semibold text-[#2d2327]">Quem participará da divisão desta despesa?</label>
              <button
                type="button"
                onClick={() => {
                  const nextVal = !isCustomParticipants;
                  setIsCustomParticipants(nextVal);
                  if (!nextVal) {
                    setSelectedParticipantIds(members.map(m => String(m.id)));
                  }
                }}
                className="text-xs font-bold text-[var(--color-pastel-red-600)] hover:underline flex items-center gap-1"
              >
                <Users className="w-3.5 h-3.5" />
                {isCustomParticipants ? 'Dividir entre todos os integrantes' : '+ Personalizar participantes da despesa'}
              </button>
            </div>

            {!isCustomParticipants ? (
              <div className="text-xs text-emerald-700 bg-emerald-50 border border-emerald-200 rounded-xl p-2.5 flex items-center gap-2">
                <CheckCircle className="w-4 h-4 shrink-0" />
                <span>O valor será dividido igualmente entre todos os <b>{members.length} integrantes</b> do grupo.</span>
              </div>
            ) : (
              <div className="bg-gray-50 border border-gray-200 rounded-xl p-3 space-y-2">
                <div className="text-xs text-gray-500 font-medium">
                  Selecione quem fará parte desta divisão (desmarque quem não deve pagar):
                </div>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 max-h-40 overflow-y-auto">
                  {members.map((m) => {
                    const isSelected = selectedParticipantIds.includes(String(m.id));
                    return (
                      <label
                        key={m.id}
                        className={`flex items-center gap-2.5 p-2 rounded-lg border text-xs cursor-pointer transition-all ${
                          isSelected
                            ? 'bg-white border-[var(--color-pastel-red-400)] text-[#2d2327] font-semibold shadow-2xs'
                            : 'bg-gray-100/60 border-transparent text-gray-400 line-through'
                        }`}
                      >
                        <input
                          type="checkbox"
                          checked={isSelected}
                          onChange={(e) => {
                            if (e.target.checked) {
                              setSelectedParticipantIds([...selectedParticipantIds, String(m.id)]);
                            } else {
                              setSelectedParticipantIds(selectedParticipantIds.filter(id => id !== String(m.id)));
                            }
                          }}
                          className="rounded text-[var(--color-pastel-red-600)] focus:ring-[var(--color-pastel-red-300)]"
                        />
                        <span className="truncate">{m.name}</span>
                      </label>
                    );
                  })}
                </div>
                {selectedParticipantIds.length === 0 && (
                  <p className="text-xs text-rose-600 font-semibold pt-1">
                    ⚠️ Atenção: Selecione pelo menos 1 participante para a divisão do rateio.
                  </p>
                )}
              </div>
            )}
          </div>

          <div className="pt-3 flex justify-end gap-2 border-t border-gray-100">
            <Button type="button" variant="ghost" onClick={() => setIsModalOpen(false)}>
              Cancelar
            </Button>
            <Button type="submit" variant="primary" leftIcon={<Sparkles className="w-4 h-4" />}>
              Salvar Rateio e Notificar
            </Button>
          </div>
        </form>
      </Modal>

      {/* Pix Payment Modal */}
      <Modal
        isOpen={!!pixModalDebt}
        onClose={() => setPixModalDebt(null)}
        title="Pagamento instantâneo via Pix ⚡"
      >
        {pixModalDebt && (
          <div className="space-y-5 text-sm text-[#2d2327]">
            <div className="p-3.5 rounded-2xl bg-gradient-to-r from-emerald-50 to-teal-50 border border-emerald-200 flex items-center justify-between">
              <div>
                <div className="text-xs font-semibold text-emerald-800">Transferência para</div>
                <div className="text-base font-extrabold text-[#2d2327]">
                  {pixModalDebt.creditorDisplayName || pixModalDebt.creditorUsername}
                </div>
                <div className="text-[11px] text-gray-500 mt-0.5">Ref: {pixModalDebt.expenseDescription}</div>
              </div>
              <div className="text-right">
                <div className="text-xs text-emerald-700 font-semibold">Valor a pagar</div>
                <div className="text-lg font-black text-emerald-700">
                  R$ {(pixModalDebt.amountInCents / 100).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
                </div>
              </div>
            </div>

            {loadingPixCode && (
              <div className="py-12 text-center space-y-3">
                <div className="w-8 h-8 border-4 border-emerald-500 border-t-transparent rounded-full animate-spin mx-auto" />
                <p className="text-xs font-medium text-gray-500">Gerando QR Code e chave copia e cola...</p>
              </div>
            )}

            {pixError && (
              <div className="p-4 rounded-2xl bg-rose-50 border border-rose-200 text-rose-700 text-xs flex items-start gap-2.5">
                <AlertCircle className="w-5 h-5 shrink-0 mt-0.5 text-rose-600" />
                <div>
                  <span className="font-bold block mb-1">Atenção</span>
                  {pixError}
                </div>
              </div>
            )}

            {pixCodeData && !loadingPixCode && (
              <div className="space-y-5 animate-fadeIn">
                {/* QR Code Container */}
                <div className="flex flex-col items-center justify-center p-4 bg-white rounded-3xl border-2 border-emerald-100 shadow-sm">
                  <div className="text-xs font-bold text-gray-400 uppercase tracking-wider mb-3 flex items-center gap-1.5">
                    <QrCode className="w-4 h-4 text-emerald-600" /> Escaneie o QR Code no app do banco
                  </div>
                  <div className="p-2.5 bg-white rounded-2xl border border-gray-100 shadow-inner">
                    <img
                      src={`https://api.qrserver.com/v1/create-qr-code/?size=220x220&margin=10&data=${encodeURIComponent(pixCodeData.pixCopyAndPaste)}`}
                      alt="QR Code Pix"
                      className="w-48 h-48 sm:w-52 sm:h-52 object-contain"
                    />
                  </div>
                  <div className="mt-3 text-[11px] text-gray-400 text-center">
                    Abra o app do seu banco e escolha a opção <b>Pix QR Code / Ler QR Code</b>
                  </div>
                </div>

                {/* Pix Key Details */}
                <div className="p-3.5 rounded-2xl bg-gray-50 border border-gray-200/80 space-y-2 text-xs">
                  <div className="flex justify-between">
                    <span className="text-gray-500 font-medium">Beneficiário:</span>
                    <span className="font-bold text-[#2d2327]">{pixCodeData.merchantName}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-500 font-medium">Cidade:</span>
                    <span className="font-bold text-[#2d2327]">{pixCodeData.merchantCity}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-500 font-medium">Chave Pix:</span>
                    <span className="font-mono font-bold text-emerald-700">{pixCodeData.pixKey}</span>
                  </div>
                </div>

                {/* Copia e Cola Field */}
                <div className="space-y-1.5">
                  <label className="text-xs font-bold text-gray-700 flex items-center justify-between">
                    <span>Pix Copia e Cola</span>
                    <span className="text-[10px] text-emerald-600 font-semibold">Código para colar no app</span>
                  </label>
                  <div className="flex gap-2">
                    <input
                      type="text"
                      readOnly
                      value={pixCodeData.pixCopyAndPaste}
                      className="w-full text-xs font-mono bg-gray-50 border border-gray-300 rounded-xl px-3 py-2 text-gray-600 select-all focus:outline-none focus:border-emerald-500"
                    />
                    <Button
                      type="button"
                      variant="primary"
                      size="sm"
                      className="!px-3.5 !bg-emerald-600 hover:!bg-emerald-700 !text-white shrink-0 flex items-center gap-1.5 font-bold"
                      onClick={() => {
                        navigator.clipboard.writeText(pixCodeData.pixCopyAndPaste);
                        setCopiedPix(true);
                        setTimeout(() => setCopiedPix(false), 3000);
                      }}
                    >
                      <Copy className="w-4 h-4" />
                      {copiedPix ? 'Copiado!' : 'Copiar'}
                    </Button>
                  </div>
                  {copiedPix && (
                    <p className="text-[11px] font-bold text-emerald-600 flex items-center gap-1 mt-1 animate-fadeIn">
                      <Check className="w-3.5 h-3.5" /> Código copiado para a área de transferência!
                    </p>
                  )}
                </div>

                {/* Confirm Action */}
                <div className="pt-3 border-t border-gray-200/80 flex flex-col sm:flex-row items-center justify-between gap-3">
                  <p className="text-[11px] text-gray-500 text-center sm:text-left">
                    Após efetuar o Pix no seu banco, confirme abaixo para atualizar o status do rateio.
                  </p>
                  <Button
                    type="button"
                    variant="primary"
                    className="w-full sm:w-auto !bg-gradient-to-r !from-emerald-600 !to-teal-600 hover:!from-emerald-700 hover:!to-teal-700 !text-white font-bold !shadow-md flex items-center justify-center gap-1.5 shrink-0"
                    onClick={handleConfirmPayment}
                  >
                    <Check className="w-4 h-4" /> Já realizei o pagamento!
                  </Button>
                </div>
              </div>
            )}

            <div className="pt-2 flex justify-end">
              <Button type="button" variant="ghost" size="sm" onClick={() => setPixModalDebt(null)}>
                Fechar
              </Button>
            </div>
          </div>
        )}
      </Modal>

    </div>
  );
};
