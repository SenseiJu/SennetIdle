CREATE TABLE IF NOT EXISTS `migrations` (
    `version`    INT      NOT NULL PRIMARY KEY,
    `checksum`   CHAR(64) NOT NULL,
    `applied_at` BIGINT   NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
