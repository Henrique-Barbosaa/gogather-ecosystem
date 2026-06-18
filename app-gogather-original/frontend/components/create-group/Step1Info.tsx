import { EventFormData } from "@/app/types";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { AlignLeft, ArrowRight, Calendar, MapPin } from "lucide-react";
import React from "react";
interface Step1InfoProps {
  formData: EventFormData;
  handleInputChange: (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>,
  ) => void;
  onNext: () => void;
}

export function Step1Info({
  formData,
  handleInputChange,
  onNext,
}: Step1InfoProps) {
  const isValid = formData.name.trim() !== "" && formData.date !== "";

  return (
    <div className="w-full max-w-xl animate-in fade-in slide-in-from-bottom-4 duration-500">
      <div className="text-center mb-8">
        <h1 className="text-3xl font-bold mb-2">Qual é o plano?</h1>
        <p className="text-gray-500 text-lg">
          Defina os detalhes básicos do seu rolê.
        </p>
      </div>

      <Card className="shadow-2xl border-gray-100 rounded-3xl overflow-hidden bg-white">
        <CardContent className="p-8 space-y-6">
          <div className="space-y-2">
            <Label
              htmlFor="name"
              className="text-gray-700 font-bold text-base tracking-wide"
            >
              Nome do Rolê
            </Label>
            <div className="relative">
              <MapPin className="absolute left-3 top-1/2 -translate-y-1/2 w-6 h-6 text-gg-cyan pointer-events-none" />
              <Input
                id="name" 
                name="name" 
                value={formData.name} 
                onChange={handleInputChange}
                placeholder="Ex: Rolê no barzinho sexta..."
                className="pl-14 py-7 bg-gray-50 border border-gray-200 focus:outline-none focus-visible:ring-2 focus-visible:ring-[#458588] focus-visible:border-[#458588] focus-visible:ring-offset-0 rounded-xl text-lg transition-all shadow-sm"
              />
            </div>
          </div>

          <div className="space-y-2">
            <Label
              htmlFor="date"
              className="text-gray-700 font-bold text-base tracking-wide"
            >
              Data e Hora
            </Label>
            <div className="relative">
              <Calendar className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gg-cyan pointer-events-none" />
              <Input
                id="date" 
                type="datetime-local" 
                name="date" 
                value={formData.date} 
                onChange={handleInputChange}
                className="pl-14 py-7 bg-gray-50 border border-gray-200 focus:outline-none focus-visible:ring-2 focus-visible:ring-[#458588] focus-visible:border-[#458588] focus-visible:ring-offset-0 rounded-xl text-lg w-full block transition-all shadow-sm"
              />
            </div>
          </div>

          <div className="space-y-2">
            <Label
              htmlFor="description"
              className="text-gray-700 font-bold text-base tracking-wide"
            >
              <span>Descrição</span>
              <span className="text-gray-500">(opcional)</span>
            </Label>
            <div className="relative">
              <AlignLeft className="absolute left-3 top-4 w-5 h-5 text-gg-cyan pointer-events-none" />
              <Textarea
                id="description" 
                name="description" 
                value={formData.description} 
                onChange={handleInputChange}
                placeholder="Regras, o que levar, ponto de encontro exato..."
                className="pl-14 py-5 min-h-[140px] bg-gray-50 border border-gray-200 focus:outline-none focus-visible:ring-2 focus-visible:ring-[#458588] focus-visible:border-[#458588] focus-visible:ring-offset-0 rounded-xl text-lg resize-none transition-all shadow-sm"
              />
            </div>
          </div>

          <Button
            onClick={onNext}
            disabled={!isValid}
            className="w-full py-6 mt-4 text-lg font-bold bg-[#cc241a] hover:bg-[#a81d15] disabled:bg-gray-300 text-white rounded-xl shadow-md transition-all flex items-center justify-center gap-2"
          >
            Definir Rota <ArrowRight className="w-5 h-5" />
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}
