CREATE TABLE IF NOT EXISTS books (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(160) NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    stock INTEGER NOT NULL,
    last_imported_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT books_isbn_not_blank CHECK (btrim(isbn) <> ''),
    CONSTRAINT books_title_not_blank CHECK (btrim(title) <> ''),
    CONSTRAINT books_author_not_blank CHECK (btrim(author) <> ''),
    CONSTRAINT books_price_nonnegative CHECK (price >= 0),
    CONSTRAINT books_stock_nonnegative CHECK (stock >= 0)
);