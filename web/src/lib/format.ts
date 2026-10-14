// Formatting helpers. The API sends local care home times without a time zone (e.g. 2026-10-13T14:44:30).

export function formatTime(value: string | null): string {
  if (!value) return "—";
  return new Date(value).toLocaleTimeString([], { hour: "numeric", minute: "2-digit" });
}

export function formatDateTime(value: string): string {
  return new Date(value).toLocaleString([], {
    month: "short",
    day: "numeric",
    hour: "numeric",
    minute: "2-digit",
  });
}

export function formatDuration(minutes: number): string {
  if (minutes < 60) return `${minutes} min`;
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  return m === 0 ? `${h} h` : `${h} h ${m} min`;
}

// Minutes since sign-in, for visitors still in the building
export function minutesSince(value: string): number {
  return Math.max(0, Math.floor((Date.now() - new Date(value).getTime()) / 60000));
}

// Today's date as YYYY-MM-DD in the browser's time zone
export function todayString(): string {
  const d = new Date();
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}
