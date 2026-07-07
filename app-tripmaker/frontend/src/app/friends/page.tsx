'use client';

import React, { useState, useEffect } from 'react';
import { AppLayout } from '../../components/layout';
import { Card, Button, Input, Modal, Badge } from '../../components/common';
import { Friend, getAvatarUrl } from '../../services/mockData';
import { Users, UserPlus, Search, Mail, Check, MessageCircle, Compass, Share2, Sparkles, Clock, X, UserCheck, AlertCircle } from 'lucide-react';
import { useNotifications } from '../../context/NotificationContext';
import { api } from '../../services/api';

export default function FriendsPage() {
  const [friends, setFriends] = useState<Friend[]>([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<'friends' | 'requests'>('friends');
  const [search, setSearch] = useState('');
  
  const [isInviteModalOpen, setIsInviteModalOpen] = useState(false);
  const [inviteEmail, setInviteEmail] = useState('');
  const [inviteSuccess, setInviteSuccess] = useState(false);
  const [inviteError, setInviteError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const { addNotification } = useNotifications();

  useEffect(() => {
    // Clean out any legacy mock data from browser localStorage immediately
    if (typeof window !== 'undefined') {
      localStorage.removeItem('tripmaker_friends');
    }
    fetchFriendsFromDb();
  }, []);

  const fetchFriendsFromDb = async () => {
    setLoading(true);
    try {
      const res = await api.get('/users/friends');
      if (Array.isArray(res.data)) {
        setFriends(res.data);
      } else {
        setFriends([]);
      }
    } catch (err) {
      console.error('Erro ao carregar amigos do banco de dados:', err);
      setFriends([]);
    } finally {
      setLoading(false);
    }
  };

  // Separate friends into accepted vs pending requests
  const activeFriends = friends.filter((f) => f.status !== 'pending');
  const pendingRequests = friends.filter((f) => f.status === 'pending');
  const incomingRequests = pendingRequests.filter((f) => f.requestDirection === 'incoming');
  const outgoingRequests = pendingRequests.filter((f) => f.requestDirection === 'outgoing');

  const filteredActiveFriends = activeFriends.filter(
    (f) =>
      f.name.toLowerCase().includes(search.toLowerCase()) ||
      f.email.toLowerCase().includes(search.toLowerCase())
  );

  const handleSendInvite = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!inviteEmail) return;
    setSubmitting(true);
    setInviteError('');
    setInviteSuccess(false);

    try {
      const res = await api.post('/users/friends/request', { email: inviteEmail });
      const newRequest: Friend = res.data;
      
      setFriends((prev) => [newRequest, ...prev]);
      setInviteSuccess(true);
      addNotification({
        groupId: 'network',
        groupTitle: 'Rede de Amigos',
        type: 'invite',
        title: 'Pedido de Amizade Enviado! ⏳',
        message: `Sua solicitação para ${inviteEmail} foi salva no banco de dados e aguarda aceitação.`,
      });

      setTimeout(() => {
        setInviteSuccess(false);
        setInviteEmail('');
        setIsInviteModalOpen(false);
        setActiveTab('requests');
      }, 1500);
    } catch (err: any) {
      const msg = err.response?.data?.message || err.response?.data?.error || err.message || 'Erro ao enviar pedido de amizade.';
      setInviteError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  const handleAcceptRequest = async (friendshipId: string | number, friendName: string) => {
    try {
      await api.post(`/users/friends/${friendshipId}/accept`);
      setFriends((prev) =>
        prev.map((f) =>
          f.id === friendshipId || f.id === String(friendshipId)
            ? { ...f, status: 'online' as const, requestDirection: undefined }
            : f
        )
      );
      addNotification({
        groupId: 'network',
        groupTitle: 'Rede de Amigos',
        type: 'invite',
        title: 'Pedido de Amizade Aceito! 🎉',
        message: `Você e ${friendName} agora estão conectados no banco de dados TripMaker!`,
      });
    } catch (err: any) {
      alert(err.response?.data?.message || 'Erro ao aceitar pedido de amizade no banco de dados.');
    }
  };

  const handleDeclineRequest = async (friendshipId: string | number) => {
    try {
      await api.delete(`/users/friends/${friendshipId}`);
      setFriends((prev) => prev.filter((f) => f.id !== friendshipId && f.id !== String(friendshipId)));
    } catch (err: any) {
      alert(err.response?.data?.message || 'Erro ao remover pedido de amizade no banco de dados.');
    }
  };

  const getStatusBadge = (status: Friend['status']) => {
    switch (status) {
      case 'online':
        return <Badge variant="green" className="text-[10px]">● Online</Badge>;
      case 'busy':
        return <Badge variant="red" className="text-[10px]">● Ocupado</Badge>;
      case 'pending':
        return <Badge variant="yellow" className="text-[10px]">⏳ Pendente</Badge>;
      default:
        return <Badge variant="gray" className="text-[10px]">● Offline</Badge>;
    }
  };

  return (
    <AppLayout>
      <div className="space-y-6 animate-fadeIn">
        
        {/* Header Section */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-gradient-to-r from-[var(--color-pastel-red-100)]/60 to-[var(--color-pastel-yellow-100)]/60 p-6 rounded-3xl border border-[var(--color-pastel-red-200)]/50 shadow-xs">
          <div>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-[#2d2327] flex items-center gap-2.5">
              <Users className="w-8 h-8 text-[var(--color-pastel-red-600)]" />
              Sua Rede de Amigos
            </h1>
            <p className="text-sm font-medium text-gray-600 mt-1">
              Conecte-se com parceiros cadastrados no banco de dados para dividir rateios e montar roteiros.
            </p>
          </div>
          <Button
            variant="primary"
            size="md"
            onClick={() => {
              setInviteError('');
              setInviteSuccess(false);
              setIsInviteModalOpen(true);
            }}
            leftIcon={<UserPlus className="w-5 h-5" />}
            className="shadow-md"
          >
            Convidar Amigo
          </Button>
        </div>

        {/* Tab Navigation */}
        <div className="flex items-center gap-3 border-b border-gray-200 pb-4 overflow-x-auto no-scrollbar">
          <button
            onClick={() => setActiveTab('friends')}
            className={`px-5 py-2.5 rounded-2xl font-bold text-sm flex items-center gap-2.5 transition-all shrink-0 ${
              activeTab === 'friends'
                ? 'bg-gradient-to-r from-[var(--color-pastel-red-500)] to-[var(--color-pastel-red-600)] text-white shadow-md'
                : 'bg-white text-gray-600 hover:bg-gray-50 border border-gray-200'
            }`}
          >
            <Users className="w-4 h-4" />
            <span>Meus Amigos</span>
            <span className={`px-2 py-0.5 rounded-full text-xs font-extrabold ${activeTab === 'friends' ? 'bg-white/20 text-white' : 'bg-gray-100 text-gray-600'}`}>
              {activeFriends.length}
            </span>
          </button>

          <button
            onClick={() => setActiveTab('requests')}
            className={`px-5 py-2.5 rounded-2xl font-bold text-sm flex items-center gap-2.5 transition-all shrink-0 relative ${
              activeTab === 'requests'
                ? 'bg-gradient-to-r from-[var(--color-pastel-yellow-400)] to-[var(--color-pastel-yellow-500)] text-[#2d2327] shadow-md'
                : 'bg-white text-gray-600 hover:bg-gray-50 border border-gray-200'
            }`}
          >
            <Clock className="w-4 h-4 text-[var(--color-pastel-red-600)]" />
            <span>Pedidos de Amizade</span>
            {pendingRequests.length > 0 && (
              <span className="px-2 py-0.5 rounded-full text-xs font-extrabold bg-[var(--color-pastel-red-500)] text-white animate-bounce shadow-xs">
                {pendingRequests.length}
              </span>
            )}
          </button>
        </div>

        {/* TAB 1: MEUS AMIGOS */}
        {activeTab === 'friends' && (
          <div className="space-y-4 animate-fadeIn">
            {/* Search Bar */}
            <div className="max-w-md">
              <Input
                placeholder="Buscar amigo por nome ou e-mail..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                leftIcon={<Search className="w-4 h-4" />}
              />
            </div>

            {loading ? (
              <div className="text-center py-12 text-gray-400 bg-white/50 rounded-3xl border border-dashed border-gray-200">
                Carregando amigos do banco de dados...
              </div>
            ) : filteredActiveFriends.length === 0 ? (
              <div className="col-span-full text-center py-12 text-gray-400 bg-white/50 rounded-3xl border border-dashed border-gray-200">
                {search ? `Nenhum amigo encontrado para "${search}".` : 'Sua lista de amigos está vazia no banco de dados. Clique em "Convidar Amigo" para buscar usuários reais cadastrados! ✨'}
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-2 gap-4">
                {filteredActiveFriends.map((friend) => (
                  <Card
                    key={friend.id}
                    variant="glass"
                    hoverEffect
                    className="flex items-center justify-between gap-4 p-5 border border-white/80"
                  >
                    <div className="flex items-center gap-4 min-w-0">
                      <div className="relative shrink-0">
                        <img
                          src={getAvatarUrl(friend.name, friend.avatar)}
                          alt={friend.name}
                          className="w-14 h-14 rounded-2xl object-cover border-2 border-[var(--color-pastel-yellow-400)] shadow-xs"
                        />
                        <div className="absolute -bottom-1 -right-1">
                          {getStatusBadge(friend.status)}
                        </div>
                      </div>
                      <div className="min-w-0">
                        <h3 className="font-bold text-[#2d2327] truncate text-base">
                          {friend.name}
                        </h3>
                        <p className="text-xs text-gray-500 truncate flex items-center gap-1">
                          <Mail className="w-3 h-3 text-gray-400 shrink-0" />
                          {friend.email}
                        </p>
                        <div className="flex items-center gap-2 mt-2">
                          <span className="inline-flex items-center gap-1 text-[11px] font-semibold text-[var(--color-pastel-red-700)] bg-[var(--color-pastel-red-100)]/70 px-2 py-0.5 rounded-md">
                            <Compass className="w-3 h-3" />
                            {friend.sharedTripsCount} viagens em comum
                          </span>
                        </div>
                      </div>
                    </div>

                    <div className="flex items-center gap-2 shrink-0">
                      <button
                        title="Enviar Mensagem Rápida"
                        onClick={() => alert(`Iniciando chat privado com ${friend.name}...`)}
                        className="p-2.5 rounded-xl bg-[var(--color-pastel-red-50)] text-[var(--color-pastel-red-600)] hover:bg-[var(--color-pastel-red-500)] hover:text-white transition-all shadow-2xs"
                      >
                        <MessageCircle className="w-4 h-4" />
                      </button>
                      <button
                        title="Convidar para um Grupo"
                        onClick={() => alert(`Um convite de viagem foi enviado para ${friend.name}!`)}
                        className="p-2.5 rounded-xl bg-[var(--color-pastel-yellow-50)] text-[var(--color-pastel-yellow-700)] hover:bg-[var(--color-pastel-yellow-500)] hover:text-[#2d2327] transition-all shadow-2xs font-bold text-xs flex items-center gap-1"
                      >
                        <Share2 className="w-4 h-4" />
                        <span className="hidden sm:inline">Convidar</span>
                      </button>
                    </div>
                  </Card>
                ))}
              </div>
            )}
          </div>
        )}

        {/* TAB 2: PEDIDOS DE AMIZADE */}
        {activeTab === 'requests' && (
          <div className="space-y-8 animate-fadeIn">
            
            {/* Pedidos Recebidos */}
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <h3 className="font-extrabold text-[#2d2327] text-lg flex items-center gap-2">
                  <UserCheck className="w-5 h-5 text-emerald-600" />
                  Pedidos Recebidos ({incomingRequests.length})
                </h3>
                <span className="text-xs font-semibold text-gray-500">
                  Amigos cadastrados que solicitaram conexão com você
                </span>
              </div>

              {loading ? (
                <div className="p-8 text-center bg-white/50 rounded-3xl border border-dashed border-gray-200 text-gray-400 text-sm font-medium">
                  Carregando pedidos do banco de dados...
                </div>
              ) : incomingRequests.length === 0 ? (
                <div className="p-8 text-center bg-white/50 rounded-3xl border border-dashed border-gray-200 text-gray-400 text-sm font-medium">
                  Nenhum pedido de amizade recebido no momento. Todos os dados provêm exclusivamente do banco de dados!
                </div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {incomingRequests.map((req) => (
                    <Card
                      key={req.id}
                      variant="glass"
                      hoverEffect
                      className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 p-5 border border-white/80 bg-gradient-to-r from-emerald-50/30 to-white"
                    >
                      <div className="flex items-center gap-3 min-w-0">
                        <img
                          src={getAvatarUrl(req.name, req.avatar)}
                          alt={req.name}
                          className="w-12 h-12 rounded-2xl object-cover border-2 border-emerald-400 shadow-xs shrink-0"
                        />
                        <div className="min-w-0">
                          <div className="flex items-center gap-2">
                            <h4 className="font-bold text-[#2d2327] text-base truncate">{req.name}</h4>
                            <Badge variant="green" className="text-[10px]">Novo</Badge>
                          </div>
                          <p className="text-xs text-gray-500 truncate">{req.email}</p>
                          <span className="text-[11px] text-emerald-700 font-medium mt-1 inline-block">
                            Deseja adicionar você à rede dele
                          </span>
                        </div>
                      </div>

                      <div className="flex items-center gap-2 w-full sm:w-auto justify-end shrink-0">
                        <button
                          onClick={() => handleDeclineRequest(req.id)}
                          title="Recusar pedido"
                          className="px-3 py-2 rounded-xl bg-gray-100 text-gray-600 hover:bg-red-500 hover:text-white font-bold text-xs transition-all flex items-center gap-1"
                        >
                          <X className="w-4 h-4" />
                          <span>Recusar</span>
                        </button>
                        <button
                          onClick={() => handleAcceptRequest(req.id, req.name)}
                          title="Aceitar pedido"
                          className="px-4 py-2 rounded-xl bg-emerald-500 text-white hover:bg-emerald-600 font-extrabold text-xs transition-all shadow-md flex items-center gap-1.5"
                        >
                          <Check className="w-4 h-4" />
                          <span>Aceitar Amizade</span>
                        </button>
                      </div>
                    </Card>
                  ))}
                </div>
              )}
            </div>

            {/* Pedidos Enviados */}
            <div className="space-y-4 pt-4 border-t border-gray-200">
              <div className="flex items-center justify-between">
                <h3 className="font-extrabold text-[#2d2327] text-lg flex items-center gap-2">
                  <Clock className="w-5 h-5 text-[var(--color-pastel-red-600)]" />
                  Pedidos Enviados ({outgoingRequests.length})
                </h3>
                <span className="text-xs font-semibold text-gray-500">
                  Aguardando aceitação pelo outro usuário do sistema
                </span>
              </div>

              {loading ? (
                <div className="p-8 text-center bg-white/50 rounded-3xl border border-dashed border-gray-200 text-gray-400 text-sm font-medium">
                  Carregando...
                </div>
              ) : outgoingRequests.length === 0 ? (
                <div className="p-8 text-center bg-white/50 rounded-3xl border border-dashed border-gray-200 text-gray-400 text-sm font-medium">
                  Você não enviou nenhum pedido de amizade pendente.
                </div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {outgoingRequests.map((req) => (
                    <Card
                      key={req.id}
                      variant="glass"
                      className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 p-5 border border-white/80 bg-gradient-to-r from-[var(--color-pastel-yellow-50)]/50 to-white"
                    >
                      <div className="flex items-center gap-3 min-w-0">
                        <img
                          src={getAvatarUrl(req.name, req.avatar)}
                          alt={req.name}
                          className="w-12 h-12 rounded-2xl object-cover border-2 border-[var(--color-pastel-yellow-400)] shadow-xs shrink-0"
                        />
                        <div className="min-w-0">
                          <h4 className="font-bold text-[#2d2327] text-base truncate">{req.name}</h4>
                          <p className="text-xs text-gray-500 truncate">{req.email}</p>
                          <Badge variant="yellow" className="mt-1 inline-block text-[10px]">
                            ⏳ Aguardando Aceitação do Usuário
                          </Badge>
                        </div>
                      </div>

                      <div className="flex items-center gap-2 w-full sm:w-auto justify-end shrink-0">
                        <button
                          onClick={() => handleDeclineRequest(req.id)}
                          title="Cancelar convite"
                          className="px-3 py-2 rounded-xl bg-gray-100 text-gray-600 hover:bg-red-50 hover:text-red-600 text-xs font-bold transition-all flex items-center gap-1"
                        >
                          <X className="w-4 h-4" />
                          <span>Cancelar Pedido</span>
                        </button>
                      </div>
                    </Card>
                  ))}
                </div>
              )}
            </div>

          </div>
        )}

        {/* Invite Friend Modal */}
        <Modal
          isOpen={isInviteModalOpen}
          onClose={() => setIsInviteModalOpen(false)}
          title="Convidar Novo Amigo (Banco de Dados)"
          maxWidth="sm"
        >
          <form onSubmit={handleSendInvite} className="space-y-4">
            <p className="text-sm text-gray-600 leading-relaxed">
              Digite o e-mail ou nome de usuário real de quem já está cadastrado no TripMaker para enviar um pedido de amizade oficial pelo banco de dados.
            </p>

            <Input
              label="E-mail ou Usuário Cadastrado"
              type="text"
              placeholder="exemplo@email.com"
              value={inviteEmail}
              onChange={(e) => setInviteEmail(e.target.value)}
              leftIcon={<Mail className="w-4 h-4" />}
              required
            />

            {inviteError && (
              <div className="p-3.5 rounded-xl bg-red-50 border border-red-200 text-red-800 text-xs font-bold flex items-center gap-2 animate-fadeIn">
                <AlertCircle className="w-4 h-4 text-red-600 shrink-0" />
                <span>{inviteError}</span>
              </div>
            )}

            {inviteSuccess && (
              <div className="p-3.5 rounded-xl bg-emerald-50 border border-emerald-200 text-emerald-800 text-xs font-bold flex items-center gap-2 animate-fadeIn">
                <Check className="w-4 h-4 text-emerald-600 shrink-0" />
                <span>Pedido de amizade registrado com sucesso no banco de dados! ⏳</span>
              </div>
            )}

            <div className="pt-2 flex justify-end gap-2">
              <Button type="button" variant="ghost" onClick={() => setIsInviteModalOpen(false)} disabled={submitting}>
                Cancelar
              </Button>
              <Button type="submit" variant="primary" leftIcon={<Sparkles className="w-4 h-4" />} disabled={submitting}>
                {submitting ? 'Verificando...' : 'Enviar Pedido'}
              </Button>
            </div>
          </form>
        </Modal>

      </div>
    </AppLayout>
  );
}
