ALTER TABLE `scope_entry` ADD `dcql_query` LONGTEXT;
ALTER TABLE `scope_entry` MODIFY COLUMN `presentation_definition` LONGTEXT NULL DEFAULT NULL;