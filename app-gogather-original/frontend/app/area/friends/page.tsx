import { Header } from "@/components/area-components/Header";
import { Friends } from '@/components/friends-components/Friends';
import { AddFriendButton } from '@/components/friends-components/AddFriendButton';
import { PendingButton } from '@/components/friends-components/PendingButton';

export default function FriendsPage() {
  return (
    <div className="mx-auto max-w-6xl pt-4 md:pt-6 pb-12">
      <Header description="Suas amizades. Para turbinar seus rolês!" className="mb-4">Amigos</Header>
      <div className="flex flex-row gap-2 mb-4">
        <AddFriendButton/>
        <PendingButton/>
      </div>
      <Friends/>
    </div>
  )
}
