"use client";

import { EventFormData, EventStop, Step } from "@/app/types";
import { Step1Info } from "@/components/create-group/Step1Info";
import { Step2Map } from "@/components/create-group/Step2Map";
import { Step3Share } from "@/components/create-group/Step3Share";
import StepIndicator from "@/components/create-group/StepIndicator";
import { api } from "@/lib/api";
import axios from "axios";
import { ArrowLeft, Loader2 } from "lucide-react";
import Link from "next/link";
import React, { useState } from "react";

export default function CreateRolePage() {
  const [step, setStep] = useState<Step>(1);
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [generatedInviteCode, setGeneratedInviteCode] = useState<string>("");
  const [generatedGroupId, setGeneratedGroupId] = useState<string>("");

  const [formData, setFormData] = useState<EventFormData>({
    name: "",
    date: "",
    description: "",
  });

  const [stops, setStops] = useState<EventStop[]>([]);

  const handleInputChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>,
  ): void => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmitToBackend = async (): Promise<void> => {
    setIsSubmitting(true);
    console.log("data: " + formData.date);
    const dateAsInstant = new Date(formData.date).toISOString();

    const payload = {
      name: formData.name,
      date: dateAsInstant,
      description: formData.description,
      stops: stops.map((stop, index) => ({
        ...stop,
        order: index + 1,
      })),
    };

    try {
      const response = await api.post("/groups", payload);

      const data = response.data as { inviteCode: string; externalId: string };

      if (!data.inviteCode) {
        throw new Error("O servidor não retornou um código de convite válido.");
      }

      setGeneratedInviteCode(data.inviteCode);
      setGeneratedGroupId(data.externalId);
      setStep(3);
    } catch (error) {
      if (axios.isAxiosError(error)) {
        const errorMessage =
          error.response?.data?.message ||
          "Falha na comunicação com o servidor.";
        console.error("Erro na API ao criar rolê:", errorMessage);
        alert(`Erro ao criar rolê: ${errorMessage}`);
      } else if (error instanceof Error) {
        console.error("Erro interno ao criar rolê:", error.message);
        alert(`Erro: ${error.message}`);
      } else {
        console.error("Erro desconhecido:", error);
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <main className=" h-screen bg-[#fbf2c7]/30 flex flex-col font-sans text-gray-900">
      {isSubmitting && (
        <div className="absolute inset-0 bg-white/60 backdrop-blur-sm z-100 flex flex-col items-center justify-center">
          <Loader2 className="h-10 w-10 animate-spin text-[#cc241a] mb-4" />
          <p className="text-lg font-bold text-gray-800">Criando seu rolê...</p>
        </div>
      )}
	    <header className="w-full bg-white border-b border-gray-200 py-4 px-6 sticky top-0 z-50 shadow-sm">
        <div className="flex items-center justify-between">
          
          <div className="flex-1 min-w-50">
            {step === 1 && (
              <Link
                href="/area/dashboard"
                className="flex items-center gap-2 text-gray-700 hover:text-gray-900 transition-colors group w-fit"
              >
                <div className="bg-gray-100 p-2 rounded-lg group-hover:bg-gray-200 transition-colors">
                  <ArrowLeft className="w-5 h-5" />
                </div>
                <span className="font-bold text-lg tracking-tight">
                  Voltar ao Dashboard
                </span>
              </Link>
            )}

            {step === 2 && (
              <button
                onClick={() => setStep(1)}
                className="flex items-center gap-2 text-gray-700 hover:text-gray-900 transition-colors group w-fit"
              >
                <div className="bg-gray-100 p-2 rounded-lg group-hover:bg-gray-200 transition-colors">
                  <ArrowLeft className="w-5 h-5" />
                </div>
                <span className="font-bold text-lg tracking-tight">Voltar</span>
              </button>
            )}
          </div>

          <div className="hidden md:flex items-center gap-4 text-sm font-bold text-gray-400">
            <StepIndicator current={step} />
          </div>

          <div className="flex-1 hidden md:block"></div>
        </div>
      </header>

      <section
        className={`flex-1 w-full flex flex-col ${
          step === 2
            ? "h-[calc(100vh-73px)] overflow-hidden"
            : "max-w-7xl mx-auto p-6 items-center justify-center"
        }`}
      >
        {step === 1 && (
          <Step1Info
            formData={formData}
            handleInputChange={handleInputChange}
            onNext={() => setStep(2)}
          />
        )}

        {step === 2 && (
          <Step2Map
            stops={stops}
            setStops={setStops}
            onBack={() => setStep(1)}
            onNext={handleSubmitToBackend}
          />
        )}

        {step === 3 && (
          <Step3Share
            roleName={formData.name || "Novo Rolê"}
            inviteCode={generatedInviteCode}
            groupId={generatedGroupId}
          />
        )}
      </section>
    </main>
  );
}
