ALTER TABLE `scope_entry` ADD `dcql_query` LONGTEXT;
ALTER TABLE `scope_entry` ALTER COLUMN `presentation_definition` DROP NOT NULL;