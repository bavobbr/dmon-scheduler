import { useState } from 'react';
import type { Trainer, Team, TimeSlot, Dataset, Schedule } from './types';
import Navigation from './components/Navigation';
import DatasetControls from './components/DatasetControls';
import TrainersPage from './pages/TrainersPage';
import TeamsPage from './pages/TeamsPage';
import SlotsPage from './pages/SlotsPage';
import SchedulePage from './pages/SchedulePage';

type Tab = 'trainers' | 'teams' | 'slots' | 'schedule';

function App() {
  const [tab, setTab] = useState<Tab>('trainers');
  const [trainers, setTrainers] = useState<Trainer[]>([]);
  const [teams, setTeams] = useState<Team[]>([]);
  const [timeSlots, setTimeSlots] = useState<TimeSlot[]>([]);
  const [fieldCapacity, setFieldCapacity] = useState(60);
  const [schedule, setSchedule] = useState<Schedule | null>(null);

  function getDataset(): Dataset {
    return { trainers, teams, timeSlots, fieldCapacity };
  }

  function handleImport(dataset: Dataset) {
    setTrainers(dataset.trainers);
    setTeams(dataset.teams);
    setTimeSlots(dataset.timeSlots);
    setFieldCapacity(dataset.fieldCapacity);
  }

  return (
    <div className="min-h-screen bg-dmon-bg">
      <div className="max-w-6xl mx-auto px-4 py-6">
      <div className="flex justify-between items-center mb-4">
        <div className="flex items-center gap-3">
          <img src="/dmon-logo.png" alt="D-mon Hockey" className="w-12 h-12" />
          <h1 className="text-xl font-bold text-dmon-blue">Hockey Training Scheduler</h1>
        </div>
        <DatasetControls getDataset={getDataset} onImport={handleImport} />
      </div>
      <Navigation active={tab} onChange={setTab} />
      {tab === 'trainers' && (
        <TrainersPage trainers={trainers} teams={teams} onChange={setTrainers} />
      )}
      {tab === 'teams' && <TeamsPage teams={teams} onChange={setTeams} />}
      {tab === 'slots' && (
        <SlotsPage
          timeSlots={timeSlots}
          fieldCapacity={fieldCapacity}
          onSlotsChange={setTimeSlots}
          onCapacityChange={setFieldCapacity}
        />
      )}
      {tab === 'schedule' && (
        <SchedulePage
          dataset={getDataset()}
          schedule={schedule}
          onScheduleChange={setSchedule}
        />
      )}
      </div>
    </div>
  );
}

export default App;
