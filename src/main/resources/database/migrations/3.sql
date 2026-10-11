CREATE TABLE IF NOT EXISTS `balance_transactions` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `user_uuid`  CHAR(36)     NOT NULL,
    `currency`   VARCHAR(32)  NOT NULL,
    `amount`     DOUBLE       NOT NULL,
    `balance`    DOUBLE       NOT NULL,
    `reason`     VARCHAR(128) NOT NULL,
    `created_at` BIGINT       NOT NULL,
    INDEX `idx_balance_transactions_user` (`user_uuid`, `created_at`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `user_prestige_progress` (
    `user_uuid` CHAR(36)    NOT NULL,
    `resource`  VARCHAR(64) NOT NULL,
    `amount`    DOUBLE      NOT NULL DEFAULT 0,
    PRIMARY KEY (`user_uuid`, `resource`),
    FOREIGN KEY (`user_uuid`) REFERENCES `users` (`uuid`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `user_upgrades` (
    `user_uuid` CHAR(36)    NOT NULL,
    `upgrade`   VARCHAR(64) NOT NULL,
    `level`     INT         NOT NULL DEFAULT 0,
    PRIMARY KEY (`user_uuid`, `upgrade`),
    FOREIGN KEY (`user_uuid`) REFERENCES `users` (`uuid`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `user_generators` (
    `user_uuid`      CHAR(36)    NOT NULL,
    `slot`           INT         NOT NULL,
    `blueprint`      VARCHAR(64) NOT NULL,
    `blueprint_uuid` CHAR(36)    NOT NULL,
    `uses`           INT         NOT NULL,
    `progress_ms`    BIGINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`user_uuid`, `slot`),
    UNIQUE KEY `uq_user_generators_blueprint` (`blueprint_uuid`),
    FOREIGN KEY (`user_uuid`) REFERENCES `users` (`uuid`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
