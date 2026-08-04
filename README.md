<div align="center">

# ⚡ FundSpark
### Crowdfunding & Startup Pitch Platform — Frontend

A modern, responsive frontend prototype for a crowdfunding platform connecting startup founders with investors. Built with **pure HTML, CSS & JavaScript** — no frameworks, no build tools.

<img src="https://img.shields.io/badge/HTML5-E34F26?style=flat-square&logo=html5&logoColor=white" />
<img src="https://img.shields.io/badge/CSS3-1572B6?style=flat-square&logo=css3&logoColor=white" />
<img src="https://img.shields.io/badge/JavaScript-F7DF1E?style=flat-square&logo=javascript&logoColor=black" />
<img src="https://img.shields.io/badge/Chart.js-FF6384?style=flat-square&logo=chartdotjs&logoColor=white" />
<img src="https://img.shields.io/badge/status-frontend%20prototype-blue?style=flat-square" />
<img src="https://img.shields.io/badge/license-MIT-green?style=flat-square" />

</div>

---

## 📖 About

**FundSpark** connects startup founders with investors and supporters. Founders publish
startup pitches with funding goals and campaign deadlines; investors browse, filter,
comment, rate, bookmark, and invest in campaigns; admins verify startups and moderate
the platform.

> This is a **frontend-only academic build** (DBMS mini-project) — all data is realistic
> dummy/mock data with no real backend, authentication, or payment processing. It's
> designed to plug into a **Java + MySQL** backend next.

---

## ✨ Features

- 🎨 Consistent design system — blue → purple gradient, rounded cards, soft shadows, dark mode
- 🔍 Explore page with live search, category/goal/location filters, sorting & pagination
- 📊 Founder, Investor, and Admin dashboards with charts (funding growth, category split, user growth)
- 💬 Startup details page with tabs (Overview, Problem, Solution, Business Model, Team, Timeline, Media, Comments)
- ⭐ Ratings, comments, bookmarking/wishlist, invest & "I'm interested" flows (simulated)
- ✅ Admin campaign approval queue, user management, reports/moderation
- 🌗 Persistent dark mode toggle
- 📱 Fully responsive — desktop, laptop, tablet, mobile
- 🧩 Reusable component system (navbar, sidebar, footer injected via JS — no duplication)

---

## 🗂️ Project Structure

```
fundspark/
├── index.html                  Landing page
├── explore.html                 Search & filter startups
├── startup-details.html         Full campaign profile
├── categories.html
├── about.html · contact.html · faq.html
├── login.html · register.html · forgot-password.html
├── 404.html
│
├── founder/                     Founder dashboard
│   ├── dashboard.html · my-startups.html
│   ├── create-startup.html · edit-startup.html
│   ├── analytics.html · investors.html
│   └── notifications.html · profile.html · settings.html
│
├── investor/                    Investor dashboard
│   ├── dashboard.html · browse.html · wishlist.html
│   ├── investments.html · messages.html
│   └── notifications.html · profile.html · settings.html
│
├── admin/                       Admin dashboard
│   ├── dashboard.html · users.html · startups.html
│   ├── campaign-approval.html · reports.html
│   └── analytics.html · categories.html · settings.html
│
└── assets/
    ├── css/style.css            Design system (tokens, buttons, cards, forms, tables...)
    ├── css/dashboard.css        Sidebar / topbar dashboard shell
    └── js/
        ├── data.js               Dummy startups, users, comments, notifications
        ├── components.js         Injects navbar / footer / sidebar / topbar
        ├── main.js                Dark mode, toasts, counters, tabs, modals, bookmarks
        └── charts.js              Chart.js helper wrappers
```

---

## 🚀 Getting Started

### Option 1 — VS Code + Live Server (recommended)
1. Clone or download this repo
2. Open the folder in VS Code
3. Install the **Live Server** extension
4. Right-click `index.html` → **Open with Live Server**

### Option 2 — Node
```bash
npx serve .
```

### Option 3 — Just open it
Double-click `index.html`. (Some browsers restrict `file://` pages from loading fonts/CDN
scripts — Live Server or `npx serve` is more reliable.)

> ⚠️ Don't move an HTML file out of its folder without its assets — dashboard pages use
> `../assets/...` paths, so `assets/` must stay one level up from `founder/`, `investor/`,
> and `admin/`.

---

## 🎨 Design Tokens

| Token | Value |
|---|---|
| Primary | `#2563EB` |
| Secondary | `#7C3AED` |
| Success | `#22C55E` |
| Warning | `#F59E0B` |
| Danger | `#EF4444` |
| Background | `#F8FAFC` |
| Text | `#0F172A` |
| Headings | Poppins |
| Body | Inter |
| Icons | Lucide |

---

## 🛠️ Tech Stack

- **HTML5 / CSS3 / Vanilla JavaScript** — no framework, no bundler
- **[Chart.js](https://www.chartjs.org/)** — funding, growth & category charts (via CDN)
- **[Lucide Icons](https://lucide.dev/)** — icon set (via CDN)
- **Google Fonts** — Poppins & Inter

Planned backend (not included here): **Java (Spring Boot / Servlets)** + **MySQL**.
Each dummy record shape in `assets/js/data.js` (startups, users, comments, investors,
notifications) maps cleanly to a MySQL table.

---

## 🗺️ Roadmap

- [ ] Connect forms to a real Java + MySQL backend
- [ ] Real authentication (JWT / sessions)
- [ ] File upload handling (logos, pitch videos, business plan PDFs)
- [ ] Real-time notifications
- [ ] Payment gateway integration

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

---

<div align="center">
Built as a DBMS mini-project — frontend prototype only, no real data or transactions.
</div>
