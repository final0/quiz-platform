-- ============================================================
-- 在线答题系统 - 数据库表结构设计（MySQL 8.0+）
-- 定位：企业内部轻量使用，按部门维度隔离数据，不引入多租户中间件
-- 字符集统一 utf8mb4，避免中文题库出现生僻字/符号乱码
-- ============================================================


CREATE TABLE sys_dept (
    id           BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(64)  NOT NULL COMMENT '部门/组织名称',
    parent_id    BIGINT UNSIGNED DEFAULT NULL COMMENT '上级部门id，顶级为NULL',
    status       TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门/组织';

CREATE TABLE sys_user (
    id           BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    username     VARCHAR(64)  NOT NULL COMMENT '登录账号',
    password     VARCHAR(128) NOT NULL COMMENT 'BCrypt加密后的密码',
    real_name    VARCHAR(64)  NOT NULL COMMENT '姓名',
    dept_id      BIGINT UNSIGNED NOT NULL COMMENT '所属部门，数据隔离维度',
    role         VARCHAR(20)  NOT NULL DEFAULT 'STUDENT'
                 COMMENT 'ADMIN超管 / DEPT_ADMIN部门管理员 / AUTHOR出题人 / STUDENT考生',
    status       TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username),
    KEY idx_dept (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户账号';


CREATE TABLE question_bank (
    id             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(128) NOT NULL COMMENT '题库名称',
    subject        VARCHAR(64)  DEFAULT NULL COMMENT '学科/科目分类，用于筛选',
    dept_id        BIGINT UNSIGNED NOT NULL COMMENT '所属部门',
    source_type    VARCHAR(20)  NOT NULL DEFAULT 'MANUAL' COMMENT 'MANUAL手工创建 / IMPORT文件导入',
    single_count   INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '单选题数量',
    multiple_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '多选题数量',
    judge_count    INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '判断题数量',
    short_count    INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '简答题数量',
    creator_id     BIGINT UNSIGNED NOT NULL,
    status         TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    delete_flag    TINYINT      NOT NULL DEFAULT 0 COMMENT '0正常 1逻辑删除',
    KEY idx_dept (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题库';

CREATE TABLE question (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    bank_id         BIGINT UNSIGNED NOT NULL COMMENT '所属题库',
    type            VARCHAR(20)  NOT NULL COMMENT 'SINGLE单选 / MULTIPLE多选 / JUDGE判断 / SHORT_ANSWER简答',
    stem            TEXT         NOT NULL COMMENT '题干',
    options         JSON         DEFAULT NULL COMMENT '选择题选项',
    answer          VARCHAR(255) DEFAULT NULL COMMENT '答案',
    analysis        TEXT         DEFAULT NULL COMMENT '答案解析',
    difficulty      TINYINT      NOT NULL DEFAULT 2 COMMENT '1易 2中 3难',
    tags            VARCHAR(255) DEFAULT NULL COMMENT '知识点标签',

    source_num      VARCHAR(20)  DEFAULT NULL COMMENT '原文档中的题号',
    parse_confidence VARCHAR(10) NOT NULL DEFAULT 'HIGH' COMMENT 'HIGH/LOW',
    review_status   VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',

    creator_id      BIGINT UNSIGNED NOT NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    delete_flag     TINYINT      NOT NULL DEFAULT 0 COMMENT '0正常 1逻辑删除',
    KEY idx_bank (bank_id),
    KEY idx_bank_type_status (bank_id, type, review_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目';


CREATE TABLE import_task (
    id             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    bank_id        BIGINT UNSIGNED NOT NULL,
    file_name      VARCHAR(255) NOT NULL,
    minio_path     VARCHAR(255) NOT NULL,
    file_type      VARCHAR(10)  NOT NULL,
    parse_mode     VARCHAR(10)  NOT NULL DEFAULT 'RULE',
    status         VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    total_count    INT UNSIGNED NOT NULL DEFAULT 0,
    high_conf_count INT UNSIGNED NOT NULL DEFAULT 0,
    low_conf_count INT UNSIGNED NOT NULL DEFAULT 0,
    anomaly_count  INT UNSIGNED NOT NULL DEFAULT 0,
    error_message  VARCHAR(500) DEFAULT NULL,
    creator_id     BIGINT UNSIGNED NOT NULL,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finished_at    DATETIME     DEFAULT NULL,
    KEY idx_bank (bank_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题库文件导入任务';

CREATE TABLE import_anomaly (
    id           BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    task_id      BIGINT UNSIGNED NOT NULL,
    raw_text     TEXT         NOT NULL,
    resolved     TINYINT      NOT NULL DEFAULT 0,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_task (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='解析异常段落';


CREATE TABLE exam_paper (
    id               BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(128) NOT NULL,
    bank_id          BIGINT UNSIGNED NOT NULL,
    dept_id          BIGINT UNSIGNED NOT NULL,
    mode             VARCHAR(10)  NOT NULL DEFAULT 'EXAM',
    compose_strategy VARCHAR(10)  NOT NULL DEFAULT 'RANDOM',

    single_count     INT UNSIGNED NOT NULL DEFAULT 0,
    single_score     DECIMAL(5,2) NOT NULL DEFAULT 0,
    multiple_count   INT UNSIGNED NOT NULL DEFAULT 0,
    multiple_score   DECIMAL(5,2) NOT NULL DEFAULT 0,
    judge_count      INT UNSIGNED NOT NULL DEFAULT 0,
    judge_score      DECIMAL(5,2) NOT NULL DEFAULT 0,

    duration_minutes INT UNSIGNED NOT NULL DEFAULT 45,
    pass_score       DECIMAL(6,2) NOT NULL DEFAULT 60,
    total_score      DECIMAL(6,2) NOT NULL DEFAULT 100,

    status           TINYINT      NOT NULL DEFAULT 1,
    creator_id       BIGINT UNSIGNED NOT NULL,
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_dept (dept_id),
    KEY idx_bank (bank_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷/考试配置';

CREATE TABLE exam_paper_question (
    id           BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    paper_id     BIGINT UNSIGNED NOT NULL,
    question_id  BIGINT UNSIGNED NOT NULL,
    score        DECIMAL(5,2) NOT NULL,
    sort_order   INT UNSIGNED NOT NULL DEFAULT 0,
    UNIQUE KEY uk_paper_question (paper_id, question_id),
    KEY idx_paper (paper_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='固定试卷的题目组成';


CREATE TABLE exam_record (
    id             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    paper_id       BIGINT UNSIGNED NOT NULL,
    user_id        BIGINT UNSIGNED NOT NULL,
    mode           VARCHAR(10)  NOT NULL,
    question_snapshot JSON      NOT NULL COMMENT '抽题快照',

    start_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    submit_time    DATETIME     DEFAULT NULL,
    status         VARCHAR(20)  NOT NULL DEFAULT 'IN_PROGRESS',
    score          DECIMAL(6,2) DEFAULT NULL,
    pass_flag      TINYINT      DEFAULT NULL,

    KEY idx_user_paper (user_id, paper_id),
    KEY idx_paper (paper_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试/练习记录';

CREATE TABLE exam_answer_detail (
    id             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    record_id      BIGINT UNSIGNED NOT NULL,
    question_id    BIGINT UNSIGNED NOT NULL,
    question_type  VARCHAR(20)  NOT NULL,
    user_answer    VARCHAR(500) DEFAULT NULL,
    correct_answer VARCHAR(500) DEFAULT NULL,
    is_correct     TINYINT      DEFAULT NULL,
    score          DECIMAL(5,2) DEFAULT NULL,
    answered_at    DATETIME     DEFAULT NULL,
    UNIQUE KEY uk_record_question (record_id, question_id),
    KEY idx_question (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试作答明细';


CREATE TABLE wrong_question_book (
    id             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT UNSIGNED NOT NULL,
    question_id    BIGINT UNSIGNED NOT NULL,
    wrong_count    INT UNSIGNED NOT NULL DEFAULT 1,
    last_wrong_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    mastered_flag  TINYINT      NOT NULL DEFAULT 0,
    UNIQUE KEY uk_user_question (user_id, question_id),
    KEY idx_user_mastered (user_id, mastered_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='错题本';
