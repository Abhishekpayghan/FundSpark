/* ==========================================================
   FundSpark — Live Backend API Layer
   Talks to the Spring Boot backend at FS_API_BASE.
   Every function falls back to null on failure so calling code
   can gracefully fall back to assets/js/data.js dummy data —
   this means the site still works/demos fine even if the
   backend server isn't running.
   ========================================================== */

const FS_API_BASE = "http://localhost:8080/api";

// category name -> brand color, used since the DB doesn't store a color per startup
const FS_CATEGORY_COLORS = {
  "Artificial Intelligence": "#2563EB", "Healthcare": "#EF4444", "Agriculture": "#22C55E",
  "Education": "#7C3AED", "FinTech": "#2563EB", "Robotics": "#F59E0B", "Food": "#F59E0B",
  "Travel": "#0EA5E9", "Gaming": "#F59E0B", "Cybersecurity": "#0EA5E9", "E-commerce": "#EF4444",
  "Renewable Energy": "#22C55E", "IoT": "#2563EB", "SaaS": "#2563EB", "Blockchain": "#7C3AED",
  "Social Impact": "#22C55E"
};

function fsInitials(name){
  return (name || "??").split(" ").map(w=>w[0]).filter(Boolean).slice(0,2).join("").toUpperCase();
}

// Transforms a backend feed row into the exact shape the frontend cards/pages expect
/* ==========================================================
   FundSpark — Bug fix: fsMapFeedRow() was dropping the new
   valuation / founderEquity fields even though the backend
   now sends them. Find fsMapFeedRow() in assets/js/api.js
   and REPLACE it with this version.
   ========================================================== */

function fsMapFeedRow(r){
  return {
    id: r.id, name: r.name, founder: r.founder, category: r.category,
    tagline: r.tagline, description: r.description,
    goal: Number(r.goal), raised: Number(r.raised),
    location: r.location, score: r.score, status: r.status,
    verified: !!r.verified, trending: !!r.trending,
    days: r.days != null ? Math.max(0, r.days) : 0,
    rating: 4.5, reviews: 0,                          // ratings/reviews aggregation endpoint not built yet — safe defaults
    color: FS_CATEGORY_COLORS[r.category] || "#2563EB",
    logo: fsInitials(r.name),
    // ==== NEW: equity feature fields ====
    valuation: r.valuation != null ? Number(r.valuation) : null,
    founderEquity: r.founderEquity != null ? Number(r.founderEquity) : null
  };
}

// GET all startups from the live backend. Returns an array, or null on failure.
async function fsApiGetStartups(){
  try{
    const res = await fetch(`${FS_API_BASE}/startups/feed`);
    if(!res.ok) throw new Error('Bad response');
    const rows = await res.json();
    return rows.map(fsMapFeedRow);
  }catch(err){
    console.warn('FundSpark API unavailable, using local dummy data instead.', err);
    return null;
  }
}

// GET a single startup by id from the live backend. Returns an object, or null on failure.
async function fsApiGetStartup(id){
  try{
    const res = await fetch(`${FS_API_BASE}/startups/feed/${id}`);
    if(!res.ok) throw new Error('Bad response');
    const r = await res.json();
    return fsMapFeedRow(r);
  }catch(err){
    console.warn('FundSpark API unavailable, using local dummy data instead.', err);
    return null;
  }
}

// GET comments for a startup. Returns an array, or null on failure.
async function fsApiGetComments(startupId){
  try{
    const res = await fetch(`${FS_API_BASE}/comments/startup/${startupId}`);
    if(!res.ok) throw new Error('Bad response');
    const rows = await res.json();
    return rows.map(c => ({
      name: c.name, init: c.initials || fsInitials(c.name),
      time: new Date(c.createdAt).toLocaleDateString(),
      text: c.text, likes: c.likes
    }));
  }catch(err){
    console.warn('FundSpark API unavailable, using local dummy comments instead.', err);
    return null;
  }
}

