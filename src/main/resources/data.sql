INSERT INTO user_profiles (
    user_id, child_name, age_group, child_age, child_nationality, parent_country,
    first_language, first_language_proficiency, second_language, second_language_proficiency,
    first_language_listening, first_language_speaking, second_language_listening, second_language_speaking,
    family_structure, story_preference,
    primary_language, secondary_language,
    created_at
)
SELECT
    u.user_id, '민준', 'AGE_5_6', 6, '대한민국', '베트남',
    'ko', 'BUTTERFLY', 'vi', 'CATERPILLAR',
    'BUTTERFLY', 'BUTTERFLY', 'CHRYSALIS', 'CATERPILLAR',
    'TWO_PARENTS', 'WARM',
    'ko', 'vi',
    NOW()
FROM users u
WHERE u.email = 'testuser@example.com'
AND NOT EXISTS (SELECT 1 FROM user_profiles WHERE user_id = u.user_id);