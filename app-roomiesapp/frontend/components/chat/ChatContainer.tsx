"use client";

import React, { useEffect, useState, useCallback } from "react";
import { useChatWebSocket } from "@/lib/hooks/useChatWebSocket";
import { MessageList } from "./MessageList";
import { ChatInput } from "./ChatInput";
import { PaginatedChatHistory, GroupDetails } from "@/types/chat";
import { Loader2, Users, MapPin } from "lucide-react";
import { useAuth } from "@/context/AuthContext";
import { api } from "@/lib/api";

interface ChatContainerProps {
  inviteCode: string;
}

export const ChatContainer: React.FC<ChatContainerProps> = ({ inviteCode }) => {
  const { messages, setMessages, typingUsers, isConnected, sendMessage, sendTypingEvent } = useChatWebSocket(inviteCode);
  
  const [isLoadingHistory, setIsLoadingHistory] = useState(true);
  const [groupDetails, setGroupDetails] = useState<GroupDetails | null>(null);
  const [prevInviteCode, setPrevInviteCode] = useState(inviteCode);
  const { user } = useAuth();
  
  // Reseta os estados se o código de convite mudar (Evita o Cascading Render)
  if (inviteCode !== prevInviteCode) {
    setPrevInviteCode(inviteCode);
    setIsLoadingHistory(true);
    setGroupDetails(null);
  }

  const handleVote = useCallback(async (optionId: number) => {
    try {
      await api.post(`/groups/${inviteCode}/polls/options/${optionId}/vote`);
    } catch (error) {
      console.error("Erro ao votar na enquete:", error);
    }
  }, [inviteCode]);

  useEffect(() => {
    if (!user) return;

    let isMounted = true;

    const loadChatData = async () => {
      try {
        const groupResponse = await api.get<GroupDetails>(`/groups/${inviteCode}`);
        if (isMounted) {
          setGroupDetails(groupResponse.data);
        }

        const historyResponse = await api.get<PaginatedChatHistory>(`/groups/${inviteCode}/chat?page=0&size=50`);
        if (isMounted) {
          const reversedMessages = [...historyResponse.data.content].reverse();
          setMessages((prev) => {
            const existingIds = new Set(prev.map(m => m.id ?? `${m.createdAt}-${m.senderName}`));
            const newHistory = reversedMessages.filter(
              m => !existingIds.has(m.id ?? `${m.createdAt}-${m.senderName}`)
            );
            return [...newHistory, ...prev];
          });
        }
      } catch (error) {
        console.error("Erro ao carregar dados do chat:", error);
      } finally {
        if (isMounted) {
          setIsLoadingHistory(false);
        }
      }
    };

    loadChatData();

    return () => {
      isMounted = false;
    };
  }, [inviteCode, user, setMessages]); 

  const members = groupDetails?.members ?? [];

  return (
    <div className="flex flex-col h-full w-full border rounded-xl shadow-sm overflow-hidden bg-background">
      {/* Header */}
      <div className="p-4 border-b bg-card flex items-center justify-between">
        <div className="flex flex-col gap-0.5">
          <h3 className="font-semibold text-card-foreground text-lg leading-tight">
            {groupDetails?.name ?? "Carregando..."}
          </h3>
          <div className="flex items-center gap-2 text-xs text-muted-foreground">
            {groupDetails?.address && (
              <>
                <span className="flex items-center gap-1">
                  <MapPin className="h-3 w-3" />
                  {groupDetails.address}
                </span>
                <span>•</span>
              </>
            )}
            {members.length > 0 && (
              <>
                <span className="flex items-center gap-1">
                  <Users className="h-3 w-3" />
                  {members.length} morador{members.length !== 1 ? "es" : ""}
                </span>
                <span>•</span>
              </>
            )}
            <span className="flex items-center gap-1">
              <span
                className={`w-2 h-2 rounded-full ${
                  isConnected ? "bg-ra-green" : "bg-red-500"
                }`}
              />
              {isConnected ? "Online" : "Conectando..."}
            </span>
          </div>
        </div>
      </div>

      {/* Body */}
      <div className="flex-1 relative overflow-hidden flex flex-col bg-muted/10">
        {isLoadingHistory && (
          <div className="absolute inset-0 flex items-center justify-center bg-background/50 z-10 backdrop-blur-sm">
            <Loader2 className="h-8 w-8 animate-spin text-ra-green" />
          </div>
        )}

        <MessageList
          messages={messages}
          typingUsers={typingUsers}
          onVote={handleVote}
          totalMembers={members.length}
        />
      </div>

      {/* Input */}
      <ChatInput
        onSendMessage={sendMessage}
        onTypingEvent={sendTypingEvent}
        disabled={!isConnected || isLoadingHistory}
        members={members}
      />
    </div>
  );
};