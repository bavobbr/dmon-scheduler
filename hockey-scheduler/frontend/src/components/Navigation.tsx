type Tab = 'trainers' | 'teams' | 'slots' | 'schedule';

interface Props {
  active: Tab;
  onChange: (tab: Tab) => void;
}

const TABS: { key: Tab; label: string }[] = [
  { key: 'trainers', label: 'Trainers' },
  { key: 'teams', label: 'Teams' },
  { key: 'slots', label: 'Field & Slots' },
  { key: 'schedule', label: 'Schedule' },
];

export default function Navigation({ active, onChange }: Props) {
  return (
    <nav className="flex border-b border-gray-200 mb-6">
      {TABS.map((tab) => (
        <button
          key={tab.key}
          onClick={() => onChange(tab.key)}
          className={`px-4 py-2 text-sm font-medium border-b-2 -mb-px transition-colors ${
            active === tab.key
              ? 'border-dmon-blue text-dmon-blue'
              : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
          }`}
        >
          {tab.label}
        </button>
      ))}
    </nav>
  );
}
