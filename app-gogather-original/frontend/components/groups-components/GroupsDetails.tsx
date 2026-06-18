import { GroupInfoSection } from "./GroupInfoSection";
import { GroupExpensesSection } from "./GroupExpensesSection";
import { GroupParticipantsSection } from "./GroupParticipantsSection";

export function GroupsDetails({ groupId }: { groupId: string }) {
  return (
    <aside className="w-full h-full flex flex-col bg-gg-beige-extralight overflow-y-auto">
      <div className="p-6 flex flex-col gap-8">
        <GroupInfoSection groupId={groupId} />
        <hr className="border-gray-100" />
        <GroupExpensesSection groupId={groupId} />
        <hr className="border-gray-100" />
        <GroupParticipantsSection groupId={groupId} />
      </div>
    </aside>
  );
}
