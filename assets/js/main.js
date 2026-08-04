/* ==========================================================
   FundSpark — Shared Behaviors
   ========================================================== */

/* ---------- Theme (dark mode) ---------- */
(function(){
  const saved = localStorage.getItem('fs-theme');
  if(saved==='dark') document.documentElement.setAttribute('data-theme','dark');
})();

function fsToggleTheme(){
  const html = document.documentElement;
  const isDark = html.getAttribute('data-theme')==='dark';
  html.setAttribute('data-theme', isDark ? 'light' : 'dark');
  localStorage.setItem('fs-theme', isDark ? 'light' : 'dark');
  document.querySelectorAll('#themeToggle i').forEach(i=>{
    i.setAttribute('data-lucide', isDark ? 'moon' : 'sun');
  });
  if(window.lucide) lucide.createIcons();
}

function fsLogout(e){
  if(e) e.preventDefault();
  localStorage.removeItem('fs-user');
  sessionStorage.removeItem('fs-user');
  fsToast('Logged out successfully.','success');
  const base = window.FS_BASE || (typeof FS_BASE !== 'undefined' ? FS_BASE : '');
  setTimeout(()=>{ location.href = base + 'index.html'; }, 450);
}

/* ---------- Toasts ---------- */
function fsToast(message, type='info', title=''){
  let stack = document.querySelector('.toast-stack');
  if(!stack){
    stack = document.createElement('div');
    stack.className = 'toast-stack';
    document.body.appendChild(stack);
  }
  const icons = {success:'check-circle', error:'x-circle', info:'info'};
  const el = document.createElement('div');
  el.className = `toast ${type}`;
  el.innerHTML = `<i data-lucide="${icons[type]||'bell'}" style="width:18px;height:18px;flex-shrink:0;"></i>
    <div>${title?`<strong style="display:block;font-size:13.5px;">${title}</strong>`:''}<span style="color:var(--text-muted);font-weight:400;">${message}</span></div>`;
  stack.appendChild(el);
  if(window.lucide) lucide.createIcons();
  setTimeout(()=>{ el.style.opacity='0'; el.style.transform='translateY(12px)'; el.style.transition='.3s ease'; setTimeout(()=>el.remove(),300); }, 3500);
}

/* ---------- Animated counters ---------- */
function fsAnimateCounters(root=document){
  const els = root.querySelectorAll('[data-count]');
  const obs = new IntersectionObserver((entries)=>{
    entries.forEach(entry=>{
      if(!entry.isIntersecting) return;
      const el = entry.target;
      const target = parseFloat(el.getAttribute('data-count'));
      const suffix = el.getAttribute('data-suffix')||'';
      const prefix = el.getAttribute('data-prefix')||'';
      const decimals = el.getAttribute('data-decimals') ? parseInt(el.getAttribute('data-decimals')) : 0;
      let start = 0;
      const dur = 1400;
      const t0 = performance.now();
      function step(t){
        const p = Math.min(1,(t-t0)/dur);
        const eased = 1 - Math.pow(1-p,3);
        const val = start + (target-start)*eased;
        el.textContent = prefix + val.toFixed(decimals) + suffix;
        if(p<1) requestAnimationFrame(step);
      }
      requestAnimationFrame(step);
      obs.unobserve(el);
    });
  }, {threshold:.4});
  els.forEach(el=>obs.observe(el));
}

/* ---------- Progress bar animation ---------- */
function fsAnimateProgressBars(root=document){
  const bars = root.querySelectorAll('.progress-bar[data-target]');
  const obs = new IntersectionObserver((entries)=>{
    entries.forEach(entry=>{
      if(!entry.isIntersecting) return;
      requestAnimationFrame(()=> entry.target.style.width = entry.target.getAttribute('data-target') + '%');
      obs.unobserve(entry.target);
    });
  }, {threshold:.3});
  bars.forEach(b=>obs.observe(b));
}

