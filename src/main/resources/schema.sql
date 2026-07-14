CREATE TABLE IF NOT EXISTS binary_contents
(
    id           UUID PRIMARY KEY,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    file_name    VARCHAR(255)             NOT NULL,
    size         BIGINT                   NOT NULL,
    content_type VARCHAR(100)             NOT NULL,
    status       VARCHAR(20)              NOT NULL DEFAULT 'PROCESSING'
);

-- 기존 테이블에 컬럼만 추가해야 하는 경우:
-- ALTER TABLE binary_contents ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now();
-- ALTER TABLE binary_contents ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'PROCESSING';

CREATE TABLE IF NOT EXISTS users
(
    id         UUID PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    username   VARCHAR(50)              NOT NULL UNIQUE,
    email      VARCHAR(100)             NOT NULL UNIQUE,
    password   VARCHAR(60)              NOT NULL,
    profile_id UUID,
    role       VARCHAR(20)              NOT NULL DEFAULT 'USER',
    FOREIGN KEY (profile_id) REFERENCES binary_contents (id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS user_statuses
(
    id             UUID PRIMARY KEY,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    user_id        UUID                     NOT NULL UNIQUE,
    last_active_at TIMESTAMP WITH TIME ZONE NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS channels
(
    id          UUID PRIMARY KEY,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    type        VARCHAR(10)              NOT NULL,
    name        VARCHAR(100),
    description VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS messages
(
    id         UUID PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    content    TEXT,
    channel_id UUID                     NOT NULL,
    author_id  UUID,
    FOREIGN KEY (channel_id) REFERENCES channels (id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS message_attachments
(
    message_id    UUID NOT NULL,
    attachment_id UUID NOT NULL,
    PRIMARY KEY (message_id, attachment_id),
    FOREIGN KEY (message_id) REFERENCES messages (id) ON DELETE CASCADE,
    FOREIGN KEY (attachment_id) REFERENCES binary_contents (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS read_statuses
(
    id                    UUID PRIMARY KEY,
    created_at            TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at            TIMESTAMP WITH TIME ZONE NOT NULL,
    user_id               UUID                     NOT NULL,
    channel_id            UUID                     NOT NULL,
    last_read_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    notification_enabled  BOOLEAN                  NOT NULL DEFAULT FALSE,
    UNIQUE (user_id, channel_id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (channel_id) REFERENCES channels (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS notifications
(
    id          UUID PRIMARY KEY,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    receiver_id UUID                     NOT NULL,
    title       VARCHAR(255)             NOT NULL,
    content     TEXT                     NOT NULL,
    FOREIGN KEY (receiver_id) REFERENCES users (id) ON DELETE CASCADE
);

-- 기존 테이블에 컬럼만 추가해야 하는 경우:
-- ALTER TABLE read_statuses ADD COLUMN IF NOT EXISTS notification_enabled BOOLEAN NOT NULL DEFAULT FALSE;