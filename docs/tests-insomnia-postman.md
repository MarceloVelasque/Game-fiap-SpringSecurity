# Testes com Postman / Insomnia — passo a passo

Este guia descreve como testar a API localmente usando Postman ou Insomnia. Contém: configuração de ambiente (variáveis), requests (GET, POST), exemplos de payload JSON, headers necessários e cenários de verificação (login, register, uso do token Bearer).

Observação importante antes de começar
- Este projeto contém algumas inconsistências conhecidas na implementação (por exemplo: `Usuario.getPassword()` atualmente retorna string vazia). Se o login falhar ao testar, veja a seção *Depuração / Problemas conhecidos* no final — há alternativas (inserir usuário direto no banco com senha codificada, ajustar código localmente).

Pré-requisitos
- Projeto rodando localmente: `mvnw spring-boot:run` (ou executar pela sua IDE). Porta padrão: 8080 (ajuste se for diferente).
- Postman ou Insomnia instalado.

Configurar ambiente (variáveis)
- Configure um ambiente chamado `local` com as variáveis:
  - `base_url` = `http://localhost:8080`
  - `jwt_token` = (vazio inicialmente, será preenchido após login)

Exemplo no Postman (Environment):
- KEY: base_url  VALUE: http://localhost:8080
- KEY: jwt_token VALUE:

Como usar essas variáveis nos requests:
- URL: `{{base_url}}/auth/login`
- Header Authorization: `Bearer {{jwt_token}}`

Requests e testes (passo a passo)

Passo 1 — Registrar um usuário (POST /auth/register)
- Endpoint: POST {{base_url}}/auth/register
- Headers:
  - Content-Type: application/json
- Body (JSON) — exemplo de usuário comum (ROLE_USUARIO):
{
  "nome": "João Silva",
  "email": "joao@example.com",
  "senha": "senha123",
  "papel": "USUARIO"
}

- Body (JSON) — exemplo de usuário admin (para testar endpoints protegidos):
{
  "nome": "Admin",
  "email": "admin@example.com",
  "senha": "admin123",
  "papel": "ADMIN"
}

- O que esperar:
  - Status: 201 Created
  - Body: o objeto `Usuario` criado (com campo `id` e `senha` gravada — pode estar codificada).

Observação: se o registro não gravar a senha corretamente devido a bug (ver seção de depuração), use a alternativa de inserir usuário diretamente no banco (com `senha` já criptografada com BCrypt) para testar o fluxo de login.

Passo 2 — Fazer login (POST /auth/login)
- Endpoint: POST {{base_url}}/auth/login
- Headers:
  - Content-Type: application/json
- Body (JSON):
{
  "email": "joao@example.com",
  "password": "senha123"
}

- O que esperar:
  - Status: 200 OK
  - Body: { "token": "<jwt_token_string>" }

- Ação: copiar o valor do token recebido e salvar na variável de ambiente `jwt_token` (ou no Insomnia use Quick Look).

Passo 3 — Testar GET público (GET /games)
- Endpoint: GET {{base_url}}/games
- Headers: (opcional) Authorization Bearer se quiser testar ambos os casos
  - Content-Type: application/json
  - Authorization: Bearer {{jwt_token}} (opcional)
- O que esperar:
  - Status: 200 OK
  - Body: array de `Game` (pode estar vazio se não houver registros)

Passo 4 — Criar um game (POST /games) — endpoint normalmente protegido (ROLE_ADMIN)
- Endpoint: POST {{base_url}}/games
- Headers:
  - Content-Type: application/json
  - Authorization: Bearer {{jwt_token}}  (token do usuário ADMIN)
- Body (JSON):
{
  "titulo": "Super Game",
  "descricao": "Jogo de exemplo",
  "preco": 59.90
}

- O que esperar:
  - Se o token pertencer a um usuário com `ROLE_ADMIN`:
    - Status: 201 Created
    - Body: objeto `Game` criado com `id`.
  - Se o token for de usuário comum (ROLE_USUARIO) ou token ausente/incorreto:
    - Status: 403 Forbidden (ou 401 Unauthorized) dependendo da configuração.

Passo 5 — Consultar game por id (GET /games/{id})
- Endpoint: GET {{base_url}}/games/1
- Headers:
  - Content-Type: application/json
  - Authorization: Bearer {{jwt_token}} (se endpoint protegido)
- O que esperar:
  - Status 200 OK com o objeto `Game`, ou 404 Not Found se o id não existir.

Cenários de teste adicionais
- Token inválido:
  - Coloque um token aleatório em `jwt_token` e tente acessar POST /games — deve retornar 401/403.
- Token expirado (simular usando token manual com exp passado ou reduzir exp time no código):
  - Deve retornar 401/403 ao chamar endpoints protegidos.
- Sem token:
  - Acessar endpoint protegido deve retornar 401/403.

Como popular a variável `jwt_token` automaticamente no Postman (exemplo rápido):
1. Após executar POST /auth/login, vá em Tests (aba do Postman) e adicione:

```javascript
// Postman test script para salvar token em variável de ambiente "jwt_token"
const body = pm.response.json();
if (body && body.token) {
  pm.environment.set("jwt_token", body.token);
}
```

2. Agora os requests seguintes que usarem `{{jwt_token}}` receberão o token salvo.

Problemas conhecidos / Depuração
- Problema: login falha mesmo após registro (senha aparentemente incorreta)
  - Causa provável: a classe `Usuario` possui `getPassword()` que atualmente retorna string vazia — isso fará com que a senha salva ou comparada não funcione corretamente.
  - Solução rápida para teste local:
    1. Insira manualmente um usuário na tabela `tb_usuario` com senha codificada em BCrypt. Para gerar hash BCrypt você pode usar um pequeno script Java/online bcrypt generator (em produção não use online).
    2. Exemplo de hash de teste (não usar em produção): `bcrypt("senha123")` => `$2a$10$...`
    3. Insira no banco (nome, email, senha = hash, papel = 'ADMIN' ou 'USUARIO').
  - Alternativa (melhor): ajustar localmente `Usuario.getPassword()` para `return senha;` e reiniciar a aplicação; então use o fluxo normal de register/login.

- Se ao registrar o usuário a resposta tiver `senha` vazia ou diferente, verifique se o controller está recebendo JSON com chave `senha` (não `password`).

Exemplo de coleção (exemplo de ordem de execução)
1. Register (POST /auth/register) — criar admin user
2. Login (POST /auth/login) — obter token
3. POST /games (com token admin) — criar um jogo
4. GET /games — listar jogos
5. GET /games/{id} — verificar jogo criado
6. Testes de falha (token inválido, sem token)

Dicas rápidas para Insomnia
- Crie um environment `local` com `base_url` e `jwt_token`.
- Use template tag `{{ base_url }}` nas URLs.
- No request de login, crie um Response Hook para salvar o token automaticamente em `jwt_token`.

Dicas rápidas para Postman
- Crie um Environment `local` com `base_url` e `jwt_token`.
- No tab Tests do request de login, cole o script mostrado acima para salvar token.
- Crie uma Collection com os requests na ordem do exemplo e rode como Collection Runner para validar os passos.

Se quiser, eu posso:
- Gerar automaticamente uma coleção JSON do Postman com os requests (v2.1) pronta para importar (registro, login, create game, get games) — você só precisará apontar `{{base_url}}`.
- Ou: ajustar o código do projeto para corrigir o bug de `Usuario.getPassword()` e aplicar as melhorias de segurança mencionadas previamente, depois revalidar os testes.

Quer que eu gere a coleção Postman (opção: sim/não)?
