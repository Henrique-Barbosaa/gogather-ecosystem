'use client';

import React, { useState, useEffect } from 'react';
import { TravelGroup, Member, getAvatarUrl } from '../../../../services/mockData';
import { Card, Button, Input, Modal, Badge } from '../../../../components/common';
import { Users, UserPlus, Mail, Shield, ShieldCheck, User, Sparkles, Check, UserCheck, HeartHandshake, Loader2 } from 'lucide-react';
import { useNotifications } from '../../../../context/NotificationContext';
import { api } from '../../../../services/api';

interface MembersTabProps {
  group: TravelGroup;
  onUpdateMembers: (newMembers: Member[]) => void;
}

export const MembersTab: React.FC<MembersTabProps> = ({ group, onUpdateMembers }) => {
  const [members, setMembers] = useState<Member[]>(group.members || []);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [inviteEmail, setInviteEmail] = useState('');
  const [inviteName, setInviteName] = useState('');
  const [inviteSuccess, setInviteSuccess] = useState(false);
  
  // New state for user's friend list from database
  const [friends, setFriends] = useState<any[]>([]);
  const [loadingFriends, setLoadingFriends] = useState(false);

  const { addNotification } = useNotifications();

  useEffect(() => {
    if (isModalOpen) {
      setLoadingFriends(true);
      api.get('/users/friends')
        .then((res) => {
          if (Array.isArray(res.data)) {
            setFriends(res.data);
          } else {
            setFriends([]);
          }
        })
        .catch((err) => {
          console.error('Erro ao buscar amigos do banco de dados:', err);
          setFriends([]);
        })
        .finally(() => setLoadingFriends(false));
    }
  }, [isModalOpen]);

  const availableFriends = friends.filter(
    (f) =>
      f.status !== 'pending' &&
      !members.some(
        (m) =>
          m.email?.toLowerCase() === f.email?.toLowerCase() ||
          m.name?.toLowerCase() === f.name?.toLowerCase()
      )
  );

  const handleInviteFriend = async (friend: any) => {
    try {
      const response = await api.post(`/groups/${group.id}/members`, {
        userId: Number(friend.friendUserId || friend.id),
        email: friend.email,
      });
      const newMember: Member = response.data;

      const updated = [...members, newMember];
      setMembers(updated);
      onUpdateMembers(updated);
      setInviteSuccess(true);

      addNotification({
        groupId: group.id,
        groupTitle: group.title,
        type: 'invite',
        title: 'Amigo Adicionado à Viagem! ✈️',
        message: `${newMember.name} foi adicionado direto da sua lista de amigos à viagem "${group.title}".`,
      });

      setTimeout(() => {
        setInviteSuccess(false);
        setIsModalOpen(false);
      }, 1500);
    } catch (err: any) {
      alert(err.response?.data?.message || err.message || 'Erro ao adicionar amigo ao grupo de viagem.');
    }
  };

  const handleInvite = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!inviteEmail) return;

    try {
      const response = await api.post(`/groups/${group.id}/members`, {
        email: inviteEmail,
      });
      const newMember: Member = response.data;

      const updated = [...members, newMember];
      setMembers(updated);
      onUpdateMembers(updated);
      setInviteSuccess(true);

      addNotification({
        groupId: group.id,
        groupTitle: group.title,
        type: 'invite',
        title: 'Novo Integrante no Grupo! ✈️',
        message: `${newMember.name} foi adicionado à viagem "${group.title}".`,
      });

      setTimeout(() => {
        setInviteSuccess(false);
        setInviteEmail('');
        setInviteName('');
        setIsModalOpen(false);
      }, 1500);
    } catch (err: any) {
      alert(err.response?.data?.message || err.message || 'Erro ao adicionar pessoa ao grupo. Verifique se o e-mail está cadastrado no sistema.');
    }
  };

  return (
    <div className="space-y-6 animate-fadeIn">
      
      {/* Header Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-gradient-to-r from-[var(--color-pastel-red-50)] to-[var(--color-pastel-yellow-50)] p-6 rounded-3xl border border-[var(--color-pastel-red-200)] shadow-2xs">
        <div>
          <h3 className="text-xl font-extrabold text-[#2d2327] flex items-center gap-2">
            <Users className="w-6 h-6 text-[var(--color-pastel-red-600)]" />
            Integrantes da Viagem ({members.length})
          </h3>
          <p className="text-xs text-gray-600 mt-0.5">
            Quem está participando deste roteiro e ajudando na divisão de despesas.
          </p>
        </div>
        <Button
          variant="primary"
          onClick={() => {
            setInviteSuccess(false);
            setIsModalOpen(true);
          }}
          leftIcon={<UserPlus className="w-4 h-4" />}
          className="shadow-md"
        >
          Convidar Integrante
        </Button>
      </div>

      {/* Members Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-2 gap-4">
        {members.map((member) => (
          <Card
            key={member.id}
            variant="glass"
            hoverEffect
            className="flex items-center justify-between gap-4 p-5 border-2 border-white/80"
          >
            <div className="flex items-center gap-4 min-w-0">
              <img
                src={getAvatarUrl(member.name, member.avatar)}
                alt={member.name}
                className="w-14 h-14 rounded-2xl object-cover border-2 border-[var(--color-pastel-yellow-400)] shadow-xs shrink-0"
              />
              <div className="min-w-0">
                <div className="flex items-center gap-2">
                  <h4 className="font-bold text-[#2d2327] truncate text-base">
                    {member.name}
                  </h4>
                  {member.role === 'admin' ? (
                    <Badge variant="yellow" className="text-[10px] py-0 px-2 flex items-center gap-1">
                      <ShieldCheck className="w-3 h-3" /> Admin
                    </Badge>
                  ) : (
                    <Badge variant="gray" className="text-[10px] py-0 px-2">
                      Membro
                    </Badge>
                  )}
                </div>
                <p className="text-xs text-gray-500 truncate flex items-center gap-1 mt-0.5">
                  <Mail className="w-3 h-3 text-gray-400 shrink-0" />
                  {member.email}
                </p>
                <div className="mt-2 flex items-center gap-1.5 text-[11px] font-semibold text-[var(--color-pastel-red-700)]">
                  <span className="w-2 h-2 rounded-full bg-emerald-500 inline-block animate-pulse" />
                  <span>Participando ativamente dos rateios</span>
                </div>
              </div>
            </div>

            <div className="shrink-0">
              <button
                onClick={() => alert(`Enviando lembrete de roteiro para ${member.name}...`)}
                title="Enviar Lembrete"
                className="p-2.5 rounded-xl bg-[var(--color-pastel-red-50)] text-[var(--color-pastel-red-600)] hover:bg-[var(--color-pastel-red-500)] hover:text-white transition-all shadow-2xs text-xs font-bold"
              >
                Lembrar
              </button>
            </div>
          </Card>
        ))}
      </div>

      {/* Invite Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Convidar Novo Integrante"
        maxWidth="md"
      >
        <div className="space-y-6">
          <p className="text-sm text-gray-600 leading-relaxed">
            Adicione um companheiro à sua viagem de duas formas: chame direto da sua lista de amigos cadastrados ou convide por e-mail!
          </p>

          {inviteSuccess && (
            <div className="p-3.5 rounded-2xl bg-emerald-50 border border-emerald-200 text-emerald-800 text-xs font-bold flex items-center gap-2 animate-fadeIn shadow-xs">
              <Check className="w-4 h-4 text-emerald-600 shrink-0" />
              <span>Integrante adicionado ao grupo com sucesso! ✨</span>
            </div>
          )}

          {/* Section 1: Meus Amigos Adicionados */}
          <div className="space-y-3 bg-gradient-to-r from-[var(--color-pastel-yellow-50)]/60 to-white p-4 rounded-2xl border border-[var(--color-pastel-yellow-200)]/60 shadow-2xs">
            <div className="flex items-center justify-between">
              <h4 className="font-extrabold text-[#2d2327] text-sm flex items-center gap-1.5">
                <HeartHandshake className="w-4 h-4 text-[var(--color-pastel-red-600)]" />
                <span>Meus Amigos Adicionados</span>
              </h4>
              <span className="text-[11px] font-semibold text-gray-500">
                Chame com 1 clique
              </span>
            </div>

            {loadingFriends ? (
              <div className="p-6 text-center text-gray-400 text-xs font-medium flex items-center justify-center gap-2">
                <Loader2 className="w-4 h-4 animate-spin text-[var(--color-pastel-red-500)]" />
                <span>Carregando seus amigos do banco de dados...</span>
              </div>
            ) : availableFriends.length === 0 ? (
              <div className="p-4 text-center text-gray-500 text-xs font-medium bg-white/70 rounded-xl border border-dashed border-gray-200">
                {friends.filter(f => f.status !== 'pending').length === 0 ? (
                  <span>Você ainda não possui amigos confirmados no banco de dados. Utilize o formulário abaixo para convidar por e-mail!</span>
                ) : (
                  <span>Todos os seus amigos cadastrados já fazem parte desta viagem! 🎉</span>
                )}
              </div>
            ) : (
              <div className="max-h-52 overflow-y-auto pr-1 space-y-2 no-scrollbar">
                {availableFriends.map((friend) => (
                  <div
                    key={friend.id}
                    className="flex items-center justify-between p-2.5 rounded-xl bg-white border border-gray-200/80 hover:border-[var(--color-pastel-red-300)] transition-all shadow-2xs"
                  >
                    <div className="flex items-center gap-3 min-w-0">
                      <img
                        src={getAvatarUrl(friend.name, friend.avatar)}
                        alt={friend.name}
                        className="w-9 h-9 rounded-xl object-cover border border-[var(--color-pastel-yellow-400)] shrink-0 shadow-2xs"
                      />
                      <div className="min-w-0">
                        <h5 className="font-bold text-xs text-[#2d2327] truncate">{friend.name}</h5>
                        <p className="text-[11px] text-gray-500 truncate flex items-center gap-1">
                          <Mail className="w-3 h-3 text-gray-400 shrink-0" />
                          {friend.email}
                        </p>
                      </div>
                    </div>
                    <button
                      type="button"
                      onClick={() => handleInviteFriend(friend)}
                      className="px-3 py-1.5 rounded-xl bg-gradient-to-r from-[var(--color-pastel-red-500)] to-[var(--color-pastel-red-600)] text-white hover:opacity-90 font-extrabold text-xs transition-all shadow-xs shrink-0 flex items-center gap-1"
                    >
                      <UserPlus className="w-3.5 h-3.5" />
                      <span>Chamar</span>
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Elegant Divider */}
          <div className="relative flex py-1 items-center">
            <div className="flex-grow border-t border-gray-200"></div>
            <span className="flex-shrink mx-3 text-xs font-bold text-gray-400 uppercase tracking-wider">Ou Convidar Por E-mail</span>
            <div className="flex-grow border-t border-gray-200"></div>
          </div>

          {/* Section 2: Convidar Nova Pessoa */}
          <form onSubmit={handleInvite} className="space-y-4">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <Input
                label="Nome ou Apelido"
                placeholder="Ex: Lucas Santos"
                value={inviteName}
                onChange={(e) => setInviteName(e.target.value)}
                leftIcon={<User className="w-4 h-4" />}
                required
              />

              <Input
                label="E-mail do Convidado"
                type="email"
                placeholder="lucas@tripmaker.com"
                value={inviteEmail}
                onChange={(e) => setInviteEmail(e.target.value)}
                leftIcon={<Mail className="w-4 h-4" />}
                required
              />
            </div>

            <div className="pt-2 flex justify-end gap-2 border-t border-gray-100">
              <Button type="button" variant="ghost" onClick={() => setIsModalOpen(false)}>
                Fechar
              </Button>
              <Button type="submit" variant="primary" leftIcon={<Sparkles className="w-4 h-4" />}>
                Adicionar Integrante
              </Button>
            </div>
          </form>
        </div>
      </Modal>

    </div>
  );
};
