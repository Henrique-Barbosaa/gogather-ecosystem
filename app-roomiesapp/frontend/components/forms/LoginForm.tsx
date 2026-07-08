'use client'

import { z } from 'zod';
import { GGInput } from '../personal-form-components/GGInput';
import { Form } from '../personal-form-components/Form';
import { Button } from '../ui/button';
import { KeyRound, AtSign, Loader2, AlertCircle } from 'lucide-react';
import { api } from '@/lib/api';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useState } from 'react';
import { AxiosError } from 'axios';
import { StandardError } from '@/app/types';
import { useMutation } from '@tanstack/react-query';

const loginSchema = z.object({
  username: z.string().min(6, "Mínimo de 6 caracteres"),
  password: z.string().min(8, "Mínimo de 8 caracteres")
});

type LoginData = z.infer<typeof loginSchema>;

export function LoginForm() {
  const router = useRouter();

  const [errorMessage, setErrorMessage] = useState<null | string>('');

  const loginMutation = useMutation({
    mutationFn: async (data: LoginData) => {
      return await api.post('/auth/login', data);
    },
    onSuccess: () => {
      setErrorMessage(null);
      router.push('/area/dashboard');
      router.refresh();
    },
    onError: (error: AxiosError<StandardError>) => {
      const message = error.response?.data.message || "Usuário ou senha incorretos.";
      setErrorMessage(message);
    }
  });

  const onSubmit = async (data: LoginData) => {
    loginMutation.mutate(data);
  }

  return (
    <Form schema={loginSchema} onSubmit={onSubmit} className="grid grid-cols-12 gap-x-4 center w-full">

      {errorMessage && (
        <div className="col-span-12 mb-4 p-3 bg-red-100 border border-red-300 text-red-700 rounded-md flex items-center gap-2 text-sm font-medium">
          <AlertCircle className="w-5 h-5 shrink-0" />
          <p>{errorMessage}</p>
        </div>
      )}

      <GGInput name='username' type='text' guideText='Username' Icon={AtSign} className='col-span-12'/>
      <GGInput name='password' type='password' guideText='Password' Icon={KeyRound} className='col-span-12'/>
      <div className="flex flex-row justify-around col-span-12 mb-2">
        <Link href={'/'} className='text-ra-green underline hover:text-ra-green-dark'>Esqueceu a senha?</Link>
        <Link href={'/'} className='text-ra-green underline hover:text-ra-green-dark'>Voltar ao início</Link>
      </div>
      <Button type="submit"
        disabled={loginMutation.isPending}
        className="col-start-4 col-span-6 bg-ra-blue-extralight
          border border-dotted border-ra-blue-extradark
        text-ra-blue-extradark mt-2 hover:cursor-pointer
        hover:bg-ra-blue-light">
          {loginMutation.isPending && <Loader2 className="w-4 h-4 animate-spin" />}
          {loginMutation.isPending ? "Entrando..." : "Login"}
      </Button>
    </Form>
  )
}
