-- Normaliza ISBN-10 legado para ISBN-13 e troca a unicidade global
-- por unicidade apenas entre livros ativos.

ALTER TABLE livro DROP CONSTRAINT IF EXISTS livro_isbn_key;

DO $$
DECLARE
    rec         RECORD;
    base13      TEXT;
    total       INT;
    i           INT;
    check_digit INT;
BEGIN
    FOR rec IN
        SELECT id, isbn
          FROM livro
         WHERE isbn ~ '^[0-9]{9}[0-9Xx]$'
    LOOP
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

CREATE UNIQUE INDEX IF NOT EXISTS uq_livro_isbn_ativo
    ON livro (isbn)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_livro_titulo
    ON livro (titulo)
    WHERE deleted_at IS NULL;
