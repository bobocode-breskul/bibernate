create table if not exists persons
(
    id         bigint auto_increment primary key,
    first_name varchar(256),
    last_name  varchar(256)
);

create table if not exists account
(
    id bigint auto_increment primary key,
    nick varchar(256),
    person_id bigint references persons
);

create table if not exists notes
(
    id        bigint auto_increment primary key,
    title     varchar(256),
    body      varchar(256),
    person_id bigint references persons
)