const API_BASE = '';  // 필요시 'http://localhost:8080' 등으로 변경

const ENDPOINTS = {
  USERS: `${API_BASE}/api/users`,
  BINARY_CONTENT: `${API_BASE}/api/binaryContents/find`,
  CHANNELS: `${API_BASE}/api/channels`,
};

const CHANNEL_ICONS = ['💬', '📢', '🎯', '🛠️', '🎨', '📊', '🔔', '🚀', '💡', '🌐'];

// ── 공통 fetch ──
async function apiFetch(url) {
  const res = await fetch(url);
  if (!res.ok) {
    throw new Error(`HTTP ${res.status}`);
  }
  return res.json();
}

// ── 프로필 이미지: binaryContent API로 base64 변환 ──
async function fetchProfileUrl(profileId) {
  if (!profileId) {
    return null;
  }
  try {
    const profile = await apiFetch(
        `${ENDPOINTS.BINARY_CONTENT}?binaryContentId=${profileId}`);
    return `data:${profile.fileType};base64,${profile.bytes}`;
  } catch {
    return null;
  }
}

// ── 아바타 엘리먼트 생성 ──
function makeAvatarWrap(profileUrl, username, online) {
  const wrap = document.createElement('div');
  wrap.className = 'avatar-wrap';

  if (profileUrl) {
    const img = document.createElement('img');
    img.className = 'avatar';
    img.src = profileUrl;
    img.alt = username;
    img.onerror = () => img.replaceWith(makeFallback(username));
    wrap.appendChild(img);
  } else {
    wrap.appendChild(makeFallback(username));
  }

  const dot = document.createElement('span');
  dot.className = `status-dot ${online ? 'online' : 'offline'}`;
  wrap.appendChild(dot);
  return wrap;
}

function makeFallback(name) {
  const d = document.createElement('div');
  d.className = 'avatar-fallback';
  d.textContent = (name || '?').charAt(0).toUpperCase();
  return d;
}

// ── 채널 패널 ──
const panel = document.getElementById('channel-panel');
const panelName = document.getElementById('panel-username');
const panelList = document.getElementById('panel-channel-list');

let hideTimer = null;
let channelCache = {};  // userId → channels (캐시)

function showPanel(card, userId, username) {
  clearTimeout(hideTimer);

  panelName.textContent = username;
  panelList.innerHTML = '<div class="panel-loading">불러오는 중…</div>';
  panel.classList.remove('hidden');
  positionPanel(card);

  if (channelCache[userId]) {
    renderPanelChannels(channelCache[userId]);
    return;
  }

  apiFetch(`${ENDPOINTS.CHANNELS}?user-id=${userId}`)
  .then(data => {
    const channels = Array.isArray(data) ? data : (data.data ?? data.channels
        ?? []);
    channelCache[userId] = channels;
    renderPanelChannels(channels);
  })
  .catch(e => {
    panelList.innerHTML = `<div class="panel-empty">오류: ${e.message}</div>`;
  });
}

function renderPanelChannels(channels) {
  if (!channels.length) {
    panelList.innerHTML = '<div class="panel-empty">채널 없음</div>';
    return;
  }
  panelList.innerHTML = '';
  channels.forEach((ch, i) => {
    const name = ch.name || ch.channelName || `Channel ${i + 1}`;
    const item = document.createElement('div');
    item.className = 'panel-channel-item';
    item.innerHTML =
        `<span class="ch-icon">${CHANNEL_ICONS[i
        % CHANNEL_ICONS.length]}</span>` +
        `<span class="ch-name"># ${name}</span>`;
    panelList.appendChild(item);
  });
}

function positionPanel(card) {
  const rect = card.getBoundingClientRect();
  const panelW = 260;
  const gap = 10;

  let left = rect.right + gap;
  let top = rect.top + window.scrollY;

  // 오른쪽 공간 부족하면 왼쪽에 표시
  if (left + panelW > window.innerWidth - 12) {
    left = rect.left - panelW - gap;
  }

  panel.style.left = `${left}px`;
  panel.style.top = `${top}px`;
}

function hidePanel() {
  hideTimer = setTimeout(() => {
    panel.classList.add('hidden');
  }, 150);
}

// ── 사용자 목록 렌더 ──
async function loadUsers() {
  const el = document.getElementById('user-list');
  try {
    const data = await apiFetch(ENDPOINTS.USERS);
    const users = Array.isArray(data) ? data : (data.data ?? data.users ?? []);
    document.getElementById('user-count').textContent = users.length;

    if (!users.length) {
      el.innerHTML = '<div class="state-msg">사용자가 없습니다.</div>';
      return;
    }

    el.innerHTML = '';

    // 프로필 이미지를 병렬로 먼저 fetch
    const profileUrls = await Promise.all(
        users.map(u => fetchProfileUrl(u.profileId ?? u.profile_id ?? null))
    );

    users.forEach((u, i) => {
      const online = u.online ?? u.isOnline ?? u.status === 'online';
      const username = u.username || u.name || 'Unknown';
      const userId = u.id ?? u._id ?? u.userId;

      const card = document.createElement('div');
      card.className = 'user-card';
      card.style.animationDelay = `${i * 60}ms`;

      card.appendChild(makeAvatarWrap(profileUrls[i], username, online));

      const info = document.createElement('div');
      info.className = 'user-info';
      info.innerHTML =
          `<div class="user-name">${username}</div>` +
          `<div class="user-email">${u.email || ''}</div>`;
      card.appendChild(info);

      const label = document.createElement('span');
      label.className = `status-label ${online ? 'online' : 'offline'}`;
      label.textContent = online ? 'online' : 'offline';
      card.appendChild(label);

      // 호버 이벤트 → 채널 패널
      card.addEventListener('mouseenter',
          () => showPanel(card, userId, username));
      card.addEventListener('mouseleave', hidePanel);

      el.appendChild(card);
    });
  } catch (e) {
    el.innerHTML = `<div class="state-msg error">불러오기 실패: ${e.message}</div>`;
  }
}

document.addEventListener('DOMContentLoaded', loadUsers);
