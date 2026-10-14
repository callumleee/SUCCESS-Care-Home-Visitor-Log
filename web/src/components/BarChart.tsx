// Simple horizontal bar chart built with plain HTML/CSS (no chart library)
export default function BarChart({ data, emptyText }: { data: { label: string; value: number }[]; emptyText: string }) {
  if (data.length === 0) return <p className="text-sm text-slate-500">{emptyText}</p>;
  const max = Math.max(...data.map((d) => d.value));

  return (
    <div className="space-y-1.5">
      {data.map((d) => (
        <div key={d.label} className="flex items-center gap-3 text-sm">
          <span className="w-28 shrink-0 truncate text-slate-600" title={d.label}>
            {d.label}
          </span>
          <div className="h-5 flex-1 rounded bg-slate-100">
            <div className="h-5 rounded bg-teal-600" style={{ width: `${(d.value / max) * 100}%` }} />
          </div>
          <span className="w-8 text-right tabular-nums text-slate-700">{d.value}</span>
        </div>
      ))}
    </div>
  );
}
