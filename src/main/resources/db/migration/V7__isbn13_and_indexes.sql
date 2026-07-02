-- V7: Convert existing ISBN-10 records to ISBN-13 canonical form,
--     replace the broad UNIQUE constraint with a partial index (active books only),
--     and add an index for ORDER BY titulo.

-- Step 1: Convert ISBN-10 values (length = 10) to ISBN-13 in place.
-- Formula: prefix with "978", take first 9 digits of ISBN-10, compute EAN-13 check digit.
DO $$
DECLARE
    rec         RECORD;
    base13      TEXT;
    total       INT;
    i           INT;
    check_digit INT;
BEGIN
    FOR rec IN SELECT id, isbn FROM livro WHERE LENGTH(isbn) = 10 LOOP
        base13 := '978' || SUBSTRING(rec.isbn, 1, 9);
        total  := 0;
        FOR i IN 1..12 LOOP
            IF i % 2 = 1 THEN
                total := total + CAST(SUBSTRING(base13, i, 1) AS INT);
            ELSE
                total := total + 3 * CAST(SUBSTRING(base13, i, 1) AS INT);
            END IF;
        END LOOP;
        check_digit := (10 - (total % 10)) % 10;
        UPDATE livro SET isbn = base13 || CAST(check_digit AS TEXT) WHERE id = rec.id;
    END LOOP;
END $$;

-- Step 2: Drop the old broad UNIQUE constraint that blocked soft-deleted duplicates.
ALTER TABLE livro DROP CONSTRAINT IF EXISTS livro_isbn_key;

-- Step 3: Partial UNIQUE index — only active (non-deleted) books must have unique ISBNs.
CREATE UNIQUE INDEX IF NOT EXISTS uq_livro_isbn_ativo
    ON livro (isbn)
    WHERE deleted_at IS NULL;

-- Step 4: Index for ORDER BY titulo (used by findAll and findByFiltro).
CREATE INDEX IF NOT EXISTS idx_livro_titulo
    ON livro (titulo)
    WHERE deleted_at IS NULL;
