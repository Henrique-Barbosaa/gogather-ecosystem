import React, { useState, KeyboardEvent, useRef, useEffect, useMemo } from "react";
import { SendHorizontal, Sparkles, User, Bot } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { cn } from "@/lib/utils";
import { GroupMemberDTO } from "@/types/chat";

interface ChatInputProps {
  onSendMessage: (content: string) => void;
  onTypingEvent: (isTyping: boolean) => void;
  disabled?: boolean;
  members?: GroupMemberDTO[];
}

export const ChatInput: React.FC<ChatInputProps> = ({
  onSendMessage,
  onTypingEvent,
  disabled = false,
  members = [],
}) => {
  const [content, setContent] = useState("");
  const typingTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  const [mentionActive, setMentionActive] = useState(false);
  const [mentionQuery, setMentionQuery] = useState("");
  const [mentionIndex, setMentionIndex] = useState(0);
  const [mentionStartPos, setMentionStartPos] = useState<number | null>(null);

  const aiMember = useMemo(() => ({
    externalId: "ai",
    displayName: "GoGatherAI",
    username: "gogatherai",
    role: "AI",
    email: "ai@gogather.com"
  }), []);

  const allMembers = useMemo(() => {
    return [aiMember, ...members];
  }, [members, aiMember]);

  const filteredMembers = useMemo(() => {
    if (!mentionActive) return [];
    return allMembers.filter(
      (m) =>
        m.displayName.toLowerCase().includes(mentionQuery.toLowerCase()) ||
        m.username.toLowerCase().includes(mentionQuery.toLowerCase())
    );
  }, [mentionActive, mentionQuery, allMembers]);

  const handleSend = () => {
    if (content.trim() && !disabled) {
      onSendMessage(content.trim());
      setContent("");
      onTypingEvent(false);
      setMentionActive(false);
      if (typingTimeoutRef.current) clearTimeout(typingTimeoutRef.current);
    }
  };

  const insertMention = (member: typeof aiMember) => {
    if (mentionStartPos === null) return;
    const beforeMention = content.slice(0, mentionStartPos);
    const afterMention = content.slice(mentionStartPos + mentionQuery.length + 1);
    
    const newContent = `${beforeMention}@${member.username} ${afterMention}`;
    setContent(newContent);
    setMentionActive(false);
    
    setTimeout(() => {
      if (textareaRef.current) {
        textareaRef.current.focus();
        const newCursorPos = beforeMention.length + member.username.length + 2;
        textareaRef.current.setSelectionRange(newCursorPos, newCursorPos);
      }
    }, 0);
  };

  const handleKeyDown = (e: KeyboardEvent<HTMLTextAreaElement>) => {
    if (mentionActive && filteredMembers.length > 0) {
      if (e.key === "ArrowDown") {
        e.preventDefault();
        setMentionIndex((prev) => (prev + 1) % filteredMembers.length);
        return;
      }
      if (e.key === "ArrowUp") {
        e.preventDefault();
        setMentionIndex((prev) => (prev - 1 + filteredMembers.length) % filteredMembers.length);
        return;
      }
      if (e.key === "Enter" || e.key === "Tab") {
        e.preventDefault();
        insertMention(filteredMembers[mentionIndex]);
        return;
      }
      if (e.key === "Escape") {
        e.preventDefault();
        setMentionActive(false);
        return;
      }
    }

    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    const val = e.target.value;
    setContent(val);
    
    const cursorPos = e.target.selectionStart;
    const textBeforeCursor = val.slice(0, cursorPos);
    const lastAtIndex = textBeforeCursor.lastIndexOf("@");
    
    if (lastAtIndex !== -1) {
      const textAfterAt = textBeforeCursor.slice(lastAtIndex + 1);
      if (!textAfterAt.includes(" ")) {
        setMentionActive(true);
        setMentionQuery(textAfterAt);
        setMentionStartPos(lastAtIndex);
        setMentionIndex(0);
      } else {
        setMentionActive(false);
      }
    } else {
      setMentionActive(false);
    }

    if (disabled) return;

    onTypingEvent(true);
    if (typingTimeoutRef.current) {
      clearTimeout(typingTimeoutRef.current);
    }
    
    typingTimeoutRef.current = setTimeout(() => {
      onTypingEvent(false);
    }, 2000);
  };

  useEffect(() => {
    return () => {
      if (typingTimeoutRef.current) clearTimeout(typingTimeoutRef.current);
    };
  }, []);

  const isAiMentioned = content.includes("@gogatherai");

  return (
    <div className="flex flex-col border-t bg-card transition-colors relative">
      {mentionActive && filteredMembers.length > 0 && (
        <div className="absolute bottom-full left-0 w-full mb-1 bg-popover border border-border rounded-t-xl shadow-lg max-h-48 overflow-y-auto z-50">
          {filteredMembers.map((member, idx) => (
            <div
              key={member.externalId}
              onClick={() => insertMention(member)}
              onMouseEnter={() => setMentionIndex(idx)}
              className={cn(
                "px-4 py-2 flex items-center gap-2 cursor-pointer transition-colors",
                idx === mentionIndex ? "bg-muted" : "hover:bg-muted/50"
              )}
            >
              {member.role === "AI" ? (
                <div className="bg-[#cc241a]/10 p-1.5 rounded-full text-[#cc241a]">
                  <Bot className="w-4 h-4" />
                </div>
              ) : (
                <div className="bg-primary/10 p-1.5 rounded-full text-primary">
                  <User className="w-4 h-4" />
                </div>
              )}
              <div className="flex flex-col">
                <span className={cn("text-sm font-semibold", member.role === "AI" ? "text-[#cc241a]" : "")}>
                  {member.displayName}
                </span>
                <span className="text-xs text-muted-foreground">@{member.username}</span>
              </div>
            </div>
          ))}
        </div>
      )}

      <div 
        className={cn(
          "px-5 text-xs font-bold flex items-center gap-1.5 transition-all duration-300 overflow-hidden text-[#cc241a]",
          isAiMentioned ? "h-8 pt-3 opacity-100" : "h-0 pt-0 opacity-0"
        )}
      >
        <Sparkles className="h-3.5 w-3.5" />
        <span>GoGather AI ativado</span>
      </div>
      <div className="flex items-end gap-2 p-4 pt-2">
        <Textarea
          ref={textareaRef}
          value={content}
          onChange={handleChange}
          onKeyDown={handleKeyDown}
          placeholder="Digite sua mensagem..."
          className={cn(
            "min-h-[2.5rem] max-h-32 resize-none rounded-2xl transition-all duration-300 relative z-10",
            isAiMentioned ? "border-[#cc241a]/50 focus-visible:ring-[#cc241a]/30 shadow-[0_0_10px_rgba(204,36,26,0.05)]" : ""
          )}
          disabled={disabled}
          rows={1}
        />
        <Button
          size="icon"
          onClick={handleSend}
          disabled={disabled || !content.trim()}
          className={cn(
            "rounded-full h-10 w-10 shrink-0 transition-all duration-300 relative z-10",
            isAiMentioned && content.trim() ? "bg-[#cc241a] hover:bg-[#a81d15] shadow-md shadow-[#cc241a]/20" : ""
          )}
        >
          {isAiMentioned && content.trim() ? (
            <Sparkles className="h-5 w-5 animate-pulse" />
          ) : (
            <SendHorizontal className="h-5 w-5" />
          )}
          <span className="sr-only">Enviar</span>
        </Button>
      </div>
    </div>
  );
};
