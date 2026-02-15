# Seguranca - hierarquia de metodos e fluxo (junior e pleno)

Este arquivo resume os metodos do pacote `config.security`, o que cada um faz, e quando sao chamados a partir dos controllers ou pelo Spring Security. O objetivo e servir como referencia rapida para implementacao e manutencao.

## Pacote `br.com.gameStore.gameStore.config.security`

### Classe `SecurityConfig`

Responsabilidade: configurar o Spring Security (filtros, regras de acesso e beans de apoio).

1) Metodo `securityFilterChain(HttpSecurity httpSecurity)`
- O que faz: monta a cadeia de filtros do Spring Security.
- Passo a passo:
  1. Desativa CSRF (`csrf.disable()`), pois a API e stateless.
  2. Define politica de sessao como stateless.
  3. Configura regras de acesso por endpoint e metodo HTTP.
  4. Exige autenticacao para qualquer rota nao liberada.
  5. Construi o `SecurityFilterChain`.
- Quando e chamado:
  - Pelo Spring durante o startup para montar a configuracao de seguranca.

2) Metodo `authenticationManager(AuthenticationConfiguration authenticationConfiguration)`
- O que faz: fornece um `AuthenticationManager` pronto para uso na autenticacao.
- Passo a passo:
  1. Recebe `AuthenticationConfiguration`.
  2. Retorna `authenticationConfiguration.getAuthenticationManager()`.
- Quando e chamado:
  - Pelo Spring para criar o bean do `AuthenticationManager`.
  - Usado no `AuthController` para autenticar o login.

3) Metodo `passwordEncoder()`
- O que faz: cria um `BCryptPasswordEncoder`.
- Passo a passo:
  1. Instancia `BCryptPasswordEncoder`.
  2. Retorna o encoder.
- Quando e chamado:
  - Atualmente nao esta anotado com `@Bean`, entao nao e injetado automaticamente.
  - Pode ser usado manualmente para criptografar senhas (ex: cadastro).

### Classe `TokenService`

Responsabilidade: gerar e validar JWT e cadastrar usuario (registro).

1) Metodo `generationToken(UserDTO userDTO)`
- O que faz: gera um JWT para o usuario.
- Passo a passo:
  1. Cria o algoritmo HMAC256 com a chave fixa `"fiap"`.
  2. Cria o token com `issuer = "game"`.
  3. Define `subject` como email do usuario.
  4. Define expiracao com `expirationTime()`.
  5. Assina e retorna o token.
- Quando e chamado:
  - No `AuthController` apos a autenticacao bem sucedida.

2) Metodo `validateToken(String token)`
- O que faz: valida um JWT recebido.
- Passo a passo:
  1. Cria o algoritmo HMAC256 com a mesma chave `"fiap"`.
  2. Verifica `issuer` e assinatura.
  3. Retorna o `subject` (email) se valido.
  4. Em erro, retorna `"invalid token"`.
- Quando e chamado:
  - Em filtros ou servicos que precisem validar o token antes de liberar acesso.
  - Atualmente nao aparece sendo usado diretamente nos controllers.

3) Metodo `expirationTime()`
- O que faz: calcula a expiracao do token (15 minutos).
- Passo a passo:
  1. Pega o horario atual (`LocalDateTime.now()`).
  2. Soma 15 minutos.
  3. Converte para `Instant` com offset `-03:00`.
- Quando e chamado:
  - Internamente por `generationToken()`.

4) Metodo `register(Usuario user)`
- O que faz: registra um novo usuario.
- Passo a passo:
  1. Criptografa a senha com `BCryptPasswordEncoder`.
  2. Atualiza a senha no objeto `Usuario`.
  3. Salva no banco com `UsuarioRepository`.
  4. Retorna o usuario criado.
- Quando e chamado:
  - Por requisicoes `POST /auth/register`.
  - Obs: esse endpoint esta dentro de `TokenService`, o que e incomum (normalmente ficaria em um controller).

## Como os metodos sao chamados a partir do controller

### Controller `AuthController`

