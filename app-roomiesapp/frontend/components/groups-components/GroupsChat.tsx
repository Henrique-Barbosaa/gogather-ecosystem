import { ChatContainer } from "@/components/chat/ChatContainer";

export function GroupsChat({ inviteCode }: { inviteCode: string }) {
  return (
    <div className="flex-1 w-full h-full">
      <ChatContainer key={inviteCode} inviteCode={inviteCode} />
    </div>
  );
}
