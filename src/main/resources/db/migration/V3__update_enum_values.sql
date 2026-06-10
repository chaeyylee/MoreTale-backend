UPDATE "user_profiles" SET
    first_language_proficiency = CASE
        WHEN first_language_proficiency = 'CATERPILLAR' THEN 'LARVA'
        WHEN first_language_proficiency = 'CHRYSALIS' THEN 'PUPA'
        WHEN first_language_proficiency = 'BUTTERFLY' THEN 'BEE'
        ELSE first_language_proficiency END,
    second_language_proficiency = CASE
        WHEN second_language_proficiency = 'CATERPILLAR' THEN 'LARVA'
        WHEN second_language_proficiency = 'CHRYSALIS' THEN 'PUPA'
        WHEN second_language_proficiency = 'BUTTERFLY' THEN 'BEE'
        ELSE second_language_proficiency END,
    first_language_listening = CASE
        WHEN first_language_listening = 'CATERPILLAR' THEN 'LARVA'
        WHEN first_language_listening = 'CHRYSALIS' THEN 'PUPA'
        WHEN first_language_listening = 'BUTTERFLY' THEN 'BEE'
        ELSE first_language_listening END,
    first_language_speaking = CASE
        WHEN first_language_speaking = 'CATERPILLAR' THEN 'LARVA'
        WHEN first_language_speaking = 'CHRYSALIS' THEN 'PUPA'
        WHEN first_language_speaking = 'BUTTERFLY' THEN 'BEE'
        ELSE first_language_speaking END,
    second_language_listening = CASE
        WHEN second_language_listening = 'CATERPILLAR' THEN 'LARVA'
        WHEN second_language_listening = 'CHRYSALIS' THEN 'PUPA'
        WHEN second_language_listening = 'BUTTERFLY' THEN 'BEE'
        ELSE second_language_listening END,
    second_language_speaking = CASE
        WHEN second_language_speaking = 'CATERPILLAR' THEN 'LARVA'
        WHEN second_language_speaking = 'CHRYSALIS' THEN 'PUPA'
        WHEN second_language_speaking = 'BUTTERFLY' THEN 'BEE'
        ELSE second_language_speaking END,
    story_preference = CASE
        WHEN story_preference = 'WARM' THEN 'WARM_HUG'
        WHEN story_preference = 'ADVENTURE' THEN 'FUN_ADVENTURE'
        WHEN story_preference = 'DAILY' THEN 'DAILY_LIFE'
        ELSE story_preference END;