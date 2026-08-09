CREATE TABLE IF NOT EXISTS admin (
    id            INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS phrase_user (
    id              INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(100) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    account_balance NUMERIC(12,2) NOT NULL DEFAULT 0
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
    text         TEXT NOT NULL,
    issued_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    -- should self fine be possible? tending to yes (we all have weak moments and fair is fair)
    -- CONSTRAINT chk_fine_not_self CHECK (issuer_id <> receiver_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS fine_like (
    user_id  INT UNSIGNED NOT NULL REFERENCES phrase_user(id),
    fine_id  INT UNSIGNED NOT NULL REFERENCES phrase(id),
    liked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, fine_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS admin_action (
    id             INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    admin_id       INT UNSIGNED NOT NULL REFERENCES admin(id),
    target_user_id INT UNSIGNED NOT NULL REFERENCES phrase_user(id),
    action_type    VARCHAR(20) NOT NULL CHECK (action_type IN ('ADD', 'REMOVE', 'RESET')),
    performed_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE INDEX idx_fine_issued_at ON phrase (issued_at DESC);

INSERT INTO fine_type (name) VALUES
    ('LEICHT'),
    ('STANDARD'),
    ('SCHWER');

INSERT INTO phrase_user (username, password_hash, account_balance) VALUES
    ('Alex',      '$2b$12$dvjzNSB1DZHkGfW35SpC8.W5xveQYfSmMvw0kNFogDPouU018L3xq', 0),
    ('Sebastian', '$2b$12$GHDmLoNEUE/h9qWAO1hOCOV5PJv9UTwz.mkzck.xaDwJTj.jfXl1G', 0),
    ('Heidrun',   '$2b$12$NQw8HRWSlta8R.K3eeaKaecySvBZNPsKlVbLbRlQAxSnLbFObqEKC', 0);

INSERT INTO admin (username, password_hash) VALUES
    ('admin', '$2b$12$BqAFt9mV/TWBXf6AEYWuQ.R45SHy0xv6h44Me9Bd/qssVO37AjEEC');