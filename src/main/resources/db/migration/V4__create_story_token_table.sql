CREATE TABLE story_tokens (
    token_id        BIGSERIAL PRIMARY KEY,
    slide_id        BIGINT          NOT NULL,
    text            VARCHAR(100)    NOT NULL,
    token_order     INT             NOT NULL,
    highlight       BOOLEAN         NOT NULL DEFAULT FALSE,
    translation     VARCHAR(200),
    definition      TEXT,
    audio_url       VARCHAR(500),
    source_language VARCHAR(10),
    target_language VARCHAR(10),

    CONSTRAINT fk_story_token_slide
        FOREIGN KEY (slide_id) REFERENCES slides(slide_id)
        ON DELETE CASCADE
);

CREATE INDEX idx_story_token_slide_id
ON story_tokens (slide_id);