ALTER TABLE scope_entry ADD COLUMN dcql_query TEXT;
ALTER TABLE scope_entry ALTER COLUMN presentation_definition DROP NOT NULL;