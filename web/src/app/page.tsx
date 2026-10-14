"use client";

import { useRouter } from "next/navigation";
import { useEffect } from "react";
import { getSession } from "@/lib/api";

// Sends staff to the dashboard if logged in, otherwise to the login page
export default function Home() {
  const router = useRouter();
  useEffect(() => {
    router.replace(getSession() ? "/dashboard" : "/login");
  }, [router]);
  return null;
}