// POST a new comment. Returns true on success, false on failure.
async function fsApiPostComment(startupId, userId, text){
  try{
    const res = await fetch(`${FS_API_BASE}/comments`, {
      method: 'POST',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify({ startupId, userId, commentText: text })
    });
    return res.ok;
  }catch(err){
    console.warn('FundSpark API unavailable — comment not saved to backend.', err);
    return false;
  }
}

// POST an investment. Returns true on success, false on failure.
async function fsApiPostInvestment(investorId, startupId, amount, paymentMethod){
  try{
    const res = await fetch(`${FS_API_BASE}/investments`, {
      method: 'POST',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify({ investorId, startupId, amount, paymentMethod })
    });
    return res.ok;
  }catch(err){
    console.warn('FundSpark API unavailable — investment not saved to backend.', err);
    return false;
  }
}

// GET /api/wishlist/investor/{investorId} — powers the Wishlist page
async function fsApiGetWishlist(investorId){
  try{
    const res = await fetch(`${FS_API_BASE}/wishlist/investor/${investorId}`);
    if(!res.ok) throw new Error('Bad response');
    return await res.json();
  }catch(err){
    console.warn('FundSpark API unavailable — could not load wishlist.', err);
    return null;
  }
}

// POST wishlist toggle. Returns {bookmarked: bool} or null on failure.
async function fsApiToggleWishlist(investorId, startupId){
  try{
    const res = await fetch(`${FS_API_BASE}/wishlist/toggle`, {
      method: 'POST',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify({ investorId, startupId })
    });
    if(!res.ok) throw new Error('Bad response');
    return await res.json();
  }catch(err){
    console.warn('FundSpark API unavailable — wishlist not saved to backend.', err);
    return null;
  }
}

// POST /api/users/register. Returns {success, status, data} — data is the sanitized user on
// success, or an error message object on failure (e.g. {message:"..."} for a 409 duplicate email).
async function fsApiRegister(payload){
  try{
    const res = await fetch(`${FS_API_BASE}/users/register`, {
      method: 'POST',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify(payload)
    });
    const data = await res.json();
    return { success: res.ok, status: res.status, data };
  }catch(err){
    console.warn('FundSpark API unavailable — registration failed.', err);
    return { success:false, status:0, data:{message:'Backend server is not reachable. Make sure it is running on localhost:8080.'} };
  }
}

// POST /api/users/login. Returns {success, status, data} — data is the sanitized user on
// success, or {message:"Invalid email or password"} on a 401.
async function fsApiLogin(email, password){
  try{
    const res = await fetch(`${FS_API_BASE}/users/login`, {
      method: 'POST',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify({ email, password })
    });
    const data = await res.json();
    return { success: res.ok, status: res.status, data };
  }catch(err){
    console.warn('FundSpark API unavailable — login failed.', err);
    return { success:false, status:0, data:{message:'Backend server is not reachable. Make sure it is running on localhost:8080.'} };
  }
}

// GET /api/startups — raw startup entity list for admin pages
async function fsApiGetAllStartups(){
  try{
    const res = await fetch(`${FS_API_BASE}/startups`);
    if(!res.ok) throw new Error('Bad response');
    return await res.json();
  }catch(err){
    console.warn('FundSpark API unavailable — could not load all startups.', err);
    return null;
  }
}

// GET /api/startups/admin — enriched startup list for admin dashboards
async function fsApiGetAdminStartups(){
  try{
    const res = await fetch(`${FS_API_BASE}/startups/admin`);
    if(!res.ok) throw new Error('Bad response');
    return await res.json();
  }catch(err){
    console.warn('FundSpark API unavailable — could not load admin startups.', err);
    return null;
  }
}

// GET /api/users?role=... — user list for admin pages
async function fsApiGetUsers(role){
  try{
    const url = role ? `${FS_API_BASE}/users?role=${encodeURIComponent(role)}` : `${FS_API_BASE}/users`;
    const res = await fetch(url);
    if(!res.ok) throw new Error('Bad response');
    return await res.json();
  }catch(err){
    console.warn('FundSpark API unavailable — could not load users.', err);
    return null;
  }
}

