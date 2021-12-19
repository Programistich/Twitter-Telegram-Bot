CREATE TABLE IF NOT EXISTS twitter_users
(
    username   VARCHAR(512) PRIMARY KEY,
    last_like  BIGINT NULL,
    last_tweet BIGINT NULL
);

CREATE TABLE IF NOT EXISTS chats
(
    chat_id VARCHAR(512) PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS twitter_chats
(
    twitter_username VARCHAR(512),
    chat_id          VARCHAR(512)
)