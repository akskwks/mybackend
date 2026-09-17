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
