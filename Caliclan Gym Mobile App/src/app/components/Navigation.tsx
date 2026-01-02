import { Home, Dumbbell, CreditCard, User } from 'lucide-react';
import type { TabType } from '../App';

interface NavigationProps {
  activeTab: TabType;
  onTabChange: (tab: TabType) => void;
}

export function Navigation({ activeTab, onTabChange }: NavigationProps) {
  const tabs = [
    { id: 'home' as TabType, icon: Home, label: 'Home' },
    { id: 'training' as TabType, icon: Dumbbell, label: 'Training' },
    { id: 'membership' as TabType, icon: CreditCard, label: 'Membership' },
    { id: 'profile' as TabType, icon: User, label: 'Profile' },
  ];

  return (
    <nav className="fixed bottom-0 left-0 right-0 max-w-md mx-auto bg-neutral-900 border-t border-neutral-800">
      <div className="flex items-center justify-around">
        {tabs.map((tab) => {
          const Icon = tab.icon;
          const isActive = activeTab === tab.id;
          return (
            <button
              key={tab.id}
              onClick={() => onTabChange(tab.id)}
              className="flex-1 flex flex-col items-center gap-1 py-3 transition-colors"
            >
              <Icon 
                className={`w-6 h-6 ${
                  isActive ? 'text-amber-500' : 'text-neutral-500'
                }`}
              />
              <span 
                className={`text-xs ${
                  isActive ? 'text-amber-500' : 'text-neutral-500'
                }`}
              >
                {tab.label}
              </span>
            </button>
          );
        })}
      </div>
    </nav>
  );
}
