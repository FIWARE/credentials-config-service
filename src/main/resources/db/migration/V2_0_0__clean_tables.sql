DROP TABLE IF EXISTS `service` CASCADE;

-- Table: service
CREATE TABLE `service` (
    `id` varchar(255) NOT NULL PRIMARY KEY,
    `default_oidc_scope` varchar(255)
);

-- Table: scope_entry
CREATE TABLE `scope_entry` (
    `id` int NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `service_id` varchar(255) REFERENCES `service` (`id`),
    `scope_key` varchar(255)
);

-- Table: credential
CREATE TABLE `credential` (
    `id` int NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `scope_entry_id` INTEGER REFERENCES `scope_entry` (`id`),
    `credential_type` varchar(255),
    `verify_holder` BOOLEAN,
    `holder_claim` varchar(255)
);

-- Table: endpoint_entry
CREATE TABLE `endpoint_entry` (
    `id` int NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `credential_id` INTEGER REFERENCES `credential` (`id`),
    `type` varchar(255),
    `list_type` varchar(255) DEFAULT 'EBSI',
    `endpoint` varchar(255)
);

-- Table: presentation_definition
CREATE TABLE `presentation_definition` (
    `id` varchar(255) NOT NULL PRIMARY KEY,
    `scope_entry_id` INTEGER REFERENCES `scope_entry` (`id`),
    `name` varchar(255),
    `purpose` varchar(255)
);

-- Table: input_descriptor
CREATE TABLE `input_descriptor` (
    `id` varchar(255) NOT NULL PRIMARY KEY,
    `presentation_definition_id` varchar(255) REFERENCES `presentation_definition` (`id`),
    `name` varchar(255),
    `purpose` varchar(255)
);

-- Table: constraints
CREATE TABLE `constraints` (
    `id` int NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `input_descriptor_id` varchar(255) REFERENCES `input_descriptor` (`id`)
);

-- Table: field
CREATE TABLE `field` (
    `id` varchar(255) NOT NULL PRIMARY KEY,
    `constraints_id` INTEGER REFERENCES `constraints` (`id`),
    `name` varchar(255),
    `purpose` varchar(255),
    `optional` BOOLEAN
);

-- Table: field_path (element collection for field.path)
CREATE TABLE `field_path` (
    `field_id` varchar(255) REFERENCES `field` (`id`),
    `path` varchar(255),
    PRIMARY KEY (`field_id`, `path`)
);

-- Table: format_object
CREATE TABLE `format_object` (
    `id` int NOT NULL AUTO_INCREMENT PRIMARY KEY
);

-- Table: format_alg (element collection for format_object.alg)
CREATE TABLE `format_alg` (
    `format_object_id` INTEGER REFERENCES `format_object` (`id`) ON DELETE CASCADE,
    `alg` varchar(255),
    PRIMARY KEY (`format_object_id`, `alg`)
);

-- Table: format_proof_type (element collection for format_object.proofType)
CREATE TABLE `format_proof_type` (
    `format_object_id` INTEGER REFERENCES `format_object` (`id`) ON DELETE CASCADE,
    `proof_type` varchar(255),
    PRIMARY KEY (`format_object_id`, `proof_type`)
);

-- Table: format_object_mapping (join table for Map<String, FormatObject>)
CREATE TABLE `format_object_mapping` (
    `id` int NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `format_key` varchar(255) NOT NULL,
    `presentation_definition_id` varchar(255) REFERENCES `presentation_definition` (`id`) ON DELETE CASCADE,
    `format_object_id` INTEGER REFERENCES `format_object` (`id`) ON DELETE CASCADE
);

