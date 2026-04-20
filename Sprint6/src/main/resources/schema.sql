-- 기존 테이블 삭제 (초기화용)
DROP TABLE IF EXISTS message_attachment CASCADE;
DROP TABLE IF EXISTS message CASCADE;
DROP TABLE IF EXISTS channel CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- 1. Users 테이블
CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       username VARCHAR(255) UNIQUE NOT NULL,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       profile_id UUID,
                       created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Channel 테이블
CREATE TABLE channel (
                         id UUID PRIMARY KEY,
                         name VARCHAR(255) NOT NULL,
                         description TEXT,
                         type VARCHAR(50) NOT NULL,
                         created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. Message 테이블
CREATE TABLE message (
                         id UUID PRIMARY KEY,
                         content TEXT NOT NULL,
                         author_id UUID REFERENCES users(id) ON DELETE SET NULL, -- 유저 삭제 시 작성자 미상 처리
                         channel_id UUID REFERENCES channel(id) ON DELETE CASCADE, -- 채널 삭제 시 메시지 삭제
                         created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);