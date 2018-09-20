CREATE TABLE accounts (
  email VARCHAR(255) NOT NULL,
  password CHAR(60) NOT NULL,
  authority VARCHAR(45) NOT NULL DEFAULT 'ROLE_USER',
  valid_email BOOLEAN NOT NULL DEFAULT false,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_login TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  email_token CHAR(40) DEFAULT NULL UNIQUE,
  password_token CHAR(40) DEFAULT NULL UNIQUE,
  PRIMARY KEY (email)
) CHARACTER SET utf8 COLLATE utf8_bin;

CREATE UNIQUE INDEX ix_auth_email on accounts (email);

CREATE TABLE persistent_logins (
  username VARCHAR(255) NOT NULL,
  series VARCHAR(64) NOT NULL,
  token VARCHAR(64) NOT NULL,
  last_used TIMESTAMP NOT NULL,
  PRIMARY KEY (series)
);

INSERT INTO accounts (email, password, authority, valid_email) VALUES ('admin@t6sdl.tokyo', '$2a$10$Y4JO/BcrS9JBPG6cJycBp.tb2ouD5ywOnxo5GXZt2h2jR81afKv1S', 'ROLE_ADMIN', true);
INSERT INTO accounts (email, password, valid_email) VALUES ('user1@t6sdl.tokyo', '$2a$10$Y4JO/BcrS9JBPG6cJycBp.tb2ouD5ywOnxo5GXZt2h2jR81afKv1S', true);
INSERT INTO accounts (email, password, valid_email) VALUES ('user2@t6sdl.tokyo', '$2a$10$Y4JO/BcrS9JBPG6cJycBp.tb2ouD5ywOnxo5GXZt2h2jR81afKv1S', true);
INSERT INTO accounts (email, password, valid_email) VALUES ('user3@t6sdl.tokyo', '$2a$10$Y4JO/BcrS9JBPG6cJycBp.tb2ouD5ywOnxo5GXZt2h2jR81afKv1S', false);
