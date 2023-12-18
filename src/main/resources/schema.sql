drop table if exists users;

create table if not exists users (
    user_id int not null auto_increment primary key,
    username varchar(20) not null,
    password varchar(500) not null
);