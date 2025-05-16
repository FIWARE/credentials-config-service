DROP TABLE IF EXISTS `service` CASCADE;

-- Table: service
CREATE TABLE `service` (
    `id` varchar(255) NOT NULL PRIMARY KEY,
    `default_oidc_scope` varchar(255)
);

-- Table: scope_entry
CREATE TABLE `scope_entry` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `service_id` varchar(255) REFERENCES `service` (`id`) ON DELETE CASCADE,
    `scope_key` varchar(255),
    `credentials` LONGTEXT NOT NULL,
    `presentation_definition` LONGTEXT NOT NULL
);
