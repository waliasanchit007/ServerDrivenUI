import { useState } from 'react';
import { Home } from './components/Home';
import { Training } from './components/Training';
import { Membership } from './components/Membership';
import { Profile } from './components/Profile';
import { CoachProfile } from './components/CoachProfile';
import { Navigation } from './components/Navigation';

export type TabType = 'home' | 'training' | 'membership' | 'profile';

export default function App() {
  const [activeTab, setActiveTab] = useState<TabType>('home');
  const [selectedCoach, setSelectedCoach] = useState<string | null>(null);

  const renderScreen = () => {
    if (selectedCoach) {
      return <CoachProfile coachId={selectedCoach} onBack={() => setSelectedCoach(null)} />;
    }

    switch (activeTab) {
      case 'home':
        return <Home onCoachClick={setSelectedCoach} />;
      case 'training':
        return <Training />;
      case 'membership':
        return <Membership />;
      case 'profile':
        return <Profile />;
      default:
        return <Home onCoachClick={setSelectedCoach} />;
    }
  };

  return (
    <div className="min-h-screen bg-neutral-950 text-neutral-50 flex flex-col max-w-md mx-auto">
      <main className="flex-1 overflow-y-auto pb-20">
        {renderScreen()}
      </main>
      {!selectedCoach && <Navigation activeTab={activeTab} onTabChange={setActiveTab} />}
    </div>
  );
}