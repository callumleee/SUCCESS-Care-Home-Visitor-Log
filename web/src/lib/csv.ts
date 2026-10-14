import type { Visit } from "./api";

// Downloads the given visits as a CSV file (opens in Excel/Sheets)
export function downloadVisitsCsv(visits: Visit[], filename: string) {
  const header = ["Visitor", "Resident", "Room", "Time in", "Time out", "Minutes"];
  const rows = visits.map((v) => [
    v.visitorName,
    v.residentName,
    v.room ?? "",
    v.timeIn,
    v.timeOut ?? "",
    v.signedIn ? "" : String(v.durationMinutes),
  ]);
  const escape = (cell: string) => (/[",\n]/.test(cell) ? `"${cell.replace(/"/g, '""')}"` : cell);
  const csv = [header, ...rows].map((row) => row.map(escape).join(",")).join("\n");

  const url = URL.createObjectURL(new Blob([csv], { type: "text/csv" }));
  const link = document.createElement("a");
  link.href = url;
  link.download = filename;
  link.click();
  URL.revokeObjectURL(url);
}
