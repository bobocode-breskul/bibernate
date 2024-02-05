create table if not exists persons(
    id bigint auto_increment,
    first_name varchar(256),
    last_name varchar(256)
);

insert into persons(first_name, last_name) values ( 'John', 'Doe' )