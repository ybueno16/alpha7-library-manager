CREATE TABLE livro (
    id               BIGSERIAL    PRIMARY KEY,
    titulo           VARCHAR(512) NOT NULL,
    isbn             VARCHAR(20)  NOT NULL UNIQUE,
    data_publicacao  DATE,
    numero_paginas   INTEGER,
    idioma           VARCHAR(50),
    editora_id       BIGINT       REFERENCES editora(id) ON DELETE SET NULL
);

CREATE INDEX idx_livro_titulo_lower ON livro (LOWER(titulo));
CREATE INDEX idx_livro_idioma_lower  ON livro (LOWER(idioma));
CREATE INDEX idx_livro_editora_id    ON livro (editora_id);
