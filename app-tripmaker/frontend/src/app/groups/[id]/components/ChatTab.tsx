'use client';

import React, { useState, useRef, useEffect } from 'react';
import { TravelGroup, ChatMessage, getAvatarUrl } from '../../../../services/mockData';
import { api } from '../../../../services/api';
import { Card, Button } from '../../../../components/common';
import { Send, MessageSquare, Smile, Clock, Sparkles, CheckCheck } from 'lucide-react';
import { useAuth } from '../../../../context/AuthContext';

interface ChatTabProps {
  group: TravelGroup;
  onUpdateMessages: (newMessages: ChatMessage[]) => void;
}

export const ChatTab: React.FC<ChatTabProps> = ({ group, onUpdateMessages }) => {
  const { user } = useAuth();
  const [messages, setMessages] = useState<ChatMessage[]>(group.messages || []);
  const [input, setInput] = useState('');
  const [isSending, setIsSending] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    setMessages(group.messages || []);
  }, [group.messages]);

  useEffect(() => {
    const fetchMessages = async () => {
      try {
        const idOrCode = group.inviteCode || group.id;
        if (idOrCode) {
          const res = await api.get(`/groups/${idOrCode}/chat`);
          if (res.data && Array.isArray(res.data)) {
            const mapped = res.data.map((m: any) => ({
              id: String(m.id),
              groupId: String(group.id),
              senderId: String(m.senderId || ''),
              senderName: m.senderName || 'Viajante',
              senderAvatar: m.senderAvatar || getAvatarUrl(m.senderName),
              content: m.content || '',
              timestamp: m.createdAt ? new Date(m.createdAt).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' }) : '',
              type: m.type ? m.type.toLowerCase() : 'text',
            }));
            setMessages(mapped);
            onUpdateMessages(mapped);
          }
        }
      } catch (err) {
        console.error('Erro ao buscar mensagens do chat:', err);
      }
    };
    fetchMessages();
  }, [group.id, group.inviteCode]);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleSend = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!input.trim() || isSending) return;

    const content = input;
    setInput('');
    setIsSending(true);

    try {
      const idOrCode = group.inviteCode || group.id;
      const res = await api.post(`/groups/${idOrCode}/chat`, { content });
      const m = res.data;
      const newMsg: ChatMessage = {
        id: String(m.id || `msg-${Date.now()}`),
        groupId: String(group.id),
        senderId: String(m.senderId || user?.id || 'usr-1'),
        senderName: m.senderName || user?.name || 'Pedro Henrique',
        senderAvatar: m.senderAvatar || getAvatarUrl(m.senderName || user?.name, user?.avatar),
        content: m.content || content,
        timestamp: m.createdAt ? new Date(m.createdAt).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' }) : new Date().toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' }),
        type: m.type ? m.type.toLowerCase() : 'text',
      };

      const updated = [...messages, newMsg];
      setMessages(updated);
      onUpdateMessages(updated);
    } catch (err) {
      console.error('Erro ao enviar mensagem:', err);
      const newMsg: ChatMessage = {
        id: `msg-${Date.now()}`,
        groupId: group.id,
        senderId: user?.id || 'usr-1',
        senderName: user?.name || 'Pedro Henrique',
        senderAvatar: getAvatarUrl(user?.name, user?.avatar),
        content,
        timestamp: new Date().toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' }),
      };
      const updated = [...messages, newMsg];
      setMessages(updated);
      onUpdateMessages(updated);
    } finally {
      setIsSending(false);
    }
  };

  const quickSend = (text: string) => {
    setInput(text);
  };

  return (
    <div className="flex flex-col h-[600px] sm:h-[650px] animate-fadeIn">
      
      {/* Chat Header */}
      <div className="p-4 rounded-t-3xl bg-gradient-to-r from-[var(--color-pastel-red-100)] to-[var(--color-pastel-yellow-100)] border border-[var(--color-pastel-red-200)] flex items-center justify-between shadow-2xs">
        <div className="flex items-center gap-3">
          <div className="p-2.5 rounded-2xl bg-white text-[var(--color-pastel-red-600)] shadow-xs">
            <MessageSquare className="w-5 h-5" />
          </div>
          <div>
            <h3 className="font-extrabold text-[#2d2327] text-base">
              Chat da Viagem • {group.title}
            </h3>
            <span className="text-xs text-gray-600 flex items-center gap-1.5 font-medium">
              <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse inline-block" />
              {group.members?.length || 1} integrantes conectados
            </span>
          </div>
        </div>
        <div className="hidden sm:flex items-center gap-1 text-xs font-bold text-gray-500 bg-white/60 px-3 py-1.5 rounded-xl">
          <Sparkles className="w-3.5 h-3.5 text-[var(--color-pastel-yellow-700)]" />
          Notificações Inteligentes Ativas
        </div>
      </div>

      {/* Messages Container */}
      <div className="flex-1 overflow-y-auto p-4 sm:p-6 bg-white/70 backdrop-blur-md border-x border-gray-200 space-y-4">
        {messages.length === 0 ? (
          <div className="h-full flex flex-col items-center justify-center text-center text-gray-400 space-y-2">
            <MessageSquare className="w-12 h-12 text-gray-300 animate-bounce" />
            <p className="text-sm font-semibold">Nenhuma mensagem ainda no chat!</p>
            <span className="text-xs">Seja o primeiro a mandar um "Bora viajar!" para a galera.</span>
          </div>
        ) : (
          messages.map((msg) => {
            const isMe = msg.senderId === user?.id || msg.senderName === user?.name || msg.senderName === 'Pedro Henrique';
            return (
              <div
                key={msg.id}
                className={`flex items-end gap-2.5 ${isMe ? 'justify-end' : 'justify-start'} animate-fadeIn`}
              >
                {!isMe && (
                  <img
                    src={getAvatarUrl(msg.senderName, msg.senderAvatar)}
                    alt={msg.senderName}
                    className="w-8 h-8 rounded-xl object-cover border border-gray-200 shrink-0 mb-1"
                  />
                )}
                <div
                  className={`max-w-[75%] sm:max-w-md p-3.5 rounded-3xl shadow-xs ${
                    isMe
                      ? 'bg-gradient-to-r from-[var(--color-pastel-red-500)] to-[var(--color-pastel-red-600)] text-white rounded-br-xs'
                      : 'bg-white text-[#2d2327] border border-gray-100 rounded-bl-xs'
                  }`}
                >
                  {!isMe && (
                    <span className="text-[11px] font-bold text-[var(--color-pastel-red-600)] block mb-1">
                      {msg.senderName}
                    </span>
                  )}
                  <p className="text-sm leading-relaxed whitespace-pre-wrap font-normal">
                    {msg.content}
                  </p>
                  <div className={`flex items-center justify-end gap-1 mt-1 text-[10px] ${isMe ? 'text-white/80' : 'text-gray-400'}`}>
                    <span>{msg.timestamp}</span>
                    {isMe && <CheckCheck className="w-3.5 h-3.5" />}
                  </div>
                </div>
              </div>
            );
          })
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* Quick Emojis & Canned Responses */}
      <div className="px-4 py-2 bg-gray-50 border-x border-gray-200 flex items-center gap-2 overflow-x-auto no-scrollbar text-xs font-semibold">
        <span className="text-gray-400 shrink-0 flex items-center gap-1">
          <Smile className="w-3.5 h-3.5" /> Dicas rápidas:
        </span>
        <button
          onClick={() => quickSend('Galera, quando vamos fechar a hospedagem? 🏖️')}
          className="px-3 py-1 rounded-full bg-white border border-gray-200 text-gray-600 hover:border-[var(--color-pastel-red-400)] hover:text-[var(--color-pastel-red-600)] transition-all shrink-0"
        >
          Fechando hospedagem 🏖️
        </button>
        <button
          onClick={() => quickSend('Acabei de lançar o rateio das compras do mercado! 💸')}
          className="px-3 py-1 rounded-full bg-white border border-gray-200 text-gray-600 hover:border-[var(--color-pastel-red-400)] hover:text-[var(--color-pastel-red-600)] transition-all shrink-0"
        >
          Rateio no mercado 💸
        </button>
        <button
          onClick={() => quickSend('O roadmap está atualizado com os horários dos passeios 🗺️')}
          className="px-3 py-1 rounded-full bg-white border border-gray-200 text-gray-600 hover:border-[var(--color-pastel-red-400)] hover:text-[var(--color-pastel-red-600)] transition-all shrink-0"
        >
          Roadmap atualizado 🗺️
        </button>
      </div>

      {/* Input Box */}
      <form
        onSubmit={handleSend}
        className="p-3 bg-white rounded-b-3xl border border-[var(--color-pastel-red-200)] shadow-lg flex items-center gap-2"
      >
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="Digite sua mensagem para o grupo..."
          className="flex-1 bg-gray-50 border border-gray-200 rounded-2xl px-4 py-3 text-sm text-[#2d2327] outline-none focus:bg-white focus:border-[var(--color-pastel-red-400)] focus:ring-2 focus:ring-[var(--color-pastel-red-100)] transition-all"
        />
        <Button
          type="submit"
          variant="primary"
          size="md"
          disabled={!input.trim()}
          className="rounded-2xl px-5 py-3 shadow-md"
        >
          <Send className="w-4 h-4" />
          <span className="hidden sm:inline">Enviar</span>
        </Button>
      </form>

    </div>
  );
};
