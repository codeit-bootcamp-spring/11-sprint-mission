const API_BASE_URL = "/api";
const ENDPOINTS = {
  USERS_DTO: `${API_BASE_URL}/users/readAllDto`,
  CREATE_USER: `${API_BASE_URL}/users`,
  UPDATE_USER: (id) => `${API_BASE_URL}/users/${id}`,
  CREATE_MESSAGE: `${API_BASE_URL}/messages`,
  BINARY_CONTENT_BY_ID: (id) => `${API_BASE_URL}/binaryContents/${id}`,
};

document.addEventListener("DOMContentLoaded", () => {
  bindCreateUserForm();
  bindUpdateUserForm();
  bindCreateMessageForm();
  fetchAndRenderUsers();
});

function setText(el, text) {
  if (!el) {
    return;
  }
  el.textContent = text ?? "";
}

async function fetchTextOrThrow(res) {
  const text = await res.text();
  if (!res.ok) {
    throw new Error(text || `${res.status} ${res.statusText}`);
  }
  return text;
}

async function fetchJsonOrThrow(res) {
  const text = await res.text();
  if (!res.ok) {
    throw new Error(text || `${res.status} ${res.statusText}`);
  }
  return text ? JSON.parse(text) : null;
}

async function fetchAndRenderUsers() {
  try {
    const res = await fetch(ENDPOINTS.USERS_DTO);
    const users = await fetchJsonOrThrow(res);
    await renderUserList(Array.isArray(users) ? users : []);
  } catch (e) {
    console.error(e);
  }
}

async function fetchUserProfileDataUrl(profileId) {
  try {
    const res = await fetch(ENDPOINTS.BINARY_CONTENT_BY_ID(profileId));
    const profile = await fetchJsonOrThrow(res);

    // BinaryContent: { contentType: "...", content: "base64..." }
    if (!profile || !profile.contentType
        || !profile.content) {
      return "/default-avatar.png";
    }
    return `data:${profile.contentType};base64,${profile.content}`;
  } catch {
    return "/default-avatar.png";
  }
}

async function renderUserList(users) {
  const userListElement = document.getElementById("userList");
  if (!userListElement) {
    return;
  }

  userListElement.innerHTML = "";

  for (const user of users) {
    const item = document.createElement("div");
    item.className = "user-item";

    const profileUrl = user.profileId
        ? await fetchUserProfileDataUrl(user.profileId)
        : "/default-avatar.png";

    item.innerHTML = `
      <img src="${profileUrl}" class="user-avatar" alt="${user.username}" />
      <div class="user-info">
        <div class="user-name">${user.username}</div>
        <div class="user-email">${user.email}</div>
        <div class="user-meta">id: ${user.id}</div>
        <div class="user-meta">profileId: ${user.profileId ?? "-"}</div>
      </div>
      <div class="status-badge ${user.online ? "online" : "offline"}">
        ${user.online ? "온라인" : "오프라인"}
      </div>
    `;

    userListElement.appendChild(item);
  }
}

function bindCreateUserForm() {
  const form = document.getElementById("createUserForm");
  const result = document.getElementById("createUserResult");
  if (!form) {
    return;
  }

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    setText(result, "요청 중...");

    try {
      const fd = new FormData();
      fd.append("userName", form.userName.value);
      fd.append("userEmail", form.userEmail.value);
      fd.append("userPassword", form.userPassword.value);

      const file = form.profile.files && form.profile.files[0];
      if (file) {
        fd.append("profile", file);
      }

      const res = await fetch(ENDPOINTS.CREATE_USER,
          {method: "POST", body: fd});
      const text = await fetchTextOrThrow(res);

      setText(result, text);
      form.reset();
      await fetchAndRenderUsers();
    } catch (err) {
      setText(result, String(err?.message || err));
    }
  });
}

function bindUpdateUserForm() {
  const form = document.getElementById("updateUserForm");
  const result = document.getElementById("updateUserResult");
  if (!form) {
    return;
  }

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    setText(result, "요청 중...");

    try {
      const id = form.id.value;

      const fd = new FormData();
      fd.append("userName", form.userName.value);
      fd.append("userEmail", form.userEmail.value);
      fd.append("userPassword", form.userPassword.value);

      const file = form.profile.files && form.profile.files[0];
      if (file) {
        fd.append("profile", file);
      }

      const res = await fetch(ENDPOINTS.UPDATE_USER(id),
          {method: "PUT", body: fd});
      const text = await fetchTextOrThrow(res);

      setText(result, text);
      form.reset();
      await fetchAndRenderUsers();
    } catch (err) {
      setText(result, String(err?.message || err));
    }
  });
}

function bindCreateMessageForm() {
  const form = document.getElementById("createMessageForm");
  const result = document.getElementById("createMessageResult");
  if (!form) {
    return;
  }

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    setText(result, "요청 중...");

    try {
      const fd = new FormData();
      fd.append("content", form.content.value);
      fd.append("authorId", form.authorId.value);
      fd.append("channelId", form.channelId.value);

      const receiverId = form.receiverId.value.trim();
      if (receiverId) {
        fd.append("receiverId", receiverId);
      }

      const file = form.file.files && form.file.files[0];
      if (file) {
        fd.append("file", file);
      } // MessageController의 @RequestParam("file") 과 매칭

      const res = await fetch(ENDPOINTS.CREATE_MESSAGE,
          {method: "POST", body: fd});
      const text = await fetchTextOrThrow(res);

      setText(result, text);
      form.reset();
    } catch (err) {
      setText(result, String(err?.message || err));
    }
  });
}
