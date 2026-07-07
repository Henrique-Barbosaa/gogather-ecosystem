'use client';

import React, { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { api } from '../../services/api';
import { getAvatarUrl } from '../../services/mockData';
import { useAuth } from '../../context/AuthContext';
import { AppLayout } from '../../components/layout';
import { Card, Button, Input, Badge } from '../../components/common';
import { User as UserIcon, Mail, MapPin, Calendar, Award, Shield, Edit3, Check, Sparkles, DollarSign, Users, Plane, BellRing, QrCode, ArrowUpRight, ArrowDownLeft } from 'lucide-react';

export default function ProfilePage() {
  const { user, updateProfile, logout } = useAuth();
  const [isEditing, setIsEditing] = useState(false);
  const [name, setName] = useState(user?.name || '');
  const [bio, setBio] = useState(user?.bio || '');
  const [email, setEmail] = useState(user?.email || '');
  const [saveSuccess, setSaveSuccess] = useState(false);

  const [isEditingPix, setIsEditingPix] = useState(false);
  const [pixKey, setPixKey] = useState(user?.pixKey || '');
  const [pixType, setPixType] = useState(user?.pixType || 'CPF');
  const [pixKeyInput, setPixKeyInput] = useState(user?.pixKey || '');

  useEffect(() => {
    if (user) {
      setName(user.name || '');
      setBio(user.bio || '');
      setEmail(user.email || '');
      setPixKey(user.pixKey || '');
      setPixType(user.pixType || 'CPF');
      setPixKeyInput(user.pixKey || '');
    }
  }, [user]);

  const { data: groups = [] } = useQuery<any[]>({
    queryKey: ['groups'],
    queryFn: async () => {
      const res = await api.get('/groups');
      return res.data || [];
    },
  });

  const { data: friendsList = [] } = useQuery<any[]>({
    queryKey: ['friends'],
    queryFn: async () => {
      try {
        const res = await api.get('/users/friends');
        return res.data || [];
      } catch (e) {
        return [];
      }
    },
  });

  const friendsCount = friendsList.filter((f: any) => f.status !== 'pending').length;

  const totalRateiosPagos = groups.reduce((acc: number, group: any) => {
    const groupDebts = group.debts || [];
    const paidInGroup = groupDebts
      .filter((d: any) => d.status === 'PAID' && (String(d.debtorId) === String(user?.id) || d.debtorUsername === user?.name || d.debtorUsername === user?.email || d.debtorDisplayName === user?.name))
      .reduce((sum: number, d: any) => sum + (d.amountInCents || 0) / 100, 0);
    return acc + paidInGroup;
  }, 0);

  const totalRateiosRecebidos = groups.reduce((acc: number, group: any) => {
    const groupDebts = group.debts || [];
    const receivedInGroup = groupDebts
      .filter((d: any) => d.status === 'PAID' && (String(d.creditorId) === String(user?.id) || d.creditorUsername === user?.name || d.creditorUsername === user?.email || d.creditorDisplayName === user?.name))
      .reduce((sum: number, d: any) => sum + (d.amountInCents || 0) / 100, 0);
    return acc + receivedInGroup;
  }, 0);

  if (!user) return null;

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.patch('/users/profile', { name, bio, email });
      const res = await api.get('/users/me');
      if (res.data) {
        updateProfile(res.data);
      } else {
        updateProfile({ name, bio, email });
      }
      setIsEditing(false);
      setSaveSuccess(true);
      setTimeout(() => setSaveSuccess(false), 3000);
    } catch (err) {
      console.error('Erro ao atualizar perfil no banco de dados:', err);
      alert('Não foi possível salvar o perfil no banco de dados.');
    }
  };

  const handleSavePix = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.patch('/users/pix', {
        pixKey: pixKeyInput,
        pixType: pixType,
        merchantName: user.name || 'Beneficiário',
        merchantCity: 'Cidade Principal'
      });
      const res = await api.get('/users/me');
      if (res.data) {
        updateProfile(res.data);
        setPixKey(res.data.pixKey || pixKeyInput);
      } else {
        setPixKey(pixKeyInput);
        updateProfile({ pixKey: pixKeyInput, pixType });
      }
      setIsEditingPix(false);
      setSaveSuccess(true);
      setTimeout(() => setSaveSuccess(false), 3000);
    } catch (err: any) {
      console.error('Erro ao salvar Pix no banco de dados:', err);
      const msg = err.response?.data?.message || 'Não foi possível cadastrar a chave Pix no banco de dados.';
      alert(msg);
    }
  };

  return (
    <AppLayout>
      <div className="max-w-4xl mx-auto space-y-6">
        
        {/* Profile Banner */}
        <div className="relative rounded-3xl overflow-hidden shadow-lg bg-gradient-to-r from-[var(--color-pastel-red-400)] via-[var(--color-pastel-red-300)] to-[var(--color-pastel-yellow-300)] p-8 sm:p-12 text-white">
          <div className="absolute -bottom-10 -right-10 w-64 h-64 bg-white/20 rounded-full blur-2xl pointer-events-none" />
          <div className="relative z-10 flex flex-col sm:flex-row items-center sm:items-end gap-6">
            <div className="relative group">
              <img
                src={getAvatarUrl(user.name, user.avatar)}
                alt={user.name}
                className="w-28 h-28 sm:w-36 sm:h-36 rounded-3xl object-cover border-4 border-white shadow-xl bg-white"
              />
              <span className="absolute bottom-2 right-2 p-1.5 rounded-full bg-[var(--color-pastel-yellow-400)] text-[#2d2327] shadow-sm" title="Viajante Nível Ouro">
                <Sparkles className="w-4 h-4" />
              </span>
            </div>
            
            <div className="flex-1 text-center sm:text-left mb-2">
              <div className="flex flex-wrap items-center justify-center sm:justify-start gap-2 mb-1">
                <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight text-white drop-shadow-xs">
                  {user.name}
                </h1>
                <Badge variant="yellow" className="text-xs py-0.5 px-2.5 font-bold">
                  PRO Traveler
                </Badge>
              </div>
              <p className="text-white/90 text-sm font-medium flex items-center justify-center sm:justify-start gap-1.5">
                <Mail className="w-4 h-4 shrink-0" />
                {user.email}
              </p>
              <p className="text-white/90 text-xs mt-2 font-light italic max-w-lg">
                "{user.bio || 'Sem biografia definida.'}"
              </p>
            </div>

            <div className="shrink-0">
              <Button
                variant={isEditing ? 'secondary' : 'outline'}
                size="md"
                onClick={() => setIsEditing(!isEditing)}
                className={isEditing ? '' : 'bg-white/90 text-[var(--color-pastel-red-700)] border-white hover:bg-white font-bold shadow-md'}
                leftIcon={<Edit3 className="w-4 h-4" />}
              >
                {isEditing ? 'Cancelar Edição' : 'Editar Perfil'}
              </Button>
            </div>
          </div>
        </div>

        {saveSuccess && (
          <div className="p-4 rounded-2xl bg-emerald-50 border border-emerald-200 text-emerald-800 text-sm font-semibold flex items-center gap-2 animate-fadeIn">
            <Check className="w-5 h-5 text-emerald-600" />
            <span>Perfil atualizado com sucesso! ✨</span>
          </div>
        )}

        {/* Edit Form Card */}
        {isEditing && (
          <Card variant="glass" className="p-6 border-2 border-[var(--color-pastel-red-300)] animate-scaleUp">
            <h3 className="text-lg font-bold text-[#2d2327] mb-4 flex items-center gap-2">
              <Edit3 className="w-5 h-5 text-[var(--color-pastel-red-600)]" />
              Editar Dados Pessoais
            </h3>
            <form onSubmit={handleSave} className="space-y-4">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <Input
                  label="Nome de Exibição"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="Seu nome"
                />
                <Input
                  label="E-mail principal"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="seu@email.com"
                />
              </div>
              <div className="flex flex-col gap-1">
                <label className="text-sm font-semibold text-[#2d2327]">Biografia / Status</label>
                <textarea
                  value={bio}
                  onChange={(e) => setBio(e.target.value)}
                  rows={2}
                  className="w-full bg-white/90 border border-gray-200 rounded-xl p-3 text-sm text-[#2d2327] outline-none focus:border-[var(--color-pastel-red-500)] focus:ring-2 focus:ring-[var(--color-pastel-red-200)] shadow-xs"
                  placeholder="Escreva um pouco sobre seu estilo de viagem..."
                />
              </div>
              <div className="flex justify-end gap-3 pt-2">
                <Button type="button" variant="ghost" onClick={() => setIsEditing(false)}>
                  Cancelar
                </Button>
                <Button type="submit" variant="primary" leftIcon={<Check className="w-4 h-4" />}>
                  Salvar Alterações
                </Button>
              </div>
            </form>
          </Card>
        )}

        {/* Travel Stats Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <Card variant="pastel-red" className="p-5 flex items-center gap-4">
            <div className="p-3 rounded-2xl bg-white text-[var(--color-pastel-red-600)] shadow-xs">
              <Plane className="w-6 h-6" />
            </div>
            <div>
              <span className="text-2xl font-extrabold text-[#2d2327]">{groups.length}</span>
              <p className="text-xs font-semibold text-gray-500">Viagens Realizadas</p>
            </div>
          </Card>

          <Card variant="pastel-yellow" className="p-5 flex items-center gap-4">
            <div className="p-3 rounded-2xl bg-white text-[var(--color-pastel-yellow-700)] shadow-xs">
              <Users className="w-6 h-6" />
            </div>
            <div>
              <span className="text-2xl font-extrabold text-[#2d2327]">{friendsCount}</span>
              <p className="text-xs font-semibold text-gray-500">Amigos Conectados</p>
            </div>
          </Card>

          <Card variant="glass" className="p-5 flex items-center gap-4 border border-rose-100">
            <div className="p-3 rounded-2xl bg-rose-100 text-rose-700 shadow-xs">
              <ArrowDownLeft className="w-6 h-6" />
            </div>
            <div>
              <span className="text-xl font-extrabold text-[#2d2327]">
                R$ {totalRateiosPagos.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
              </span>
              <p className="text-xs font-semibold text-gray-500">Rateios Pagos</p>
            </div>
          </Card>

          <Card variant="glass" className="p-5 flex items-center gap-4 border border-emerald-100">
            <div className="p-3 rounded-2xl bg-emerald-100 text-emerald-700 shadow-xs">
              <ArrowUpRight className="w-6 h-6" />
            </div>
            <div>
              <span className="text-xl font-extrabold text-[#2d2327]">
                R$ {totalRateiosRecebidos.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
              </span>
              <p className="text-xs font-semibold text-gray-500">Rateios Recebidos</p>
            </div>
          </Card>
        </div>

        {/* PIX Key Management Card */}
        <Card variant="glass" className="p-6 border-2 border-emerald-200">
          <div className="flex items-center justify-between border-b border-gray-100 pb-3 mb-4">
            <div className="flex items-center gap-2">
              <div className="p-2 bg-emerald-100 text-emerald-700 rounded-xl">
                <QrCode className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-lg font-bold text-[#2d2327]">Chave Pix para Rateios 💸</h3>
                <p className="text-xs text-gray-500">Sua chave para receber reembolsos dos amigos em viagens</p>
              </div>
            </div>
            {pixKey && !isEditingPix && (
              <Badge variant="green" className="text-xs px-2.5 py-1 flex items-center gap-1 font-bold">
                <Check className="w-3.5 h-3.5" /> Cadastrada
              </Badge>
            )}
          </div>

          {isEditingPix ? (
            <form onSubmit={handleSavePix} className="space-y-4 animate-fadeIn">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Tipo de Chave Pix</label>
                  <select
                    value={pixType}
                    onChange={(e) => setPixType(e.target.value)}
                    className="w-full bg-white/90 border border-gray-200 rounded-xl px-3 py-2.5 text-sm text-[#2d2327] outline-none focus:border-emerald-500 focus:ring-2 focus:ring-emerald-200 shadow-xs"
                  >
                    <option value="CPF">CPF / CNPJ</option>
                    <option value="CELULAR">Celular</option>
                    <option value="EMAIL">E-mail</option>
                    <option value="ALEATORIA">Chave Aleatória (EVP)</option>
                  </select>
                </div>
                <Input
                  label="Chave Pix"
                  value={pixKeyInput}
                  onChange={(e) => setPixKeyInput(e.target.value)}
                  placeholder="Digite sua chave Pix aqui..."
                />
              </div>
              <div className="flex justify-end gap-3 pt-2">
                <Button type="button" variant="ghost" onClick={() => setIsEditingPix(false)}>
                  Cancelar
                </Button>
                <Button type="submit" variant="primary" className="bg-emerald-600 hover:bg-emerald-700 text-white border-none" leftIcon={<Check className="w-4 h-4" />}>
                  Salvar Chave Pix
                </Button>
              </div>
            </form>
          ) : (
            <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 bg-emerald-50/50 p-4 rounded-2xl border border-emerald-100">
              <div>
                <span className="text-xs font-bold text-emerald-800 uppercase tracking-wider">
                  {pixType || 'Chave Não Cadastrada'}
                </span>
                <p className="text-base font-extrabold text-[#2d2327] mt-0.5 font-mono">
                  {pixKey || 'Nenhuma chave cadastrada no momento.'}
                </p>
                <p className="text-xs text-gray-500 mt-1">
                  Esta chave será exibida para os integrantes dos seus grupos quando você tiver saldo a receber no acerto de contas.
                </p>
              </div>
              <Button
                variant="outline"
                size="sm"
                onClick={() => {
                  setPixKeyInput(pixKey);
                  setIsEditingPix(true);
                }}
                className="bg-white border-emerald-300 text-emerald-700 hover:bg-emerald-100 font-bold shrink-0"
                leftIcon={<Edit3 className="w-4 h-4" />}
              >
                {pixKey ? 'Alterar Chave Pix' : 'Cadastrar Pix'}
              </Button>
            </div>
          )}
        </Card>

        {/* Account & Notification Preferences */}
        <Card variant="glass" className="p-6">
          <h3 className="text-lg font-bold text-[#2d2327] mb-4 flex items-center gap-2 border-b border-gray-100 pb-3">
            <BellRing className="w-5 h-5 text-[var(--color-pastel-red-600)]" />
            Configurações e Preferências
          </h3>
          <div className="space-y-4 text-sm">
            <div className="flex items-center justify-between py-2 border-b border-gray-100">
              <div>
                <p className="font-bold text-[#2d2327]">Alertas de Novos Rateios</p>
                <p className="text-xs text-gray-500">Receber notificações sempre que uma conta for dividida</p>
              </div>
              <input type="checkbox" defaultChecked className="w-5 h-5 rounded text-[var(--color-pastel-red-500)] focus:ring-[var(--color-pastel-red-300)]" />
            </div>

            <div className="flex items-center justify-between py-2 border-b border-gray-100">
              <div>
                <p className="font-bold text-[#2d2327]">Mensagens do Chat no Grupo</p>
                <p className="text-xs text-gray-500">Notificar em tempo real sobre conversas no roadmap</p>
              </div>
              <input type="checkbox" defaultChecked className="w-5 h-5 rounded text-[var(--color-pastel-red-500)] focus:ring-[var(--color-pastel-red-300)]" />
            </div>

            <div className="flex items-center justify-between py-2">
              <div>
                <p className="font-bold text-rose-600">Sair da Sessão</p>
                <p className="text-xs text-gray-500">Desconectar deste dispositivo e voltar à tela de login</p>
              </div>
              <Button variant="danger" size="sm" onClick={logout}>
                Sair
              </Button>
            </div>
          </div>
        </Card>

      </div>
    </AppLayout>
  );
}
