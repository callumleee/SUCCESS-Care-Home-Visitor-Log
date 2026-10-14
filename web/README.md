# Visitor Log web app

Next.js + TypeScript + Tailwind frontend for the visitor log API.

- `/kiosk`: tablet sign-in/sign-out screen for visitors
- `/dashboard`: who's in the building (printable), visit history, CSV export
- `/admin`: stats and charts, residents, staff accounts

```bash
npm install
npm run dev   # http://localhost:3000
```

The API URL defaults to `http://localhost:8080`; set `NEXT_PUBLIC_API_URL` to change it.
