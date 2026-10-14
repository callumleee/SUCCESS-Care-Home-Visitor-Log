"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import { ErrorMessage, buttonClass, inputClass } from "@/components/ui";
import { ApiError, login } from "@/lib/api";

export default function LoginPage() {
  const router = useRouter();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      await login(username, password);
      router.replace("/dashboard");
    } catch (err) {
      setError(err instanceof ApiError && err.status === 401 ? "Wrong username or password" : "Could not reach the server");
      setBusy(false);
    }
  }

  return (
    <main className="flex min-h-screen items-center justify-center bg-slate-50 px-4">
      <form onSubmit={handleSubmit} className="w-full max-w-sm space-y-4 rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
        <div>
          <h1 className="text-xl font-semibold text-teal-800">Visitor Log</h1>
          <p className="text-sm text-slate-500">Staff sign in</p>
        </div>
        <label className="block space-y-1 text-sm font-medium text-slate-700">
          <span>Username</span>
          <input className={`${inputClass} w-full`} value={username} onChange={(e) => setUsername(e.target.value)} autoComplete="username" required />
        </label>
        <label className="block space-y-1 text-sm font-medium text-slate-700">
          <span>Password</span>
          <input type="password" className={`${inputClass} w-full`} value={password} onChange={(e) => setPassword(e.target.value)} autoComplete="current-password" required />
        </label>
        <ErrorMessage message={error} />
        <button type="submit" disabled={busy} className={`${buttonClass} w-full py-2`}>
          {busy ? "Signing in…" : "Sign in"}
        </button>
      </form>
    </main>
  );
}
