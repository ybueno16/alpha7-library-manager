CREATE TABLE livro (
    id               BIGSERIAL    PRIMARY KEY,
    titulo           VARCHAR(512) NOT NULL,
    isbn             VARCHAR(20)  NOT NULL UNIQUE,
    data_publicacao  DATE,
    numero_paginas   INTEGER,
    idioma           VARCHAR(50),
    editora_id       BIGINT       REFERENCES editora(id) ON DELETE SET NULL
);
