-- AI模特管理表
DROP TABLE IF EXISTS gh_ai_model;
CREATE TABLE gh_ai_model (
  id              BIGINT(20)      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  name            VARCHAR(100)    NOT NULL                COMMENT '模特名称',
  gender          VARCHAR(20)     DEFAULT NULL            COMMENT '性别: male-男, female-女, neutral-中性',
  age_group       VARCHAR(20)     DEFAULT NULL            COMMENT '年龄段: young-青年, middle-中年, senior-老年',
  ethnicity       VARCHAR(50)     DEFAULT NULL            COMMENT '种族/肤色',
  hair_style      VARCHAR(100)    DEFAULT NULL            COMMENT '发型',
  pose            VARCHAR(100)    DEFAULT NULL            COMMENT '姿势',
  clothing        VARCHAR(200)    DEFAULT NULL            COMMENT '着装',
  scene           VARCHAR(200)    DEFAULT NULL            COMMENT '场景',
  preview_url     VARCHAR(500)    DEFAULT NULL            COMMENT '预览图URL',
  status          CHAR(1)         DEFAULT '1'             COMMENT '状态: 0-已发布, 1-草稿',
  commercial_auth CHAR(1)         DEFAULT '1'             COMMENT '商用授权: 0-已授权, 1-未授权',
  sort            INT(11)         DEFAULT 0               COMMENT '排序',
  create_by       VARCHAR(64)     DEFAULT ''              COMMENT '创建者',
  create_time     DATETIME                                COMMENT '创建时间',
  update_by       VARCHAR(64)     DEFAULT ''              COMMENT '更新者',
  update_time     DATETIME                                COMMENT '更新时间',
  remark          VARCHAR(500)    DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='AI模特管理表';