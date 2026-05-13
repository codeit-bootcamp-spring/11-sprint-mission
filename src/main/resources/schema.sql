CREATE TABLE binary_content
(
    id           UUID PRIMARY KEY,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    file_name    VARCHAR(255) NOT NULL,
    size         BIGINT       NOT NULL,
    content_type VARCHAR(100) NOT NULL
);



CREATE TABLE "user"
(
    id         UUID PRIMARY KEY,
    created_at TIMESTAMPTZ         NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    username   VARCHAR(50) UNIQUE  NOT NULL,
    email      VARCHAR(100) UNIQUE NOT NULL,
    password   VARCHAR(60)         NOT NULL,
    profile_id UUID UNIQUE,

    FOREIGN KEY (profile_id) REFERENCES binary_content (id) ON DELETE SET NULL
);

CREATE TABLE channel
(

    id          UUID PRIMARY KEY,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ,
    name        VARCHAR(100),
    description VARCHAR(500),
    type        VARCHAR(10) CHECK (type IN ('PRIVATE', 'PUBLIC'))

);



CREATE TABLE user_status
(

    id             UUID PRIMARY KEY,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ,
    user_id        UUID        NOT NULL UNIQUE,
    last_active_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE
);


CREATE TABLE read_status
(

    id           UUID PRIMARY KEY,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ,
    user_id      UUID        NOT NULL,
    channel_id   UUID        NOT NULL,
    last_read_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    UNIQUE (user_id, channel_id),

    FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE,
    FOREIGN KEY (channel_id) REFERENCES channel (id) ON DELETE CASCADE

);



CREATE TABLE message
(

    id         UUID PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    content    TEXT,
    channel_id UUID        NOT NULL,
    author_id  UUID,


    FOREIGN KEY (channel_id) REFERENCES channel (id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES "user" (id) ON DELETE SET NULL


);


CREATE TABLE message_attachment
(

    message_id    UUID NOT NULL,
    attachment_id UUID NOT NULL,

    PRIMARY KEY (message_id, attachment_id),
    FOREIGN KEY (message_id) REFERENCES message (id) ON DELETE CASCADE,
    FOREIGN KEY (attachment_id) REFERENCES binary_content (id) ON DELETE CASCADE

);
