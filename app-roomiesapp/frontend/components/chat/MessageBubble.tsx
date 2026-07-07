import React, { useState } from "react";
import { ChatMessage } from "@/types/chat";
import { cn } from "@/lib/utils";
import { Sparkles, MapPin, Loader2, Check } from "lucide-react";
import { api } from "@/lib/api";
import { toast } from "sonner";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter, DialogClose } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";

interface MessageBubbleProps {
  message: ChatMessage;
  isCurrentUser: boolean;
  onVote: (optionId: number) => void;
  totalMembers: number;
  isAdmin?: boolean;
  groupId?: string;
  onStopAdded?: () => void;
  addedPlaceIds?: string[];
}

import { useAuth } from "@/context/AuthContext";

export const MessageBubble: React.FC<MessageBubbleProps> = ({ message, isCurrentUser, onVote, totalMembers, isAdmin, groupId, onStopAdded, addedPlaceIds = [] }) => {
  const { user } = useAuth();
  const [isAddingPlace, setIsAddingPlace] = useState<string | null>(null);
  const [confirmPlace, setConfirmPlace] = useState<{ id: string, name: string } | null>(null);
  
  const time = new Date(message.createdAt).toLocaleTimeString([], {
    hour: "2-digit",
    minute: "2-digit",
  });

  const isAi = message.type === "AI";

  const handleAddToRoute = (e: React.MouseEvent, placeId: string, placeName: string) => {
    e.stopPropagation();
    setConfirmPlace({ id: placeId, name: placeName });
  };

  const confirmAddPlace = async () => {
    if (!groupId || !confirmPlace) return;
    
    try {
      setIsAddingPlace(confirmPlace.id);
      await api.post(`/groups/${groupId}/stops/from-place/${confirmPlace.id}`);
      toast.success(`"${confirmPlace.name}" foi adicionado ao roteiro!`);
      onStopAdded?.();
    } catch (error) {
      console.error("Erro ao adicionar parada:", error);
      toast.error("Erro ao adicionar parada ao roteiro.");
    } finally {
      setIsAddingPlace(null);
      setConfirmPlace(null);
    }
  };

  return (
    <div
      className={cn(
        "flex w-full mb-4 group",
        isCurrentUser ? "justify-end" : "justify-start"
      )}
    >
      <div
        className={cn(
          "max-w-[85%] sm:max-w-[75%] rounded-2xl px-4 py-3 flex flex-col gap-1 shadow-sm transition-all relative overflow-hidden",
          isAi
            ? "bg-white text-gray-800 rounded-bl-none border border-[#cc241a]/30 shadow-[0_4px_15px_-3px_rgba(204,36,26,0.1)]"
            : isCurrentUser
              ? "bg-[#458588] text-white rounded-br-none border-none"
              : "bg-white text-gray-800 rounded-bl-none border border-gray-100"
        )}
      >
        {!isCurrentUser && (
          <span className={cn("text-xs font-bold flex items-center gap-1", isAi ? "text-[#cc241a]" : "text-[#458588]")}>
            {message.senderName} {isAi && <Sparkles className="w-3 h-3 text-[#cc241a]" />}
          </span>
        )}
        <p className="text-sm whitespace-pre-wrap break-words leading-relaxed">{message.content}</p>
        
        {message.poll && (
          <div className="mt-3 flex flex-col gap-2">
            <div className="text-xs font-semibold text-gray-500/80 mb-1 border-b border-gray-200/50 pb-1 flex justify-between items-center">
              <span>ENQUETE</span>
              <span className="font-normal text-gray-400">
                {message.poll.options.reduce((sum, opt) => sum + opt.votes, 0)} votos
              </span>
            </div>
            {[...message.poll.options]
              .sort((a, b) => a.id - b.id)
              .map((option) => {
              const hasVoted = user?.id ? option.voterIds.includes(user.id) : false;
              const percentage = totalMembers > 0 ? Math.round((option.votes / totalMembers) * 100) : 0;
              const isAddingThis = isAddingPlace === option.placeId;
              
              return (
                <div key={option.id} className="flex gap-1 items-center">
                  <button
                    onClick={() => onVote(option.id)}
                    className={cn(
                      "flex-1 relative overflow-hidden flex justify-between items-center border rounded-lg px-3 py-2 text-sm transition-all text-left group/option",
                      hasVoted 
                        ? "border-[#458588] shadow-sm" 
                        : "border-gray-200 hover:border-gray-300"
                    )}
                  >
                    <div 
                      className={cn(
                        "absolute inset-y-0 left-0 transition-all duration-500 ease-out",
                        hasVoted ? "bg-[#458588]/15" : "bg-gray-100"
                      )}
                      style={{ width: `${percentage}%` }}
                    />

                    <span className={cn("relative z-10 flex-1 font-medium pr-2", hasVoted ? "text-[#458588]" : "text-gray-700")}>
                      {option.text}
                    </span>
                    
                    <div className="relative z-10 flex items-center gap-2 shrink-0 ml-2">
                      {totalMembers > 0 && (
                        <span className={cn("text-xs font-semibold", hasVoted ? "text-[#458588]" : "text-gray-500")}>
                          {percentage}%
                        </span>
                      )}
                      <span className={cn(
                        "text-xs font-bold px-2 py-0.5 rounded-full",
                        hasVoted ? "bg-[#458588] text-white" : "bg-gray-100 text-gray-600 group-hover/option:bg-gray-200"
                      )}>
                        {option.votes}
                      </span>
                    </div>
                  </button>
                  
                  {isAdmin && option.placeId && (
                    addedPlaceIds.includes(option.placeId) ? (
                      <button
                        onClick={(e) => handleAddToRoute(e, option.placeId!, option.text)}
                        disabled={isAddingPlace !== null}
                        title="Já adicionado! Clique para adicionar novamente"
                        className="shrink-0 flex items-center justify-center p-2 border border-green-200 bg-green-50 hover:bg-green-100 rounded-lg text-green-600 transition-colors disabled:opacity-50"
                      >
                        {isAddingThis ? <Loader2 className="w-4 h-4 animate-spin" /> : <Check className="w-4 h-4" />}
                      </button>
                    ) : (
                      <button
                        onClick={(e) => handleAddToRoute(e, option.placeId!, option.text)}
                        disabled={isAddingPlace !== null}
                        title="Adicionar ao roteiro"
                        className="shrink-0 p-2 border border-gray-200 rounded-lg hover:bg-[#cc241a]/10 hover:border-[#cc241a]/30 hover:text-[#cc241a] text-gray-400 transition-colors disabled:opacity-50"
                      >
                        {isAddingThis ? <Loader2 className="w-4 h-4 animate-spin" /> : <MapPin className="w-4 h-4" />}
                      </button>
                    )
                  )}
                </div>
              );
            })}
          </div>
        )}

        <span
          className={cn(
            "text-[10px] self-end mt-1 font-medium",
            isCurrentUser ? "text-white/70" : "text-gray-400"
          )}
        >
          {time}
        </span>
      </div>

      <Dialog open={confirmPlace !== null} onOpenChange={(open) => !open && setConfirmPlace(null)}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2 text-xl text-[#458588]">
              <MapPin className="w-5 h-5" />
              Adicionar ao Roteiro
            </DialogTitle>
            <DialogDescription className="text-base pt-2">
              Tem certeza que deseja adicionar <strong className="text-gray-900">{confirmPlace?.name}</strong> ao roteiro oficial do grupo?
            </DialogDescription>
          </DialogHeader>
          <DialogFooter className="mt-4">
            <DialogClose asChild>
              <Button variant="outline" disabled={isAddingPlace !== null} className="text-gray-600">
                Cancelar
              </Button>
            </DialogClose>
            <Button 
              onClick={confirmAddPlace} 
              disabled={isAddingPlace !== null}
              className="bg-[#cc241a] text-white hover:bg-[#cc241a]/90 border-none shadow-sm"
            >
              {isAddingPlace ? <Loader2 className="w-4 h-4 mr-2 animate-spin" /> : null}
              Confirmar
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
};
