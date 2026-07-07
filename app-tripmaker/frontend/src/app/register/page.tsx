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
import { Mail, Lock, User, Plane, ArrowRight, AlertCircle, CheckCircle2, Calendar } from 'lucide-react';

const registerSchema = z
  .object({
    username: z
      .string()
      .min(3, 'O username deve ter no mínimo 3 caracteres')
      .regex(/^[a-zA-Z0-9_-]+$/, 'Apenas letras, números, _ ou -'),
    name: z
      .string()
      .min(3, 'O nome deve ter no mínimo 3 caracteres'),
    email: z
      .string()
      .min(1, 'O e-mail é obrigatório')
      .email('Digite um endereço de e-mail válido'),
    birthDate: z
      .string()
      .min(1, 'A data de nascimento é obrigatória'),
    password: z
      .string()
      .min(6, 'A senha deve ter no mínimo 6 caracteres'),
    confirmPassword: z
      .string()
      .min(1, 'Confirme sua senha'),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: 'As senhas não coincidem',
    path: ['confirmPassword'],
  });

type RegisterFormValues = z.infer<typeof registerSchema>;

export default function RegisterPage() {
  const { login } = useAuth();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
  });

  const mutation = useMutation({
    mutationFn: async (data: RegisterFormValues) => {
      // 1. Post to register
      await api.post('/auth/register', {
        username: data.username,
        email: data.email,
        password: data.password,
        displayName: data.name,
        birthDate: data.birthDate,
      });
      
      // 2. Automatically log in after registration
      const loginResponse = await api.post('/auth/login', {
        username: data.username,
        password: data.password
      });

      const { accessToken } = loginResponse.data;

      // 3. Fetch user profile
      const userProfileResponse = await api.get('/auth/me', {
        headers: {
          Authorization: `Bearer ${accessToken}`
        }
      });

      return {
        token: accessToken,
        user: userProfileResponse.data
      };
    },
    onSuccess: (data) => {
      if (data && data.token && data.user) {
        login(data.token, data.user);
      }
    },
    onError: (err: any) => {
      setErrorMessage(
        err?.response?.data?.message || 'Erro ao realizar cadastro. Tente novamente.'
      );
    },
  });

  const onSubmit = (data: RegisterFormValues) => {
    setErrorMessage(null);
    mutation.mutate(data);
  };

  return (
    <div className="min-h-screen w-full flex items-center justify-center p-4 sm:p-6 bg-gradient-to-br from-[#fff0f3] via-[#fffbfa] to-[#fffeea] relative overflow-hidden">
      
      {/* Decorative background blur elements */}
      <div className="absolute top-10 right-10 w-80 h-80 bg-[var(--color-pastel-red-200)]/40 rounded-full blur-3xl pointer-events-none animate-pulse" />
      <div className="absolute bottom-10 left-10 w-72 h-72 bg-[var(--color-pastel-yellow-200)]/50 rounded-full blur-3xl pointer-events-none" />

      <div className="w-full max-w-md z-10 animate-scaleUp">
        
        {/* Logo Header */}
        <div className="text-center mb-6 flex flex-col items-center">
          <div className="inline-flex items-center justify-center w-14 h-14 rounded-2xl bg-gradient-to-tr from-[var(--color-pastel-red-500)] to-[var(--color-pastel-yellow-500)] text-white shadow-md mb-2">
            <Plane className="w-7 h-7" />
          </div>
          <h1 className="text-2xl font-extrabold tracking-tight text-[#2d2327]">
            Crie sua conta no <span className="text-[var(--color-pastel-red-600)]">TripMaker</span>
          </h1>
          <p className="text-xs font-medium text-gray-500 mt-1">
            Junte-se à comunidade de viajantes mais engajada!
          </p>
        </div>

        {/* Register Card */}
        <Card variant="glass" className="p-8 shadow-xl border-2 border-white/80">
          {errorMessage && (
            <div className="mb-6 p-3.5 rounded-2xl bg-rose-50 border border-rose-200 text-rose-600 text-xs font-semibold flex items-center gap-2">
              <AlertCircle className="w-4 h-4 shrink-0" />
              <span>{errorMessage}</span>
            </div>
          )}

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-3.5">
            <Input
              label="Nome Completo"
              type="text"
              placeholder="Ex: Ana Silva"
              leftIcon={<User className="w-4 h-4" />}
              error={errors.name?.message}
              {...register('name')}
            />

            <Input
              label="Nome de Usuário (Username)"
              type="text"
              placeholder="Ex: ana_silva"
              leftIcon={<User className="w-4 h-4" />}
              error={errors.username?.message}
              {...register('username')}
            />

            <Input
              label="E-mail"
              type="email"
              placeholder="ana@tripmaker.com"
              leftIcon={<Mail className="w-4 h-4" />}
              error={errors.email?.message}
              {...register('email')}
            />

            <Input
              label="Data de Nascimento"
              type="date"
              leftIcon={<Calendar className="w-4 h-4" />}
              error={errors.birthDate?.message}
              {...register('birthDate')}
            />

            <Input
              label="Senha"
              type="password"
              placeholder="Mínimo de 6 caracteres"
              leftIcon={<Lock className="w-4 h-4" />}
              error={errors.password?.message}
              {...register('password')}
            />

            <Input
              label="Confirmar Senha"
              type="password"
              placeholder="Repita sua senha"
              leftIcon={<CheckCircle2 className="w-4 h-4" />}
              error={errors.confirmPassword?.message}
              {...register('confirmPassword')}
            />

            <div className="pt-3">
              <Button
                type="submit"
                variant="primary"
                size="lg"
                className="w-full shadow-lg shadow-[var(--color-pastel-red-400)]/40 text-base font-bold"
                isLoading={mutation.isPending}
                rightIcon={<ArrowRight className="w-5 h-5" />}
              >
                Cadastrar e Começar
              </Button>
            </div>
          </form>

          <div className="mt-6 text-center text-sm font-medium text-gray-600 border-t border-gray-100 pt-5">
            Já tem uma conta?{' '}
            <Link
              href="/login"
              className="text-[var(--color-pastel-red-600)] font-bold hover:underline ml-1"
            >
              Fazer Login
            </Link>
          </div>
        </Card>

      </div>
    </div>
  );
}
