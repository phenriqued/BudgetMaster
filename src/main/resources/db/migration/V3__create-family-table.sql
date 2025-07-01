CREATE TABLE tb_family (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE tb_user_family (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    family_id BIGINT NOT NULL,
    role_family VARCHAR(20) NOT NULL,
    joined_at DATE,

    CONSTRAINT fk_user_family_user FOREIGN KEY (user_id) REFERENCES tb_user(id),
    CONSTRAINT fk_user_family_family FOREIGN KEY (family_id) REFERENCES tb_family(id),
    CONSTRAINT uq_user_family_unique UNIQUE (user_id, family_id)
);

CREATE INDEX idx_user_family_user_id ON tb_user_family(user_id);
CREATE INDEX idx_user_family_family_id ON tb_user_family(family_id);