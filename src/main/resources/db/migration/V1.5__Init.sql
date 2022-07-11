ALTER TABLE faq_category
    RENAME COLUMN update_date TO updated_at;
ALTER TABLE faq_category
    RENAME COLUMN creation_date TO created_at;

ALTER TABLE faq_question
    RENAME COLUMN update_date TO updated_at;
ALTER TABLE faq_question
    RENAME COLUMN creation_date TO created_at;