CREATE TABLE autor (
    id               BIGSERIAL    PRIMARY KEY,
    nome             VARCHAR(255) NOT NULL,
    data_nascimento  DATE,
    data_falecimento DATE,
    bio              TEXT,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted_at       TIMESTAMP
);

CREATE INDEX idx_autor_nome ON autor (nome);
