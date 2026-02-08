import { useState, useCallback } from 'react';
import type { TimeSlot, DayOfWeek } from '../types';
import { DAYS_OF_WEEK, DAY_SHORT } from '../types';

interface Props {
  timeSlots: TimeSlot[];
  fieldCapacity: number;
  onSlotsChange: (slots: TimeSlot[]) => void;
  onCapacityChange: (cap: number) => void;
}

const HOURS = Array.from({ length: 14 }, (_, i) => i + 8); // 8:00 to 21:00

export default function SlotsPage({
  timeSlots,
  fieldCapacity,
  onSlotsChange,
  onCapacityChange,
}: Props) {
  const selectedIds = new Set(timeSlots.map((s) => s.id));

  const [dragging, setDragging] = useState(false);
  const [dragMode, setDragMode] = useState<'add' | 'remove'>('add');

  function slotId(day: DayOfWeek, hour: number) {
    return `${day}-${hour}`;
  }

  function toggleSlot(day: DayOfWeek, hour: number) {
    const id = slotId(day, hour);
    if (selectedIds.has(id)) {
      onSlotsChange(timeSlots.filter((s) => s.id !== id));
    } else {
      onSlotsChange([...timeSlots, { id, dayOfWeek: day, startHour: hour }]);
    }
  }

  const handleDragCell = useCallback(
    (day: DayOfWeek, hour: number) => {
      if (!dragging) return;
      const id = slotId(day, hour);
      if (dragMode === 'add' && !selectedIds.has(id)) {
        onSlotsChange([...timeSlots, { id, dayOfWeek: day, startHour: hour }]);
      } else if (dragMode === 'remove' && selectedIds.has(id)) {
        onSlotsChange(timeSlots.filter((s) => s.id !== id));
      }
    },
    [dragging, dragMode, timeSlots, selectedIds, onSlotsChange]
  );

  function handleMouseDown(day: DayOfWeek, hour: number) {
    const id = slotId(day, hour);
    setDragging(true);
    setDragMode(selectedIds.has(id) ? 'remove' : 'add');
    toggleSlot(day, hour);
  }

  return (
    <div>
      <div className="flex items-center gap-4 mb-4">
        <h2 className="text-lg font-semibold text-dmon-blue">Field & Time Slots</h2>
        <div className="flex items-center gap-2">
          <label className="text-sm text-gray-600">Field capacity:</label>
          <input
            type="number"
            min={1}
            value={fieldCapacity}
            onChange={(e) => onCapacityChange(parseInt(e.target.value) || 1)}
            className="w-20 border rounded px-2 py-1 text-sm"
          />
          <span className="text-xs text-gray-400">players</span>
        </div>
      </div>

      <p className="text-sm text-gray-500 mb-3">
        Click or drag to select/deselect time slots. Selected slots ({timeSlots.length}) are highlighted.
      </p>

      <div
        className="select-none"
        onMouseUp={() => setDragging(false)}
        onMouseLeave={() => setDragging(false)}
      >
        <table className="text-sm border-collapse bg-white shadow-sm rounded overflow-hidden">
          <thead>
            <tr>
              <th className="border border-dmon-slate p-2 bg-dmon-bg w-16">Hour</th>
              {DAYS_OF_WEEK.map((day) => (
                <th key={day} className="border border-dmon-slate p-2 bg-dmon-bg w-20">
                  {DAY_SHORT[day]}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {HOURS.map((hour) => (
              <tr key={hour}>
                <td className="border border-dmon-slate p-2 text-center font-medium bg-dmon-bg">
                  {hour}:00
                </td>
                {DAYS_OF_WEEK.map((day) => {
                  const id = slotId(day, hour);
                  const selected = selectedIds.has(id);
                  return (
                    <td
                      key={id}
                      className={`border border-dmon-slate p-2 text-center cursor-pointer transition-colors ${
                        selected
                          ? 'bg-dmon-gold text-white hover:bg-dmon-gold/80'
                          : 'bg-white hover:bg-dmon-bg'
                      }`}
                      onMouseDown={() => handleMouseDown(day, hour)}
                      onMouseEnter={() => handleDragCell(day, hour)}
                    >
                      {selected ? '\u2713' : ''}
                    </td>
                  );
                })}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
