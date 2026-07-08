"use client";

import React, { useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { api } from "@/lib/api";
import { toast } from "sonner";

interface CreateChoreDialogProps {
  isOpen: boolean;
  onClose: () => void;
  groupId: number;
  onSuccess: () => void;
}

export const CreateChoreDialog = ({ isOpen, onClose, groupId, onSuccess }: CreateChoreDialogProps) => {
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [dueDate, setDueDate] = useState("");

  const createChoreMutation = useMutation({
    mutationFn: async () => {
      await api.post(`/api/households/${groupId}/chores`, {
        title,
        description: description || undefined,
        dueDate: dueDate || undefined,
      });
    },
    onSuccess: () => {
      toast.success("Tarefa criada com sucesso!");
      onSuccess();
      onClose();
      setTitle("");
      setDescription("");
      setDueDate("");
    },
    onError: (error) => {
      console.error("Erro ao criar tarefa:", error);
      toast.error("Erro ao criar tarefa.");
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    createChoreMutation.mutate();
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Adicionar Nova Tarefa/Regra</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4 mt-4">
          <div>
            <label className="text-sm font-medium text-gray-700">Título</label>
            <Input
              required
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="Ex: Lavar a louça, Tirar o lixo..."
            />
          </div>
          <div>
            <label className="text-sm font-medium text-gray-700">Descrição (Opcional)</label>
            <Input
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Detalhes da tarefa..."
            />
          </div>
          <div>
            <label className="text-sm font-medium text-gray-700">Data (Opcional)</label>
            <Input type="date" value={dueDate} onChange={(e) => setDueDate(e.target.value)} />
          </div>
          <Button
            type="submit"
            disabled={createChoreMutation.isPending}
            className="w-full bg-[#8724df] hover:bg-[#6c1cb3] text-white"
          >
            {createChoreMutation.isPending ? "Salvando..." : "Salvar Tarefa"}
          </Button>
        </form>
      </DialogContent>
    </Dialog>
  );
};
