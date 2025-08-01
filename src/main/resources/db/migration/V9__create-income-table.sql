CREATE TABLE tb_income (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    description VARCHAR(255) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    entry_date TIMESTAMP NOT NULL,

    CONSTRAINT fk_tb_income_user FOREIGN KEY (user_id) REFERENCES tb_user(id) ON DELETE CASCADE
);