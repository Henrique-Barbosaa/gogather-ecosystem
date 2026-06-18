'use client'

import { z } from 'zod';
import { GGInput } from '../personal-form-components/GGInput';
import { GGDateInput } from '../personal-form-components/GGDateInput';
import { Form } from '../personal-form-components/Form';
import { Button } from '../ui/button';
import { CircleUserRound, KeyRound, Mail, AtSign, ShieldCheck, Loader2, AlertCircle } from 'lucide-react';
import { api } from '@/lib/api';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { AxiosError } from 'axios';
import { StandardError } from '@/app/types';

const RegisterSchema = z.object({
  username: z.string().min(6, "Mínimo de 6 caracteres"),
  email: z.email("E-mail inválido"),
  displayName: z.string().min(6, "Mínimo de 6 caracteres"),
  birthDate: z.string()
      .min(10, "Data incompleta")
      .refine((val) => {
        const [day, month, year] = val.split('/').map(Number);
        const date = new Date(year, month - 1, day);

        return date instanceof Date && !isNaN(date.getTime()) &&
               date.getDate() === day && (date.getMonth() + 1) === month;
      }, { message: "Data inválida" })
      .transform((val) => {
          const [day, month, year] = val.split('/');
          return `${year}-${month.padStart(2, '0')}-${day.padStart(2, '0')}`;
      }),
  password: z.string().min(8, "Mínimo de 8 caracteres"),
  repeatPassword: z.string(),
})
.refine((data) => data.password === data.repeatPassword, {
  error: "As senhas não coincidem",
  path: ["repeatPassword"],
})
.transform((data) => {
  const { repeatPassword, ...rest } = data;
  return rest;
});

type RegisterData = z.infer<typeof RegisterSchema>;



export function RegisterForm() {
  const router = useRouter();

  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const registerMutation = useMutation({
    mutationFn: async (data: RegisterData) => {
      return await api.post('/auth/register', data);
    },
    onSuccess: () => {
      setErrorMessage(null);
      router.push('/login');
    },
    onError: (error: AxiosError<StandardError>) => {
      const message = error.response?.data.message || "Erro inesperado!";
      setErrorMessage(message);
    }
  });

  const onSubmit = (data: RegisterData) => {
    registerMutation.mutate(data);
  }

  return (
    <Form schema={RegisterSchema} onSubmit={onSubmit} className="grid grid-cols-12 gap-x-4 center w-full">

      {errorMessage && (
        <div className="col-span-12 mb-4 p-3 bg-red-100 border border-red-300 text-red-700 rounded-md flex items-center gap-2 text-sm font-medium">
          <AlertCircle className="w-5 h-5 shrink-0" />
          <p>{errorMessage}</p>
        </div>
      )}

      <GGInput name="username" Icon={AtSign} guideText="Username" className="col-span-12" isRequired/>
      <GGInput name="email" Icon={Mail} guideText="E-mail" type="email" className="col-span-12" isRequired/>
      <GGInput name="displayName" Icon={CircleUserRound} guideText="Display name" className="col-span-9"/>
      <GGDateInput name="birthDate" label="Data de nascimento:" className="col-span-3"/>
      <GGInput name="password" Icon={KeyRound} guideText="Password" type="password" className="col-span-12" isRequired/>
      <GGInput name="repeatPassword" Icon={ShieldCheck} guideText="Repeat password" type="password" className="col-span-12" isRequired />
      <div className="flex flex-row col-span-12 mb-2">
        <Link href={'/'} className='text-gg-cyan underline hover:text-gg-cyan-dark'>Voltar ao início</Link>
      </div>
      <Button type="submit"
        disabled={registerMutation.isPending}
        className="col-start-4 col-span-6 bg-gg-beige-extralight
          border border-dotted border-gg-beige-extradark
        text-gg-beige-extradark mt-2 hover:cursor-pointer
        hover:bg-gg-beige-light">
          {registerMutation.isPending && <Loader2 className="w-4 h-4 animate-spin" />}
          {registerMutation.isPending ? "Registrando..." : "Registrar-se"}
        </Button>
    </Form>
  )
}
