
DROP INDEX idx_books_author;

CREATE INDEX idx_books_author_id 
ON books (author, id);
