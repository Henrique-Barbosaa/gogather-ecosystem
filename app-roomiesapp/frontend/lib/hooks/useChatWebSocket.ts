import { useEffect, useState, useCallback, useRef } from "react";
import { Client, IMessage } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { ChatMessage, TypingEvent, PollResponse } from "@/types/chat";
import { useAuth } from "@/context/AuthContext";

export const useChatWebSocket = (externalId: string) => {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [typingUsers, setTypingUsers] = useState<Set<string>>(new Set());
  const clientRef = useRef<Client | null>(null);
  const { user } = useAuth();
  const [isConnected, setIsConnected] = useState(false);

  useEffect(() => {
    const apiUrl = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";
    
    const client = new Client({
      webSocketFactory: () => new SockJS(`${apiUrl}/ws-chat`),
      connectHeaders: {},
      debug: (str) => {
        console.log("[STOMP]", str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        setIsConnected(true);
        
        client.subscribe(`/topic/group/${externalId}`, (message: IMessage) => {
          const chatMessage: ChatMessage = JSON.parse(message.body);
          setMessages((prev) => [...prev, chatMessage]);
        });

        client.subscribe(`/topic/group/${externalId}/typing`, (message: IMessage) => {
          const typingEvent: TypingEvent = JSON.parse(message.body);
          
          if (typingEvent.senderName === user?.displayName || typingEvent.senderName === user?.username) return;

          setTypingUsers((prev) => {
            const newSet = new Set(prev);
            if (typingEvent.isTyping) {
              newSet.add(typingEvent.senderName);
            } else {
              newSet.delete(typingEvent.senderName);
            }
            return newSet;
          });
        });

        client.subscribe(`/topic/group/${externalId}/poll-update`, (message: IMessage) => {
          const updatedPoll: PollResponse = JSON.parse(message.body);
          setMessages((prev) => 
            prev.map(m => m.poll?.id === updatedPoll.id ? { ...m, poll: updatedPoll } : m)
          );
        });
      },
      onDisconnect: () => {
        setIsConnected(false);
      },
      onStompError: (frame) => {
        console.error("Broker reported error: " + frame.headers["message"]);
        console.error("Additional details: " + frame.body);
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
      clientRef.current = null;
    };
  }, [externalId, user?.displayName, user?.username]);

  const sendMessage = useCallback(
    (content: string) => {
      if (clientRef.current && clientRef.current.connected && user) {
        const requiresAiResponse = content.includes("@gogatherai");
        clientRef.current.publish({
          destination: `/app/chat/${externalId}/send`,
          body: JSON.stringify({ content, requiresAiResponse }),
        });
      } else {
        console.warn("Cannot send message: WebSocket not connected or user not logged in.");
      }
    },
    [externalId, user]
  );

  const sendTypingEvent = useCallback(
    (isTyping: boolean) => {
      if (clientRef.current && clientRef.current.connected && user) {
        clientRef.current.publish({
          destination: `/app/chat/${externalId}/typing`,
          body: JSON.stringify({ senderName: user.displayName || user.username, isTyping }),
        });
      }
    },
    [externalId, user]
  );

  return {
    messages,
    setMessages,
    typingUsers,
    isConnected,
    sendMessage,
    sendTypingEvent,
  };
};
