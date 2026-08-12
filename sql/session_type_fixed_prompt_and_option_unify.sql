ALTER TABLE ai_session_type_config
    ADD COLUMN fixed_prompt TEXT COMMENT '固定提示词' AFTER default_deduct_type_code;

DELETE t1
FROM ai_session_type_option t1
INNER JOIN ai_session_type_option t2
    ON t1.session_type_id = t2.session_type_id
   AND t1.option_id = t2.option_id
   AND t1.id > t2.id;

ALTER TABLE ai_session_type_option
    DROP COLUMN model_name;

DELETE t1
FROM ai_image_option_config t1
INNER JOIN ai_image_option_config t2
    ON t1.option_key = t2.option_key
   AND t1.id > t2.id;

ALTER TABLE ai_image_option_config
    DROP COLUMN model_name;
