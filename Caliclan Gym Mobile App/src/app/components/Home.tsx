import { Calendar, MessageCircle, ChevronRight, Flame } from 'lucide-react';
import { CoachCard } from './CoachCard';

interface HomeProps {
  onCoachClick: (coachId: string) => void;
}

export function Home({ onCoachClick }: HomeProps) {
  // Mock data
  const member = {
    name: 'Alex',
    membershipStatus: 'active' as 'active' | 'expiring' | 'expired',
    expiryDate: '2025-02-15',
    attendanceStreak: 4,
    sessionsThisWeek: 4,
  };

  const todayTraining = {
    day: 'Wednesday',
    focus: 'Legs & Core',
    goals: ['Pistol Squats', 'L-Sits', 'Dragon Flags'],
  };

  // Weekly attendance data (Mon-Sun)
  const weeklyAttendance = [
    { day: 'Mon', attended: true, isToday: false },
    { day: 'Tue', attended: true, isToday: false },
    { day: 'Wed', attended: false, isToday: true },
    { day: 'Thu', attended: false, isToday: false },
    { day: 'Fri', attended: false, isToday: false },
    { day: 'Sat', attended: false, isToday: false },
    { day: 'Sun', attended: false, isToday: false },
  ];

  const attendedCount = weeklyAttendance.filter(d => d.attended).length;
  const missedCount = weeklyAttendance.filter(d => !d.attended && !d.isToday).length;

  const coaches = [
    { id: 'hemant', name: 'Hemant', role: 'Founder, Master Coach' },
    { id: 'ankit', name: 'Ankit', role: 'Head Coach' },
    { id: 'shoaib', name: 'Shoaib', role: 'Coach' },
    { id: 'tejas', name: 'Tejas', role: 'Coach' },
    { id: 'mayank', name: 'Mayank', role: 'Coach' },
    { id: 'gupta', name: 'Gupta Ji', role: 'Coach' },
  ];

  const announcement = {
    title: 'Advanced Skills Workshop',
    message: 'Join us this Saturday at 10 AM for a special muscle-up workshop.',
    date: '2025-01-04',
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'active':
        return 'border-emerald-600 bg-emerald-950/40';
      case 'expiring':
        return 'border-amber-600 bg-amber-950/40';
      case 'expired':
        return 'border-red-600 bg-red-950/40';
      default:
        return 'border-neutral-700 bg-neutral-900';
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case 'active':
        return 'Active';
      case 'expiring':
        return 'Expiring Soon';
      case 'expired':
        return 'Expired';
      default:
        return 'Unknown';
    }
  };

  const daysUntilExpiry = Math.ceil(
    (new Date(member.expiryDate).getTime() - new Date().getTime()) / (1000 * 60 * 60 * 24)
  );

  return (
    <div className="px-6 py-8 space-y-8">
      {/* Greeting */}
      <div>
        <h1 className="text-neutral-400">Welcome back,</h1>
        <h2 className="text-3xl mt-1">{member.name}</h2>
      </div>

      {/* Membership Status Card */}
      <div className={`border-2 rounded-2xl p-6 ${getStatusColor(member.membershipStatus)}`}>
        <div className="flex items-start justify-between mb-4">
          <div>
            <div className="text-xs uppercase tracking-wider text-neutral-400 mb-1">
              Membership
            </div>
            <div className="text-xl">{getStatusText(member.membershipStatus)}</div>
          </div>
          <Calendar className="w-5 h-5 text-neutral-400" />
        </div>
        <div className="space-y-1">
          <div className="text-sm text-neutral-400">Expires on</div>
          <div className="text-lg">
            {new Date(member.expiryDate).toLocaleDateString('en-US', {
              day: 'numeric',
              month: 'long',
              year: 'numeric',
            })}
          </div>
          {daysUntilExpiry <= 30 && daysUntilExpiry > 0 && (
            <div className="text-sm text-amber-500 mt-2">
              {daysUntilExpiry} days remaining
            </div>
          )}
        </div>
        {member.membershipStatus === 'expiring' && (
          <button className="w-full mt-6 bg-amber-600 hover:bg-amber-700 text-neutral-950 py-3 rounded-xl transition-colors">
            Renew Membership
          </button>
        )}
      </div>

      {/* Today's Training */}
      <div className="bg-neutral-900 border border-neutral-800 rounded-2xl p-6">
        <div className="flex items-center justify-between mb-4">
          <div>
            <div className="text-xs uppercase tracking-wider text-neutral-400 mb-1">
              Today's Session
            </div>
            <div className="text-xl">{todayTraining.focus}</div>
          </div>
          <ChevronRight className="w-5 h-5 text-neutral-500" />
        </div>
        <div className="space-y-2">
          <div className="text-sm text-neutral-400">Focus Areas</div>
          <div className="flex flex-wrap gap-2">
            {todayTraining.goals.map((goal) => (
              <span
                key={goal}
                className="px-3 py-1.5 bg-neutral-800 rounded-lg text-sm"
              >
                {goal}
              </span>
            ))}
          </div>
        </div>
      </div>

      {/* Training Consistency */}
      <div className="bg-neutral-900 border border-neutral-800 rounded-2xl p-6">
        <div className="flex items-center gap-2 mb-4">
          <Flame className="w-5 h-5 text-amber-500" />
          <div>
            <span className="text-lg">{member.attendanceStreak}-day streak</span>
          </div>
        </div>
        
        {/* Weekly Attendance Visual */}
        <div className="space-y-3">
          <div className="flex items-center justify-between">
            {weeklyAttendance.map((day) => (
              <div key={day.day} className="flex flex-col items-center gap-1.5">
                <div className="text-xs text-neutral-500">{day.day}</div>
                <div
                  className={`w-8 h-8 rounded-lg flex items-center justify-center ${
                    day.isToday
                      ? 'bg-amber-600 ring-2 ring-amber-600/30'
                      : day.attended
                      ? 'bg-emerald-900/50'
                      : 'bg-neutral-800'
                  }`}
                >
                  {day.attended && (
                    <div className="w-2 h-2 rounded-full bg-emerald-500" />
                  )}
                  {day.isToday && !day.attended && (
                    <div className="w-1.5 h-1.5 rounded-full bg-neutral-950" />
                  )}
                </div>
              </div>
            ))}
          </div>
          
          <div className="pt-3 border-t border-neutral-800 text-sm text-neutral-400">
            {attendedCount > 0 && `Trained ${attendedCount} ${attendedCount === 1 ? 'day' : 'days'} this week`}
            {missedCount > 0 && attendedCount > 0 && ' • '}
            {missedCount > 0 && `Missed ${missedCount} ${missedCount === 1 ? 'session' : 'sessions'}`}
            {attendedCount === 0 && missedCount === 0 && 'Start your week strong'}
          </div>
        </div>
      </div>

      {/* Coach Announcement */}
      <div className="bg-neutral-900 border border-amber-900/50 rounded-2xl p-6">
        <div className="text-xs uppercase tracking-wider text-amber-500 mb-2">
          Coach Update
        </div>
        <div className="text-lg mb-2">{announcement.title}</div>
        <p className="text-sm text-neutral-400 leading-relaxed">
          {announcement.message}
        </p>
      </div>

      {/* Contact CTA */}
      <button className="w-full bg-neutral-900 hover:bg-neutral-800 border border-neutral-800 text-neutral-50 py-4 rounded-xl transition-colors flex items-center justify-center gap-2">
        <MessageCircle className="w-5 h-5" />
        <span>Contact Gym via WhatsApp</span>
      </button>

      {/* Meet Your Coaches */}
      <div>
        <h2 className="text-xl mb-4">Meet Your Coaches</h2>
        <div className="grid grid-cols-2 gap-4">
          {coaches.map(coach => (
            <CoachCard key={coach.id} coach={coach} onClick={() => onCoachClick(coach.id)} />
          ))}
        </div>
      </div>
    </div>
  );
}
