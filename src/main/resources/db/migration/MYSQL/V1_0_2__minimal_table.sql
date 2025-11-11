DROP TABLE IF EXISTS `endpoint_entry` CASCADE;
DROP TABLE IF EXISTS `credential` CASCADE;
DROP TABLE IF EXISTS `service` CASCADE;

CREATE TABLE IF NOT EXISTS `service` (
    `id` varchar(255) NOT NULL PRIMARY KEY,
    `default_oidc_scope` varchar(255) NOT NULL,
    `oidc_scopes` LONGTEXT NOT NULL
);