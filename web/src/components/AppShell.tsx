"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { clearSession, type Session } from "@/lib/api";

// Top navigation for staff pages
export default function AppShell({ session, children }: { session: Session; children: React.ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();

  const links = [
    { href: "/dashboard", label: "Dashboard" },
    { href: "/kiosk", label: "Kiosk" },
    ...(session.role === "ADMIN" ? [{ href: "/admin", label: "Admin" }] : []),
  ];

  function logout() {
    clearSession();
    router.replace("/login");
  }

  return (
    <div className="flex min-h-screen flex-col bg-slate-50">
      <header className="border-b border-slate-200 bg-white print:hidden">
        <div className="mx-auto flex max-w-6xl flex-wrap items-center gap-x-6 gap-y-2 px-4 py-3">
          <span className="font-semibold text-teal-800">Visitor Log</span>
          <nav className="flex gap-1">
            {links.map((link) => (
              <Link
                key={link.href}
                href={link.href}
                className={`rounded-md px-3 py-1.5 text-sm font-medium ${
                  pathname === link.href ? "bg-teal-50 text-teal-800" : "text-slate-600 hover:bg-slate-100"
                }`}
              >
                {link.label}
              </Link>
            ))}
          </nav>
          <div className="ml-auto flex items-center gap-3 text-sm text-slate-500">
            <span>
              {session.username} · {session.role === "ADMIN" ? "Admin" : "Front desk"}
            </span>
            <button onClick={logout} className="rounded-md px-2 py-1 hover:bg-slate-100 hover:text-slate-800">
              Log out
            </button>
          </div>
        </div>
      </header>
      <main className="mx-auto w-full max-w-6xl flex-1 px-4 py-6">{children}</main>
    </div>
  );
}
