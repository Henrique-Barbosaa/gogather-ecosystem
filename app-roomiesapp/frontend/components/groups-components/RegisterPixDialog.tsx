"use client";

import { useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { api } from "@/lib/api";
import { toast } from "sonner";

interface PixInfoResponse {
  externalId: string;
  pixKey: string;
  merchantName: string;
  merchantCity: string;
}

interface RegisterPixDialogProps {
  isOpen: boolean;
  onClose: () => void;
  current?: PixInfoResponse | null;
  onSuccess?: () => void;
}

/**
 * Cadastro/edição da chave Pix do usuário logado.
 *
 * É pré-requisito para criar contas: o backend (RoomiesBillingService) exige que
 * o pagador tenha uma chave Pix cadastrada — sem ela, POST /billing/.../bills
 * retorna 400. O cadastro é feito em PATCH /users/pix.
 */
export function RegisterPixDialog({ isOpen, onClose, current, onSuccess }: RegisterPixDialogProps) {
  const queryClient = useQueryClient();
  const [pixKey, setPixKey] = useState(current?.pixKey ?? "");
  const [merchantName, setMerchantName] = useState(current?.merchantName ?? "");
  const [merchantCity, setMerchantCity] = useState(current?.merchantCity ?? "");

  const savePixMutation = useMutation({
    mutationFn: async () => {
      await api.patch("/users/pix", {
        pixKey: pixKey.trim(),
        merchantName: merchantName.trim(),
        merchantCity: merchantCity.trim(),
      });
    },
    onSuccess: () => {
      toast.success("Chave Pix salva com sucesso!");
      queryClient.invalidateQueries({ queryKey: ["my-pix"] });
      onSuccess?.();
      onClose();
    },
    onError: (error: unknown) => {
      const message = (error as { response?: { data?: { message?: string } } })?.response?.data
        ?.message;
      toast.error(message || "Não foi possível salvar a chave Pix.");
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    savePixMutation.mutate();
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>{current ? "Editar chave Pix" : "Cadastrar chave Pix"}</DialogTitle>
        </DialogHeader>
        <p className="text-sm text-gray-500 -mt-1">
          É necessário ter uma chave Pix cadastrada para ser pagador de uma conta e receber os rateios.
        </p>
        <form onSubmit={handleSubmit} className="space-y-4 mt-2">
          <div>
            <label className="text-sm font-medium text-gray-700">Chave Pix</label>
            <Input
              required
              value={pixKey}
              onChange={(e) => setPixKey(e.target.value)}
              placeholder="CPF, e-mail, telefone ou chave aleatória"
            />
          </div>
          <div>
            <label className="text-sm font-medium text-gray-700">Nome do beneficiário</label>
            <Input
              required
              value={merchantName}
              onChange={(e) => setMerchantName(e.target.value)}
              placeholder="Ex: João da Silva"
            />
          </div>
          <div>
            <label className="text-sm font-medium text-gray-700">Cidade do beneficiário</label>
            <Input
              required
              value={merchantCity}
              onChange={(e) => setMerchantCity(e.target.value)}
              placeholder="Ex: Campina Grande"
            />
          </div>
          <Button
            type="submit"
            disabled={savePixMutation.isPending}
            className="w-full bg-ra-purple hover:bg-ra-purple-dark text-white"
          >
            {savePixMutation.isPending ? "Salvando..." : "Salvar chave Pix"}
          </Button>
        </form>
      </DialogContent>
    </Dialog>
  );
}
