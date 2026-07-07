"use client";

import { useState } from "react";
import { GroupsSidebar } from "@/components/groups-components/GroupsSidebar";
import { GroupsChat } from "@/components/groups-components/GroupsChat";
import { GroupsDetails } from "@/components/groups-components/GroupsDetails";

export default function GroupsPage() {
  // Guardamos o inviteCode do grupo selecionado — é o identificador usado pelo
  // chat, enquetes e detalhes no backend do roomiesapp.
  const [selectedInviteCode, setSelectedInviteCode] = useState<string | null>(null);

  return (
    <div className="flex flex-col md:flex-row h-screen max-h-screen w-full overflow-hidden bg-ra-blue-light">
      <div className={`w-full md:w-[320px] lg:w-[350px] shrink-0 ${selectedInviteCode ? 'hidden md:block' : 'block'}`}>
        <GroupsSidebar selectedInviteCode={selectedInviteCode} onSelectGroup={setSelectedInviteCode} />
      </div>

      <div className={`flex-1 flex-col ${!selectedInviteCode ? 'hidden md:flex' : 'flex'} min-w-0 bg-ra-blue-light border-x border-ra-blue-dark/20`}>
        {selectedInviteCode ? (
          <GroupsChat inviteCode={selectedInviteCode} />
        ) : (
          <div className="flex-1 flex flex-col items-center justify-center text-gray-400 p-6 text-center">
            <div className="w-16 h-16 rounded-2xl bg-ra-blue-extralight flex items-center justify-center mb-4 border border-ra-blue-dark/20">
              <span className="text-2xl">🏠</span>
            </div>
            <h3 className="text-lg font-medium text-gray-900 mb-2">Nenhuma casa selecionada</h3>
            <p className="max-w-xs">Selecione uma casa na lista ao lado para ver os detalhes e as mensagens.</p>
          </div>
        )}
      </div>

      {selectedInviteCode && (
        <div className="hidden xl:block w-[320px] shrink-0 bg-white">
          <GroupsDetails inviteCode={selectedInviteCode} />
        </div>
      )}
    </div>
  )
}
