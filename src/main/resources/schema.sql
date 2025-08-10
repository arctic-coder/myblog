-- Посты
CREATE TABLE posts (
    id IDENTITY PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    text CLOB NOT NULL,
    tags_csv VARCHAR(512) NOT NULL DEFAULT '',
    image_data BLOB,
    likes_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_posts_created_at ON posts(created_at DESC);

-- Комментарии
CREATE TABLE comments (
    id IDENTITY PRIMARY KEY,
    post_id BIGINT NOT NULL,
    text CLOB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comments_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);

CREATE INDEX idx_comments_post ON comments(post_id);
