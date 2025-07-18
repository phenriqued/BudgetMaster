ALTER TABLE tb_security_user_token
DROP FOREIGN KEY fk_security_token_user;

ALTER TABLE tb_security_user_token
ADD CONSTRAINT fk_security_token_user
FOREIGN KEY (user_id) REFERENCES tb_user(id)
ON DELETE CASCADE;