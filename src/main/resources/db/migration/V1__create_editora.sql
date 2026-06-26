CREATE TABLE editora (
    id   BIGSERIAL    PRIMARY KEY,
    nome VARCHAR(255) NOT NULL
);

CREATE INDEX idx_editora_nome ON editora (nome);
