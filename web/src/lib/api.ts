// Client for the visitor log REST API.
// The login token is kept in localStorage and sent as a Bearer token on every request.

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";
const SESSION_KEY = "visitorlog.session";

export type Role = "FRONT_DESK" | "ADMIN";

export type Session = {
  token: string;
  username: string;
  role: Role;
  expiresAt: string;
};

export type Resident = { id: number; fullName: string; room: string | null; active: boolean };

export type Visit = {
  id: number;
  visitorName: string;
  residentId: number;
  residentName: string;
  room: string | null;
  timeIn: string;
  timeOut: string | null;
  signedIn: boolean;
  durationMinutes: number;
};

export type Stats = {
  totalVisits: number;
  currentlySignedIn: number;
  averageVisitMinutes: number;
  mostVisitedResident: string | null;
  mostFrequentVisitor: string | null;
};

export type StaffUser = { id: number; username: string; role: Role };

export class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
  }
}

// Parses a stored session, returning null if it is missing, invalid, or expired
export function parseSession(raw: string | null): Session | null {
  if (!raw) return null;
  try {
    const session = JSON.parse(raw) as Session;
    return new Date(session.expiresAt) > new Date() ? session : null;
  } catch {
    return null;
  }
}

export function getStoredSession(): string | null {
  return localStorage.getItem(SESSION_KEY);
}

export function getSession(): Session | null {
  return parseSession(getStoredSession());
}

export function clearSession() {
  localStorage.removeItem(SESSION_KEY);
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const session = getSession();
  const headers: Record<string, string> = { "Content-Type": "application/json" };
  if (session) headers.Authorization = `Bearer ${session.token}`;

  const res = await fetch(`${API_URL}${path}`, { ...options, headers });

  if (res.status === 401 && path !== "/api/auth/login") {
    // Token expired or invalid: drop it and do a full reload to the login page.
    clearSession();
    // eslint-disable-next-line @next/next/no-location-assign-relative-destination -- called outside React, so no router is available
    window.location.href = "/login";
  }
  if (!res.ok) {
    const body = await res.json().catch(() => null);
    throw new ApiError(res.status, body?.detail ?? `Request failed (${res.status})`);
  }
  return res.status === 204 ? (undefined as T) : res.json();
}

export async function login(username: string, password: string): Promise<Session> {
  const session = await request<Session>("/api/auth/login", {
    method: "POST",
    body: JSON.stringify({ username, password }),
  });
  localStorage.setItem(SESSION_KEY, JSON.stringify(session));
  return session;
}

export const api = {
  residents: () => request<Resident[]>("/api/residents"),
  residentVisits: (id: number) => request<Visit[]>(`/api/residents/${id}/visits`),
  addResident: (fullName: string, room: string) =>
    request<Resident>("/api/residents", { method: "POST", body: JSON.stringify({ fullName, room: room || null }) }),
  deactivateResident: (id: number) => request<void>(`/api/residents/${id}`, { method: "DELETE" }),

  visits: (params: { date?: string; active?: boolean } = {}) => {
    const query = new URLSearchParams();
    if (params.date) query.set("date", params.date);
    if (params.active) query.set("active", "true");
    const qs = query.toString();
    return request<Visit[]>(`/api/visits${qs ? `?${qs}` : ""}`);
  },
  signIn: (visitorName: string, visitorPhone: string, residentId: number) =>
    request<Visit>("/api/visits", {
      method: "POST",
      body: JSON.stringify({ visitorName, visitorPhone: visitorPhone || null, residentId }),
    }),
  signOut: (visitId: number) => request<Visit>(`/api/visits/${visitId}/signout`, { method: "PATCH" }),

  stats: () => request<Stats>("/api/stats"),
  staff: () => request<StaffUser[]>("/api/staff"),
  addStaff: (username: string, password: string, role: Role) =>
    request<StaffUser>("/api/staff", { method: "POST", body: JSON.stringify({ username, password, role }) }),
};
