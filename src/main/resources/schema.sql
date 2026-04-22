CREATE TABLE binary_contents
(
    id           UUID PRIMARY KEY,
    created_at   TIMESTAMPTZ  NOT NULL,
    file_name    VARCHAR(255) NOT NULL,
    size         BIGINT       NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    bytes        BYTEA        NOT NULL
);

CREATE TABLE users
(
    id         UUID PRIMARY KEY,
    created_at TIMESTAMPTZ  NOT NULL,
    updated_at TIMESTAMPTZ,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    email      VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(60)  NOT NULL,
    profile_id UUID UNIQUE,
    CONSTRAINT fk_users_profile
        FOREIGN KEY (profile_id)
            REFERENCES binary_contents (id)
            ON DELETE SET NULL
);

CREATE TABLE channels(
    id          UUID PRIMARY KEY,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ,
    name        VARCHAR(100),
    description VARCHAR(500),
    type        VARCHAR(10) NOT NULL,
    CONSTRAINT chk_channels_type
        CHECK (type IN ('PUBLIC', 'PRIVATE'))
);

CREATE TABLE user_statuses
(
    id             UUID PRIMARY KEY,
    created_at     TIMESTAMPTZ NOT NULL,
    updated_at     TIMESTAMPTZ,
    user_id        UUID        NOT NULL UNIQUE,
    last_active_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_user_statuses_users
        FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE CASCADE
);

CREATE TABLE read_statuses
(
    id           UUID PRIMARY KEY,
    created_at   TIMESTAMPTZ NOT NULL,
    updated_at   TIMESTAMPTZ,
    user_id      UUID        NOT NULL,
    channel_id   UUID        NOT NULL,
    last_read_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_read_statues_user_channel UNIQUE (user_id, channel_id),
    CONSTRAINT fk_read_statues_users
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_read_statues_channels
        FOREIGN KEY (channel_id)
            REFERENCES channels (id)
            ON DELETE CASCADE
);

CREATE TABLE messages
(
    id         UUID PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ,
    content    TEXT,
    channel_id UUID        NOT NULL,
    author_id  UUID,
    CONSTRAINT fk_messages_channels
        FOREIGN KEY (channel_id)
            REFERENCES channels (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_messages_authors
        FOREIGN KEY (author_id)
            REFERENCES users (id)
            ON DELETE SET NULL
);

CREATE TABLE message_attachments
(
    message_id    UUID NOT NULL,
    attachment_id UUID NOT NULL,
    PRIMARY KEY (message_id, attachment_id),
    CONSTRAINT fk_message_attachments_messages
        FOREIGN KEY (message_id)
            REFERENCES messages (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_message_attachments_attachments
        FOREIGN KEY (attachment_id)
            REFERENCES binary_contents (id)
            ON DELETE CASCADE
);