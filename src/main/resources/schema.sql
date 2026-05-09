-- =========================
-- users 테이블
-- =========================
CREATE TABLE IF NOT EXISTS users (
    user_id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    nickname VARCHAR(50),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    provider VARCHAR(20),
    provider_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- user_profiles 테이블
-- =========================
CREATE TABLE IF NOT EXISTS user_profiles (
    user_id BIGINT PRIMARY KEY,
    child_name VARCHAR(50) NOT NULL,
    child_age INT NOT NULL,
    child_nationality VARCHAR(50),
    parent_country VARCHAR(50) NOT NULL,
    primary_language VARCHAR(10) NOT NULL DEFAULT 'ko',
    secondary_language VARCHAR(10) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_user_profiles_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
);

-- =========================
-- 인덱스
-- =========================
CREATE INDEX IF NOT EXISTS idx_user_profiles_user_id ON user_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_provider ON users(provider, provider_id);