1) `POST /auth/login`
- Cria `UsernamePasswordAuthenticationToken` com email e senha.
- Chama `authenticationManager.authenticate(...)`.
- Se autenticar, chama `tokenService.generationToken(...)`.
- Retorna um `TokenDTO` com o token.

## Fluxo completo de autenticacao (passo a passo)

1) Usuario envia `POST /auth/login` com email e senha.
2) `AuthController` cria o token de autenticacao e chama `AuthenticationManager`.
3) O `AuthenticationManager` usa o `UserDetailsService` (classe `AuthorizationService`) para buscar o usuario.
4) Se a senha bater, o `AuthController` chama `TokenService.generationToken`.
5) O token JWT e devolvido ao cliente em um `TokenDTO`.
6) Nas chamadas futuras, o cliente envia o token no header `Authorization`.
7) Um filtro (quando existir) deve chamar `TokenService.validateToken` para liberar o acesso.

## Observacoes para evolucao

- Se a aplicacao usar JWT em todas as rotas protegidas, e recomendavel criar um filtro que leia o header `Authorization` e valide o token.
- O metodo `passwordEncoder()` pode virar `@Bean` para reuso com injecao.
- O endpoint `register` deveria ficar em um controller dedicado (ex: `AuthController`).

---

# Mapeamento e guia numerado (o que foi feito e ordem recomendada para implementar a camada de seguranca)

Abaixo um passo a passo numerado, desde a criacao do projeto ate as principais implementacoes de seguranca (dependencias, pacotes, classes e metodos). Use esta sequencia como checklist de implementacao.

1) Criacao do projeto e dependencias (pom.xml)
   - Adicionar dependencias principais:
     1. `spring-boot-starter-security` — fornece a infraestrutura de autenticação e autorizacao do Spring Security.
     2. `com.auth0:java-jwt` — biblioteca para gerar/validar JWT (usada por `TokenService`).
     3. `spring-boot-starter-data-jpa` e driver do banco (ex: `mysql-connector-j`) — para persistencia do `Usuario`.
     4. `spring-boot-starter-flyway` (opcional) — migrations do BD (já presente no projeto).
   - Porque: sem estas dependencias nao e possivel integrar autenticação, persistência e tokens JWT.

2) Modelo e repositorio
   - Criar `model/Usuario` com campos (id, nome, email, senha, papel) e implementar `UserDetails`.
     - getUsername() deve retornar o email para usarmos o email como login.
     - getAuthorities() deve transformar `Papel` em `GrantedAuthority` (ex: ROLE_ADMIN).
   - Criar `repository/UsuarioRepository` com `findByEmail(String email)`.
   - Porque: o Spring Security precisa de um `UserDetails` e de um repositório para buscar o usuario no banco.

3) Servico de autenticacao (UserDetailsService)
   - Criar `service/AuthorizationService` implementando `UserDetailsService`.
   - Implementar `loadUserByUsername(String username)` para buscar o usuario por email e retornar um `UserDetails`.
   - Porque: o `AuthenticationManager` usa esse servico para validar credenciais.

4) Password encoding
   - Usar `BCryptPasswordEncoder` para criptografar senhas.
   - Criar um `@Bean` em `SecurityConfig` (recomendado) ou usar manualmente em pontos de cadastro.
   - Porque: armazenar senha em texto plano e inseguro; bcrypt é padrão seguro e compatível com Spring.

5) Configuracao de seguranca central
   - Criar `config/security/SecurityConfig` com:
     1. `securityFilterChain(HttpSecurity)` para configurar CSRF, session management, regras de acesso por endpoint/role.
     2. Bean de `AuthenticationManager` a partir de `AuthenticationConfiguration` para permitir autenticar manualmente no `AuthController`.
     3. Registrar filtros personalizados (ex: `VerificarToken`) na cadeia, se usar JWT.
   - Porque: concentra regras e filtros de seguranca em um lugar central e permite personalizacao por rota.

