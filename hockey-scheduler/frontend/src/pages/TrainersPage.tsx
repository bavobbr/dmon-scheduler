import { useState } from 'react';
import type { Trainer, Team, AgeGroup } from '../types';
import { AGE_GROUPS } from '../types';

interface Props {
  trainers: Trainer[];
  teams: Team[];
  onChange: (trainers: Trainer[]) => void;
}

const EMPTY_TRAINER: Omit<Trainer, 'id'> = {
  name: '',
  maxHoursPerWeek: 4,
  trainableAgeGroups: [],
  preferredTeamId: null,
};

export default function TrainersPage({ trainers, teams, onChange }: Props) {
  const [editing, setEditing] = useState<Trainer | null>(null);
  const [form, setForm] = useState<Omit<Trainer, 'id'>>(EMPTY_TRAINER);

  function startAdd() {
    setEditing(null);
    setForm({ ...EMPTY_TRAINER });
  }

  function startEdit(t: Trainer) {
    setEditing(t);
    setForm({
      name: t.name,
      maxHoursPerWeek: t.maxHoursPerWeek,
      trainableAgeGroups: [...t.trainableAgeGroups],
      preferredTeamId: t.preferredTeamId ?? null,
    });
  }

  function save() {
    if (!form.name.trim()) return;
    if (editing) {
      onChange(
        trainers.map((t) =>
          t.id === editing.id ? { ...t, ...form } : t
        )
      );
    } else {
      const id = 'trainer-' + Date.now();
      onChange([...trainers, { id, ...form }]);
    }
    setEditing(null);
    setForm({ ...EMPTY_TRAINER });
  }

  function remove(id: string) {
    onChange(trainers.filter((t) => t.id !== id));
  }

  function toggleAgeGroup(ag: AgeGroup) {
    setForm((f) => ({
      ...f,
      trainableAgeGroups: f.trainableAgeGroups.includes(ag)
        ? f.trainableAgeGroups.filter((g) => g !== ag)
        : [...f.trainableAgeGroups, ag],
    }));
  }

  const [showForm, setShowForm] = useState(false);

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-lg font-semibold text-dmon-blue">Trainers ({trainers.length})</h2>
        <button
          onClick={() => {
            startAdd();
            setShowForm(true);
          }}
          className="px-3 py-1.5 text-sm bg-dmon-blue text-white rounded hover:bg-dmon-slate"
        >
          + Add Trainer
        </button>
      </div>

      {showForm && (
        <div className="mb-6 p-4 border border-dmon-slate rounded bg-white shadow-sm">
          <h3 className="font-medium mb-3">
            {editing ? 'Edit Trainer' : 'New Trainer'}
          </h3>
          <div className="grid grid-cols-2 gap-4 mb-3">
            <div>
              <label className="block text-sm text-gray-600 mb-1">Name</label>
              <input
                type="text"
                value={form.name}
                onChange={(e) => setForm({ ...form, name: e.target.value })}
                className="w-full border rounded px-2 py-1 text-sm"
              />
            </div>
            <div>
              <label className="block text-sm text-gray-600 mb-1">
                Max hours/week
              </label>
              <input
                type="number"
                min={1}
                value={form.maxHoursPerWeek}
                onChange={(e) =>
                  setForm({ ...form, maxHoursPerWeek: parseInt(e.target.value) || 1 })
                }
                className="w-full border rounded px-2 py-1 text-sm"
              />
            </div>
          </div>
          <div className="mb-3">
            <label className="block text-sm text-gray-600 mb-1">
              Trainable age groups
            </label>
            <div className="flex flex-wrap gap-2">
              {AGE_GROUPS.map((ag) => (
                <label
                  key={ag}
                  className={`flex items-center gap-1 px-2 py-1 rounded text-xs border cursor-pointer ${
                    form.trainableAgeGroups.includes(ag)
                      ? 'bg-dmon-blue text-white border-dmon-blue'
                      : 'bg-white border-gray-300'
                  }`}
                >
                  <input
                    type="checkbox"
                    checked={form.trainableAgeGroups.includes(ag)}
                    onChange={() => toggleAgeGroup(ag)}
                    className="sr-only"
                  />
                  {ag}
                </label>
              ))}
            </div>
          </div>
          <div className="mb-4">
            <label className="block text-sm text-gray-600 mb-1">
              Preferred team (optional)
            </label>
            <select
              value={form.preferredTeamId ?? ''}
              onChange={(e) =>
                setForm({ ...form, preferredTeamId: e.target.value || null })
              }
              className="border rounded px-2 py-1 text-sm"
            >
              <option value="">None</option>
              {teams.map((t) => (
                <option key={t.id} value={t.id}>
                  {t.name}
                </option>
              ))}
            </select>
          </div>
          <div className="flex gap-2">
            <button
              onClick={save}
              className="px-3 py-1.5 text-sm bg-dmon-gold text-white rounded hover:bg-dmon-gold/80"
            >
              Save
            </button>
            <button
              onClick={() => setShowForm(false)}
              className="px-3 py-1.5 text-sm bg-gray-200 rounded hover:bg-gray-300"
            >
              Cancel
            </button>
          </div>
        </div>
      )}

      <table className="w-full text-sm border-collapse bg-white shadow-sm rounded overflow-hidden">
        <thead>
          <tr className="bg-dmon-bg">
            <th className="text-left p-2 border border-dmon-slate">Name</th>
            <th className="text-left p-2 border border-dmon-slate">Max hrs/wk</th>
            <th className="text-left p-2 border border-dmon-slate">Age groups</th>
            <th className="text-left p-2 border border-dmon-slate">Preferred team</th>
            <th className="text-left p-2 border border-dmon-slate w-24">Actions</th>
          </tr>
        </thead>
        <tbody>
          {trainers.map((t) => (
            <tr key={t.id} className="hover:bg-dmon-bg">
              <td className="p-2 border border-dmon-slate">{t.name}</td>
              <td className="p-2 border border-dmon-slate">{t.maxHoursPerWeek}</td>
              <td className="p-2 border border-dmon-slate">
                {t.trainableAgeGroups.join(', ')}
              </td>
              <td className="p-2 border border-dmon-slate">
                {teams.find((team) => team.id === t.preferredTeamId)?.name ?? '-'}
              </td>
              <td className="p-2 border border-dmon-slate">
                <button
                  onClick={() => {
                    startEdit(t);
                    setShowForm(true);
                  }}
                  className="text-dmon-blue hover:underline mr-2"
                >
                  Edit
                </button>
                <button
                  onClick={() => remove(t.id)}
                  className="text-dmon-red hover:underline"
                >
                  Delete
                </button>
              </td>
            </tr>
          ))}
          {trainers.length === 0 && (
            <tr>
              <td colSpan={5} className="p-4 text-center text-gray-400 border border-dmon-slate">
                No trainers yet. Click "+ Add Trainer" to get started.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}
