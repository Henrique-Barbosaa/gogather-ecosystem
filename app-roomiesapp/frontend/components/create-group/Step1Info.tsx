import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { AlignLeft, ArrowRight, Home } from "lucide-react";
import React from "react";

interface Step1InfoProps {
  formData: { name: string; description: string };
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
  // Validação agora só exige o nome da casa
  const isValid = formData.name.trim() !== "";

  return (
    <div className="w-full max-w-xl animate-in fade-in slide-in-from-bottom-4 duration-500">
      <div className="text-center mb-8">
        <h1 className="text-3xl font-bold mb-2">Detalhes da Casa</h1>
        <p className="text-gray-500 text-lg">
          Defina as informações básicas da sua república ou apartamento.
        </p>
      </div>

      <Card className="shadow-2xl border-gray-100 rounded-3xl overflow-hidden bg-white">
        <CardContent className="p-8 space-y-6">
          <div className="space-y-2">
            <Label
              htmlFor="name"
              className="text-gray-700 font-bold text-base tracking-wide"
            >
              Nome da Casa
            </Label>
            <div className="relative">
              <Home className="absolute left-3 top-1/2 -translate-y-1/2 w-6 h-6 text-ra-purple pointer-events-none" />
              <Input
                id="name" 
                name="name" 
                value={formData.name} 
                onChange={handleInputChange}
                placeholder="Ex: República do Pão de Queijo..."
                className="pl-14 py-7 bg-gray-50 border border-gray-200 focus:outline-none focus-visible:ring-2 focus-visible:ring-[#8724df] focus-visible:border-[#8724df] focus-visible:ring-offset-0 rounded-xl text-lg transition-all shadow-sm"
              />
            </div>
          </div>

          <div className="space-y-2">
            <Label
              htmlFor="description"
              className="text-gray-700 font-bold text-base tracking-wide"
            >
              <span>Regras Básicas / Descrição</span>
              <span className="text-gray-500 ml-2">(opcional)</span>
            </Label>
            <div className="relative">
              <AlignLeft className="absolute left-3 top-4 w-5 h-5 text-ra-purple pointer-events-none" />
              <Textarea
                id="description" 
                name="description" 
                value={formData.description} 
                onChange={handleInputChange}
                placeholder="Regra #1: Quem cozinha não lava a louça..."
                className="pl-14 py-5 min-h-[140px] bg-gray-50 border border-gray-200 focus:outline-none focus-visible:ring-2 focus-visible:ring-[#8724df] focus-visible:border-[#8724df] focus-visible:ring-offset-0 rounded-xl text-lg resize-none transition-all shadow-sm"
              />
            </div>
          </div>

          <Button
            onClick={onNext}
            disabled={!isValid}
            className="w-full py-6 mt-4 text-lg font-bold bg-[#8724df] hover:bg-[#6419a8] disabled:bg-gray-300 text-white rounded-xl shadow-md transition-all flex items-center justify-center gap-2"
          >
            Criar Casa e Gerar Convite <ArrowRight className="w-5 h-5" />
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}