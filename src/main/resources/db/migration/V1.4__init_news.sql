create table news
(
    id           uuid primary key,
    title        varchar(255) not null unique,
    summary      text         not null unique,
    published_at timestamp    not null,
    modified_by  varchar(255) not null
);

create table news_keywords
(
    news_id uuid        not null references news (id),
    keyword varchar(50) not null unique
);

create index news_keywords_index on news_keywords (keyword);