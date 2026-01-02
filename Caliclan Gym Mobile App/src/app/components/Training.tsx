import { Target, Clock, Check } from 'lucide-react';

interface TrainingDay {
  day: string;
  date: string;
  focus: string;
  goals: string[];
  supporting: string[];
  isToday: boolean;
  attended: boolean;
}

export function Training() {
  const currentWeek: TrainingDay[] = [
    {
      day: 'Monday',
      date: 'Dec 30',
      focus: 'Pull Strength & Skills',
      goals: ['Muscle-ups', 'Front Lever Progressions', 'Wide Grip Pull-ups'],
      supporting: ['Core Stability', 'Shoulder Mobility'],
      isToday: false,
      attended: true,
    },
    {
      day: 'Tuesday',
      date: 'Dec 31',
      focus: 'Push Strength & Balance',
      goals: ['Handstand Push-ups', 'Planche Leans', 'Ring Dips'],
      supporting: ['Wrist Conditioning', 'Scapular Control'],
      isToday: false,
      attended: true,
    },
    {
      day: 'Wednesday',
      date: 'Jan 1',
      focus: 'Legs & Core',
      goals: ['Pistol Squats', 'L-Sits', 'Dragon Flags'],
      supporting: ['Hip Mobility', 'Ankle Strength'],
      isToday: true,
      attended: false,
    },
    {
      day: 'Thursday',
      date: 'Jan 2',
      focus: 'Skills & Flow',
      goals: ['Bar Muscle-up', 'Handstand Holds', 'Bar Flow Combinations'],
      supporting: ['Flexibility', 'Movement Coordination'],
      isToday: false,
      attended: false,
    },
    {
      day: 'Friday',
      date: 'Jan 3',
      focus: 'Mobility & Recovery',
      goals: ['Deep Stretching', 'Active Flexibility', 'Joint Preparation'],
      supporting: ['Breath Work', 'Light Conditioning'],
      isToday: false,
      attended: false,
    },
    {
      day: 'Saturday',
      date: 'Jan 4',
      focus: 'Full Body Strength',
      goals: ['Weighted Pull-ups', 'Ring Muscle-ups', 'Squat Variations'],
      supporting: ['Core Compression', 'Endurance'],
      isToday: false,
      attended: false,
    },
    {
      day: 'Sunday',
      date: 'Jan 5',
      focus: 'Active Rest',
      goals: ['Light Movement', 'Yoga Flow', 'Mobility Work'],
      supporting: ['Recovery', 'Mindfulness'],
      isToday: false,
      attended: false,
    },
  ];

  return (
    <div className="px-6 py-8 space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-3xl mb-2">This Week's Training</h1>
        <p className="text-neutral-400">Structured calisthenics program</p>
      </div>

      {/* Weekly Overview */}
      <div className="space-y-4">
        {currentWeek.map((day) => (
          <div
            key={day.day}
            className={`rounded-2xl p-6 border-2 transition-all ${
              day.isToday
                ? 'bg-amber-950/20 border-amber-600'
                : 'bg-neutral-900 border-neutral-800'
            }`}
          >
            {/* Day Header */}
            <div className="flex items-start justify-between mb-4">
              <div className="flex-1">
                <div className="flex items-center gap-2 mb-1">
                  <span className={day.isToday ? 'text-amber-500' : 'text-neutral-400'}>
                    {day.day}
                  </span>
                  {day.isToday && (
                    <span className="px-2 py-0.5 bg-amber-600 text-neutral-950 text-xs rounded-full uppercase tracking-wider">
                      Today
                    </span>
                  )}
                  {day.attended && !day.isToday && (
                    <div className="w-5 h-5 rounded-full bg-emerald-900/50 flex items-center justify-center">
                      <Check className="w-3 h-3 text-emerald-500" />
                    </div>
                  )}
                </div>
                <div className="text-xl">{day.focus}</div>
              </div>
              <div className="text-sm text-neutral-500">{day.date}</div>
            </div>

            {/* Main Goals */}
            <div className="space-y-3">
              <div className="flex items-center gap-2 text-sm text-neutral-400">
                <Target className="w-4 h-4" />
                <span>Primary Goals</span>
              </div>
              <div className="flex flex-wrap gap-2">
                {day.goals.map((goal) => (
                  <span
                    key={goal}
                    className={`px-3 py-1.5 rounded-lg text-sm ${
                      day.isToday
                        ? 'bg-amber-900/30 text-amber-200'
                        : 'bg-neutral-800 text-neutral-300'
                    }`}
                  >
                    {goal}
                  </span>
                ))}
              </div>
            </div>

            {/* Supporting Elements */}
            <div className="mt-4 space-y-2">
              <div className="flex items-center gap-2 text-sm text-neutral-500">
                <Clock className="w-4 h-4" />
                <span>Supporting</span>
              </div>
              <div className="text-sm text-neutral-400">
                {day.supporting.join(' • ')}
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Training Notes */}
      <div className="bg-neutral-900 border border-neutral-800 rounded-2xl p-6">
        <div className="text-xs uppercase tracking-wider text-neutral-400 mb-2">
          Program Notes
        </div>
        <p className="text-sm text-neutral-400 leading-relaxed">
          This module focuses on building foundational strength and mastering key calisthenics skills. Progress at your own pace and prioritize form over speed.
        </p>
      </div>
    </div>
  );
}
