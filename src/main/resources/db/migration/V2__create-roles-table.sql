CREATE TABLE tb_role(
        id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
        name VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO tb_role (name)
VALUES('USER');
INSERT INTO tb_role (name)
VALUES('ADMIN');

ALTER TABLE tb_user ADD CONSTRAINT
    fk_user_role FOREIGN KEY (role_id) REFERENCES tb_role(id);
