-- =============================
-- 💣 Drop all tables if exist
-- =============================
DROP TABLE IF EXISTS news_article_interests CASCADE;
DROP TABLE IF EXISTS news_views CASCADE;
DROP TABLE IF EXISTS comment_likes CASCADE;
DROP TABLE IF EXISTS comments CASCADE;
DROP TABLE IF EXISTS activity_logs CASCADE;
DROP TABLE IF EXISTS notifications CASCADE;
DROP TABLE IF EXISTS interest_keywords CASCADE;
DROP TABLE IF EXISTS subscriptions CASCADE;
DROP TABLE IF EXISTS news_articles CASCADE;
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

CREATE TABLE news_articles
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
    news_article_id UUID                     NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (news_article_id) REFERENCES news_articles (id)
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

CREATE TABLE news_views
(
    id              UUID PRIMARY KEY,
    user_id         UUID                     NOT NULL,
    news_article_id UUID                     NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (user_id, news_article_id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (news_article_id) REFERENCES news_articles (id)
);

CREATE TABLE news_article_interests
(
    id              UUID PRIMARY KEY,
    news_article_id UUID                     NOT NULL,
    interest_id     UUID                     NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (news_article_id, interest_id),
    FOREIGN KEY (news_article_id) REFERENCES news_articles (id),
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
    CHECK (action_type IN ('VIEW_NEWS', 'LIKE_COMMENT', 'COMMENT', 'SUBSCRIBE'))
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
