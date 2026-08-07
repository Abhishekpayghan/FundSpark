/* ==========================================================
   FundSpark — Shared Components (navbar / footer / sidebar)
   Reads: FS_BASE / window.FS_BASE ("" or "../"), body[data-active], body[data-role]
   ========================================================== */

(function(){
  const BASE = window.FS_BASE || (typeof FS_BASE !== "undefined" ? FS_BASE : "");
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
    return `
    <nav class="navbar">
      <div class="container">
        <a href="${BASE}index.html" class="brand">
          <span class="mark"><i data-lucide="zap"></i></span> FundSpark
        </a>
        <div class="nav-links">${links}</div>
        <div class="nav-actions">
          <button class="theme-toggle" id="themeToggle" aria-label="Toggle dark mode"><i data-lucide="moon"></i></button>
          <a href="${BASE}login.html" class="btn btn-ghost btn-sm">Log in</a>
          <a href="${BASE}register.html" class="btn btn-primary btn-sm">Get Started</a>
          <button class="nav-burger" id="navBurger" aria-label="Menu"><i data-lucide="menu"></i></button>
        </div>
      </div>
      <div class="mobile-menu" id="mobileMenu">${links}
        <a href="${BASE}login.html">Log in</a>
        <a href="${BASE}register.html">Get Started</a>
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
        ["campaign-approval.html","campaign-approval","badge-check","Campaign Approval",3],
        ["categories.html","categories","shapes","Categories"],
        ["reports.html","reports","flag","Reports"],
        ["group","Account"],
        ["settings.html","settings","settings","Settings"],
      ]
    }
  };

  function sidebarHTML(role){
    const cfg = roleConfig[role];
    const items = cfg.links.map(item=>{
      if(item[0]==="group") return `<div class="side-section-label">${item[1]}</div>`;
      const [href,key,icon,label,count] = item;
      return `<a href="${href}" class="side-link ${active===key?'active':''}">
        <i data-lucide="${icon}"></i><span>${label}</span>${count?`<span class="count">${count}</span>`:``}
      </a>`;
    }).join("");
    return `
    <aside class="sidebar" id="sidebar">
      <a href="${BASE}index.html" class="brand"><span class="mark"><i data-lucide="zap"></i></span> FundSpark</a>
      ${items}
      <div class="sidebar-foot">
        <a href="${BASE}index.html" class="side-link" data-logout><i data-lucide="log-out"></i><span>Log Out</span></a>
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
        ${role !== 'admin' ? `<button type="button" id="topbarNotifications" class="btn-icon icon-btn-bell" aria-label="Notifications"><i data-lucide="bell"></i><span class="dot"></span></button>` : ''}
        <a href="${BASE}index.html" class="btn btn-outline btn-sm logout-topbar" data-logout title="Log Out" aria-label="Log Out"><i data-lucide="log-out"></i><span>Log Out</span></a>
        <div class="user-chip">
          <div class="avatar">${init}</div>
          <div>
            <div class="name">${name}</div>
            <div class="role">${cfg.label}</div>
          </div>
          <i data-lucide="chevron-down" style="width:15px;height:15px;color:var(--text-muted);"></i>
        </div>
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
    }
  };

  document.addEventListener('DOMContentLoaded', function(){
    FS_MOUNT.navbar();
    FS_MOUNT.footer();
    if(window.lucide) lucide.createIcons();
    if(window.FS_AFTER_MOUNT) window.FS_AFTER_MOUNT();
  });
})();
