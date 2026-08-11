CREATE TABLE IF NOT EXISTS phrase_user (
    id              INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(100) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    account_balance NUMERIC(12,2) NOT NULL DEFAULT 0,
    is_admin        TINYINT(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS fine_type (
    id     INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name   VARCHAR(20) NOT NULL UNIQUE CHECK (name IN ('LEICHT', 'STANDARD', 'SCHWER'))
) ENGINE=InnoDB;


CREATE TABLE IF NOT EXISTS phrase (
    id           INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    issuer_id    INT UNSIGNED NOT NULL REFERENCES phrase_user(id),
    receiver_id  INT UNSIGNED NOT NULL REFERENCES phrase_user(id),
    fine_type_id INT UNSIGNED NOT NULL REFERENCES fine_type(id),
    text         TEXT,
    issued_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    -- should self fine be possible? tending to yes (we all have weak moments and fair is fair)
    -- CONSTRAINT chk_fine_not_self CHECK (issuer_id <> receiver_id)
) ENGINE=InnoDB;

CREATE INDEX idx_fine_issued_at ON phrase (issued_at DESC);

CREATE TABLE IF NOT EXISTS phrase_like (
    id         INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    phrase_id  INT UNSIGNED NOT NULL REFERENCES phrase(id),
    user_id    INT UNSIGNED NOT NULL REFERENCES phrase_user(id),
    liked_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_phrase_like_phrase_user UNIQUE (phrase_id, user_id)
) ENGINE=InnoDB;

INSERT INTO fine_type (name) VALUES
    ('LEICHT'),
    ('STANDARD'),
    ('SCHWER');

INSERT INTO phrase_user (username, password_hash, account_balance, is_admin) VALUES
    ('Alex',      '$2b$12$dvjzNSB1DZHkGfW35SpC8.W5xveQYfSmMvw0kNFogDPouU018L3xq', 0, 0),
    ('Sebastian', '$2b$12$GHDmLoNEUE/h9qWAO1hOCOV5PJv9UTwz.mkzck.xaDwJTj.jfXl1G', 0, 0),
    ('Heidrun',   '$2b$12$NQw8HRWSlta8R.K3eeaKaecySvBZNPsKlVbLbRlQAxSnLbFObqEKC', 0, 0),
    -- default password: admin123 -- change before any real deployment
    ('admin',     '$2a$10$CtbogRWmKjrR584zOKCRy.cWnjOPFVGtJu/EPrWlsiu/hGdjOVmYu', 0, 1);