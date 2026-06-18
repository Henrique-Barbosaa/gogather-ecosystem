'use client'

import * as React from "react";
import { Input } from "@/components/ui/input";
import { PatternFormat } from "react-number-format";
import { Button } from "@/components/ui/button";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import { Calendar } from "@/components/ui/calendar";
import { CalendarSearch } from 'lucide-react';
import { useFormContext, Controller } from "react-hook-form";

interface DateInputProps {
  className?: string;
  label: string;
  name: string;
  isRequired?: boolean;
}

export function GGDateInput({ label, name, isRequired = false, className }: DateInputProps) {
  const { control, formState: { errors } } = useFormContext();
  const errorMessage = errors[name]?.message as string | undefined;

  const validateDate = (values: { value: string }) => {
    const { value: val } = values;

    if (val.length === 0) return true;

    const day = parseInt(val.substring(0, 2), 10);
    const month = parseInt(val.substring(2, 4), 10);

    if (val.length >= 1 && parseInt(val[0]) > 3) return false;
    if (val.length >= 2 && (day > 31 || day === 0)) return false;
    if (val.length >= 3 && parseInt(val[2]) > 1) return false;
    if (val.length >= 4 && (month > 12 || month === 0)) return false;

    return true;
  };

  const parseStringToDate = (dateStr: string) => {
    if (!dateStr || dateStr.length < 10) return undefined;
    const [day, month, year] = dateStr.split('/');
    return new Date(Number(year), Number(month) - 1, Number(day));
  };

  return (
    <div className={`flex flex-col my-2 ${className}`}>
      <p className="text-xs text-gg-beige-extradark">
        {label}{isRequired && <span>*</span>}
      </p>

      <Controller
        control={control}
        name={name}
        render={({ field }) => {
          const currentValue = field.value || "";

          return (
            <div className="relative flex flex-row w-full">
              <PatternFormat
                format="##/##/####"
                mask="_"
                placeholder="__/__/____"
                customInput={Input}
                isAllowed={validateDate}
                onValueChange={(values) => {
                  field.onChange(values.formattedValue);
                }}
                value={currentValue}
                onBlur={field.onBlur}
                className="bg-gg-beige-extralight text-center h-8 w-full rounded-r-none border-gg-beige-extradark border-dotted"
              />
              <Popover>
                <PopoverTrigger asChild>
                  <Button variant="outline" className="bg-gg-beige-extralight rounded-l-none h-8 border-l-0 border-gg-beige-extradark border-dotted">
                    <CalendarSearch className="text-gg-beige-extradark" />
                  </Button>
                </PopoverTrigger>
                <PopoverContent className="flex flex-row justify-center">
                  <Calendar
                    mode="single"
                    selected={parseStringToDate(currentValue)}
                    onSelect={(date) => {
                      if (date) {
                        const day = date.getDate().toString().padStart(2, '0');
                        const month = (date.getMonth() + 1).toString().padStart(2, '0');
                        const year = date.getFullYear().toString();
                        field.onChange(`${day}/${month}/${year}`);
                      }
                    }}
                    className="rounded-lg"
                    captionLayout="dropdown"
                  />
                </PopoverContent>
              </Popover>
            </div>
          );
        }}
      />
      {errorMessage && <span className="text-red-500 text-sm mt-1">{errorMessage}</span>}
    </div>
  );
}
