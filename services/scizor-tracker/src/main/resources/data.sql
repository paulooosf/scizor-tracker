-- Inserção de usuários de exemplo
INSERT INTO usuarios (nome, email, senha, data_criacao) VALUES
('João Silva', 'joao.silva@example.com', '$2a$10$XYZ...', NOW()),
('Maria Santos', 'maria.santos@example.com', '$2a$10$ABC...', NOW()),
('Pedro Costa', 'pedro.costa@example.com', '$2a$10$DEF...', NOW())
ON CONFLICT (email) DO NOTHING;

-- Inserção de projetos de exemplo
INSERT INTO projetos (nome, descricao, data_criacao) VALUES
('Sistema E-commerce', 'Plataforma de vendas online com carrinho e pagamento integrado', NOW()),
('API de Integração', 'API REST para integração com sistemas externos', NOW()),
('Dashboard Analytics', 'Painel de visualização de métricas e relatórios', NOW())
ON CONFLICT DO NOTHING;

-- Inserção de bugs de exemplo
INSERT INTO bugs (titulo, descricao, prioridade, status, projeto_id, usuario_responsavel_id, data_criacao, data_atualizacao)
SELECT 'Erro ao processar pagamento com cartão', 'Ao finalizar compra com cartão de crédito, sistema retorna erro 500', 'CRITICA', 'ABERTO', p.id, u.id, NOW(), NOW()
FROM projetos p, usuarios u
WHERE p.nome = 'Sistema E-commerce' AND u.email = 'joao.silva@example.com'
AND NOT EXISTS (SELECT 1 FROM bugs WHERE titulo = 'Erro ao processar pagamento com cartão');

INSERT INTO bugs (titulo, descricao, prioridade, status, projeto_id, usuario_responsavel_id, data_criacao, data_atualizacao)
SELECT 'Layout quebrado em dispositivos móveis', 'A página de checkout não renderiza corretamente em telas menores que 768px', 'ALTA', 'EM_ANDAMENTO', p.id, u.id, NOW(), NOW()
FROM projetos p, usuarios u
WHERE p.nome = 'Sistema E-commerce' AND u.email = 'maria.santos@example.com'
AND NOT EXISTS (SELECT 1 FROM bugs WHERE titulo = 'Layout quebrado em dispositivos móveis');

INSERT INTO bugs (titulo, descricao, prioridade, status, projeto_id, usuario_responsavel_id, data_criacao, data_atualizacao)
SELECT 'Carrinho de compras não persiste itens', 'Ao sair e retornar ao site, os itens do carrinho desaparecem', 'MEDIA', 'ABERTO', p.id, NULL, NOW(), NOW()
FROM projetos p
WHERE p.nome = 'Sistema E-commerce'
AND NOT EXISTS (SELECT 1 FROM bugs WHERE titulo = 'Carrinho de compras não persiste itens');

INSERT INTO bugs (titulo, descricao, prioridade, status, projeto_id, usuario_responsavel_id, data_criacao, data_atualizacao)
SELECT 'Endpoint de autenticação lento', 'Endpoint /api/auth/login demora mais de 3 segundos para responder', 'ALTA', 'ABERTO', p.id, u.id, NOW(), NOW()
FROM projetos p, usuarios u
WHERE p.nome = 'API de Integração' AND u.email = 'pedro.costa@example.com'
AND NOT EXISTS (SELECT 1 FROM bugs WHERE titulo = 'Endpoint de autenticação lento');

INSERT INTO bugs (titulo, descricao, prioridade, status, projeto_id, usuario_responsavel_id, data_criacao, data_atualizacao)
SELECT 'Documentação Swagger desatualizada', 'Novos endpoints não aparecem na documentação OpenAPI', 'BAIXA', 'RESOLVIDO', p.id, u.id, NOW(), NOW()
FROM projetos p, usuarios u
WHERE p.nome = 'API de Integração' AND u.email = 'maria.santos@example.com'
AND NOT EXISTS (SELECT 1 FROM bugs WHERE titulo = 'Documentação Swagger desatualizada');

