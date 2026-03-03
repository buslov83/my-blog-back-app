CREATE TABLE IF NOT EXISTS posts (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    title              VARCHAR(255)  NOT NULL,
    text               CLOB          NOT NULL,
    image              BLOB,
    image_content_type VARCHAR(128),
    likes_count        INT           NOT NULL DEFAULT 0,
    tags               VARCHAR(1024)
);

CREATE TABLE IF NOT EXISTS comments (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    text    VARCHAR(4096) NOT NULL,
    post_id BIGINT        NOT NULL,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);