// GET /api/startups/pending — pending campaigns for admin approval
async function fsApiGetPendingStartups(){
  try{
    const res = await fetch(`${FS_API_BASE}/startups/pending`);
    if(!res.ok) throw new Error('Bad response');
    return await res.json();
  }catch(err){
    console.warn('FundSpark API unavailable — could not load pending startups.', err);
    return null;
  }
}

// PUT /api/startups/{id}/verify — approve a pending startup
async function fsApiVerifyStartup(startupId){
  try{
    const res = await fetch(`${FS_API_BASE}/startups/${startupId}/verify`, {
      method: 'PUT',
      headers: {'Content-Type':'application/json'}
    });
    return res.ok;
  }catch(err){
    console.warn('FundSpark API unavailable — verify startup failed.', err);
    return false;
  }
}

// POST /api/startups
async function fsApiCreateStartup(payload){
  try{
    const res = await fetch(`${FS_API_BASE}/startups`, {
      method: 'POST',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify(payload)
    });
    const data = await res.json();
    return { success: res.ok, status: res.status, data };
  }catch(err){
    console.warn('FundSpark API unavailable — create startup failed.', err);
    return { success:false, status:0, data:{message:'Backend server is not reachable. Make sure it is running on localhost:8080.'} };
  }
}

// GET /api/users/{id} — fetches specific user profile
async function fsApiGetUser(userId){
  try{
    const res = await fetch(`${FS_API_BASE}/users/${userId}`);
    if(!res.ok) throw new Error('Bad response');
    return await res.json();
  }catch(err){
    console.warn('FundSpark API unavailable — could not fetch user profile.', err);
    return null;
  }
}

// PUT /api/users/{id} — updates specific user profile
async function fsApiUpdateUser(userId, payload){
  try{
    const res = await fetch(`${FS_API_BASE}/users/${userId}`, {
      method: 'PUT',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify(payload)
    });
    if(!res.ok) throw new Error('Bad response');
    return await res.json();
  }catch(err){
    console.warn('FundSpark API unavailable — profile update not saved to backend.', err);
    return null;
  }
}

// GET /api/startups/founder/{founderId}/feed — powers Founder's own campaign list
async function fsApiGetFounderFeed(founderId){
  try{
    const res = await fetch(`${FS_API_BASE}/startups/founder/${founderId}/feed`);
    if(!res.ok) throw new Error('Bad response');
    return await res.json();
  }catch(err){
    console.warn('FundSpark API unavailable — could not load founder startups.', err);
    return null;
  }
}

// GET /api/investments/founder/{founderId}/feed — powers Founder's recent investments feed
async function fsApiGetFounderInvestmentsFeed(founderId){
  try{
    const res = await fetch(`${FS_API_BASE}/investments/founder/${founderId}/feed`);
    if(!res.ok) throw new Error('Bad response');
    return await res.json();
  }catch(err){
    console.warn('FundSpark API unavailable — could not load founder investments.', err);
    return null;
  }
}

// GET /api/notifications/user/{userId} — powers notifications feed
async function fsApiGetNotifications(userId){
  try{
    const res = await fetch(`${FS_API_BASE}/notifications/user/${userId}`);
    if(!res.ok) throw new Error('Bad response');
    return await res.json();
  }catch(err){
    console.warn('FundSpark API unavailable — could not load notifications.', err);
    return null;
  }
}

// GET /api/dashboard/founder/{founderId} — powers founder KPI metrics
async function fsApiGetFounderDashboard(founderId){
  try{
    const res = await fetch(`${FS_API_BASE}/dashboard/founder/${founderId}`);
    if(!res.ok) throw new Error('Bad response');
    return await res.json();
  }catch(err){
    console.warn('FundSpark API unavailable — could not load founder dashboard summary.', err);
    return null;
  }
}

// PUT /api/startups/{id}/reject — reject a pending startup
async function fsApiRejectStartup(startupId){
  try{
    const res = await fetch(`${FS_API_BASE}/startups/${startupId}/reject`, {
      method: 'PUT',
      headers: {'Content-Type':'application/json'}
    });
    return res.ok;
  }catch(err){
    console.warn('FundSpark API unavailable — reject startup failed.', err);
    return false;
  }
}

