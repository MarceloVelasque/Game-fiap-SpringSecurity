
-- Migration SQL para criar a tabela de jogos
-- Esta migração cria a tabela "tb_jogo" com os seguintes campos:
-- - id: Identificador único do jogo (chave primária, auto-incremento)
-- - titulo: Título do jogo (obrigatório)
-- - descricao: Descrição do jogo (opcional)
-- - preco: Preço do jogo (obrigatório)

CREATE TABLE tb_jogo (
    id BIGINT NOT NULL AUTO_INCREMENT,
    titulo VARCHAR(255) NOT NULL,
    descricao TEXT,
    preco DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (id)
);
