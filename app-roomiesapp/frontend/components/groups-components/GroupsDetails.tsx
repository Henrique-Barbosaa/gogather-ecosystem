import { GroupInfoSection } from "./GroupInfoSection";
import { GroupExpensesSection } from "./GroupExpensesSection";

export function GroupsDetails({ inviteCode }: { inviteCode: string }) {
  return (
    <aside className="w-full h-full flex flex-col bg-ra-blue-extralight overflow-y-auto">
      <div className="p-6 flex flex-col gap-8">
        <GroupInfoSection inviteCode={inviteCode} />
        <hr className="border-gray-100" />
        <GroupExpensesSection inviteCode={inviteCode} />
      </div>
    </aside>
  );
}
