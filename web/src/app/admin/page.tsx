"use client";

import { useCallback, useEffect, useState } from "react";
import AppShell from "@/components/AppShell";
import BarChart from "@/components/BarChart";
import { Card, ErrorMessage, Loading, buttonClass, inputClass } from "@/components/ui";
import { ApiError, api, type Resident, type Role, type StaffUser, type Stats, type Visit } from "@/lib/api";
import { formatDuration } from "@/lib/format";
import { useSession } from "@/lib/useSession";

export default function AdminPage() {
  const session = useSession("ADMIN");
  if (!session) return <Loading />;

  return (
    <AppShell session={session}>
      <div className="space-y-6">
        <StatsSection />
        <div className="grid gap-6 lg:grid-cols-2">
          <ResidentsSection />
          <StaffSection />
        </div>
      </div>
    </AppShell>
  );
}

function StatsSection() {
  const [stats, setStats] = useState<Stats | null>(null);
  const [visits, setVisits] = useState<Visit[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    Promise.all([api.stats(), api.visits()])
      .then(([s, v]) => {
        setStats(s);
        setVisits(v);
      })
      .catch(() => setError("Could not load stats"));
  }, []);

  // Number of sign-ins in each hour of the day, e.g. "2 PM": 14
  const byHour = new Map<number, number>();
  for (const v of visits) {
    const hour = new Date(v.timeIn).getHours();
    byHour.set(hour, (byHour.get(hour) ?? 0) + 1);
  }
  const busiestHours = [...byHour.entries()]
    .sort((a, b) => a[0] - b[0])
    .map(([hour, value]) => ({ label: new Date(2000, 0, 1, hour).toLocaleTimeString([], { hour: "numeric" }), value }));

  // Top 8 residents by number of visits
  const byResident = new Map<string, number>();
  for (const v of visits) byResident.set(v.residentName, (byResident.get(v.residentName) ?? 0) + 1);
  const topResidents = [...byResident.entries()]
    .sort((a, b) => b[1] - a[1])
    .slice(0, 8)
    .map(([label, value]) => ({ label, value }));

  return (
    <div className="space-y-4">
      <ErrorMessage message={error} />
      <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
        <StatCard label="Total visits" value={stats ? String(stats.totalVisits) : "…"} />
        <StatCard label="In the building" value={stats ? String(stats.currentlySignedIn) : "…"} />
        <StatCard label="Average visit" value={stats ? formatDuration(Math.round(stats.averageVisitMinutes)) : "…"} />
        <StatCard label="Most visited" value={stats ? (stats.mostVisitedResident ?? "—") : "…"} />
      </div>
      <div className="grid gap-6 lg:grid-cols-2">
        <Card title="Busiest times (sign-ins by hour)">
          <BarChart data={busiestHours} emptyText="No visits yet." />
        </Card>
        <Card title="Visits per resident">
          <BarChart data={topResidents} emptyText="No visits yet." />
        </Card>
      </div>
    </div>
  );
}

function StatCard({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
      <p className="text-sm text-slate-500">{label}</p>
      <p className="mt-1 truncate text-2xl font-semibold text-slate-800" title={value}>
        {value}
      </p>
    </div>
  );
}

function ResidentsSection() {
  const [residents, setResidents] = useState<Resident[] | null>(null);
  const [name, setName] = useState("");
  const [room, setRoom] = useState("");
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(() => {
    api.residents().then(setResidents).catch(() => setError("Could not load residents"));
  }, []);
  useEffect(load, [load]);

  async function add(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      await api.addResident(name.trim(), room.trim());
      setName("");
      setRoom("");
      load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not add resident");
    }
  }

  async function deactivate(resident: Resident) {
    if (!confirm(`Remove ${resident.fullName} from the resident list? Their visit history is kept.`)) return;
    try {
      await api.deactivateResident(resident.id);
      load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not remove resident");
    }
  }

  return (
    <Card title="Residents">
      <form onSubmit={add} className="mb-4 flex flex-wrap gap-2">
        <input placeholder="Full name" value={name} onChange={(e) => setName(e.target.value)} className={`${inputClass} flex-1`} required />
        <input placeholder="Room" value={room} onChange={(e) => setRoom(e.target.value)} className={`${inputClass} w-24`} />
        <button type="submit" className={buttonClass}>
          Add
        </button>
      </form>
      <ErrorMessage message={error} />
      <ul className="divide-y divide-slate-100 text-sm">
        {residents?.map((r) => (
          <li key={r.id} className="flex items-center justify-between py-2">
            <span>
              {r.fullName} {r.room && <span className="text-slate-500">· Room {r.room}</span>}
            </span>
            <button onClick={() => deactivate(r)} className="text-slate-500 hover:text-red-700">
              Remove
            </button>
          </li>
        ))}
      </ul>
    </Card>
  );
}

function StaffSection() {
  const [staff, setStaff] = useState<StaffUser[] | null>(null);
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState<Role>("FRONT_DESK");
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(() => {
    api.staff().then(setStaff).catch(() => setError("Could not load staff"));
  }, []);
  useEffect(load, [load]);

  async function add(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      await api.addStaff(username.trim(), password, role);
      setUsername("");
      setPassword("");
      load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not create account");
    }
  }

  return (
    <Card title="Staff accounts">
      <form onSubmit={add} className="mb-4 flex flex-wrap gap-2">
        <input placeholder="Username" value={username} onChange={(e) => setUsername(e.target.value)} className={`${inputClass} flex-1`} required />
        <input type="password" placeholder="Password (8+ characters)" value={password} onChange={(e) => setPassword(e.target.value)} className={`${inputClass} flex-1`} minLength={8} required />
        <select value={role} onChange={(e) => setRole(e.target.value as Role)} className={inputClass}>
          <option value="FRONT_DESK">Front desk</option>
          <option value="ADMIN">Admin</option>
        </select>
        <button type="submit" className={buttonClass}>
          Add
        </button>
      </form>
      <ErrorMessage message={error} />
      <ul className="divide-y divide-slate-100 text-sm">
        {staff?.map((s) => (
          <li key={s.id} className="flex justify-between py-2">
            <span>{s.username}</span>
            <span className="text-slate-500">{s.role === "ADMIN" ? "Admin" : "Front desk"}</span>
          </li>
        ))}
      </ul>
    </Card>
  );
}
