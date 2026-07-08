"use client";

import React from "react";
import { GroupSimpleData } from "@/app/types";

interface HouseSelectorProps {
  houses: GroupSimpleData[];
  selectedInviteCode: string | null;
  onSelect: (inviteCode: string) => void;
  className?: string;
}

const avatarColors = [
  "bg-ra-green text-white",
  "bg-ra-purple text-white",
  "bg-ra-blue-extradark text-white",
];

export function HouseSelector({
  houses,
  selectedInviteCode,
  onSelect,
  className = "",
}: HouseSelectorProps) {
  if (houses.length === 0) return null;

  return (
    <div className={`flex gap-3 overflow-x-auto pb-2 -mx-1 px-1 ${className}`}>
      {houses.map((house) => {
        const isSelected = selectedInviteCode === house.inviteCode;
        const initial = house.name.substring(0, 1).toUpperCase();
        const colorIndex =
          house.inviteCode
            .split("")
            .reduce((acc, char) => acc + char.charCodeAt(0), 0) % avatarColors.length;

        return (
          <button
            key={house.inviteCode}
            onClick={() => onSelect(house.inviteCode)}
            className={`shrink-0 flex items-center gap-3 pl-2 pr-5 py-2 rounded-2xl border transition-all
              ${
                isSelected
                  ? "bg-white border-ra-purple shadow-sm ring-1 ring-ra-purple/30"
                  : "bg-white/60 border-gray-200 hover:bg-white hover:border-gray-300"
              }`}
          >
            <div
              className={`w-10 h-10 rounded-xl flex items-center justify-center font-bold text-lg shrink-0 ${avatarColors[colorIndex]}`}
            >
              {initial}
            </div>
            <div className="text-left min-w-0">
              <p
                className={`text-sm font-bold truncate max-w-[160px] ${
                  isSelected ? "text-ra-purple-dark" : "text-gray-800"
                }`}
              >
                {house.name}
              </p>
              <p className="text-xs text-gray-400 truncate max-w-[160px]">
                {typeof house.memberAmount === "number"
                  ? `${house.memberAmount} ${house.memberAmount === 1 ? "morador" : "moradores"}`
                  : house.address || "República"}
              </p>
            </div>
          </button>
        );
      })}
    </div>
  );
}
