/* ==========================================================
   FundSpark — Session & Auth Guards
   Session is stored in localStorage after a real login/register
   call succeeds against the Spring Boot backend. This is a
   lightweight session for a college project — not a JWT/cookie
   session — but it's driven by real, validated backend auth.
   Requires api.js to be loaded first (uses FS_API_BASE etc).
   ========================================================== */

const FS_SESSION_KEY = 'fs_session';

function fsSaveSession(user){
  localStorage.setItem(FS_SESSION_KEY, JSON.stringify(user));
}

function fsGetSession(){
  try{
    const raw = localStorage.getItem(FS_SESSION_KEY);
    return raw ? JSON.parse(raw) : null;
  }catch(e){ return null; }
}

function fsClearSession(){
  localStorage.removeItem(FS_SESSION_KEY);
}

function fsLogout(base){
  fsClearSession();
  fsToast('You have been logged out.','info');
  const fallbackBase = typeof window.FS_BASE !== 'undefined' ? window.FS_BASE : (/\/(investor|founder|admin)\//.test(location.pathname) ? '../' : '');
  setTimeout(()=>{ location.href = (base || fallbackBase || '') + 'index.html'; }, 600);
}

// Call this at the top of any PUBLIC page action (comment, invest, bookmark, "I'm interested")
// that should be blocked for logged-out visitors. Returns the session if logged in;
// if not, shows a toast and redirects to login, returning null so the caller can bail out.
function fsRequireLogin(base){
  const session = fsGetSession();
  if(!session){
    fsToast('Please log in to do that.','error','Login required');
    setTimeout(()=>{ location.href = (base||'') + 'login.html'; }, 900);
    return null;
  }
  return session;
}

// Call this at the top of any INVESTING-related action (Invest Now, I'm Interested, Bookmark).
// Unlike fsRequireLogin, this also checks the account ROLE — founders and admins are
// correctly logged in, but investing/bookmarking is an investor-only action on FundSpark.
// Returns the session only if logged in AND role === 'investor'; otherwise shows a clear
// toast explaining why the action was blocked and returns null so the caller can bail out.
function fsRequireInvestor(base){
  const session = fsGetSession();
  if(!session){
    fsToast('Please log in as an investor to do that.','error','Login required');
    setTimeout(()=>{ location.href = (base||'') + 'login.html'; }, 900);
    return null;
  }
  if(session.role !== 'investor'){
    const roleLabel = session.role.charAt(0).toUpperCase() + session.role.slice(1);
    fsToast(`${roleLabel} accounts can view campaigns but can't invest or bookmark them. Only investor accounts can.`,'error','Action not available');
    return null;
  }
  return session;
}

// Call this at the top of any DASHBOARD page (founder/investor/admin subfolders).
// Redirects to login if not authenticated, or if the logged-in user's role doesn't
// match the dashboard section they're trying to view (e.g. an investor opening
// /admin/dashboard.html). Returns the session on success and mounts the sidebar/topbar
// with the REAL logged-in user's name — replacing the old hardcoded demo names.
function fsInitDashboard(expectedRole){
  const session = fsGetSession();
  if(!session){
    fsToast('Please log in to view your dashboard.','error','Login required');
    setTimeout(()=>{ location.href = '../login.html'; }, 900);
    return null;
  }
  if(session.role !== expectedRole){
    fsToast(`This dashboard is for ${expectedRole}s. You're logged in as ${session.role}.`,'error');
    setTimeout(()=>{ location.href = '../' + session.role + '/dashboard.html'; }, 1200);
    return null;
  }
  const initials = session.avatarInitials || (session.fullName||'??').split(' ').map(w=>w[0]).slice(0,2).join('').toUpperCase();
  if(window.FS_MOUNT) FS_MOUNT.dashboard(expectedRole, session.fullName, initials);
  return session;
}
