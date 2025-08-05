CREATE TABLE tb_expense_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    spending_priority VARCHAR(255) NOT NULL,
    user_id BIGINT,
    CONSTRAINT fk_expense_category_user_id FOREIGN KEY (user_id) REFERENCES tb_user(id)
);