drop table if exists persons cascade;
create table if not exists persons
(
    id         bigint  primary key auto_increment,
    first_name varchar(256),
    last_name  varchar(256)
);

drop table if exists notes;
create table if not exists notes
(
    id        bigint primary key auto_increment,
    title     varchar(256),
    body      varchar(256),
    person_id bigint references persons (id)
);

insert into persons(id, first_name, last_name)
values (1, 'John', 'Doe'),
       (2, 'Robert', 'Martin'),
       (3, 'Martin', 'Fowler'),
       (4, 'Vlad', 'Mihalcea');

insert into notes (id, title, body, person_id)
values (1, 'John Doe first note', 'hello', 1),
       (2, 'John Doe second note', 'hello', 1),
       (3, 'Robert Martin first note', 'hello', 2),
       (4, 'Robert Martin second note', 'hello', 2),
       (5, 'Robert Martin third note', 'hello', 2),
       (6, 'Martin Fowler first note', 'hello', 3),
       (7, 'Martin Fowler second note', 'hello', 3),
       (8, 'Vlad Mihalcea first note', 'hello', 4)