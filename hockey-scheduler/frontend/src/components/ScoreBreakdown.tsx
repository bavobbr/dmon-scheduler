import type { ScoreAnalysis } from '../types';

interface Props {
  analysis: ScoreAnalysis | null;
}

export default function ScoreBreakdown({ analysis }: Props) {
  if (!analysis) {
    return null;
  }

  const isFeasible = analysis.hardScore === 0;
  const assignmentRate = analysis.totalSessions > 0
    ? Math.round((analysis.assignedSessions / analysis.totalSessions) * 100)
    : 0;

  // Separate hard and soft constraints
  const hardConstraints = analysis.constraintMatches.filter(c => c.level === 'HARD');
  const softConstraints = analysis.constraintMatches.filter(c => c.level === 'SOFT');

  return (
    <div className="space-y-4">
      {/* Score Summary */}
      <div className="grid grid-cols-3 gap-4">
        <div className="bg-white p-4 rounded-lg border border-dmon-slate shadow-sm">
          <div className="text-sm text-gray-500 mb-1">Hard Score</div>
          <div className={`text-2xl font-bold ${
            analysis.hardScore === 0 ? 'text-dmon-gold' : 'text-dmon-red'
          }`}>
            {analysis.hardScore}
          </div>
          <div className="text-xs text-gray-400 mt-1">
            {analysis.hardScore === 0 ? 'Feasible' : 'Constraints violated'}
          </div>
        </div>

        <div className="bg-white p-4 rounded-lg border border-dmon-slate shadow-sm">
          <div className="text-sm text-gray-500 mb-1">Soft Score</div>
          <div className="text-2xl font-bold text-dmon-blue">
            {analysis.softScore}
          </div>
          <div className="text-xs text-gray-400 mt-1">
            {analysis.softScore < 0 ? 'Penalties' : 'Optimized'}
          </div>
        </div>

        <div className="bg-white p-4 rounded-lg border border-dmon-slate shadow-sm">
          <div className="text-sm text-gray-500 mb-1">Assignment Rate</div>
          <div className="text-2xl font-bold text-dmon-blue">
            {assignmentRate}%
          </div>
          <div className="text-xs text-gray-400 mt-1">
            {analysis.assignedSessions}/{analysis.totalSessions} sessions
          </div>
        </div>
      </div>

      {/* Overall Status */}
      {!isFeasible && (
        <div className="bg-red-50 border border-dmon-red rounded-lg p-4">
          <div className="flex items-start gap-2">
            <svg className="w-5 h-5 text-dmon-red mt-0.5" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
            </svg>
            <div>
              <div className="font-semibold text-dmon-red">Infeasible Solution</div>
              <div className="text-sm text-red-700 mt-1">
                Some hard constraints are violated. The schedule cannot be used in its current form.
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Hard Constraints */}
      {hardConstraints.length > 0 && (
        <div className="bg-white rounded-lg border border-dmon-slate shadow-sm p-4">
          <h3 className="font-semibold text-dmon-blue mb-3">
            Hard Constraints {isFeasible ? '✓ All satisfied' : `(${hardConstraints.filter(c => c.score < 0).length} violated)`}
          </h3>
          <div className="space-y-2">
            {hardConstraints.map((constraint, idx) => (
              <div
                key={idx}
                className={`flex items-center justify-between p-2 rounded ${
                  constraint.score < 0 ? 'bg-red-50' : 'bg-green-50'
                }`}
              >
                <div className="flex-1">
                  <div className="font-medium text-sm">{constraint.constraintName}</div>
                  <div className="text-xs text-gray-500">
                    {constraint.matchCount} {constraint.matchCount === 1 ? 'match' : 'matches'}
                  </div>
                </div>
                <div className={`text-sm font-bold ${
                  constraint.score < 0 ? 'text-dmon-red' : 'text-green-600'
                }`}>
                  {constraint.score}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Soft Constraints */}
      {softConstraints.length > 0 && (
        <div className="bg-white rounded-lg border border-dmon-slate shadow-sm p-4">
          <h3 className="font-semibold text-dmon-blue mb-3">
            Soft Constraints (Optimization)
          </h3>
          <div className="space-y-2">
            {softConstraints.map((constraint, idx) => (
              <div
                key={idx}
                className="flex items-center justify-between p-2 rounded bg-gray-50"
              >
                <div className="flex-1">
                  <div className="font-medium text-sm">{constraint.constraintName}</div>
                  <div className="text-xs text-gray-500">
                    {constraint.matchCount} {constraint.matchCount === 1 ? 'match' : 'matches'}
                  </div>
                </div>
                <div className={`text-sm font-bold ${
                  constraint.score < 0 ? 'text-orange-600' : 'text-green-600'
                }`}>
                  {constraint.score}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* No Constraints Info */}
      {analysis.constraintMatches.length === 0 && (
        <div className="bg-gray-50 border border-gray-200 rounded-lg p-4 text-center text-gray-500 text-sm">
          No constraint violations detected or score not yet calculated.
        </div>
      )}
    </div>
  );
}
