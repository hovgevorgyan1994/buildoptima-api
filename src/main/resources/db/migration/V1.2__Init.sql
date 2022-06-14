create table confirmation_token
(
    id         UUID primary key not null,
    token      varchar(255)     not null,
    expires_at timestamp        not null,
    user_id    UUID             not null references buildoptima.buildoptima.users(id)
);