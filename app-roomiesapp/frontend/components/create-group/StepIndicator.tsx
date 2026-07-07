"use client";

import { Check } from "lucide-react";

// Definimos uma interface para os passos
interface StepItem {
  num: number;
  label: string;
}

interface StepIndicatorProps {
  current: number;
  steps: StepItem[];
}

const StepIndicator = ({ current, steps }: StepIndicatorProps) => {
  return (
    <div className="flex items-center gap-2">
      {steps.map((s, i) => (
        <div key={s.num} className="flex items-center gap-2">
          <div
            className={`flex items-center justify-center h-10 w-10 rounded-full text-lg font-bold transition-colors ${
              current > s.num
                ? "bg-[#299227] text-white"
                : current === s.num
                  ? "bg-[#299227] text-white shadow-lg"
                  : "bg-gray-200 text-gray-500"
            }`}
          >
            {current > s.num ? <Check className="h-5 w-5" /> : s.num}
          </div>
          <span
            className={`hidden sm:inline text-lg font-medium ${
              current >= s.num ? "text-gray-900" : "text-gray-400"
            }`}
          >
            {s.label}
          </span>
          {i < steps.length - 1 && (
            <div
              className={`w-8 h-0.5 rounded-full ${
                current > s.num ? "bg-[#299227]" : "bg-gray-200"
              }`}
            />
          )}
        </div>
      ))}
    </div>
  );
};

export default StepIndicator;