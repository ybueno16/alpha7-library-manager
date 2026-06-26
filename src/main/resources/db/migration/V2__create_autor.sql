CREATE TABLE autor (
    id               BIGSERIAL    PRIMARY KEY,
    nome             VARCHAR(255) NOT NULL,
    data_nascimento  DATE,
    data_falecimento DATE,
    bio              TEXT
);

CREATE INDEX idx_autor_nome ON autor (nome);
