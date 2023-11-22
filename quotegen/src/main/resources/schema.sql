CREATE TABLE IF NOT EXISTS book
(
    id         BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    title      varchar(255) NOT NULL,
    author     varchar(255) NOT NULL,
    format     varchar(20)  NOT NULL,
    file_path  varchar      NOT NULL,
    bookStatus varchar(20)  NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE IF NOT EXISTS quote
(
    id           BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    content      TEXT        NOT NULL,
    status       varchar(20) NOT NULL,
    pending_time TIMESTAMP,
    image_url    varchar(255),
    used_at      TIMESTAMP,
    book_id      BIGINT REFERENCES book (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS users
(
    id        BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    usertg_id BIGINT UNIQUE,
    username  varchar(100) UNIQUE NOT NULL,
    password  varchar(100)        NOT NULL,
    role      varchar(100)        NOT NULL
);

CREATE TABLE IF NOT EXISTS greeting
(
    id      BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    message varchar NOT NULL
);

CREATE TABLE IF NOT EXISTS scheduled_status
(
    id      BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    last_executed TIMESTAMP
);