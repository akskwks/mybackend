CREATE TABLE IF NOT EXISTS app_calendar (
    event_id BIGINT NOT NULL AUTO_INCREMENT,
    event_title VARCHAR(255) NULL,
    event_date DATE NULL,
    start_time TIME NULL,
    end_time TIME NULL,
    event_catg VARCHAR(255) NULL,
    color VARCHAR(255) NULL,
    event_dsc VARCHAR(2000) NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS app_memo (
    memo_id BIGINT NOT NULL AUTO_INCREMENT,
    memo_title VARCHAR(40) NOT NULL,
    memo_sort VARCHAR(255) NOT NULL,
    memo_cnnt VARCHAR(10000) NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (memo_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS app_ai_conversation (
    conversation_id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(120) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (conversation_id),
    INDEX idx_ai_conversation_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS app_ai_message (
    message_id BIGINT NOT NULL AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    content LONGTEXT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (message_id),
    INDEX idx_ai_message_conversation (conversation_id, created_at),
    CONSTRAINT fk_ai_message_conversation
        FOREIGN KEY (conversation_id)
        REFERENCES app_ai_conversation (conversation_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS app_project (
    project_id BIGINT NOT NULL AUTO_INCREMENT,
    work_environment VARCHAR(20) NOT NULL,
    project_name VARCHAR(100) NOT NULL,
    project_status VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (project_id),
    INDEX idx_work_project_environment (work_environment),
    INDEX idx_work_project_status (project_status)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS app_work (
    work_id BIGINT NOT NULL AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    work_date DATE NOT NULL,
    work_title VARCHAR(100) NOT NULL,
    work_cnnt VARCHAR(10000) NULL,
    work_status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (work_id),
    INDEX idx_app_work_project_id (project_id),
    INDEX idx_app_work_date (work_date),
    INDEX idx_app_work_status (work_status),
    CONSTRAINT fk_app_work_project
        FOREIGN KEY (project_id)
        REFERENCES app_project (project_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS app_work_file (
    work_file_id BIGINT NOT NULL AUTO_INCREMENT,
    work_id BIGINT NOT NULL,
    orgn_file_name VARCHAR(255) NOT NULL,
    sv_file_name VARCHAR(100) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_extension VARCHAR(30) NOT NULL,
    mime_type VARCHAR(150) NOT NULL,
    file_size BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (work_file_id),
    INDEX idx_app_work_file_work_id (work_id),
    CONSTRAINT fk_app_work_file_work
        FOREIGN KEY (work_id)
        REFERENCES app_work (work_id)
        ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;
