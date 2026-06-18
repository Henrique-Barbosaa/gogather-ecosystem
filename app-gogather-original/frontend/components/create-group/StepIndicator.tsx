import { Check } from "lucide-react";

const steps = [
  { num: 1, label: "Info" },
  { num: 2, label: "Rota" },
  { num: 3, label: "Convite" },
];

const StepIndicator = ({ current }: { current: number }) => (
  <div className="flex items-center gap-2">
    {steps.map((s, i) => (
      <div key={s.num} className="flex items-center gap-2">
        <div
          className={`flex items-center justify-center h-10 w-10 rounded-full text-lg font-bold transition-colors ${
            current > s.num
              ? "bg-gg-cyan text-primary-foreground"
              : current === s.num
                ? "bg-gg-cyan text-primary-foreground shadow-card"
                : "bg-muted text-muted-foreground"
          }`}
        >
          {current > s.num ? <Check className="h-4 w-4" /> : s.num}
        </div>
        <span
          className={`hidden sm:inline text-lg font-medium ${
            current >= s.num ? "text-foreground" : "text-muted-foreground"
          }`}
        >
          {s.label}
        </span>
        {i < steps.length - 1 && (
          <div
            className={`w-8 h-0.5 rounded-full ${
              current > s.num ? "bg-primary" : "bg-border"
            }`}
          />
        )}
      </div>
    ))}
  </div>
);

export default StepIndicator;
