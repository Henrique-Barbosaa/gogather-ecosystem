  'use client'

  import { Input } from "@/components/ui/input";
  import { ElementType } from "react";
  import { Checkbox } from "@/components/ui/checkbox";
  import { useFormContext, Controller } from "react-hook-form";

  interface GGInputProps {
    className?: string;
    guideText?: string;
    name: string;
    type?: "text" | "password" | "email" | "checkbox";
    isRequired?: boolean
    Icon?: ElementType;
  }

  export function GGInput({ guideText, name, type = "text", isRequired = false, Icon, className }: GGInputProps) {
    const {register, control, formState: {errors}} = useFormContext();
    const errorMessage = errors[name]?.message as string | undefined;

    if(type === "checkbox"){
      return (
        <div className={`flex flex-col my-2 ${className}`}>
          <div className="flex flex-col gap-2">
            <Controller
              control={control}
              name={name}
              render={({ field }) => (
                <Checkbox
                  checked={field.value}
                  onCheckedChange={field.onChange}
                  className="border border-ra-blue-extradark selection:bg-ra-blue-extradark rounded-full
                    data-[state=checked]:bg-ra-blue-extradark data-[state=checked]:border-ra-blue-extradark [&_svg]:hidden"/>
              )}/>
            <p className="text-ra-blue-extradark">{guideText}{guideText && isRequired && <span>*</span>}</p>
          </div>
          {errorMessage && <span className="text-red-500 text-sm mt-1">{errorMessage}</span>}
        </div>
      )
    }
    return (
      <div className={`relative my-2 flex flex-col ${className}`}>
        <div className="relative w-full">
          {Icon && (<Icon className="absolute left-3 top-1/2 -translate-y-1/2 w-6 h-6 text-ra-blue-extradark"/>)}
          <Input
            {...register(name)}
            placeholder={`${guideText ? guideText : ""} ${isRequired ? "*" : ""}`}
            type={type}
            className="pl-12 bg-ra-blue-extralight border border-dotted border-ra-blue-extradark h-12"/>
        </div>
        {errorMessage && <span className="text-red-500 text-sm mt-1">{errorMessage}</span>}
      </div>
    )
  }