INSERT INTO bugs (titulo, descricao, prioridade, status, projeto_id, usuario_responsavel_id, data_criacao, data_atualizacao)
SELECT 'Gráfico de conversão não carrega', 'Dashboard exibe erro ao tentar carregar gráfico de funil de conversão', 'MEDIA', 'EM_ANDAMENTO', p.id, u.id, NOW(), NOW()
FROM projetos p, usuarios u
WHERE p.nome = 'Dashboard Analytics' AND u.email = 'joao.silva@example.com'
AND NOT EXISTS (SELECT 1 FROM bugs WHERE titulo = 'Gráfico de conversão não carrega');

-- Inserção de comentários de exemplo
INSERT INTO comentarios (texto, bug_id, usuario_id, data_comentario)
SELECT 'Identifiquei que o problema ocorre apenas com cartões da bandeira Visa', b.id, u.id, NOW()
FROM bugs b, usuarios u
WHERE b.titulo = 'Erro ao processar pagamento com cartão' AND u.email = 'joao.silva@example.com'
AND NOT EXISTS (SELECT 1 FROM comentarios c WHERE c.bug_id = b.id AND c.texto = 'Identifiquei que o problema ocorre apenas com cartões da bandeira Visa');

INSERT INTO comentarios (texto, bug_id, usuario_id, data_comentario)
SELECT 'Logs indicam timeout na comunicação com gateway de pagamento', b.id, u.id, NOW()
FROM bugs b, usuarios u
WHERE b.titulo = 'Erro ao processar pagamento com cartão' AND u.email = 'maria.santos@example.com'
AND NOT EXISTS (SELECT 1 FROM comentarios c WHERE c.bug_id = b.id AND c.texto = 'Logs indicam timeout na comunicação com gateway de pagamento');

INSERT INTO comentarios (texto, bug_id, usuario_id, data_comentario)
SELECT 'Consegui reproduzir o bug em iPhone 12 e Samsung Galaxy S21', b.id, u.id, NOW()
FROM bugs b, usuarios u
WHERE b.titulo = 'Layout quebrado em dispositivos móveis' AND u.email = 'maria.santos@example.com'
AND NOT EXISTS (SELECT 1 FROM comentarios c WHERE c.bug_id = b.id AND c.texto = 'Consegui reproduzir o bug em iPhone 12 e Samsung Galaxy S21');

INSERT INTO comentarios (texto, bug_id, usuario_id, data_comentario)
SELECT 'Já corrigi o CSS responsivo, aguardando review do PR', b.id, u.id, NOW() + INTERVAL '1 hour'
FROM bugs b, usuarios u
WHERE b.titulo = 'Layout quebrado em dispositivos móveis' AND u.email = 'maria.santos@example.com'
AND NOT EXISTS (SELECT 1 FROM comentarios c WHERE c.bug_id = b.id AND c.texto = 'Já corrigi o CSS responsivo, aguardando review do PR');

INSERT INTO comentarios (texto, bug_id, usuario_id, data_comentario)
SELECT 'Verifiquei que estamos usando cache Redis, pode ser problema de expiração', b.id, u.id, NOW()
FROM bugs b, usuarios u
WHERE b.titulo = 'Carrinho de compras não persiste itens' AND u.email = 'pedro.costa@example.com'
AND NOT EXISTS (SELECT 1 FROM comentarios c WHERE c.bug_id = b.id AND c.texto = 'Verifiquei que estamos usando cache Redis, pode ser problema de expiração');

INSERT INTO comentarios (texto, bug_id, usuario_id, data_comentario)
SELECT 'Adicionei índice na tabela de autenticação, melhorou para 800ms', b.id, u.id, NOW()
FROM bugs b, usuarios u
WHERE b.titulo = 'Endpoint de autenticação lento' AND u.email = 'pedro.costa@example.com'
AND NOT EXISTS (SELECT 1 FROM comentarios c WHERE c.bug_id = b.id AND c.texto = 'Adicionei índice na tabela de autenticação, melhorou para 800ms');

INSERT INTO comentarios (texto, bug_id, usuario_id, data_comentario)
SELECT 'Swagger atualizado e republicado. Bug resolvido!', b.id, u.id, NOW()
FROM bugs b, usuarios u
WHERE b.titulo = 'Documentação Swagger desatualizada' AND u.email = 'maria.santos@example.com'
AND NOT EXISTS (SELECT 1 FROM comentarios c WHERE c.bug_id = b.id AND c.texto = 'Swagger atualizado e republicado. Bug resolvido!');