6) Geracao e validacao de JWT
   - Criar `config/security/TokenService` com metodos:
     1. `generationToken(Usuario user)` — cria token JWT HMAC256, com issuer, subject (email) e expiracao (15 minutos).
     2. `validateToken(String token)` — valida assinatura/issuer e retorna o subject (email) ou string vazia/indicacao de invalido.
     3. `expirationTime()` — calcula expiracao.
   - Onde usar: `AuthController` pos-authentication para gerar token; filtro para validar em requisicoes subsequentes.
   - Observacao: mover a chave secreta (`"fiap"`) para `application.properties` e usar `@Value` (recomendado em producao).

7) Endpoints de Auth
   - Criar `controller/AuthController` com endpoints:
     1. `POST /auth/register` — criptografa senha e salva usuario (registro).
     2. `POST /auth/login` — autentica usando `AuthenticationManager`, e em caso de sucesso chama `TokenService.generationToken` e retorna `TokenDTO`.
   - Porque: separar responsabilidades (controller para exposicao HTTP, services para logica de negocio).

8) Filtro de requisicoes (bearer token)
   - Criar `config/security/VerificarToken` estendendo `OncePerRequestFilter`:
     1. Ler header `Authorization` e extrair token (remover `Bearer `).
     2. Validar token (usar `TokenService.validateToken`) e recuperar email.
     3. Buscar `UserDetails` via `UsuarioRepository` e setar `Authentication` no `SecurityContextHolder`.
   - Porque: garante que em requisicoes protegidas o usuario seja autenticado por token.

9) Protecao das rotas e roles
   - Em `SecurityConfig`, liberar public endpoints (`GET /games`, `POST /auth/login`, `POST /auth/register`) e proteger rotas como `POST /games` para `ROLE_ADMIN`.
   - Testar cenarios com token valido, token invalido e sem token.

10) Testes e evolucao
   - Escrever testes unitarios para `TokenService`, `AuthorizationService` e testes de integração da `AuthController`.
   - Externalizar secrets e ajustar expiracao conforme necessidade de negocio.

---

# Diagrama textual (mapa) das responsabilidades

[Cliente] --(POST /auth/login)-> [AuthController] --(authenticate)-> [AuthenticationManager] --(loadUserByUsername)-> [AuthorizationService] --(db)-> [UsuarioRepository]

Se autenticado:
[AuthController] --(TokenService.generationToken)-> [TokenService] -> retorna Token -> [Cliente]

Em requisicoes subsequentes:
[Cliente] --(Header: Authorization: Bearer <token])-> [Filtro VerificarToken] --(TokenService.validateToken)-> [TokenService]
Se valido: [Filtro] busca UserDetails em [UsuarioRepository] e popula [SecurityContextHolder] -> [Controllers protegidos]

---

# Ordem concreta de implementacao (o que fazer primeiro, segundo, terceiro...)

1) Configurar `pom.xml` com as dependencias listadas (Security, java-jwt, JPA, Flyway, driver DB).
2) Criar entidade `Usuario` e `UsuarioRepository` com `findByEmail`.
3) Implementar `AuthorizationService` (UserDetailsService).
4) Adicionar `BCryptPasswordEncoder` (criar bean em `SecurityConfig`).
5) Implementar `SecurityConfig` com `securityFilterChain` e `authenticationManager`.
6) Implementar `TokenService` (geracao/validacao), externalizar secret em `application.properties`.
7) Implementar `AuthController` (register/login), usar `AuthenticationManager` para login.
8) Implementar `VerificarToken` e registrar o filtro (se necessario) na cadeia do `SecurityConfig`.
9) Proteger endpoints por role e testar os fluxos.
10) Escrever testes unitarios e de integracao e ajustar configuracoes de seguranca (CSRF, tempo de expiracao, refresh token se necessario).

---

Se quiser, eu posso:
- Gerar automaticamente um arquivo `docs/seguranca-mapping.md` separado com este passo a passo e um diagrama em formato ascii mais detalhado.
- Aplicar as pequenas melhorias recomendadas (externalizar chave, transformar `passwordEncoder()` em `@Bean`, mover `register` para `AuthController` e limpar warnings).
