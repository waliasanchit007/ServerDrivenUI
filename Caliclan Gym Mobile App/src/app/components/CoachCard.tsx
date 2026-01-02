import { ImageWithFallback } from './figma/ImageWithFallback';

interface Coach {
  id: string;
  name: string;
  role: string;
}

interface CoachCardProps {
  coach: Coach;
  onClick: () => void;
}

export function CoachCard({ coach, onClick }: CoachCardProps) {
  return (
    <button
      onClick={onClick}
      className="bg-neutral-900 border border-neutral-800 rounded-xl p-4 text-left hover:bg-neutral-800 transition-colors"
    >
      <div className="w-16 h-16 bg-gradient-to-br from-amber-600 to-amber-700 rounded-full mb-3 flex items-center justify-center overflow-hidden">
        <span className="text-2xl text-neutral-950">{coach.name.charAt(0)}</span>
      </div>
      <div className="text-sm mb-1">{coach.name}</div>
      <div className="text-xs text-neutral-500">{coach.role}</div>
    </button>
  );
}
