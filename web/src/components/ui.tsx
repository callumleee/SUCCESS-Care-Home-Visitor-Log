// Small shared UI pieces

export function Card({ title, action, children }: { title: string; action?: React.ReactNode; children: React.ReactNode }) {
  return (
    <section className="rounded-xl border border-slate-200 bg-white shadow-sm">
      <div className="flex flex-wrap items-center gap-3 border-b border-slate-100 px-4 py-3">
        <h2 className="font-semibold text-slate-800">{title}</h2>
        {action && <div className="ml-auto flex flex-wrap items-center gap-2">{action}</div>}
      </div>
      <div className="p-4">{children}</div>
    </section>
  );
}

export function ErrorMessage({ message }: { message: string | null }) {
  if (!message) return null;
  return <p className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-700">{message}</p>;
}

export function Loading() {
  return <p className="p-6 text-slate-500">Loading…</p>;
}

export const buttonClass =
  "rounded-md bg-teal-700 px-3 py-1.5 text-sm font-medium text-white hover:bg-teal-800 disabled:opacity-50";
export const secondaryButtonClass =
  "rounded-md border border-slate-300 bg-white px-3 py-1.5 text-sm font-medium text-slate-700 hover:bg-slate-50 disabled:opacity-50";
export const inputClass =
  "rounded-md border border-slate-300 px-3 py-1.5 text-sm focus:border-teal-600 focus:outline-none focus:ring-1 focus:ring-teal-600";
