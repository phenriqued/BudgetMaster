CREATE TABLE tb_expense (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    description VARCHAR(255) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    expense_category_id BIGINT NOT NULL,
    entry_date TIMESTAMP NOT NULL,

    CONSTRAINT fk_tb_expense_user FOREIGN KEY (user_id) REFERENCES tb_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_tb_expense_category_id FOREIGN KEY (expense_category_id) REFERENCES tb_expense_category(id)
);