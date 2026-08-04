# FundSpark — Crowdfunding & Startup Pitch Platform (Frontend)

A complete, production-quality **frontend prototype** for a DBMS mini-project: a crowdfunding
platform connecting startup founders with investors. Built with **plain HTML, CSS, and
JavaScript only** — no framework, no build step. All data is realistic dummy/mock data
(see `assets/js/data.js`); there is no backend, authentication, or payment integration.

Later, plug in **Java (Spring/Servlets) + MySQL** for the backend — the HTML forms, IDs, and
data shapes here are designed to map cleanly onto typical DB tables (`startups`, `users`,
`investments`, `comments`, `categories`, `notifications`, etc).

## How to run
No build tools needed. Just open `index.html` in a browser, or serve the folder with any
static server, e.g.:
```
npx serve .
```
(Recommended over opening as a raw `file://` path in case your browser blocks fetch/module
features — though this project avoids those, so plain double-click works too.)

## Folder structure
```
fundspark/
├── index.html                 Landing page
├── explore.html                Explore / search / filter startups
├── startup-details.html        Full startup profile (tabs, gallery, comments, invest widget)
├── categories.html             All startup categories
├── about.html / contact.html / faq.html
├── login.html / register.html / forgot-password.html
├── 404.html
├── founder/                    Founder dashboard area
│   ├── dashboard.html  my-startups.html  create-startup.html  edit-startup.html
│   ├── analytics.html  investors.html  notifications.html  profile.html  settings.html
├── investor/                   Investor dashboard area
│   ├── dashboard.html  browse.html  wishlist.html  investments.html
│   ├── messages.html  notifications.html  profile.html  settings.html
├── admin/                      Admin dashboard area
│   ├── dashboard.html  users.html  startups.html  campaign-approval.html
│   ├── reports.html  analytics.html  categories.html  settings.html
└── assets/
    ├── css/style.css           Design system: tokens, buttons, cards, forms, tables, tabs...
    ├── css/dashboard.css       Sidebar/topbar dashboard shell layout
    └── js/
        ├── data.js             Dummy startups, users, notifications, comments...
        ├── components.js       Injects navbar / footer / sidebar / topbar into every page
        ├── main.js              Dark mode, toasts, counters, tabs, accordion, modal, bookmarks
        └── charts.js            Chart.js wrapper helpers styled to the FundSpark palette
```

## Design system
- **Colors:** Primary `#2563EB`, Secondary `#7C3AED`, Success `#22C55E`, Warning `#F59E0B`,
  Danger `#EF4444`, Background `#F8FAFC`, on a blue→purple gradient.
- **Type:** Poppins (headings) + Inter (body), loaded from Google Fonts.
- **Icons:** Lucide (via CDN `<script>`), initialized on every page load.
- **Charts:** Chart.js (via CDN), wrapped in `assets/js/charts.js` helpers.
- Dark mode toggle persists via `localStorage`; every page respects it.

## Notes for the backend phase
- Each dummy record shape in `data.js` (startup, user, comment, investor, notification) maps
  to a natural MySQL table.
- Forms already have sensible `name`-able fields (amounts, dates, selects) ready to be wired
  to real POST endpoints.
- All "successful" actions (invest, comment, approve, register, login) currently just show a
  toast notification and/or redirect — swap those handlers for real `fetch()` calls to your
  Java backend when ready.
