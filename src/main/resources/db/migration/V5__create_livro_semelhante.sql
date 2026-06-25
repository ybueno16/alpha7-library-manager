CREATE TABLE livro_semelhante (
    livro_id      BIGINT NOT NULL REFERENCES livro(id) ON DELETE CASCADE,
    semelhante_id BIGINT NOT NULL REFERENCES livro(id) ON DELETE CASCADE,
    PRIMARY KEY (livro_id, semelhante_id),
    CHECK (livro_id <> semelhante_id)
);
