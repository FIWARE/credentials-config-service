CREATE TABLE IF NOT EXISTS `service` (
    `id` varchar(255) NOT NULL PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS `credential` (
    `id` int NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `credential_type` varchar(255) NOT NULL,
    `service_id` varchar(255) NOT NULL,
    CONSTRAINT `fk_service` FOREIGN KEY (`service_id`) REFERENCES `service` (`id`) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS `endpoint_entry` (
    `id` int NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `endpoint` varchar(255) NOT NULL,
    `type` varchar(100) NOT NULL,
    `credential_id` int NOT NULL,
    CONSTRAINT `fk_credential` FOREIGN KEY (`credential_id`) REFERENCES `credential` (`id`) ON DELETE CASCADE
);