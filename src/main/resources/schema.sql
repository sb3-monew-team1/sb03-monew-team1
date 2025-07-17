-- 유저에게 DB 권한 부여
GRANT ALL PRIVILEGES ON DATABASE monew TO monew_user;

-- 기본 public 스키마 대신 사용자 전용 공간에서의 관리를 위한 스키마 생성
CREATE SCHEMA IF NOT EXISTS monew AUTHORIZATION monew_user;

-- monew_user 유저 기본 접근 스키마 설정
ALTER ROLE monew_user SET search_path TO monew;

-- 이후 모든 테이블 생성 및 쿼리는 monew 스키마 내에서 진행
SET search_path TO monew;

-- =============================
-- 💣 Drop all tables if exist
-- =============================
DROP TABLE IF EXISTS article_interests CASCADE;
DROP TABLE IF EXISTS article_views CASCADE;
DROP TABLE IF EXISTS comment_likes CASCADE;
DROP TABLE IF EXISTS comments CASCADE;
DROP TABLE IF EXISTS activity_logs CASCADE;
DROP TABLE IF EXISTS notifications CASCADE;
DROP TABLE IF EXISTS interest_keywords CASCADE;
DROP TABLE IF EXISTS subscriptions CASCADE;
DROP TABLE IF EXISTS articles CASCADE;
DROP TABLE IF EXISTS interests CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- =============================
-- 🛠 Create tables (UUID version, NO DEFAULT)
-- =============================

CREATE TABLE users
(
    id         UUID PRIMARY KEY,
    email      VARCHAR(100)             NOT NULL UNIQUE,
    nickname   VARCHAR(20)              NOT NULL,
    password   VARCHAR(60)              NOT NULL,
    is_deleted BOOLEAN                  NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE interests
(
    id               UUID PRIMARY KEY,
    name             VARCHAR(255)             NOT NULL,
    subscriber_count BIGINT DEFAULT 0,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at       TIMESTAMP WITH TIME ZONE
);

CREATE TABLE subscriptions
(
    id          UUID PRIMARY KEY,
    interest_id UUID                    NOT NULL,
    user_id     UUID                    NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (interest_id, user_id),
    FOREIGN KEY (interest_id) REFERENCES interests (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE interest_keywords
(
    id          UUID PRIMARY KEY,
    keyword     VARCHAR(255)             NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    interest_id UUID                    NOT NULL,
    FOREIGN KEY (interest_id) REFERENCES interests (id)
);

CREATE TABLE articles
(
    id            UUID PRIMARY KEY,
    source        VARCHAR(50)              NOT NULL,
    source_url    TEXT                     NOT NULL,
    title         VARCHAR(500)             NOT NULL,
    publish_date  TIMESTAMP WITH TIME ZONE NOT NULL,
    summary       TEXT,
    comment_count BIGINT                   NOT NULL DEFAULT 0,
    view_count    BIGINT                   NOT NULL DEFAULT 0,
    is_deleted    BOOLEAN                  NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE comments
(
    id              UUID PRIMARY KEY,
    content         VARCHAR(500)             NOT NULL,
    like_count      BIGINT                   NOT NULL DEFAULT 0,
    is_deleted      BOOLEAN                  NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE,
    user_id         UUID                     NOT NULL,
    article_id UUID                     NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (article_id) REFERENCES articles (id)
);

CREATE TABLE comment_likes
(
    id         UUID PRIMARY KEY,
    comment_id UUID                     NOT NULL,
    user_id    UUID                     NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (comment_id, user_id),
    FOREIGN KEY (comment_id) REFERENCES comments (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE article_views
(
    id              UUID PRIMARY KEY,
    user_id         UUID                     NOT NULL,
    article_id UUID                     NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (user_id, article_id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (article_id) REFERENCES articles (id)
);

CREATE TABLE article_interests
(
    id              UUID PRIMARY KEY,
    article_id UUID                     NOT NULL,
    interest_id     UUID                     NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (article_id, interest_id),
    FOREIGN KEY (article_id) REFERENCES articles (id),
    FOREIGN KEY (interest_id) REFERENCES interests (id)
);

CREATE TABLE activity_logs
(
    id          UUID PRIMARY KEY,
    action_type VARCHAR(15)              NOT NULL,
    target_id   UUID                     NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    user_id     UUID                     NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id),
    CHECK (action_type IN ('VIEW_ARTICLE', 'LIKE_COMMENT', 'COMMENT', 'SUBSCRIBE'))
);

CREATE TABLE notifications
(
    id            UUID PRIMARY KEY,
    content       TEXT                     NOT NULL,
    resource_type VARCHAR(10)              NOT NULL,
    resource_id   UUID,
    is_checked    BOOLEAN                  NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITH TIME ZONE,
    user_id       UUID                     NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id),
    CHECK (resource_type IN ('INTEREST', 'COMMENT'))
);