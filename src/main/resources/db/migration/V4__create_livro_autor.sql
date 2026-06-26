CREATE TABLE livro_autor (
    livro_id BIGINT NOT NULL REFERENCES livro(id) ON DELETE CASCADE,
    autor_id BIGINT NOT NULL REFERENCES autor(id) ON DELETE CASCADE,
    PRIMARY KEY (livro_id, autor_id)
);

-- livro_id já coberto pela PK; índice reverso para lookup autor -> livros
CREATE INDEX idx_livro_autor_autor_id ON livro_autor (autor_id);
