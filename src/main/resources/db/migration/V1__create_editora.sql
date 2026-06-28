CREATE TABLE editora (
    id         BIGSERIAL    PRIMARY KEY,
    nome       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);

CREATE INDEX idx_editora_nome ON editora (nome);
