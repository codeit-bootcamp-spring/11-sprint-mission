create table binary_contents
(
    id           UUID PRIMARY KEY,
    created_at   timestamptz  not null,
    file_name    varchar(255) not null,
    size         bigint       not null,
    content_type varchar(100) not null
);

create table users
(
    id         UUID PRIMARY KEY,
    created_at TIMESTAMPTZ         NOT NULL,
    updated_at TIMESTAMPTZ,
    username   varchar(50) UNIQUE  NOT NULL,
    email      varchar(100) UNIQUE NOT NULL,
    password   varchar(60)         NOT NULL,
    profile_id UUID UNIQUE         references binary_contents (id) on delete set null,
    role       varchar(20)         NOT NULL
);

create table channels
(
    id          uuid primary key,
    created_at  timestamptz not null,
    updated_at  timestamptz,
    name        varchar(100),
    description varchar(500),
    type        varchar(10) not null check (type in ('PUBLIC', 'PRIVATE'))
);

create table read_statuses
(
    id           uuid primary key,
    created_at   timestamptz not null,
    updated_at   timestamptz,
    user_id      uuid        not null references users (id) on delete cascade,
    channel_id   uuid        not null references channels (id) on delete cascade,
    last_read_at timestamptz,

    UNIQUE (user_id, channel_id)
);

create table messages
(
    id         uuid primary key,
    created_at timestamptz not null,
    updated_at timestamptz,
    content    text,
    channel_id uuid        not null references channels (id) on delete cascade,
    author_id  uuid        references users (id) on delete set null
);

create table message_attachments
(
    message_id    uuid not null references messages (id) on delete cascade,
    attachment_id uuid not null references binary_contents (id) on delete cascade
);