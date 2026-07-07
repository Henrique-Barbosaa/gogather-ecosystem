'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { useMutation } from '@tanstack/react-query';
import { api } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import { Button, Input, Card } from '../../components/common';
import { Mail, Lock, Plane, ArrowRight, Sparkles, AlertCircle } from 'lucide-react';

const loginSchema = z.object({
  email: z
    .string()
    .min(1, 'O e-mail é obrigatório')
    .email('Digite um endereço de e-mail válido'),
  password: z
    .string()
    .min(6, 'A senha deve ter no mínimo 6 caracteres'),
});

type LoginFormValues = z.infer<typeof loginSchema>;

export default function LoginPage() {
  const { login } = useAuth();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: '',
      password: '',
    },
  });

  const mutation = useMutation({
    mutationFn: async (data: LoginFormValues) => {
      // 1. Post login credentials
      const response = await api.post('/auth/login', {
        username: data.email, // Can be username or email
        password: data.password,
      });

      const { accessToken } = response.data;

      // 2. Fetch logged-in user details
      const userProfileResponse = await api.get('/auth/me', {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      });

      return {
        token: accessToken,
        user: userProfileResponse.data,
      };
    },
    onSuccess: (data) => {
      if (data && data.token && data.user) {
        login(data.token, data.user);
      }
    },
    onError: (err: any) => {
      setErrorMessage(
        err?.response?.data?.message || 'Falha ao autenticar. Verifique suas credenciais.'
      );
    },
  });

  const onSubmit = (data: LoginFormValues) => {
    setErrorMessage(null);
    mutation.mutate(data);
  };

  return (
    <div className="min-h-screen w-full flex items-center justify-center p-4 sm:p-6 bg-gradient-to-br from-[#fff0f3] via-[#fffbfa] to-[#fffeea] relative overflow-hidden">
      
      {/* Decorative background blur elements */}
      <div className="absolute top-10 left-10 w-72 h-72 bg-[var(--color-pastel-red-200)]/40 rounded-full blur-3xl pointer-events-none animate-pulse" />
      <div className="absolute bottom-10 right-10 w-80 h-80 bg-[var(--color-pastel-yellow-200)]/50 rounded-full blur-3xl pointer-events-none" />

      <div className="w-full max-w-md z-10 animate-scaleUp">
        
        {/* Logo Header */}
        <div className="text-center mb-8 flex flex-col items-center">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-3xl bg-gradient-to-tr from-[var(--color-pastel-red-500)] to-[var(--color-pastel-yellow-500)] text-white shadow-lg shadow-[var(--color-pastel-red-300)]/50 mb-3 transform -rotate-6 hover:rotate-0 transition-transform duration-300">
            <Plane className="w-8 h-8" />
          </div>
          <h1 className="text-3xl font-extrabold tracking-tight bg-gradient-to-r from-[var(--color-pastel-red-700)] via-[var(--color-pastel-red-600)] to-[var(--color-pastel-yellow-600)] bg-clip-text text-transparent">
            TripMaker
          </h1>
          <p className="text-sm font-medium text-gray-500 mt-1 flex items-center justify-center gap-1">
            Organize suas viagens e divida contas facilmente <Sparkles className="w-4 h-4 text-[var(--color-pastel-yellow-600)]" />
          </p>
        </div>

        {/* Login Card */}
        <Card variant="glass" className="p-8 shadow-xl border-2 border-white/80">
          <h2 className="text-xl font-bold text-[#2d2327] mb-6 text-center">
            Acesse sua conta
          </h2>

          {errorMessage && (
            <div className="mb-6 p-3.5 rounded-2xl bg-rose-50 border border-rose-200 text-rose-600 text-xs font-semibold flex items-center gap-2 animate-fadeIn">
              <AlertCircle className="w-4 h-4 shrink-0" />
              <span>{errorMessage}</span>
            </div>
          )}

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <Input
              label="E-mail"
              type="email"
              placeholder="pedro@tripmaker.com"
              leftIcon={<Mail className="w-4 h-4" />}
              error={errors.email?.message}
              {...register('email')}
            />

            <Input
              label="Senha"
              type="password"
              placeholder="••••••••"
              leftIcon={<Lock className="w-4 h-4" />}
              error={errors.password?.message}
              {...register('password')}
            />

            <div className="flex items-center justify-between text-xs font-semibold text-[var(--color-pastel-red-600)] pt-1">
              <label className="flex items-center gap-2 text-gray-600 font-normal cursor-pointer">
                <input type="checkbox" defaultChecked className="rounded text-[var(--color-pastel-red-500)] focus:ring-[var(--color-pastel-red-300)]" />
                Lembrar de mim
              </label>
              <a href="#" className="hover:underline">
                Esqueceu a senha?
              </a>
            </div>

            <div className="pt-2">
              <Button
                type="submit"
                variant="primary"
                size="lg"
                className="w-full shadow-lg shadow-[var(--color-pastel-red-400)]/40 text-base font-bold"
                isLoading={mutation.isPending}
                rightIcon={<ArrowRight className="w-5 h-5" />}
              >
                Entrar na Plataforma
              </Button>
            </div>
          </form>

          {/* Quick Demo Credentials hint */}
          <div className="mt-6 p-3 rounded-xl bg-[var(--color-pastel-yellow-50)] border border-[var(--color-pastel-yellow-200)] text-center">
            <span className="text-xs font-bold text-[var(--color-pastel-yellow-700)] block mb-0.5">
              💡 Modo de Demonstração Interativa
            </span>
            <span className="text-[11px] text-gray-600">
              As credenciais acima já estão preenchidas. Clique em <b>Entrar</b> para testar!
            </span>
          </div>

          <div className="mt-8 text-center text-sm font-medium text-gray-600 border-t border-gray-100 pt-6">
            Ainda não tem uma conta?{' '}
            <Link
              href="/register"
              className="text-[var(--color-pastel-red-600)] font-bold hover:underline ml-1"
            >
              Cadastre-se grátis
            </Link>
          </div>
        </Card>

      </div>
    </div>
  );
}
