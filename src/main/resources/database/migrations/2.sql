CREATE TABLE IF NOT EXISTS `users` (
    `uuid`            CHAR(36)    NOT NULL PRIMARY KEY,
    `stage`           INT         NOT NULL DEFAULT 1,
    `wave`            INT         NOT NULL DEFAULT 1,
    `prestige`        INT         NOT NULL DEFAULT 0,
    `prestige_points` INT         NOT NULL DEFAULT 0,
    `offline_time_ms` BIGINT      NOT NULL DEFAULT 0,
    `last_seen`       BIGINT      NOT NULL,
    `created_at`      BIGINT      NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `user_resources` (
    `user_uuid` CHAR(36)    NOT NULL,
    `resource`    VARCHAR(64) NOT NULL,
    `amount`      DOUBLE      NOT NULL DEFAULT 0,
    PRIMARY KEY (`user_uuid`, `resource`),
    FOREIGN KEY (`user_uuid`) REFERENCES `users` (`uuid`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `user_balances` (
    `user_uuid` CHAR(36)    NOT NULL,
    `currency`    VARCHAR(32) NOT NULL,
    `amount`      DOUBLE      NOT NULL DEFAULT 0,
    PRIMARY KEY (`user_uuid`, `currency`),
    FOREIGN KEY (`user_uuid`) REFERENCES `users` (`uuid`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
