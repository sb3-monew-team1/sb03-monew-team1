-- =============================
-- 💣 Drop all tables if exist
-- H2는 항상 초기화니까 불필요
-- Postgres에서 필요할 때 활성화 또는 별도 쿼리문 적용
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

CREATE TABLE IF NOT EXISTS users
(
    id         UUID PRIMARY KEY,
    email      VARCHAR(100)             NOT NULL UNIQUE,
    nickname   VARCHAR(20)              NOT NULL,
    password   VARCHAR(60)              NOT NULL,
    is_deleted BOOLEAN                  NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS interests
(
    id               UUID PRIMARY KEY,
    name             VARCHAR(255)             NOT NULL,
    subscriber_count BIGINT DEFAULT 0,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at       TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS subscriptions
(
    id          UUID PRIMARY KEY,
    interest_id UUID                     NOT NULL,
    user_id     UUID                     NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (interest_id, user_id),
    FOREIGN KEY (interest_id) REFERENCES interests (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS interest_keywords
(
    id          UUID PRIMARY KEY,
    keyword     VARCHAR(255)             NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    interest_id UUID                     NOT NULL,
    FOREIGN KEY (interest_id) REFERENCES interests (id)
);

CREATE TABLE IF NOT EXISTS articles
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

CREATE TABLE IF NOT EXISTS comments
(
    id         UUID PRIMARY KEY,
    content    VARCHAR(500)             NOT NULL,
    like_count BIGINT                   NOT NULL DEFAULT 0,
    is_deleted BOOLEAN                  NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    user_id    UUID                     NOT NULL,
    article_id UUID                     NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (article_id) REFERENCES articles (id)
);

CREATE TABLE IF NOT EXISTS comment_likes
(
    id         UUID PRIMARY KEY,
    comment_id UUID                     NOT NULL,
    user_id    UUID                     NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (comment_id, user_id),
    FOREIGN KEY (comment_id) REFERENCES comments (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS article_views
(
    id         UUID PRIMARY KEY,
    user_id    UUID                     NOT NULL,
    article_id UUID                     NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (user_id, article_id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (article_id) REFERENCES articles (id)
);

CREATE TABLE IF NOT EXISTS article_interests
(
    id          UUID PRIMARY KEY,
    article_id  UUID                     NOT NULL,
    interest_id UUID                     NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (article_id, interest_id),
    FOREIGN KEY (article_id) REFERENCES articles (id),
    FOREIGN KEY (interest_id) REFERENCES interests (id)
);

CREATE TABLE IF NOT EXISTS activity_logs
(
    id          UUID PRIMARY KEY,
    action_type VARCHAR(15)              NOT NULL,
    target_id   UUID                     NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    user_id     UUID                     NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id),
    CHECK (action_type IN ('VIEW_ARTICLE', 'LIKE_COMMENT', 'COMMENT', 'SUBSCRIBE'))
);

CREATE TABLE IF NOT EXISTS notifications
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