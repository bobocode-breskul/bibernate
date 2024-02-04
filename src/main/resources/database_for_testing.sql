CREATE TABLE IF NOT EXISTS persons
(
    id         bigserial PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name  VARCHAR(255) NOT NULL,
    age        integer default 0
);

CREATE TABLE IF NOT EXISTS photo
(
    id          serial PRIMARY KEY,
    url         text,
    description text
);

CREATE TABLE IF NOT EXISTS photo_comment
(
    id       serial PRIMARY KEY,
    text     text,
    photo_id integer,
    constraint photo_comment_FK FOREIGN KEY (photo_id) references photo (id)
);

INSERT INTO persons (id, first_name, last_name, age) values (1, 'Taras', 'Shevchenko', 47);