'use client'

import { ReactNode } from "react";
import { useForm, FormProvider, SubmitHandler, FieldValues } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod';
import { ZodType } from "zod";

interface FormProps<T extends FieldValues> {
  schema: ZodType<T, T>;
  onSubmit: SubmitHandler<T>;
  children: ReactNode;
  className?: string;
}

export function Form<T extends FieldValues>({
  schema,
  onSubmit,
  children,
  className
} : FormProps<T>) {

  const methods = useForm<T>({
    resolver: zodResolver(schema),
  });

  return (
    <FormProvider {...methods}>
      <form
        onSubmit={methods.handleSubmit(onSubmit)}
        className={className}>
        {children}
      </form>
    </FormProvider>
  )
}
