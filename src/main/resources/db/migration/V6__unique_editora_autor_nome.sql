CREATE UNIQUE INDEX uq_autor_nome_ativo
    ON autor (LOWER(TRIM(nome)))
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uq_editora_nome_ativo
    ON editora (LOWER(TRIM(nome)))
    WHERE deleted_at IS NULL;