// PUT /api/startups/{id}/close — close a startup, removing it from the public portal
// (used by both a founder closing their own campaign, and an admin removing any campaign)
async function fsApiCloseStartup(startupId){
  try{
    const res = await fetch(`${FS_API_BASE}/startups/${startupId}/close`, {
      method: 'PUT',
      headers: {'Content-Type':'application/json'}
    });
    return res.ok;
  }catch(err){
    console.warn('FundSpark API unavailable — close startup failed.', err);
    return false;
  }
}

// GET /api/comments/user/{userId} — comments THIS user has posted (investor activity feed)
async function fsApiGetCommentsByUser(userId){
  try{
    const res = await fetch(`${FS_API_BASE}/comments/user/${userId}`);
    if(!res.ok) throw new Error('Bad response');
    return await res.json();
  }catch(err){
    console.warn('FundSpark API unavailable — could not load user comments.', err);
    return null;
  }
}

// GET /api/comments/founder/{founderId}/feed — comments received on this founder's startups
async function fsApiGetFounderCommentsFeed(founderId){
  try{
    const res = await fetch(`${FS_API_BASE}/comments/founder/${founderId}/feed`);
    if(!res.ok) throw new Error('Bad response');
    return await res.json();
  }catch(err){
    console.warn('FundSpark API unavailable — could not load founder comments feed.', err);
    return null;
  }
}

// PUT /api/users/{id}/password — updates user password
async function fsApiUpdatePassword(userId, currentPassword, newPassword){
  try{
    const res = await fetch(`${FS_API_BASE}/users/${userId}/password`, {
      method: 'PUT',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify({ currentPassword, newPassword })
    });
    const data = await res.json();
    return { success: res.ok, message: data.message };
  }catch(err){
    console.warn('FundSpark API unavailable — could not update password.', err);
    return { success: false, message: 'Backend server not reachable.' };
  }
}
// DELETE /api/users/{id} — permanently deletes the user's account
async function fsApiDeleteUser(userId){
  try{
    const res = await fetch(`${FS_API_BASE}/users/${userId}`, {
      method: 'DELETE'
    });
    return res.ok;
  }catch(err){
    console.warn('FundSpark API unavailable — account not deleted.', err);
    return false;
  }
}
// POST /api/uploads — uploads a single file (PDF/image) and returns its public URL, or null on failure
async function fsApiUploadFile(file){
  if(!file) return null;
  try{
    const formData = new FormData();
    formData.append('file', file);
    const res = await fetch(`${FS_API_BASE}/uploads`, { method: 'POST', body: formData });
    if(!res.ok) throw new Error('Upload failed');
    const data = await res.json();
    return data.url;
  }catch(err){
    console.warn('FundSpark API unavailable — file upload failed.', err);
    return null;
  }
}

// POST /api/startup-documents — links an already-uploaded file to a startup, under a founder-chosen name
async function fsApiAddStartupDocument(startupId, documentName, documentUrl){
  try{
    const res = await fetch(`${FS_API_BASE}/startup-documents`, {
      method: 'POST',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify({ startupId, documentName, documentUrl })
    });
    return res.ok;
  }catch(err){
    console.warn('FundSpark API unavailable — could not save document record.', err);
    return false;
  }
}

// GET /api/startup-documents/startup/{id} — fetches every document uploaded for one startup
async function fsApiGetStartupDocuments(startupId){
  try{
    const res = await fetch(`${FS_API_BASE}/startup-documents/startup/${startupId}`);
    if(!res.ok) return [];
    return await res.json();
  }catch(err){
    console.warn('FundSpark API unavailable — could not load documents.', err);
    return [];
  }
}

/* ==========================================================
   FundSpark — Equity Feature API functions
   Append this whole block to the END of assets/js/api.js
   (same fetch/try-catch/null-on-failure pattern as the rest
   of the file, so pages can gracefully fall back if the
   backend isn't running).
   ========================================================== */

// GET /api/cap-table/{startupId} — powers the Live Cap Table (pie chart + table)
async function fsApiGetCapTable(startupId){
  try{
    const res = await fetch(`${FS_API_BASE}/cap-table/${startupId}`);
    if(!res.ok) throw new Error('Bad response');
    return await res.json();
  }catch(err){
    console.warn('FundSpark API unavailable — could not load cap table.', err);
    return null;
  }
}

