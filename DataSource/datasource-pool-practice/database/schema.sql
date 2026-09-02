CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    owner_name VARCHAR(100) NOT NULL
        CHECK (btrim(owner_name) <> ''),
    balance NUMERIC(12, 2) NOT NULL
        CHECK (balance >= 0)
);