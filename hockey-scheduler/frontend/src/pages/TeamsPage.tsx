import { useState } from 'react';
import type { Team, AgeGroup, DayOfWeek } from '../types';
import { AGE_GROUPS, DAYS_OF_WEEK, DAY_SHORT } from '../types';

interface Props {
  teams: Team[];
  onChange: (teams: Team[]) => void;
}

const EMPTY_TEAM: Omit<Team, 'id'> = {
  name: '',
  ageGroup: 'U10',
  size: 14,
  trainingsPerWeek: 2,
  availableDays: [],
  earliestHour: 17,
  latestHour: 20,
};

export default function TeamsPage({ teams, onChange }: Props) {
  const [editing, setEditing] = useState<Team | null>(null);
  const [form, setForm] = useState<Omit<Team, 'id'>>(EMPTY_TEAM);
  const [showForm, setShowForm] = useState(false);

  function startAdd() {
    setEditing(null);
    setForm({ ...EMPTY_TEAM, availableDays: [] });
    setShowForm(true);
  }

  function startEdit(t: Team) {
    setEditing(t);
    setForm({
      name: t.name,
      ageGroup: t.ageGroup,
      size: t.size,
      trainingsPerWeek: t.trainingsPerWeek,
      availableDays: [...t.availableDays],
      earliestHour: t.earliestHour,
      latestHour: t.latestHour,
    });
    setShowForm(true);
  }

  function save() {
    if (!form.name.trim()) return;
    if (editing) {
      onChange(teams.map((t) => (t.id === editing.id ? { ...t, ...form } : t)));
    } else {
      const id = 'team-' + Date.now();
      onChange([...teams, { id, ...form }]);
    }
    setShowForm(false);
  }

  function remove(id: string) {
    onChange(teams.filter((t) => t.id !== id));
  }

  function toggleDay(day: DayOfWeek) {
    setForm((f) => ({
      ...f,
      availableDays: f.availableDays.includes(day)
        ? f.availableDays.filter((d) => d !== day)
        : [...f.availableDays, day],
    }));
  }

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-lg font-semibold text-dmon-blue">Teams ({teams.length})</h2>
        <button
          onClick={startAdd}
          className="px-3 py-1.5 text-sm bg-dmon-blue text-white rounded hover:bg-dmon-slate"
        >
          + Add Team
        </button>
      </div>

      {showForm && (
        <div className="mb-6 p-4 border border-dmon-slate rounded bg-white shadow-sm">
          <h3 className="font-medium mb-3">{editing ? 'Edit Team' : 'New Team'}</h3>
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
              <label className="block text-sm text-gray-600 mb-1">Age group</label>
              <select
                value={form.ageGroup}
                onChange={(e) =>
                  setForm({ ...form, ageGroup: e.target.value as AgeGroup })
                }
                className="w-full border rounded px-2 py-1 text-sm"
              >
                {AGE_GROUPS.map((ag) => (
                  <option key={ag} value={ag}>
                    {ag}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-sm text-gray-600 mb-1">
                Player count
              </label>
              <input
                type="number"
                min={1}
                value={form.size}
                onChange={(e) =>
                  setForm({ ...form, size: parseInt(e.target.value) || 1 })
                }
                className="w-full border rounded px-2 py-1 text-sm"
              />
            </div>
            <div>
              <label className="block text-sm text-gray-600 mb-1">
                Trainings/week
              </label>
              <input
                type="number"
                min={1}
                max={7}
                value={form.trainingsPerWeek}
                onChange={(e) =>
                  setForm({
                    ...form,
                    trainingsPerWeek: parseInt(e.target.value) || 1,
                  })
                }
                className="w-full border rounded px-2 py-1 text-sm"
              />
            </div>
            <div>
              <label className="block text-sm text-gray-600 mb-1">
                Earliest hour
              </label>
              <input
                type="number"
                min={6}
                max={22}
                value={form.earliestHour}
                onChange={(e) =>
                  setForm({ ...form, earliestHour: parseInt(e.target.value) || 17 })
                }
                className="w-full border rounded px-2 py-1 text-sm"
              />
            </div>
            <div>
              <label className="block text-sm text-gray-600 mb-1">
                Latest hour
              </label>
              <input
                type="number"
                min={7}
                max={23}
                value={form.latestHour}
                onChange={(e) =>
                  setForm({ ...form, latestHour: parseInt(e.target.value) || 20 })
                }
                className="w-full border rounded px-2 py-1 text-sm"
              />
            </div>
          </div>
          <div className="mb-4">
            <label className="block text-sm text-gray-600 mb-1">
              Available days
            </label>
            <div className="flex flex-wrap gap-2">
              {DAYS_OF_WEEK.map((day) => (
                <label
                  key={day}
                  className={`flex items-center gap-1 px-2 py-1 rounded text-xs border cursor-pointer ${
                    form.availableDays.includes(day)
                      ? 'bg-dmon-blue text-white border-dmon-blue'
                      : 'bg-white border-gray-300'
                  }`}
                >
                  <input
                    type="checkbox"
                    checked={form.availableDays.includes(day)}
                    onChange={() => toggleDay(day)}
                    className="sr-only"
                  />
                  {DAY_SHORT[day]}
                </label>
              ))}
            </div>
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
            <th className="text-left p-2 border border-dmon-slate">Age</th>
            <th className="text-left p-2 border border-dmon-slate">Size</th>
            <th className="text-left p-2 border border-dmon-slate">Train/wk</th>
            <th className="text-left p-2 border border-dmon-slate">Days</th>
            <th className="text-left p-2 border border-dmon-slate">Hours</th>
            <th className="text-left p-2 border border-dmon-slate w-24">Actions</th>
          </tr>
        </thead>
        <tbody>
          {teams.map((t) => (
            <tr key={t.id} className="hover:bg-dmon-bg">
              <td className="p-2 border border-dmon-slate">{t.name}</td>
              <td className="p-2 border border-dmon-slate">{t.ageGroup}</td>
              <td className="p-2 border border-dmon-slate">{t.size}</td>
              <td className="p-2 border border-dmon-slate">{t.trainingsPerWeek}</td>
              <td className="p-2 border border-dmon-slate">
                {t.availableDays.map((d) => DAY_SHORT[d]).join(', ')}
              </td>
              <td className="p-2 border border-dmon-slate">
                {t.earliestHour}:00 - {t.latestHour}:00
              </td>
              <td className="p-2 border border-dmon-slate">
                <button
                  onClick={() => startEdit(t)}
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
          {teams.length === 0 && (
            <tr>
              <td colSpan={7} className="p-4 text-center text-gray-400 border border-dmon-slate">
                No teams yet. Click "+ Add Team" to get started.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}
