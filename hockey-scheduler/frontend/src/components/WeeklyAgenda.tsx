import type { TrainingSession, DayOfWeek, SessionAnalysis } from '../types';
import { DAYS_OF_WEEK, DAY_SHORT } from '../types';

interface Props {
  sessions: TrainingSession[];
  fieldCapacity: number;
  sessionAnalysis?: SessionAnalysis[];
}

export default function WeeklyAgenda({ sessions, fieldCapacity, sessionAnalysis }: Props) {
  const assigned = sessions.filter((s) => s.timeSlot && s.trainer);

  // Create a lookup map for session analysis
  const analysisMap = new Map<string, SessionAnalysis>();
  if (sessionAnalysis) {
    sessionAnalysis.forEach(a => analysisMap.set(a.sessionId, a));
  }

  // Determine hour range from sessions
  const hours = assigned.map((s) => s.timeSlot!.startHour);
  if (hours.length === 0) {
    return <p className="text-gray-500">No sessions assigned yet.</p>;
  }
  const minHour = Math.min(...hours);
  const maxHour = Math.max(...hours);
  const hourRange: number[] = [];
  for (let h = minHour; h <= maxHour; h++) hourRange.push(h);

  // Build lookup: day+hour → sessions
  const lookup = new Map<string, TrainingSession[]>();
  for (const s of assigned) {
    const key = `${s.timeSlot!.dayOfWeek}-${s.timeSlot!.startHour}`;
    if (!lookup.has(key)) lookup.set(key, []);
    lookup.get(key)!.push(s);
  }

  // Which days have sessions?
  const activeDays = DAYS_OF_WEEK.filter((d) =>
    assigned.some((s) => s.timeSlot!.dayOfWeek === d)
  );

  function slotPlayerCount(day: DayOfWeek, hour: number): number {
    const key = `${day}-${hour}`;
    const slotSessions = lookup.get(key) || [];
    return slotSessions.reduce((sum, s) => sum + s.team.size, 0);
  }

  return (
    <div className="overflow-x-auto">
      <table className="w-full text-sm border-collapse">
        <thead>
          <tr>
            <th className="border border-dmon-slate p-2 bg-dmon-bg w-16">Hour</th>
            {activeDays.map((day) => (
              <th key={day} className="border border-dmon-slate p-2 bg-dmon-bg">
                {DAY_SHORT[day]}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {hourRange.map((hour) => (
            <tr key={hour}>
              <td className="border border-dmon-slate p-2 text-center font-medium bg-dmon-bg">
                {hour}:00
              </td>
              {activeDays.map((day) => {
                const key = `${day}-${hour}`;
                const slotSessions = lookup.get(key) || [];
                const totalPlayers = slotPlayerCount(day, hour);
                const overCapacity = totalPlayers > fieldCapacity;
                return (
                  <td
                    key={key}
                    className={`border border-dmon-slate p-1 align-top bg-white ${
                      overCapacity ? 'bg-red-50' : ''
                    }`}
                  >
                    {slotSessions.map((s) => {
                      const analysis = analysisMap.get(s.id);
                      const hasHardViolations = analysis?.violations.some(v => v.level === 'HARD');
                      const hasSoftViolations = analysis?.violations.some(v => v.level === 'SOFT');

                      return (
                        <div
                          key={s.id}
                          className={`mb-1 p-1 rounded text-xs border group relative ${
                            hasHardViolations
                              ? 'bg-red-100 border-dmon-red'
                              : hasSoftViolations
                              ? 'bg-yellow-50 border-yellow-400'
                              : 'bg-dmon-blue/10 border-dmon-blue/30'
                          }`}
                          title={analysis?.violations.map(v => v.message).join('\n') || ''}
                        >
                          <div className={`font-semibold ${hasHardViolations ? 'text-dmon-red' : 'text-dmon-blue'}`}>
                            {s.team.name}
                            {hasHardViolations && ' ⚠️'}
                            {hasSoftViolations && !hasHardViolations && ' ⚡'}
                          </div>
                          <div className="text-dmon-slate">
                            {s.trainer?.name ?? '?'} &middot; {s.team.size}p
                          </div>

                          {/* Violation tooltip on hover */}
                          {analysis?.violations && analysis.violations.length > 0 && (
                            <div className="hidden group-hover:block absolute z-10 left-0 top-full mt-1 w-64 bg-white border-2 border-dmon-red rounded-lg shadow-lg p-2 text-xs">
                              <div className="font-semibold text-dmon-red mb-1">Issues:</div>
                              {analysis.violations.map((v, idx) => (
                                <div key={idx} className={`mb-1 pb-1 ${idx < analysis.violations.length - 1 ? 'border-b' : ''}`}>
                                  <div className={`font-medium ${v.level === 'HARD' ? 'text-dmon-red' : 'text-orange-600'}`}>
                                    {v.level}: {v.constraintName}
                                  </div>
                                  <div className="text-gray-600">{v.message}</div>
                                  <div className="text-gray-400">Score: {v.score}</div>
                                </div>
                              ))}
                            </div>
                          )}
                        </div>
                      );
                    })}
                    {slotSessions.length > 0 && (
                      <div
                        className={`text-xs text-center ${
                          overCapacity ? 'text-dmon-red font-bold' : 'text-gray-400'
                        }`}
                      >
                        {totalPlayers}/{fieldCapacity}
                      </div>
                    )}
                  </td>
                );
              })}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