// GET /api/cap-table/investor/{investorId}/portfolio — an investor's equity across all startups
async function fsApiGetInvestorPortfolio(investorId){
  try{
    const res = await fetch(`${FS_API_BASE}/cap-table/investor/${investorId}/portfolio`);
    if(!res.ok) throw new Error('Bad response');
    return await res.json();
  }catch(err){
    console.warn('FundSpark API unavailable — could not load portfolio.', err);
    return null;
  }
}

// GET /api/certificates/investor/{investorId} — powers "My Certificates" gallery
async function fsApiGetCertificatesByInvestor(investorId){
  try{
    const res = await fetch(`${FS_API_BASE}/certificates/investor/${investorId}`);
    if(!res.ok) throw new Error('Bad response');
    return await res.json();
  }catch(err){
    console.warn('FundSpark API unavailable — could not load certificates.', err);
    return null;
  }
}

// GET /api/certificates/startup/{startupId} — every certificate issued for one startup (founder view)
async function fsApiGetCertificatesByStartup(startupId){
  try{
    const res = await fetch(`${FS_API_BASE}/certificates/startup/${startupId}`);
    if(!res.ok) throw new Error('Bad response');
    return await res.json();
  }catch(err){
    console.warn('FundSpark API unavailable — could not load certificates.', err);
    return null;
  }
}

// GET /api/certificates/{id} — a single certificate's full detail (for the certificate modal)
async function fsApiGetCertificate(id){
  try{
    const res = await fetch(`${FS_API_BASE}/certificates/${id}`);
    if(!res.ok) throw new Error('Bad response');
    return await res.json();
  }catch(err){
    console.warn('FundSpark API unavailable — could not load certificate.', err);
    return null;
  }
}

// GET /api/equity-history/investor/{investorId}/startup/{startupId} — dilution timeline for ONE startup
async function fsApiGetEquityHistory(investorId, startupId){
  try{
    const res = await fetch(`${FS_API_BASE}/equity-history/investor/${investorId}/startup/${startupId}`);
    if(!res.ok) throw new Error('Bad response');
    return await res.json();
  }catch(err){
    console.warn('FundSpark API unavailable — could not load equity history.', err);
    return null;
  }
}

// GET /api/equity-history/investor/{investorId} — dilution events across ALL startups
async function fsApiGetEquityHistoryForInvestor(investorId){
  try{
    const res = await fetch(`${FS_API_BASE}/equity-history/investor/${investorId}`);
    if(!res.ok) throw new Error('Bad response');
    return await res.json();
  }catch(err){
    console.warn('FundSpark API unavailable — could not load equity history.', err);
    return null;
  }
}

// GET /api/badges/tiers — the 4 reference tiers (Bronze/Silver/Gold/Platinum)
async function fsApiGetBadgeTiers(){
  try{
    const res = await fetch(`${FS_API_BASE}/badges/tiers`);
    if(!res.ok) throw new Error('Bad response');
    return await res.json();
  }catch(err){
    console.warn('FundSpark API unavailable — could not load badge tiers.', err);
    return null;
  }
}

// GET /api/badges/investor/{investorId} — one investor's current badge + progress to next tier
async function fsApiGetInvestorBadge(investorId){
  try{
    const res = await fetch(`${FS_API_BASE}/badges/investor/${investorId}`);
    if(!res.ok) throw new Error('Bad response');
    return await res.json();
  }catch(err){
    console.warn('FundSpark API unavailable — could not load badge.', err);
    return null;
  }
}

// GET /api/badges/leaderboard — top investors by total invested
async function fsApiGetLeaderboard(){
  try{
    const res = await fetch(`${FS_API_BASE}/badges/leaderboard`);
    if(!res.ok) throw new Error('Bad response');
    return await res.json();
  }catch(err){
    console.warn('FundSpark API unavailable — could not load leaderboard.', err);
    return null;
  }
}

