-- Criação da tabela de usuários
CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    data_criacao TIMESTAMP NOT NULL
);

-- Criação da tabela de projetos
CREATE TABLE projetos (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT,
    data_criacao TIMESTAMP NOT NULL
);

-- Criação da tabela de bugs
CREATE TABLE bugs (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    descricao TEXT,
    prioridade VARCHAR(20) NOT NULL CHECK (prioridade IN ('BAIXA', 'MEDIA', 'ALTA', 'CRITICA')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('ABERTO', 'EM_ANDAMENTO', 'RESOLVIDO', 'FECHADO', 'REABERTO')),
    projeto_id BIGINT NOT NULL,
    usuario_responsavel_id BIGINT,
    data_criacao TIMESTAMP NOT NULL,
    data_atualizacao TIMESTAMP,
    CONSTRAINT fk_bug_projeto FOREIGN KEY (projeto_id) REFERENCES projetos(id) ON DELETE CASCADE,
    CONSTRAINT fk_bug_usuario FOREIGN KEY (usuario_responsavel_id) REFERENCES usuarios(id) ON DELETE SET NULL
);

-- Criação da tabela de comentários
CREATE TABLE comentarios (
    id BIGSERIAL PRIMARY KEY,
    texto TEXT NOT NULL,
    bug_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    data_comentario TIMESTAMP NOT NULL,
    CONSTRAINT fk_comentario_bug FOREIGN KEY (bug_id) REFERENCES bugs(id) ON DELETE CASCADE,
    CONSTRAINT fk_comentario_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- Índices para melhorar performance de queries
CREATE INDEX idx_bugs_projeto_id ON bugs(projeto_id);
CREATE INDEX idx_bugs_usuario_responsavel_id ON bugs(usuario_responsavel_id);
CREATE INDEX idx_bugs_status ON bugs(status);
CREATE INDEX idx_bugs_prioridade ON bugs(prioridade);
CREATE INDEX idx_comentarios_bug_id ON comentarios(bug_id);
CREATE INDEX idx_comentarios_usuario_id ON comentarios(usuario_id);
CREATE INDEX idx_usuarios_email ON usuarios(email);
