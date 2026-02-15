
-- V2__create_table_usuario.sql
-- Criação da tabela de usuários
-- Esta migração cria a tabela "tb_usuario" com os seguintes campos:
-- - id: Identificador único do usuário (chave primária, auto-incremento)
-- - nome: Nome do usuário (obrigatório)
-- - email: Email do usuário (obrigatório, único)
-- - senha: Senha do usuário (obrigatório)
-- - papel: Papel do usuário (obrigatório, ex: "ADMIN", "USER")
CREATE TABLE tb_usuario (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    papel VARCHAR(50) NOT NULL,
    PRIMARY KEY (id)
);
