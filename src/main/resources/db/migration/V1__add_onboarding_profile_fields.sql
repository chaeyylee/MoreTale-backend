-- UserProfile 테이블 구조 변경 (PostgreSQL)
ALTER TABLE user_profiles
    -- 1. 나이 그룹 추가
    ADD COLUMN age_group VARCHAR(20),

    -- 2. 언어 설정 및 능력 추가
    ADD COLUMN first_language VARCHAR(10),
    ADD COLUMN first_language_proficiency VARCHAR(20),
    ADD COLUMN second_language VARCHAR(10),
    ADD COLUMN second_language_proficiency VARCHAR(20),
    ADD COLUMN first_language_listening VARCHAR(20),
    ADD COLUMN first_language_speaking VARCHAR(20),
    ADD COLUMN second_language_listening VARCHAR(20),
    ADD COLUMN second_language_speaking VARCHAR(20),

    -- 3. 가족 구조 및 선호도 추가
    ADD COLUMN family_structure VARCHAR(30),
    ADD COLUMN custom_family_structure VARCHAR(200),
    ADD COLUMN story_preference VARCHAR(30),
    ADD COLUMN custom_story_preference VARCHAR(200),

    -- 4. 기존 컬럼 제약 조건 수정 (PostgreSQL 문법)
    ALTER COLUMN primary_language DROP NOT NULL,
    ALTER COLUMN secondary_language DROP NOT NULL;
