import { useState, useEffect, useRef } from 'react';
import type { Dataset, Schedule, ScoreAnalysis, SessionAnalysis } from '../types';
import { solve, getSchedule, getStatus, stopSolving, getScoreAnalysis, getSessionAnalysis } from '../api/scheduleApi';
import WeeklyAgenda from '../components/WeeklyAgenda';
import ScoreBreakdown from '../components/ScoreBreakdown';

interface Props {
  dataset: Dataset;
  schedule: Schedule | null;
  onScheduleChange: (schedule: Schedule | null) => void;
}

export default function SchedulePage({ dataset, schedule, onScheduleChange }: Props) {
  const [jobId, setJobId] = useState<string | null>(null);
  const [solving, setSolving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [analysis, setAnalysis] = useState<ScoreAnalysis | null>(null);
  const [sessionAnalysis, setSessionAnalysis] = useState<SessionAnalysis[]>([]);
  const [showAnalysis, setShowAnalysis] = useState(true);
  const pollRef = useRef<ReturnType<typeof setInterval> | null>(null);

  function stopPolling() {
    if (pollRef.current) {
      clearInterval(pollRef.current);
      pollRef.current = null;
    }
  }

  useEffect(() => {
    return () => stopPolling();
  }, []);

  async function handleSolve() {
    setError(null);
    try {
      const id = await solve(dataset);
      setJobId(id);
      setSolving(true);

      // Start polling
      stopPolling();
      pollRef.current = setInterval(async () => {
        try {
          const status = await getStatus(id);
          if (status.solverStatus === 'NOT_SOLVING') {
            stopPolling();
            setSolving(false);
            const result = await getSchedule(id);
            onScheduleChange(result);
            // Fetch analysis after solving completes
            try {
              const [scoreAnalysis, sessAnalysis] = await Promise.all([
                getScoreAnalysis(id),
                getSessionAnalysis(id)
              ]);
              setAnalysis(scoreAnalysis);
              setSessionAnalysis(sessAnalysis);
            } catch (e) {
              console.warn('Could not fetch analysis:', e);
            }
          } else {
            // Fetch intermediate best solution
            const result = await getSchedule(id);
            onScheduleChange(result);
            // Also try to fetch analysis during solving
            try {
              const [scoreAnalysis, sessAnalysis] = await Promise.all([
                getScoreAnalysis(id),
                getSessionAnalysis(id)
              ]);
              setAnalysis(scoreAnalysis);
              setSessionAnalysis(sessAnalysis);
            } catch (e) {
              // Ignore errors during intermediate polling
            }
          }
        } catch (e) {
          stopPolling();
          setSolving(false);
          setError(String(e));
        }
      }, 2000);
    } catch (e) {
      setError(String(e));
    }
  }

  async function handleStop() {
    if (!jobId) return;
    try {
      stopPolling();
      const result = await stopSolving(jobId);
      onScheduleChange(result);
      setSolving(false);
    } catch (e) {
      setError(String(e));
    }
  }

  const canSolve =
    dataset.trainers.length > 0 &&
    dataset.teams.length > 0 &&
    dataset.timeSlots.length > 0;

  return (
    <div>
      <div className="flex items-center gap-4 mb-4">
        <h2 className="text-lg font-semibold text-dmon-blue">Schedule</h2>
        {!solving ? (
          <button
            onClick={handleSolve}
            disabled={!canSolve}
            className={`px-4 py-2 text-sm rounded text-white ${
              canSolve
                ? 'bg-dmon-blue hover:bg-dmon-slate'
                : 'bg-gray-300 cursor-not-allowed'
            }`}
          >
            Solve
          </button>
        ) : (
          <button
            onClick={handleStop}
            className="px-4 py-2 text-sm bg-dmon-red text-white rounded hover:bg-dmon-red/80"
          >
            Stop
          </button>
        )}
        {solving && (
          <span className="text-sm text-dmon-blue animate-pulse">
            Solving...
          </span>
        )}
      </div>

      {!canSolve && !schedule && (
        <p className="text-gray-500 text-sm">
          Add at least one trainer, team, and time slot before solving.
        </p>
      )}

      {error && (
        <div className="mb-4 p-3 bg-red-50 border border-dmon-red rounded text-sm text-dmon-red">
          {error}
        </div>
      )}

      {schedule && (
        <div>
          <div className="mb-4 flex gap-4 items-center text-sm">
            <span
              className={`px-2 py-1 rounded ${
                schedule.score?.toString().startsWith('0hard')
                  ? 'bg-dmon-gold/20 text-dmon-gold border border-dmon-gold'
                  : 'bg-yellow-100 text-yellow-800 border border-yellow-300'
              }`}
            >
              Score: {schedule.score?.toString() ?? 'N/A'}
            </span>
            <span className="px-2 py-1 rounded bg-dmon-bg text-dmon-slate border border-dmon-slate">
              Status: {schedule.solverStatus ?? 'N/A'}
            </span>
            <button
              onClick={() => setShowAnalysis(!showAnalysis)}
              className="ml-auto px-3 py-1.5 text-sm bg-dmon-blue text-white rounded hover:bg-dmon-slate"
            >
              {showAnalysis ? 'Hide' : 'Show'} Score Analysis
            </button>
          </div>

          {showAnalysis && (
            <div className="mb-6">
              <ScoreBreakdown analysis={analysis} />
            </div>
          )}

          <WeeklyAgenda
            sessions={schedule.sessions}
            fieldCapacity={dataset.fieldCapacity}
            sessionAnalysis={sessionAnalysis}
          />
        </div>
      )}
    </div>
  );
}
