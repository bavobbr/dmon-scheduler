export const AGE_GROUPS = [
  'U6', 'U7', 'U8', 'U10', 'U11', 'U12', 'U14', 'U16', 'U19', 'SENIOR'
] as const;

export type AgeGroup = typeof AGE_GROUPS[number];

export const DAYS_OF_WEEK = [
  'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'
] as const;

export type DayOfWeek = typeof DAYS_OF_WEEK[number];

export const DAY_SHORT: Record<DayOfWeek, string> = {
  MONDAY: 'Mon',
  TUESDAY: 'Tue',
  WEDNESDAY: 'Wed',
  THURSDAY: 'Thu',
  FRIDAY: 'Fri',
  SATURDAY: 'Sat',
  SUNDAY: 'Sun',
};

export interface Trainer {
  id: string;
  name: string;
  maxHoursPerWeek: number;
  trainableAgeGroups: AgeGroup[];
  preferredTeamId?: string | null;
}

export interface Team {
  id: string;
  name: string;
  ageGroup: AgeGroup;
  size: number;
  trainingsPerWeek: number;
  availableDays: DayOfWeek[];
  earliestHour: number;
  latestHour: number;
}

export interface TimeSlot {
  id: string;
  dayOfWeek: DayOfWeek;
  startHour: number;
}

export interface Dataset {
  trainers: Trainer[];
  teams: Team[];
  timeSlots: TimeSlot[];
  fieldCapacity: number;
}

export interface TrainingSession {
  id: string;
  team: Team;
  timeSlot: TimeSlot | null;
  trainer: Trainer | null;
}

export interface Schedule {
  trainers: Trainer[];
  teams: Team[];
  timeSlots: TimeSlot[];
  fieldConfigs: { id: string; capacity: number }[];
  sessions: TrainingSession[];
  score: string | null;
  solverStatus: 'SOLVING_ACTIVE' | 'NOT_SOLVING' | null;
}

export interface ScoreAnalysis {
  hardScore: number;
  softScore: number;
  assignedSessions: number;
  unassignedSessions: number;
  totalSessions: number;
  constraintMatches: ConstraintMatchInfo[];
}

export interface ConstraintMatchInfo {
  constraintName: string;
  constraintPackage: string;
  score: number;
  matchCount: number;
  level: 'HARD' | 'SOFT';
}

export interface SessionAnalysis {
  sessionId: string;
  teamName: string;
  trainerName: string;
  timeSlotInfo: string;
  hasViolations: boolean;
  totalScore: number;
  violations: ViolationInfo[];
}

export interface ViolationInfo {
  constraintName: string;
  level: 'HARD' | 'SOFT';
  score: number;
  message: string;
}
