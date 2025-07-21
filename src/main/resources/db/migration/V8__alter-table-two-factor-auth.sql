ALTER TABLE tb_two_factor_auth
ADD (
    expiration_at DATETIME DEFAULT NULL,
    is_active BOOLEAN NOT NULL
);
