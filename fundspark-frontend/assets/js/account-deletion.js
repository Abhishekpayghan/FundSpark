/* ==========================================================
   FundSpark — Account deletion with admin approval (user side)
   Used by founder/settings.html and investor/settings.html.
   Renders the "Delete Account" area inside <div id="deleteAccountArea">.
   Flow: Request deletion -> admin approves/rejects -> user confirms the final delete.
   ========================================================== */

const FS_DEL_BASE = `${FS_API_BASE}/deletion-requests`;

function fsDelEsc(t){
  return String(t == null ? '' : t).replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
}

// GET /api/deletion-requests/user/{id} — latest request of this user, or null if none
async function fsApiGetMyDeletionRequest(userId){
  try{
    const res = await fetch(`${FS_DEL_BASE}/user/${userId}`);
    if(res.status === 204) return null;
    if(!res.ok) throw new Error('bad response');
    return await res.json();
  }catch(err){
    console.warn('FundSpark API unavailable — could not load deletion request.', err);
    return undefined;   // undefined = backend problem, null = no request yet
  }
}

// POST /api/deletion-requests — ask the admin for permission
async function fsApiSendDeletionRequest(userId, reason){
  try{
    const res = await fetch(FS_DEL_BASE, {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify({ userId, reason })
    });
    const data = await res.json().catch(() => ({}));
    return { success: res.ok, message: data.message };
  }catch(err){
    return { success: false, message: 'Backend server not reachable.' };
  }
}

// DELETE /api/users/{id} — only works after the admin approved the request
async function fsApiConfirmDeleteAccount(userId){
  try{
    const res = await fetch(`${FS_API_BASE}/users/${userId}`, { method: 'DELETE' });
    const data = await res.json().catch(() => ({}));
    return { success: res.ok, message: data.message };
  }catch(err){
    return { success: false, message: 'Backend server not reachable.' };
  }
}

async function fsRenderDeletionArea(session, homeUrl){
  const box = document.getElementById('deleteAccountArea');
  if(!box) return;
  const req = await fsApiGetMyDeletionRequest(session.userId);

  if(req === undefined){
    box.innerHTML = `<p class="text-sm" style="color:var(--danger);">Could not load deletion status. Is the backend running?</p>`;
    return;
  }

  const title = `<b style="font-size:14px;">Delete Account</b>`;

  // 1) Admin approved -> the user can now delete permanently
  if(req && req.status === 'approved'){
    box.innerHTML = `
      <div style="display:flex;justify-content:space-between;align-items:center;gap:16px;flex-wrap:wrap;">
        <div>${title} <span class="badge badge-success">Approved by admin</span>
          <p class="text-sm" style="margin:4px 0 0;">An admin approved your request.${req.adminNote ? ' Note: ' + fsDelEsc(req.adminNote) : ''} Deleting is permanent and cannot be undone.</p></div>
        <button class="btn btn-danger btn-sm" id="delFinalBtn">Delete my account permanently</button>
      </div>`;
    document.getElementById('delFinalBtn').onclick = async () => {
      if(!confirm('This will permanently delete your account and related data. This cannot be undone. Continue?')) return;
      const r = await fsApiConfirmDeleteAccount(session.userId);
      if(r.success){
        fsToast('Account deleted.', 'success');
        fsClearSession();
        setTimeout(() => { location.href = homeUrl; }, 800);
      } else {
        fsToast(r.message || 'Could not delete account.', 'error');
      }
    };
    return;
  }

  // 2) Waiting for the admin
  if(req && req.status === 'pending'){
    box.innerHTML = `
      <div style="display:flex;justify-content:space-between;align-items:center;gap:16px;flex-wrap:wrap;">
        <div>${title} <span class="badge badge-warning">Waiting for admin</span>
          <p class="text-sm" style="margin:4px 0 0;">Your deletion request is waiting for admin review. Your reason: ${fsDelEsc(req.reason)}</p></div>
        <button class="btn btn-outline btn-sm" disabled>Request sent</button>
      </div>`;
    return;
  }

  // 3) No request yet, or the last one was rejected -> show the request form
  const rejected = (req && req.status === 'rejected')
    ? `<p class="text-sm mt-8" style="color:var(--danger);"><b>Your last request was rejected.</b> Reason: ${fsDelEsc(req.adminNote || 'No reason given')}</p>` : '';
  box.innerHTML = `
    <div style="display:flex;justify-content:space-between;align-items:center;gap:16px;flex-wrap:wrap;">
      <div>${title}
        <p class="text-sm" style="margin:2px 0 0;">To delete your account you first need permission from an admin.</p>${rejected}</div>
      <button class="btn btn-danger btn-sm" id="delAskBtn">Request deletion</button>
    </div>
    <div id="delForm" style="display:none;margin-top:14px;">
      <div class="form-group"><label>Why do you want to delete your account?</label>
        <textarea id="delReason" class="form-control" rows="3" maxlength="500" placeholder="Write at least 10 characters"></textarea></div>
      <button class="btn btn-danger btn-sm" id="delSendBtn">Send request to admin</button>
      <button class="btn btn-outline btn-sm" id="delCancelBtn">Cancel</button>
    </div>`;
  document.getElementById('delAskBtn').onclick = () => { document.getElementById('delForm').style.display = 'block'; };
  document.getElementById('delCancelBtn').onclick = () => { document.getElementById('delForm').style.display = 'none'; };
  document.getElementById('delSendBtn').onclick = async () => {
    const reason = document.getElementById('delReason').value.trim();
    if(reason.length < 10){ fsToast('Please write at least 10 characters.', 'error'); return; }
    const r = await fsApiSendDeletionRequest(session.userId, reason);
    if(r.success){
      fsToast(r.message || 'Request sent.', 'success');
      fsRenderDeletionArea(session, homeUrl);
    } else {
      fsToast(r.message || 'Could not send the request.', 'error');
    }
  };
}