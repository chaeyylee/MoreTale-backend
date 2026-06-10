-- 1. 컬럼 추가 (PostgreSQL) --
ALTER TABLE user_profiles
    ADD COLUMN first_language_enum VARCHAR(10),
    ADD COLUMN custom_first_language VARCHAR(100),
    ADD COLUMN second_language_enum VARCHAR(10),
    ADD COLUMN custom_second_language VARCHAR(100);

-- 2. 기존 데이터 → Enum 변환
UPDATE user_profiles
SET first_language_enum = CASE first_language
    WHEN 'ko' THEN 'KO'
    WHEN 'en' THEN 'EN'
    WHEN 'ja' THEN 'JA'
    WHEN 'zh' THEN 'ZH'
    WHEN 'es' THEN 'ES'
    WHEN 'vi' THEN 'VI'
    ELSE 'OTHER'
END,
custom_first_language = CASE
    WHEN first_language NOT IN ('ko','en','ja','zh','es','vi') THEN first_language
    ELSE NULL
END;

UPDATE user_profiles
SET second_language_enum = CASE second_language
    WHEN 'ko' THEN 'KO'
    WHEN 'en' THEN 'EN'
    WHEN 'ja' THEN 'JA'
    WHEN 'zh' THEN 'ZH'
    WHEN 'es' THEN 'ES'
    WHEN 'vi' THEN 'VI'
    ELSE 'OTHER'
END,
custom_second_language = CASE
    WHEN second_language NOT IN ('ko','en','ja','zh','es','vi') THEN second_language
    ELSE NULL
END;

-- 3. NOT NULL 제약 (PostgreSQL) --
ALTER TABLE user_profiles
    ALTER COLUMN first_language_enum SET NOT NULL;

ALTER TABLE user_profiles
    ALTER COLUMN second_language_enum SET NOT NULL;