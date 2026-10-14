"use client";

import { useRouter } from "next/navigation";
import { useEffect, useMemo, useSyncExternalStore } from "react";
import { getStoredSession, parseSession, type Role, type Session } from "./api";

// The session lives in localStorage, which React reads as an external store.
// On the server there is no localStorage, so the session is null until the page loads in the browser.
function subscribe(onChange: () => void) {
  window.addEventListener("storage", onChange);
  return () => window.removeEventListener("storage", onChange);
}

// Returns the logged-in staff session, redirecting to /login if there is none.
// If requiredRole is ADMIN and the user is front desk, redirects to the dashboard.
export function useSession(requiredRole?: Role): Session | null {
  const router = useRouter();
  const raw = useSyncExternalStore(subscribe, getStoredSession, () => undefined);
  const session = useMemo(() => (raw === undefined ? null : parseSession(raw)), [raw]);
  const allowed = session !== null && (requiredRole !== "ADMIN" || session.role === "ADMIN");

  useEffect(() => {
    if (raw === undefined) return; // still rendering on the server
    if (!session) router.replace("/login");
    else if (!allowed) router.replace("/dashboard");
  }, [raw, session, allowed, router]);

  return allowed ? session : null;
}
