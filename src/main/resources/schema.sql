CREATE TABLE IF NOT EXISTS binary_contents (
    id           UUID PRIMARY KEY,
    created_at   TIMESTAMPTZ  NOT NULL,
    updated_at   TIMESTAMPTZ,
    file_name    VARCHAR(255) NOT NULL,
    size         BIGINT       NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'PROCESSING'
);

CREATE TABLE IF NOT EXISTS users (
    id         UUID PRIMARY KEY,
    created_at TIMESTAMPTZ  NOT NULL,
    updated_at TIMESTAMPTZ,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    email      VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(60)  NOT NULL,
    profile_id UUID UNIQUE,
    role       VARCHAR(20)  NOT NULL,
    FOREIGN KEY (profile_id) REFERENCES binary_contents (id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS user_statuses (
    id             UUID PRIMARY KEY,
    created_at     TIMESTAMPTZ NOT NULL,
    updated_at     TIMESTAMPTZ,
    user_id        UUID        NOT NULL UNIQUE,
    last_active_at TIMESTAMPTZ NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS channels (
    id          UUID PRIMARY KEY,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ,
    type        VARCHAR(10) NOT NULL CHECK (type IN ('PUBLIC', 'PRIVATE')),
    name        VARCHAR(100),
    description VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS read_statuses (
    id           UUID PRIMARY KEY,
    created_at   TIMESTAMPTZ NOT NULL,
    updated_at   TIMESTAMPTZ,
    user_id      UUID        NOT NULL,
    channel_id   UUID        NOT NULL,
    last_read_at TIMESTAMPTZ NOT NULL,
    UNIQUE (user_id, channel_id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (channel_id) REFERENCES channels (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS messages (
    id         UUID PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ,
    content    TEXT,
    channel_id UUID        NOT NULL,
    author_id  UUID,
    FOREIGN KEY (channel_id) REFERENCES channels (id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS message_attachments (
    message_id    UUID NOT NULL,
    attachment_id UUID NOT NULL,
    PRIMARY KEY (message_id, attachment_id),
    FOREIGN KEY (message_id) REFERENCES messages (id) ON DELETE CASCADE,
    FOREIGN KEY (attachment_id) REFERENCES binary_contents (id) ON DELETE CASCADE
);