/* ---------- Scroll reveal ---------- */
function fsInitReveal(){
  const els = document.querySelectorAll('.reveal');
  const obs = new IntersectionObserver((entries)=>{
    entries.forEach(entry=>{ if(entry.isIntersecting){ entry.target.classList.add('in'); obs.unobserve(entry.target);} });
  }, {threshold:.15});
  els.forEach(el=>obs.observe(el));
}

/* ---------- Tabs ---------- */
function fsInitTabs(){
  document.querySelectorAll('.tabs').forEach(tabgroup=>{
    const target = tabgroup.getAttribute('data-target');
    const panelWrap = document.querySelector(target);
    tabgroup.querySelectorAll('.tab-btn').forEach(btn=>{
      btn.addEventListener('click', ()=>{
        tabgroup.querySelectorAll('.tab-btn').forEach(b=>b.classList.remove('active'));
        btn.classList.add('active');
        const panelId = btn.getAttribute('data-tab');
        panelWrap.querySelectorAll('.tab-panel').forEach(p=>p.classList.remove('active'));
        panelWrap.querySelector('#'+panelId).classList.add('active');
      });
    });
  });
}

/* ---------- Accordion ---------- */
function fsInitAccordion(){
  document.querySelectorAll('.accordion-head').forEach(head=>{
    head.addEventListener('click', ()=>{
      const item = head.closest('.accordion-item');
      const wasOpen = item.classList.contains('open');
      item.parentElement.querySelectorAll('.accordion-item').forEach(i=>i.classList.remove('open'));
      if(!wasOpen) item.classList.add('open');
    });
  });
}

/* ---------- Modal ---------- */
function fsOpenModal(id){ document.getElementById(id)?.classList.add('open'); }
function fsCloseModal(id){ document.getElementById(id)?.classList.remove('open'); }
document.addEventListener('click', (e)=>{
  if(e.target.classList.contains('modal-overlay')) e.target.classList.remove('open');
});

/* ---------- Bookmark toggle ---------- */
function fsToggleBookmark(btn, name){
  btn.classList.toggle('active');
  const isActive = btn.classList.contains('active');
  const icon = btn.querySelector('i');
  if(icon){ icon.setAttribute('fill', isActive ? 'currentColor' : 'none'); }
  fsToast(isActive ? `${name||'Startup'} added to your wishlist.` : `${name||'Startup'} removed from wishlist.`, isActive?'success':'info');
}

/* ---------- Mobile nav + sidebar toggle ---------- */
document.addEventListener('click', (e)=>{
  if(e.target.closest('#navBurger')){
    document.getElementById('mobileMenu')?.classList.toggle('open');
  }
  if(e.target.closest('#themeToggle')){
    fsToggleTheme();
  }
  if(e.target.closest('[data-logout]')){
    fsLogout(e);
  }
  if(e.target.closest('#sidebarToggle')){
    document.getElementById('sidebar')?.classList.toggle('open');
    document.getElementById('sidebarOverlay')?.classList.toggle('open');
  }
  if(e.target.closest('#sidebarOverlay')){
    document.getElementById('sidebar')?.classList.remove('open');
    document.getElementById('sidebarOverlay')?.classList.remove('open');
  }
});

function fsShowSidebarToggleOnMobile(){
  const btn = document.getElementById('sidebarToggle');
  function check(){ if(btn) btn.style.display = window.innerWidth<=960 ? 'flex' : 'none'; }
  check();
  window.addEventListener('resize', check);
}

/* ---------- Init on load ---------- */
window.addEventListener('load', ()=>{
  fsAnimateCounters();
  fsAnimateProgressBars();
  fsInitReveal();
  fsInitTabs();
  fsInitAccordion();
  if(window.lucide) lucide.createIcons();
  const themeIcon = document.querySelector('#themeToggle i');
  if(themeIcon) themeIcon.setAttribute('data-lucide', document.documentElement.getAttribute('data-theme')==='dark'?'sun':'moon');
  if(window.lucide) lucide.createIcons();
  fsShowSidebarToggleOnMobile();
});
