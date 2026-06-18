import React, { useEffect, useRef } from "react";
import { ChatMessage } from "@/types/chat";
import { MessageBubble } from "./MessageBubble";
import { useAuth } from "@/context/AuthContext";

interface MessageListProps {
  messages: ChatMessage[];
  typingUsers: Set<string>;
  onVote: (optionId: number) => void;
  totalMembers: number;
  isAdmin?: boolean;
  groupId?: string;
  onStopAdded?: () => void;
  addedPlaceIds?: string[];
}

export const MessageList: React.FC<MessageListProps> = ({ messages, typingUsers, onVote, totalMembers, isAdmin, groupId, onStopAdded, addedPlaceIds }) => {
  const { user } = useAuth();
  const bottomRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, typingUsers]);

  const typingArray = Array.from(typingUsers);

  return (
    <div className="flex-1 overflow-y-auto p-4 space-y-4 bg-muted/20">
      {messages.length === 0 ? (
        <div className="flex h-full items-center justify-center text-muted-foreground text-sm">
          Nenhuma mensagem ainda. Seja o primeiro a dizer olá!
        </div>
      ) : (
        messages.map((msg, index) => (
          <MessageBubble
            key={`${msg.createdAt}-${msg.senderName}-${index}`}
            message={msg}
            isCurrentUser={
              msg.type === "USER" &&
              (user?.displayName === msg.senderName || user?.username === msg.senderName)
            }
            onVote={onVote}
            totalMembers={totalMembers}
            isAdmin={isAdmin}
            groupId={groupId}
            onStopAdded={onStopAdded}
            addedPlaceIds={addedPlaceIds}
          />
        ))
      )}

      {typingArray.length > 0 && (
        <div className="flex items-center gap-2 text-xs text-muted-foreground italic px-2">
          <div className="flex space-x-1">
            <div className="w-1.5 h-1.5 bg-muted-foreground rounded-full animate-bounce" />
            <div className="w-1.5 h-1.5 bg-muted-foreground rounded-full animate-bounce delay-75" />
            <div className="w-1.5 h-1.5 bg-muted-foreground rounded-full animate-bounce delay-150" />
          </div>
          <span>
            {typingArray.join(", ")} {typingArray.length === 1 ? "está digitando..." : "estão digitando..."}
          </span>
        </div>
      )}

      <div ref={bottomRef} />
    </div>
  );
};
