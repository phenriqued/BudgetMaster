CREATE TABLE tb_refresh_token (
    id BINARY(16) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token TEXT NOT NULL UNIQUE,
    token_type VARCHAR(50) NOT NULL,
    identifier VARCHAR(100) NOT NULL UNIQUE,
    expiration_token DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_refresh_user FOREIGN KEY (user_id) REFERENCES tb_user(id)
);
