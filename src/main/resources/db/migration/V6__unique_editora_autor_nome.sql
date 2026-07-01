ALTER TABLE editora ADD CONSTRAINT uq_editora_nome UNIQUE (nome);
ALTER TABLE autor   ADD CONSTRAINT uq_autor_nome   UNIQUE (nome);