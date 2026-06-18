"use client";

import { useState } from "react";
import { GroupsSidebar } from "@/components/groups-components/GroupsSidebar";
import { GroupsChat } from "@/components/groups-components/GroupsChat";
import { GroupsDetails } from "@/components/groups-components/GroupsDetails";

export default function GroupsPage() {
  const [selectedGroupId, setSelectedGroupId] = useState<string | null>(null);

  return (
    <div className="flex flex-col md:flex-row h-screen max-h-screen w-full overflow-hidden bg-gg-beige-light">
      <div className={`w-full md:w-[320px] lg:w-[350px] shrink-0 ${selectedGroupId ? 'hidden md:block' : 'block'}`}>
        <GroupsSidebar selectedGroupId={selectedGroupId} onSelectGroup={setSelectedGroupId} />
      </div>

      <div className={`flex-1 flex-col ${!selectedGroupId ? 'hidden md:flex' : 'flex'} min-w-0 bg-gg-beige-light border-x border-gg-beige-dark/20`}>
        {selectedGroupId ? (
          <GroupsChat groupId={selectedGroupId} />
        ) : (
          <div className="flex-1 flex flex-col items-center justify-center text-gray-400 p-6 text-center">
            <div className="w-16 h-16 rounded-2xl bg-gg-beige-extralight flex items-center justify-center mb-4 border border-gg-beige-dark/20">
              <span className="text-2xl">👋</span>
            </div>
            <h3 className="text-lg font-medium text-gray-900 mb-2">Nenhum rolê selecionado</h3>
            <p className="max-w-xs">Selecione um grupo na lista ao lado para ver os detalhes e as mensagens.</p>
          </div>
        )}
      </div>

      {selectedGroupId && (
        <div className="hidden xl:block w-[320px] shrink-0 bg-white">
          <GroupsDetails groupId={selectedGroupId} />
        </div>
      )}
    </div>
  )
}
