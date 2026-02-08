import type { Dataset, Schedule, ScoreAnalysis, SessionAnalysis } from '../types';

const BASE_URL = 'http://localhost:8080/schedule';

export async function solve(dataset: Dataset): Promise<string> {
  const res = await fetch(`${BASE_URL}/solve`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(dataset),
  });
  if (!res.ok) throw new Error(`Solve failed: ${res.statusText}`);
  return res.text();
}

export async function getSchedule(jobId: string): Promise<Schedule> {
  const res = await fetch(`${BASE_URL}/${jobId}`);
  if (!res.ok) throw new Error(`Get schedule failed: ${res.statusText}`);
  return res.json();
}

export async function getStatus(jobId: string): Promise<Schedule> {
  const res = await fetch(`${BASE_URL}/${jobId}/status`);
  if (!res.ok) throw new Error(`Get status failed: ${res.statusText}`);
  return res.json();
}

export async function stopSolving(jobId: string): Promise<Schedule> {
  const res = await fetch(`${BASE_URL}/${jobId}`, { method: 'DELETE' });
  if (!res.ok) throw new Error(`Stop solving failed: ${res.statusText}`);
  return res.json();
}

export async function getScoreAnalysis(jobId: string): Promise<ScoreAnalysis> {
  const res = await fetch(`${BASE_URL}/${jobId}/analysis`);
  if (!res.ok) throw new Error(`Get analysis failed: ${res.statusText}`);
  return res.json();
}

export async function getSessionAnalysis(jobId: string): Promise<SessionAnalysis[]> {
  const res = await fetch(`${BASE_URL}/${jobId}/sessions/analysis`);
  if (!res.ok) throw new Error(`Get session analysis failed: ${res.statusText}`);
  return res.json();
}
