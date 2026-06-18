import { ChatContainer } from "@/components/chat/ChatContainer";

export function GroupsChat({ groupId }: { groupId: string }) {
  return (
    <div className="flex-1 w-full h-full">
      <ChatContainer key={groupId} externalId={groupId} />
    </div>
  );
}
