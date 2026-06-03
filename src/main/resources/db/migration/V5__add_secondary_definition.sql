ALTER TABLE story_tokens
    ADD COLUMN IF NOT EXISTS secondary_definition TEXT;

ALTER TABLE vocabulary_entries
    ADD COLUMN IF NOT EXISTS secondary_definition TEXT;