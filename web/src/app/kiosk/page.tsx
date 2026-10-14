"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { ErrorMessage } from "@/components/ui";
import { ApiError, api, type Resident, type Visit } from "@/lib/api";
import { formatTime } from "@/lib/format";
import { useSession } from "@/lib/useSession";

// Tablet kiosk at the front door. Visitors tap "I'm arriving" or "I'm leaving".
// The tablet stays logged in with a front desk account.

type Screen = { name: "home" } | { name: "arrive" } | { name: "leave" } | { name: "done"; message: string };

const bigButton =
  "rounded-2xl px-6 py-5 text-xl font-semibold shadow-sm transition active:scale-[0.98] disabled:opacity-50";
const bigInput =
  "w-full rounded-xl border-2 border-slate-300 px-4 py-3 text-lg focus:border-teal-600 focus:outline-none";

export default function KioskPage() {
  const session = useSession();
  const [screen, setScreen] = useState<Screen>({ name: "home" });

  // Go back to the start screen a few seconds after a sign-in/out
  useEffect(() => {
    if (screen.name !== "done") return;
    const timer = setTimeout(() => setScreen({ name: "home" }), 5000);
    return () => clearTimeout(timer);
  }, [screen]);

  if (!session) return null;

  return (
    <main className="flex min-h-screen flex-col bg-teal-50">
      <header className="flex items-center justify-between px-6 py-4">
        <h1 className="text-2xl font-bold text-teal-900">Welcome! Please sign in</h1>
        <Link href="/dashboard" className="text-sm text-teal-700 underline">
          Staff
        </Link>
      </header>
      <div className="mx-auto flex w-full max-w-2xl flex-1 flex-col justify-center px-6 pb-10">
        {screen.name === "home" && (
          <div className="grid gap-5 sm:grid-cols-2">
            <button onClick={() => setScreen({ name: "arrive" })} className={`${bigButton} bg-teal-700 py-14 text-white`}>
              I&apos;m arriving
            </button>
            <button onClick={() => setScreen({ name: "leave" })} className={`${bigButton} bg-white py-14 text-teal-800 ring-2 ring-teal-700`}>
              I&apos;m leaving
            </button>
          </div>
        )}
        {screen.name === "arrive" && (
          <ArriveForm onCancel={() => setScreen({ name: "home" })} onDone={(message) => setScreen({ name: "done", message })} />
        )}
        {screen.name === "leave" && (
          <LeaveList onCancel={() => setScreen({ name: "home" })} onDone={(message) => setScreen({ name: "done", message })} />
        )}
        {screen.name === "done" && (
          <div className="rounded-2xl bg-white p-10 text-center shadow-sm">
            <p className="text-3xl font-semibold text-teal-800">{screen.message}</p>
            <button onClick={() => setScreen({ name: "home" })} className={`${bigButton} mt-8 bg-teal-700 text-white`}>
              Done
            </button>
          </div>
        )}
      </div>
    </main>
  );
}

