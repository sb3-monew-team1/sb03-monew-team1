-- =============================
-- 💣 Drop all tables if exist
-- =============================
DROP TABLE IF EXISTS news_article_interest CASCADE;
DROP TABLE IF EXISTS news_view CASCADE;
DROP TABLE IF EXISTS comment_like CASCADE;
DROP TABLE IF EXISTS comment CASCADE;
DROP TABLE IF EXISTS activity_log CASCADE;
DROP TABLE IF EXISTS notification CASCADE;
DROP TABLE IF EXISTS interest_keyword CASCADE;
DROP TABLE IF EXISTS subscription CASCADE;
DROP TABLE IF EXISTS news_article CASCADE;
DROP TABLE IF EXISTS interest CASCADE;
DROP TABLE IF EXISTS "user" CASCADE;

-- =============================
-- 🛠 Create tables
-- =============================

CREATE TABLE "user"
(
    id         BIGSERIAL PRIMARY KEY,
    email      VARCHAR(100)             NOT NULL UNIQUE,
    nickname   VARCHAR(20)              NOT NULL,
    password   VARCHAR(60)              NOT NULL,
    is_deleted BOOLEAN                  NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE interest
(
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(255)             NOT NULL,
    subscriber_count BIGINT DEFAULT 0,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at       TIMESTAMP WITH TIME ZONE
);

CREATE TABLE subscription
(
    id          BIGSERIAL PRIMARY KEY,
    interest_id BIGINT                   NOT NULL,
    user_id     BIGINT                   NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (interest_id, user_id),
    FOREIGN KEY (interest_id) REFERENCES interest (id),
    FOREIGN KEY (user_id) REFERENCES "user" (id)
);

CREATE TABLE interest_keyword
(
    id          BIGSERIAL PRIMARY KEY,
    keyword     VARCHAR(255)             NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    interest_id BIGINT                   NOT NULL,
    FOREIGN KEY (interest_id) REFERENCES interest (id)
);

CREATE TABLE news_article
(
    id            BIGSERIAL PRIMARY KEY,
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

CREATE TABLE "comment"
(
    id              BIGSERIAL PRIMARY KEY,
    content         VARCHAR(500)             NOT NULL,
    like_count      BIGINT                   NOT NULL DEFAULT 0,
    is_deleted      BOOLEAN                  NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE,
    user_id         BIGINT                   NOT NULL,
    news_article_id BIGINT                   NOT NULL,
    FOREIGN KEY (user_id) REFERENCES "user" (id),
    FOREIGN KEY (news_article_id) REFERENCES news_article (id)
);

CREATE TABLE comment_like
(
    id         BIGSERIAL PRIMARY KEY,
    comment_id BIGINT                   NOT NULL,
    user_id    BIGINT                   NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (comment_id, user_id),
    FOREIGN KEY (comment_id) REFERENCES "comment" (id),
    FOREIGN KEY (user_id) REFERENCES "user" (id)
);

CREATE TABLE news_view
(
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT                   NOT NULL,
    news_article_id BIGINT                   NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (user_id, news_article_id),
    FOREIGN KEY (user_id) REFERENCES "user" (id),
    FOREIGN KEY (news_article_id) REFERENCES news_article (id)
);

CREATE TABLE news_article_interest
(
    id              BIGSERIAL PRIMARY KEY,
    news_article_id BIGINT                   NOT NULL,
    interest_id     BIGINT                   NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (news_article_id, interest_id),
    FOREIGN KEY (news_article_id) REFERENCES news_article (id),
    FOREIGN KEY (interest_id) REFERENCES interest (id)
);

CREATE TABLE activity_log
(
    id          BIGSERIAL PRIMARY KEY,
    action_type VARCHAR(15)              NOT NULL,
    target_id   BIGINT                   NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    user_id     BIGINT                   NOT NULL,
    FOREIGN KEY (user_id) REFERENCES "user" (id),
    CHECK (action_type IN ('VIEW_NEWS', 'LIKE_COMMENT', 'COMMENT', 'SUBSCRIBE'))
);

CREATE TABLE notification
(
    id            BIGSERIAL PRIMARY KEY,
    content       TEXT                     NOT NULL,
    resource_type VARCHAR(10)              NOT NULL,
    resource_id   BIGINT,
    is_checked    BOOLEAN                  NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITH TIME ZONE,
    user_id       BIGINT                   NOT NULL,
    FOREIGN KEY (user_id) REFERENCES "user" (id),
    CHECK (resource_type IN ('INTEREST', 'COMMENT'))
);