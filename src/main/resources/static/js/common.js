/**
 * 后端登录鉴权还没接（见 README「已知的简化」），当前用 localStorage 存一个
 * "当前身份"（userId + deptId），所有页面调接口时用这个身份代入 operatorId/userId/deptId 参数。
 * 接入真正的登录后，把 Session 换成从 JWT/Session 里取即可，页面调用方式不用改。
 */
const Session = {
  getUserId() {
    return localStorage.getItem('quiz_userId') || '1';
  },
  setUserId(v) {
    localStorage.setItem('quiz_userId', v);
  },
  getDeptId() {
    return localStorage.getItem('quiz_deptId') || '1';
  },
  setDeptId(v) {
    localStorage.setItem('quiz_deptId', v);
  },
};

function escapeHtml(s) {
  if (s === null || s === undefined) return '';
  return String(s)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

function showBanner(el, message, type = 'error') {
  if (!el) return;
  el.className = 'banner ' + type;
  el.textContent = message;
  el.style.display = 'block';
}

function hideBanner(el) {
  if (!el) return;
  el.style.display = 'none';
}

const NAV_ITEMS = [
  { key: 'banks', href: 'banks.html', label: '题库管理' },
  { key: 'papers', href: 'papers.html', label: '试卷管理' },
  { key: 'wrongbook', href: 'wrongbook.html', label: '我的错题本' },
];

function renderNav(activeKey) {
  const root = document.getElementById('topbar-root');
  if (!root) return;

  const navHtml = NAV_ITEMS.map(
    (item) =>
      `<a href="${item.href}" class="${item.key === activeKey ? 'active' : ''}">${item.label}</a>`
  ).join('');

  root.innerHTML = `
    <div class="topbar">
      <a href="index.html" class="brand" style="color:#fff">答题<span>系统</span></a>
      <nav>${navHtml}</nav>
      <div class="who">
        <span>用户 #<span id="who-user">${escapeHtml(Session.getUserId())}</span> · 部门 #<span id="who-dept">${escapeHtml(Session.getDeptId())}</span></span>
        <button id="who-switch">切换身份</button>
      </div>
    </div>
  `;

  document.getElementById('who-switch').addEventListener('click', () => {
    const uid = prompt('设置当前用户ID（用于记录操作人/考生）', Session.getUserId());
    if (uid) Session.setUserId(uid.trim());
    const did = prompt('设置当前部门ID', Session.getDeptId());
    if (did) Session.setDeptId(did.trim());
    location.reload();
  });
}
