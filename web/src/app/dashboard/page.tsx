"use client";

import { useCallback, useEffect, useState } from "react";
import AppShell from "@/components/AppShell";
import { Card, ErrorMessage, Loading, inputClass, secondaryButtonClass } from "@/components/ui";
import { ApiError, api, type Visit } from "@/lib/api";
import { downloadVisitsCsv } from "@/lib/csv";
import { formatDuration, formatTime, minutesSince, todayString } from "@/lib/format";
import { useSession } from "@/lib/useSession";

const REFRESH_MS = 30_000;

export default function DashboardPage() {
  const session = useSession();
  if (!session) return <Loading />;

  return (
    <AppShell session={session}>
      <div className="space-y-6">
        <InBuilding />
        <VisitHistory />
      </div>
    </AppShell>
  );
}

// Everyone currently signed in. Refreshes every 30 seconds and can be printed for evacuations.
function InBuilding() {
  const [visits, setVisits] = useState<Visit[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(() => {
    api.visits({ active: true }).then(setVisits).catch(() => setError("Could not load visitors"));
  }, []);

  useEffect(() => {
    load();
    const timer = setInterval(load, REFRESH_MS);
    return () => clearInterval(timer);
  }, [load]);

  async function signOut(visit: Visit) {
    if (!confirm(`Sign out ${visit.visitorName}?`)) return;
    try {
      await api.signOut(visit.id);
      load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not sign out");
    }
  }

  return (
    <Card
      title={`In the building now${visits ? ` (${visits.length})` : ""}`}
      action={
        <button onClick={() => window.print()} className={`${secondaryButtonClass} print:hidden`}>
          Print list
        </button>
      }
    >
      <ErrorMessage message={error} />
      {visits === null ? (
        <p className="text-slate-500">Loading…</p>
      ) : visits.length === 0 ? (
        <p className="text-slate-500">No visitors in the building.</p>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full whitespace-nowrap text-left text-sm">
            <thead className="text-slate-500">
              <tr>
                <th className="py-2 pr-4 font-medium">Visitor</th>
                <th className="py-2 pr-4 font-medium">Visiting</th>
                <th className="py-2 pr-4 font-medium">Room</th>
                <th className="py-2 pr-4 font-medium">Signed in</th>
                <th className="py-2 pr-4 font-medium">Here for</th>
                <th className="print:hidden" />
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {visits.map((v) => (
                <tr key={v.id}>
                  <td className="py-2 pr-4 font-medium">{v.visitorName}</td>
                  <td className="py-2 pr-4">{v.residentName}</td>
                  <td className="py-2 pr-4">{v.room ?? "—"}</td>
                  <td className="py-2 pr-4">{formatTime(v.timeIn)}</td>
                  <td className="py-2 pr-4">{formatDuration(minutesSince(v.timeIn))}</td>
                  <td className="py-2 text-right print:hidden">
                    <button onClick={() => signOut(v)} className="text-teal-700 hover:underline">
                      Sign out
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </Card>
  );
}

// Visits on a chosen day, with search and CSV export
function VisitHistory() {
  const [date, setDate] = useState(todayString);
  const [search, setSearch] = useState("");
  const [visits, setVisits] = useState<Visit[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    api
      .visits({ date })
      .then((result) => !cancelled && setVisits(result))
      .catch(() => !cancelled && setError("Could not load visits"));
    return () => {
      cancelled = true;
    };
  }, [date]);

  const term = search.toLowerCase();
  const shown = (visits ?? []).filter(
    (v) => v.visitorName.toLowerCase().includes(term) || v.residentName.toLowerCase().includes(term),
  );

  return (
    <Card
      title="Visit history"
      action={
        <>
          <input type="date" value={date} onChange={(e) => setDate(e.target.value)} className={inputClass} />
          <input placeholder="Search visitor or resident" value={search} onChange={(e) => setSearch(e.target.value)} className={inputClass} />
          <button onClick={() => downloadVisitsCsv(shown, `visits-${date}.csv`)} disabled={shown.length === 0} className={secondaryButtonClass}>
            Export CSV
          </button>
        </>
      }
    >
      <ErrorMessage message={error} />
      {visits === null ? (
        <p className="text-slate-500">Loading…</p>
      ) : shown.length === 0 ? (
        <p className="text-slate-500">No visits found.</p>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full whitespace-nowrap text-left text-sm">
            <thead className="text-slate-500">
              <tr>
                <th className="py-2 pr-4 font-medium">Visitor</th>
                <th className="py-2 pr-4 font-medium">Resident</th>
                <th className="py-2 pr-4 font-medium">In</th>
                <th className="py-2 pr-4 font-medium">Out</th>
                <th className="py-2 font-medium">Length</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {shown.map((v) => (
                <tr key={v.id}>
                  <td className="py-2 pr-4 font-medium">{v.visitorName}</td>
                  <td className="py-2 pr-4">{v.residentName}</td>
                  <td className="py-2 pr-4">{formatTime(v.timeIn)}</td>
                  <td className="py-2 pr-4">{v.signedIn ? <span className="text-teal-700">Still here</span> : formatTime(v.timeOut)}</td>
                  <td className="py-2">{v.signedIn ? "—" : formatDuration(v.durationMinutes)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </Card>
  );
}
