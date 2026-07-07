"use client";

import React from "react";
import { ChatContainer } from "@/components/chat/ChatContainer";

export default function TestChatPage() {
  // Use um inviteCode válido de um grupo para testar o chat isoladamente.
  const inviteCode = "TESTCODE";

  return (
    <div className="min-h-screen bg-background flex flex-col items-center justify-center p-4 md:p-8">
      <div className="w-full max-w-4xl space-y-6">
        <ChatContainer inviteCode={inviteCode} />
      </div>
    </div>
  );
}
