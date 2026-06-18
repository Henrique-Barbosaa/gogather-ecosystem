"use client";

import React from "react";
import { ChatContainer } from "@/components/chat/ChatContainer";

export default function TestChatPage() {
  const externalId = "13c001c8-7cc3-4ead-97e7-31bdbb78661c";

  return (
    <div className="min-h-screen bg-background flex flex-col items-center justify-center p-4 md:p-8">
      <div className="w-full max-w-4xl space-y-6">
        <ChatContainer externalId={externalId} />
      </div>
    </div>
  );
}