// GET /api/valuation/{startupId} — full valuation timeline (Seed -> Series A -> ...)
async function fsApiGetValuationTimeline(startupId){
  try{
    const res = await fetch(`${FS_API_BASE}/valuation/${startupId}`);
    if(!res.ok) throw new Error('Bad response');
    return await res.json();
  }catch(err){
    console.warn('FundSpark API unavailable — could not load valuation timeline.', err);
    return null;
  }
}

// POST /api/valuation/{startupId}/round — Founder "Add Valuation Round" form. Returns true/false.
/* ==========================================================
   FundSpark — Update: fsApiAddValuationRound now also sends
   raiseAmount + addToFundingGoal, so the backend can optionally
   grow the startup's total Funding Goal for this new round.
   Find fsApiAddValuationRound() in assets/js/api.js and
   REPLACE it with this version.
   ========================================================== */

async function fsApiAddValuationRound(startupId, roundName, valuationAmount, raiseAmount, addToFundingGoal){
  try{
    const res = await fetch(`${FS_API_BASE}/valuation/${startupId}/round`, {
      method: 'POST',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify({ roundName, valuationAmount, raiseAmount, addToFundingGoal })
    });
    return res.ok;
  }catch(err){
    console.warn('FundSpark API unavailable — valuation round not saved.', err);
    return false;
  }
}

// GET /api/dashboard/admin/analytics — powers Admin Analytics and Admin Dashboard graphs
async function fsApiGetAdminAnalytics(){
  try{
    const res = await fetch(`${FS_API_BASE}/dashboard/admin/analytics`);
    if(!res.ok) throw new Error('Bad response');
    return await res.json();
  }catch(err){
    console.warn('FundSpark API unavailable — could not load admin analytics.', err);
    return null;
  }
}

// GET /api/categories/stats — powers Admin Categories page with real DB counts
async function fsApiGetCategoryStats(){
  try{
    const res = await fetch(`${FS_API_BASE}/categories/stats`);
    if(!res.ok) throw new Error('Bad response');
    return await res.json();
  }catch(err){
    console.warn('FundSpark API unavailable — could not load category stats.', err);
    return null;
  }
}

// POST /api/categories — Admin add category
async function fsApiAddCategory(category){
  try{
    const res = await fetch(`${FS_API_BASE}/categories`, {
      method: 'POST',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify(category)
    });
    return res.ok;
  }catch(err){
    console.warn('FundSpark API unavailable — category not added.', err);
    return false;
  }
}

// DELETE /api/categories/{id} — Admin delete category
async function fsApiDeleteCategory(id){
  try{
    const res = await fetch(`${FS_API_BASE}/categories/${id}`, { method: 'DELETE' });
    return res.ok;
  }catch(err){
    console.warn('FundSpark API unavailable — category not deleted.', err);
    return false;
  }
}

// GET /api/reports — powers Admin Reports moderation with real DB records
async function fsApiGetReports(){
  try{
    const res = await fetch(`${FS_API_BASE}/reports`);
    if(!res.ok) throw new Error('Bad response');
    return await res.json();
  }catch(err){
    console.warn('FundSpark API unavailable — could not load reports.', err);
    return null;
  }
}

// PUT /api/reports/{id}/dismiss
async function fsApiDismissReport(id){
  try{
    const res = await fetch(`${FS_API_BASE}/reports/${id}/dismiss`, { method: 'PUT' });
    return res.ok;
  }catch(err){
    return false;
  }
}

// PUT /api/reports/{id}/resolve
async function fsApiResolveReport(id){
  try{
    const res = await fetch(`${FS_API_BASE}/reports/${id}/resolve`, { method: 'PUT' });
    return res.ok;
  }catch(err){
    return false;
  }
}

// GET /api/investment-requests/investor/{investorId}/verification-profile
// Founder verifies investor: views bio, badge, and portfolio of other startups invested in
async function fsApiGetInvestorVerificationProfile(investorId){
  try{
    const res = await fetch(`${FS_API_BASE}/investment-requests/investor/${investorId}/verification-profile`);
    if(!res.ok) throw new Error('Bad response');
    return await res.json();
  }catch(err){
    console.warn('FundSpark API unavailable — could not load investor verification profile.', err);
    return null;
  }
}