CREATE TABLE IF NOT EXISTS news (
    id UUID PRIMARY KEY NOT NULL,
    title VARCHAR(255) NOT NULL,
    text VARCHAR(200000) NOT NULL,
    username VARCHAR(50) NOT NULL,
    creation_time TIMESTAMP NOT NULL,
    update_time TIMESTAMP NOT NULL,

    author_id UUID NOT NULL
);

