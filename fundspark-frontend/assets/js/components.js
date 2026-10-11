/* ==========================================================
   FundSpark — Shared Components (navbar / footer / sidebar)
   Reads: window.FS_BASE ("" or "../"), body[data-active], body[data-role]
   ========================================================== */

(function(){
  function getBasePath(){
    if(typeof window.FS_BASE !== 'undefined') return window.FS_BASE;
    return /\/(investor|founder|admin)\//.test(location.pathname) ? '../' : '';
  }
  const BASE = getBasePath();
  const active = document.body.getAttribute('data-active') || "";

  const publicLinks = [
    ["index.html","home","Home"],
    ["explore.html","explore","Explore"],
    ["categories.html","categories","Categories"],
    ["about.html","about","About"],
    ["contact.html","contact","Contact"],
    ["faq.html","faq","FAQ"]
  ];

  function navHTML(){
    const links = publicLinks.map(([href,key,label]) =>
      `<a href="${BASE}${href}" class="${active===key?'active':''}">${label}</a>`).join("");

    const session = (window.fsGetSession) ? fsGetSession() : null;
    const authArea = session ? `
          <a href="${BASE}${session.role}/dashboard.html" class="btn btn-ghost btn-sm"><i data-lucide="layout-dashboard" style="width:15px;height:15px;"></i> ${session.fullName.split(' ')[0]}</a>
          <button class="btn btn-danger btn-sm" onclick="fsLogout('${BASE}')">Log Out</button>`
      : `
          <a href="${BASE}login.html" class="btn btn-ghost btn-sm">Log in</a>
          <a href="${BASE}register.html" class="btn btn-primary btn-sm">Get Started</a>`;
    const authAreaMobile = session ? `
        <a href="${BASE}${session.role}/dashboard.html">My Dashboard</a>
        <a href="#" class="btn btn-danger btn-sm" onclick="fsLogout('${BASE}'); return false;">Log Out</a>`
      : `
        <a href="${BASE}login.html">Log in</a>
        <a href="${BASE}register.html">Get Started</a>`;

    return `
    <nav class="navbar">
      <div class="container">
        <a href="${BASE}index.html" class="brand${active==='home' ? ' active' : ''}">
          <span class="mark"><i data-lucide="zap"></i></span> FundSpark
        </a>
        <div class="nav-links">${links}</div>
        <div class="nav-actions">
          <button class="theme-toggle" id="themeToggle" aria-label="Toggle dark mode"><i data-lucide="moon"></i></button>
          ${authArea}
          <button class="nav-burger" id="navBurger" aria-label="Menu"><i data-lucide="menu"></i></button>
        </div>
      </div>
      <div class="mobile-menu" id="mobileMenu">${links}${authAreaMobile}
      </div>
    </nav>`;
  }

  function footerHTML(){
    return `
    <footer class="footer">
      <div class="container">
        <div class="footer-grid">
          <div>
            <a href="${BASE}index.html" class="brand"><span class="mark"><i data-lucide="zap"></i></span> FundSpark</a>
            <p class="text-sm mt-16" style="max-width:280px;">Connecting bold founders with investors who believe in what's next. A DBMS mini-project prototype.</p>
            <div class="social-row mt-16">
              <a href="#"><i data-lucide="twitter"></i></a>
              <a href="#"><i data-lucide="linkedin"></i></a>
              <a href="#"><i data-lucide="instagram"></i></a>
              <a href="#"><i data-lucide="youtube"></i></a>
            </div>
          </div>
          <div>
            <h5>Platform</h5>
            <a href="${BASE}explore.html">Explore Startups</a>
            <a href="${BASE}categories.html">Categories</a>
            <a href="${BASE}register.html">Launch a Startup</a>
            <a href="${BASE}faq.html">How it works</a>
          </div>
          <div>
            <h5>Company</h5>
            <a href="${BASE}about.html">About Us</a>
            <a href="${BASE}contact.html">Contact</a>
            <a href="#">Careers</a>
            <a href="#">Press Kit</a>
          </div>
          <div>
            <h5>Resources</h5>
            <a href="${BASE}faq.html">FAQ</a>
            <a href="#">Founder Guide</a>
            <a href="#">Investor Guide</a>
            <a href="#">Blog</a>
          </div>
          <div>
            <h5>Legal</h5>
            <a href="#">Terms of Service</a>
            <a href="#">Privacy Policy</a>
            <a href="#">Cookie Policy</a>
            <a href="#">Disclaimer</a>
          </div>
        </div>
        <div class="footer-bottom">
          <span>© 2026 FundSpark. Built for academic demonstration — DBMS Mini Project.</span>
          <span>Made with <i data-lucide="heart" style="width:14px;height:14px;color:var(--danger);vertical-align:-2px;"></i> using HTML, CSS &amp; JS</span>
        </div>
      </div>
    </footer>`;
  }

  const roleConfig = {
    founder: {
      label:"Startup Founder",
      links:[
        ["group","Overview"],
        ["dashboard.html","dashboard","layout-dashboard","Dashboard"],
        ["my-startups.html","my-startups","rocket","My Startups"],
        ["create-startup.html","create-startup","plus-circle","Create Startup"],
        ["analytics.html","analytics","bar-chart-3","Campaign Analytics"],
        ["group","Engagement"],
        ["investors.html","investors","users","Investors",5],
        ["notifications.html","notifications","bell","Notifications",3],
        ["group","Account"],
        ["profile.html","profile","user-circle","Profile"],
        ["settings.html","settings","settings","Settings"],
      ]
    },
    investor: {
      label:"Investor",
      links:[
        ["group","Overview"],
        ["dashboard.html","dashboard","layout-dashboard","Dashboard"],
        ["browse.html","browse","search","Browse Startups"],
        ["wishlist.html","wishlist","heart","Wishlist"],
        ["investments.html","investments","wallet","My Investments"],
        ["my-requests.html","my-requests","send","My Requests"],
        ["leaderboard.html","leaderboard","trophy","Leaderboard"],
        ["group","Engagement"],
        ["messages.html","messages","message-square","Messages",2],
        ["notifications.html","notifications","bell","Notifications",4],
        ["group","Account"],
        ["profile.html","profile","user-circle","Profile"],
        ["settings.html","settings","settings","Settings"],
      ]
    },
    admin: {
      label:"Administrator",
      links:[
        ["group","Overview"],
        ["dashboard.html","dashboard","layout-dashboard","Dashboard"],
        ["analytics.html","analytics","bar-chart-3","Analytics"],
        ["group","Management"],
        ["users.html","users","users","Users"],
        ["startups.html","startups","rocket","Startups"],
        ["campaign-approval.html","campaign-approval","badge-check","Campaign Approval",0],
        ["categories.html","categories","shapes","Categories"],
        ["reports.html","reports","flag","Reports"],
        ["deletion-requests.html","deletion-requests","user-x","Deletion Requests",0],
        ["notifications.html","notifications","bell","Notifications"],
        ["group","Account"],
        ["profile.html","profile","user-circle","Profile"],
        ["settings.html","settings","settings","Settings"],
      ]
    }
  };

  function sidebarHTML(role){
    const cfg = roleConfig[role];
    const items = cfg.links.map(item=>{
      if(item[0]==="group") return `<div class="side-section-label">${item[1]}</div>`;
      const [href,key,icon,label,count] = item;
      const countId = key === 'investors' ? ' id="sidebarInvestorBadge"' : (key === 'campaign-approval' ? ' id="sidebarPendingBadge"' : (key === 'deletion-requests' ? ' id="sidebarDeletionBadge"' : ''));
      return `<a href="${href}" class="side-link ${active===key?'active':''}">
        <i data-lucide="${icon}"></i><span>${label}</span>${count!==undefined?`<span class="count"${countId}>${count}</span>`:``}
      </a>`;
    }).join("");
    return `
    <aside class="sidebar" id="sidebar">
      <a href="${BASE}index.html" class="brand"><span class="mark"><i data-lucide="zap"></i></span> FundSpark</a>
      ${items}
      <div class="sidebar-foot">
        <button class="btn btn-danger btn-sm" style="width:100%;" onclick="fsLogout('${BASE}')">Log Out</button>
      </div>
    </aside>
    <div class="sidebar-overlay" id="sidebarOverlay"></div>`;
  }

  function topbarHTML(role,name,init){
    const cfg = roleConfig[role];
    return `
    <div class="topbar">
      <div class="flex items-center gap-16">
        <button class="btn-icon" id="sidebarToggle" style="display:none" aria-label="Menu"><i data-lucide="menu"></i></button>
        <div class="topbar-search input-icon">
          <i data-lucide="search" style="width:17px;height:17px;"></i>
          <input class="form-control" placeholder="Search ${role==='admin'?'users, startups...':'startups, investors...'}">
        </div>
      </div>
      <div class="topbar-actions">
        <button class="theme-toggle" id="themeToggle" aria-label="Toggle dark mode"><i data-lucide="moon"></i></button>
        <button class="btn-icon icon-btn-bell" aria-label="Notifications" onclick="fsGoToNotifications('${role}')"><i data-lucide="bell"></i><span class="dot"></span></button>
        <button class="user-chip" type="button" onclick="fsGoToProfile('${role}')" aria-label="Profile">
          <div class="avatar">${init}</div>
          <div>
            <div class="name">${name}</div>
            <div class="role">${cfg.label}</div>
          </div>
          <i data-lucide="chevron-down" style="width:15px;height:15px;color:var(--text-muted);"></i>
        </button>
      </div>
    </div>`;
  }

  window.FS_MOUNT = {
    navbar(){ const el=document.getElementById('fs-navbar'); if(el) el.innerHTML = navHTML(); },
    footer(){ const el=document.getElementById('fs-footer'); if(el) el.innerHTML = footerHTML(); },
    dashboard(role,name,init){
      const sb = document.getElementById('fs-sidebar');
      const tb = document.getElementById('fs-topbar');
      if(sb) sb.innerHTML = sidebarHTML(role);
      if(tb) tb.innerHTML = topbarHTML(role,name,init);

      const API_BASE = window.FS_API_BASE || (window.localStorage && localStorage.getItem('FS_API_BASE')) || 'http://localhost:8080/api';

      // Dynamically update sidebar investor badge for logged-in founder
      if(role === 'founder' && window.fsGetSession){
        const sess = fsGetSession();
        if(sess && sess.userId){
          fetch(`${API_BASE}/dashboard/founder/${sess.userId}`)
            .then(res => res.ok ? res.json() : null)
            .then(kpi => {
              if(kpi && kpi.investorCount !== undefined){
                const badge = document.getElementById('sidebarInvestorBadge');
                if(badge) badge.textContent = kpi.investorCount;
              }
            }).catch(e => {});
        }
      }

      // Dynamically update sidebar Campaign Approval badge with the REAL pending count
      if(role === 'admin'){
        fetch(`${API_BASE}/dashboard/admin`)
          .then(res => res.ok ? res.json() : null)
          .then(kpi => {
            if(kpi && kpi.pendingApprovals !== undefined){
              const badge = document.getElementById('sidebarPendingBadge');
              if(badge) badge.textContent = kpi.pendingApprovals;
            }
          }).catch(e => {});
          // Pending account deletion requests badge
        fetch(`${API_BASE}/deletion-requests/pending-count`)
         .then(res => res.ok ? res.json() : null)
  .then(d => {
    if(d && d.count !== undefined){
      const badge = document.getElementById('sidebarDeletionBadge');
      if(badge) badge.textContent = d.count;
    }
  }).catch(e => {});
      }
    }
  };

window.fsGoToNotifications = function(role){
      const base = window.FS_BASE || "";
      const path = window.location.pathname;
      if(path.includes(`/${role}/`)){
        window.location.href = `${base}notifications.html`;
      } else {
        window.location.href = `${base}${role}/notifications.html`;
      }
    };

    window.fsGoToProfile = function(role){
      const base = window.FS_BASE || "";
      const path = window.location.pathname;
      if(path.includes(`/${role}/`)){
        window.location.href = `${base}profile.html`;
      } else {
        window.location.href = `${base}${role}/profile.html`;
      }
    };

    document.addEventListener('DOMContentLoaded', function(){
      FS_MOUNT.navbar();
      FS_MOUNT.footer();
    if(window.lucide) lucide.createIcons();
    if(window.FS_AFTER_MOUNT) window.FS_AFTER_MOUNT();
  });
})();