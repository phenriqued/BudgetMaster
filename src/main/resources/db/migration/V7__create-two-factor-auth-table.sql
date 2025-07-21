CREATE TABLE tb_two_factor_auth (
    id BINARY(16) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    secret VARCHAR(255) NOT NULL UNIQUE,
    type_2FA VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_two_factor_auth_user FOREIGN KEY (user_id) REFERENCES tb_user(id) ON DELETE CASCADE
    );
