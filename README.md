⚡ FundSpark
Crowdfunding & Startup Pitch Platform — Frontend

A modern, responsive frontend prototype for a crowdfunding platform connecting startup founders with investors. Built with pure HTML, CSS & JavaScript — no frameworks, no build tools.

<img src="https://img.shields.io/badge/HTML5-E34F26?style=flat-square&logo=html5&logoColor=white" /> <img src="https://img.shields.io/badge/CSS3-1572B6?style=flat-square&logo=css3&logoColor=white" /> <img src="https://img.shields.io/badge/JavaScript-F7DF1E?style=flat-square&logo=javascript&logoColor=black" /> <img src="https://img.shields.io/badge/Chart.js-FF6384?style=flat-square&logo=chartdotjs&logoColor=white" /> <img src="https://img.shields.io/badge/status-frontend%20prototype-blue?style=flat-square" /> <img src="https://img.shields.io/badge/license-MIT-green?style=flat-square" /> </div>
📖 About

FundSpark connects startup founders with investors and supporters. Founders publish startup pitches with funding goals and campaign deadlines; investors browse, filter, comment, rate, bookmark, and invest in campaigns; admins verify startups and moderate the platform.

This is a frontend-only academic build (DBMS mini-project) — all data is realistic dummy/mock data with no real backend, authentication, or payment processing. It's designed to plug into a Java + MySQL backend next.

✨ Features
🎨 Consistent design system — blue → purple gradient, rounded cards, soft shadows, dark mode
🔍 Explore page with live search, category/goal/location filters, sorting & pagination
📊 Founder, Investor, and Admin dashboards with charts (funding growth, category split, user growth)
💬 Startup details page with tabs (Overview, Problem, Solution, Business Model, Team, Timeline, Media, Comments)
⭐ Ratings, comments, bookmarking/wishlist, invest & "I'm interested" flows (simulated)
✅ Admin campaign approval queue, user management, reports/moderation
🌗 Persistent dark mode toggle
📱 Fully responsive — desktop, laptop, tablet, mobile
🧩 Reusable component system (navbar, sidebar, footer injected via JS — no duplication)
🗂️ Project Structure
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
