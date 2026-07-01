// API endpoints
const API_BASE_URL = '/api';
const ENDPOINTS = {
    USERS: `${API_BASE_URL}/users/findAll`,       // 수정됨 (user -> users)
    BINARY_CONTENT: `${API_BASE_URL}/contents/find` // 수정됨 (binaryContent -> contents)
};

// Initialize the application
document.addEventListener('DOMContentLoaded', () => {
    fetchAndRenderUsers();
});

// Fetch users from the API
async function fetchAndRenderUsers() {
    try {
        const response = await fetch(ENDPOINTS.USERS);
        if (!response.ok) throw new Error('Failed to fetch users');
        const users = await response.json();
        renderUserList(users);
    } catch (error) {
        console.error('Error fetching users:', error);
    }
}

// Fetch user profile image
async function fetchUserProfile(profileId) {
    try {
        const response = await fetch(`${ENDPOINTS.BINARY_CONTENT}?binaryContentId=${profileId}`);
        if (!response.ok) throw new Error('Failed to fetch profile');
        const profile = await response.json();

        // Convert base64 encoded bytes to data URL (bytes -> data로 수정)
        return `data:${profile.contentType};base64,${profile.data}`;
    } catch (error) {
        console.error('Error fetching profile:', error);
        // 이미지가 없을 때 띄울 기본 이미지 URL 설정 (빈 이미지 처리)
        return 'data:image/svg+xml;charset=UTF-8,%3Csvg xmlns="http://www.w3.org/2000/svg" width="60" height="60" viewBox="0 0 60 60"%3E%3Crect fill="%23ddd" width="60" height="60"/%3E%3Cpath fill="%23999" d="M30 30c-4.4 0-8-3.6-8-8s3.6-8 8-8 8 3.6 8 8-3.6 8-8 8zm0 4c8.8 0 16 4.4 16 9.6V46H14v-2.4C14 38.4 21.2 34 30 34z"/%3E%3C/svg%3E';
    }
}

// Render user list
async function renderUserList(users) {
    const userListElement = document.getElementById('userList');
    userListElement.innerHTML = ''; // Clear existing content

    for (const user of users) {
        const userElement = document.createElement('div');
        userElement.className = 'user-item';

        // Get profile image URL
        const profileUrl = user.profileId ?
            await fetchUserProfile(user.profileId) :
            'data:image/svg+xml;charset=UTF-8,%3Csvg xmlns="http://www.w3.org/2000/svg" width="60" height="60" viewBox="0 0 60 60"%3E%3Crect fill="%23ddd" width="60" height="60"/%3E%3Cpath fill="%23999" d="M30 30c-4.4 0-8-3.6-8-8s3.6-8 8-8 8 3.6 8 8-3.6 8-8 8zm0 4c8.8 0 16 4.4 16 9.6V46H14v-2.4C14 38.4 21.2 34 30 34z"/%3E%3C/svg%3E';

        userElement.innerHTML = `
            <img src="${profileUrl}" alt="${user.username}" class="user-avatar">
            <div class="user-info">
                <div class="user-name">${user.username}</div>
                <div class="user-email">${user.email}</div>
            </div>
            <div class="status-badge ${user.online ? 'online' : 'offline'}">
                ${user.online ? '온라인' : '오프라인'}
            </div>
        `;

        userListElement.appendChild(userElement);
    }
}