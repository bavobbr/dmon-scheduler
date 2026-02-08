import { useRef } from 'react';
import type { Dataset } from '../types';

interface Props {
  getDataset: () => Dataset;
  onImport: (dataset: Dataset) => void;
}

export default function DatasetControls({ getDataset, onImport }: Props) {
  const fileInputRef = useRef<HTMLInputElement>(null);

  function handleExport() {
    const dataset = getDataset();
    const json = JSON.stringify(dataset, null, 2);
    const blob = new Blob([json], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    const date = new Date().toISOString().slice(0, 10);
    a.download = `hockey-dataset-${date}.json`;
    a.click();
    URL.revokeObjectURL(url);
  }

  function handleImport(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = () => {
      try {
        const data = JSON.parse(reader.result as string);
        if (!data.trainers || !data.teams || !data.timeSlots || data.fieldCapacity == null) {
          alert('Invalid dataset: must contain trainers, teams, timeSlots, and fieldCapacity');
          return;
        }
        onImport(data as Dataset);
      } catch {
        alert('Failed to parse JSON file');
      }
    };
    reader.readAsText(file);
    if (fileInputRef.current) fileInputRef.current.value = '';
  }

  return (
    <div className="flex gap-2">
      <button
        onClick={handleExport}
        className="px-3 py-1.5 text-sm bg-white hover:bg-dmon-bg rounded border border-dmon-slate text-dmon-slate"
      >
        Export JSON
      </button>
      <label className="px-3 py-1.5 text-sm bg-white hover:bg-dmon-bg rounded border border-dmon-slate text-dmon-slate cursor-pointer">
        Import JSON
        <input
          ref={fileInputRef}
          type="file"
          accept=".json"
          onChange={handleImport}
          className="hidden"
        />
      </label>
    </div>
  );
}
