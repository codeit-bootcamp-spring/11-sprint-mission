document.addEventListener('DOMContentLoaded', () => {
    fetchAndRenderUsers();
});

async function fetchAndRenderUsers() {
    const container = document.getElementById('user-list-container');
    container.innerHTML = '';

    try {
        const response = await fetch('/api/user/findAll');
        if (!response.ok) {
            throw new Error('네트워크 응답이 좋지 않습니다.');
        }
        const users = await response.json();

        if (users.length === 0) {
            container.innerHTML = '<p class="user-email">가입된 사용자가 없습니다.</p>';
            return;
        }

        users.forEach(user => {
            const userCard = createUserCard(user);
            container.appendChild(userCard);
        });

    } catch (error) {
        console.error('유저 목록 불러오기 실패:', error);
        container.innerHTML = `<p class="offline">데이터를 불러오는 중 오류가 발생했습니다: ${error.message}</p>`;
    }
}

function createUserCard(user) {
    const card = document.createElement('div');
    card.className = 'user-card';

    const avatar = document.createElement('div');
    avatar.className = 'user-avatar-placeholder';
    avatar.textContent = (user.username && user.username.length > 0) ? user.username.charAt(0).toUpperCase() : '?';

    if (user.online === true) {
        const dot = document.createElement('div');
        dot.className = 'status-dot online';
        avatar.appendChild(dot);
    }
    card.appendChild(avatar);

    const info = document.createElement('div');
    info.className = 'user-info';

    const name = document.createElement('h2');
    name.className = 'user-name';
    name.textContent = user.username;
    info.appendChild(name);

    const email = document.createElement('p');
    email.className = 'user-email';
    email.textContent = user.email;
    info.appendChild(email);
    card.appendChild(info);

    if (user.online !== true) {
        const badge = document.createElement('span');
        badge.className = 'status-badge offline';
        badge.textContent = '오프라인';
        card.appendChild(badge);
    }

    return card;
}