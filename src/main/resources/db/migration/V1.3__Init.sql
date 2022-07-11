CREATE TABLE IF NOT EXISTS faq_category
(
    id            UUID primary key NOT NULL,
    name          VARCHAR          NOT NULL,
    updated_by_id UUID             NOT NULL REFERENCES users (id),
    creation_date TIMESTAMP        NOT NULL,
    update_date   TIMESTAMP        NOT NULL
);

CREATE TABLE IF NOT EXISTS faq_question
(
    id            UUID primary key NOT NULL,
    question      TEXT             NOT NULL,
    answer        TEXT             NOT NULL,
    status        VARCHAR          NOT NULL,
    category_id   UUID             NOT NULL REFERENCES faq_category (id),
    updated_by_id UUID             NOT NULL REFERENCES users (id),
    creation_date TIMESTAMP        NOT NULL,
    update_date   TIMESTAMP        NOT NULL
);