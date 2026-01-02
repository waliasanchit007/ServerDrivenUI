import { ArrowLeft, Instagram } from 'lucide-react';

interface CoachProfileProps {
  coachId: string;
  onBack: () => void;
}

export function CoachProfile({ coachId, onBack }: CoachProfileProps) {
  // Coach data
  const coaches: Record<string, any> = {
    hemant: {
      name: 'Hemant',
      role: 'Founder, Master Coach',
      bio: 'With over 10 years of experience, Hemant founded Caliclan to create a community-driven space for calisthenics excellence.',
      skills: ['Advanced Skills', 'Muscle-ups', 'Handstands', 'Programming'],
      instagram: '@hemant_caliclan',
    },
    ankit: {
      name: 'Ankit',
      role: 'Head Coach',
      bio: 'Ankit specializes in strength development and helps members progress from basics to advanced movements.',
      skills: ['Strength Training', 'Pull-ups', 'Planche', 'Front Lever'],
      instagram: '@ankit_coach',
    },
    shoaib: {
      name: 'Shoaib',
      role: 'Coach',
      bio: 'Focused on mobility and flow, Shoaib brings creativity and movement mastery to every session.',
      skills: ['Mobility', 'Flow', 'Flexibility', 'Movement'],
      instagram: '@shoaib_movement',
    },
    tejas: {
      name: 'Tejas',
      role: 'Coach',
      bio: 'Tejas helps members build foundational strength and proper technique for long-term progress.',
      skills: ['Foundations', 'Form', 'Progressive Training', 'Core'],
      instagram: '@tejas_fit',
    },
    mayank: {
      name: 'Mayank',
      role: 'Coach',
      bio: 'Mayank brings energy and motivation to every class, specializing in dynamic movements and skills.',
      skills: ['Skills', 'Dynamics', 'Bar Flow', 'Motivation'],
      instagram: '@mayank_athlete',
    },
    gupta: {
      name: 'Gupta Ji',
      role: 'Coach',
      bio: 'With a calm and methodical approach, Gupta Ji focuses on mindful training and injury prevention.',
      skills: ['Mobility', 'Recovery', 'Mindfulness', 'Injury Prevention'],
      instagram: '@gupta_wellness',
    },
  };

  const coach = coaches[coachId] || coaches.hemant;

  return (
    <div className="min-h-screen bg-neutral-950">
      {/* Header */}
      <div className="px-6 py-6 border-b border-neutral-800">
        <button
          onClick={onBack}
          className="flex items-center gap-2 text-neutral-400 hover:text-neutral-50 transition-colors"
        >
          <ArrowLeft className="w-5 h-5" />
          <span>Back</span>
        </button>
      </div>

      {/* Content */}
      <div className="px-6 py-8 space-y-8">
        {/* Profile Photo */}
        <div className="flex flex-col items-center">
          <div className="w-32 h-32 bg-gradient-to-br from-amber-600 to-amber-700 rounded-full mb-4 flex items-center justify-center">
            <span className="text-5xl text-neutral-950">{coach.name.charAt(0)}</span>
          </div>
          <div className="text-2xl mb-1">{coach.name}</div>
          <div className="text-neutral-400">{coach.role}</div>
        </div>

        {/* Bio */}
        <div className="bg-neutral-900 border border-neutral-800 rounded-2xl p-6">
          <div className="text-xs uppercase tracking-wider text-neutral-400 mb-3">About</div>
          <p className="text-sm text-neutral-300 leading-relaxed">{coach.bio}</p>
        </div>

        {/* Skills */}
        <div className="bg-neutral-900 border border-neutral-800 rounded-2xl p-6">
          <div className="text-xs uppercase tracking-wider text-neutral-400 mb-3">Specializations</div>
          <div className="flex flex-wrap gap-2">
            {coach.skills.map((skill: string) => (
              <span
                key={skill}
                className="px-3 py-1.5 bg-neutral-800 rounded-lg text-sm text-neutral-300"
              >
                {skill}
              </span>
            ))}
          </div>
        </div>

        {/* Instagram */}
        <button className="w-full bg-neutral-900 hover:bg-neutral-800 border border-neutral-800 text-neutral-50 py-4 rounded-xl transition-colors flex items-center justify-center gap-2">
          <Instagram className="w-5 h-5" />
          <span>Follow on Instagram</span>
        </button>
      </div>
    </div>
  );
}
