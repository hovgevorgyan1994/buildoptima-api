create table users
(
    id UUID primary key not null,
    first_name varchar(255) not null,
    last_name varchar(255) not null,
    phone varchar(255) unique,
    email varchar(255) unique not null,
    password varchar(255) not null,
    creation_date timestamp not null,
    update_date timestamp not null,
    active boolean not null
);