function ArriveForm({ onCancel, onDone }: { onCancel: () => void; onDone: (message: string) => void }) {
  const [residents, setResidents] = useState<Resident[]>([]);
  const [name, setName] = useState("");
  const [phone, setPhone] = useState("");
  const [search, setSearch] = useState("");
  const [resident, setResident] = useState<Resident | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    api.residents().then(setResidents).catch(() => setError("Could not load residents"));
  }, []);

  const matches = residents.filter(
    (r) => r.fullName.toLowerCase().includes(search.toLowerCase()) || (r.room ?? "").includes(search),
  );

  async function submit() {
    if (!resident) return;
    setBusy(true);
    setError(null);
    try {
      await api.signIn(name.trim(), phone, resident.id);
      onDone(`Welcome, ${name.trim()}! Enjoy your visit with ${resident.fullName}.`);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Something went wrong. Please ask the front desk.");
      setBusy(false);
    }
  }

  return (
    <div className="space-y-5 rounded-2xl bg-white p-6 shadow-sm">
      <label className="block space-y-2">
        <span className="text-lg font-medium">Your name</span>
        <input className={bigInput} value={name} onChange={(e) => setName(e.target.value)} autoFocus />
      </label>
      <label className="block space-y-2">
        <span className="text-lg font-medium">
          Phone number <span className="text-slate-400">(optional)</span>
        </span>
        <input className={bigInput} value={phone} onChange={(e) => setPhone(e.target.value)} inputMode="tel" />
      </label>
      <div className="space-y-2">
        <span className="text-lg font-medium">Who are you visiting?</span>
        {resident ? (
          <div className="flex items-center justify-between rounded-xl bg-teal-50 px-4 py-3 text-lg">
            <span>
              {resident.fullName} {resident.room && <span className="text-slate-500">· Room {resident.room}</span>}
            </span>
            <button onClick={() => setResident(null)} className="text-base text-teal-700 underline">
              Change
            </button>
          </div>
        ) : (
          <>
            <input className={bigInput} placeholder="Search by name or room" value={search} onChange={(e) => setSearch(e.target.value)} />
            <div className="max-h-64 space-y-2 overflow-y-auto">
              {matches.map((r) => (
                <button key={r.id} onClick={() => setResident(r)} className="block w-full rounded-xl border border-slate-200 px-4 py-3 text-left text-lg hover:bg-slate-50">
                  {r.fullName} {r.room && <span className="text-slate-500">· Room {r.room}</span>}
                </button>
              ))}
              {matches.length === 0 && <p className="text-slate-500">No residents found</p>}
            </div>
          </>
        )}
      </div>
      <ErrorMessage message={error} />
      <div className="flex gap-3">
        <button onClick={onCancel} className={`${bigButton} flex-1 bg-slate-100 text-slate-700`}>
          Back
        </button>
        <button onClick={submit} disabled={busy || !name.trim() || !resident} className={`${bigButton} flex-[2] bg-teal-700 text-white`}>
          Sign in
        </button>
      </div>
    </div>
  );
}

function LeaveList({ onCancel, onDone }: { onCancel: () => void; onDone: (message: string) => void }) {
  const [visits, setVisits] = useState<Visit[] | null>(null);
  const [search, setSearch] = useState("");
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api.visits({ active: true }).then(setVisits).catch(() => setError("Could not load visitors"));
  }, []);

  const matches = (visits ?? []).filter((v) => v.visitorName.toLowerCase().includes(search.toLowerCase()));

  async function signOut(visit: Visit) {
    setError(null);
    try {
      await api.signOut(visit.id);
      onDone(`Goodbye, ${visit.visitorName}! Thanks for visiting.`);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Something went wrong. Please ask the front desk.");
    }
  }

  return (
    <div className="space-y-4 rounded-2xl bg-white p-6 shadow-sm">
      <input className={bigInput} placeholder="Find your name" value={search} onChange={(e) => setSearch(e.target.value)} autoFocus />
      <ErrorMessage message={error} />
      <div className="max-h-96 space-y-2 overflow-y-auto">
        {visits === null && <p className="text-slate-500">Loading…</p>}
        {visits !== null && matches.length === 0 && <p className="text-slate-500">No one found. Please ask the front desk.</p>}
        {matches.map((v) => (
          <button key={v.id} onClick={() => signOut(v)} className="flex w-full items-center justify-between rounded-xl border border-slate-200 px-4 py-3 text-left text-lg hover:bg-slate-50">
            <span>
              {v.visitorName} <span className="text-slate-500">visiting {v.residentName}</span>
            </span>
            <span className="text-base text-slate-500">since {formatTime(v.timeIn)}</span>
          </button>
        ))}
      </div>
      <button onClick={onCancel} className={`${bigButton} w-full bg-slate-100 text-slate-700`}>
        Back
      </button>
    </div>
  );
}
