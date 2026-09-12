CREATE TABLE IF NOT EXISTS books (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    isbn VARCHAR(20) NOT NULL UNIQUE CHECK (btrim(isbn) <> ''),
    title VARCHAR(200) NOT NULL CHECK (btrim(title) <> ''),
    author VARCHAR(120) NOT NULL CHECK (btrim(author) <> ''),
    price NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
    stock INTEGER NOT NULL CHECK (stock >= 0)
);