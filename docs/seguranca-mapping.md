# Segurança — Mapeamento, diagrama e passo a passo de implementação

Este arquivo é uma versão focada e prática do guia de segurança: ordem de implementação, mapeamento das classes e um diagrama ASCII do fluxo de autenticação/validação JWT.

Objetivo: dar ao desenvolvedor uma checklist executável (passo 1, passo 2, ...) e justificativas curtas para cada escolha de implementação.

---

SUMÁRIO RÁPIDO
- Arquivo: docs/seguranca-mapping.md
- Público-alvo: desenvolvedores que vão implementar a camada de segurança (junior/pleno)
- Saída esperada: projeto com autenticação JWT, endpoints de login/register, filtro JWT e regras por roles.

---

MAPEAMENTO NUMERADO (PASSO A PASSO)

Passo 1 — Dependências (pom.xml)
  1. Verificar e manter:
     - `spring-boot-starter-security` — infraestrutura de autenticação/autorização.
     - `com.auth0:java-jwt` — geração/validação de JWT.
     - `spring-boot-starter-data-jpa` + `mysql-connector-j` — persistência de usuários.
     - `spring-boot-starter-flyway` — migrations (opcional, já presente).
  2. Porque: sem essas libs não é possível autenticar, gerar tokens e persistir usuários.

Passo 2 — Model e Repository
  1. Criar/confirmar `model/Usuario` com: id, nome, email, senha, papel (enum `Papel`).
  2. Implementar `UserDetails` ou criar um adaptador que retorne `UserDetails`.
     - `getUsername()` -> email
     - `getAuthorities()` -> converte `Papel` para `GrantedAuthority` (ex: ROLE_ADMIN)
  3. Criar `repository/UsuarioRepository` com `Usuario findByEmail(String email)`.
  4. Porque: o Spring Security precisa de uma fonte de usuários para autenticação.

Passo 3 — AuthorizationService (UserDetailsService)
  1. Criar `service/AuthorizationService implements UserDetailsService`.
  2. Implementar `loadUserByUsername(String username)` chamando `UsuarioRepository.findByEmail`.
  3. Retornar `UserDetails` (ou lançar `UsernameNotFoundException`).
  4. Porque: o `AuthenticationManager` delega a verificação de credenciais a esse serviço.

Passo 4 — Password encoding
  1. Usar `BCryptPasswordEncoder` para hash de senhas.
  2. Implementar um `@Bean` em `SecurityConfig`:
     - `@Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }`
  3. Garantir que no registro (`register`) a senha seja codificada antes de salvar.
  4. Porque: segurança (não armazenar senhas em texto claro).

Passo 5 — SecurityConfig (cadeia e beans)
  1. Criar `config/security/SecurityConfig` com:
     - `securityFilterChain(HttpSecurity)` configurado para API stateless (csrf disabled, session stateless).
     - Definir regras: liberar endpoints públicos (login/register/GET públicos) e proteger outros por ROLE.
     - Declarar bean `AuthenticationManager authenticationManager(AuthenticationConfiguration cfg)`.
     - Declarar bean `PasswordEncoder` (passo 4).
     - Registrar o filtro JWT (ex: `VerificarToken`) antes do filtro de autenticação padrão.
  2. Porque: centraliza regras e permite injeção de `AuthenticationManager` no controller.

Passo 6 — TokenService (JWT)
  1. Implementar `config/security/TokenService` com:
     - `String generationToken(Usuario user)` — cria JWT (HMAC256, issuer "game", subject=email, expiresAt).
     - `String validateToken(String token)` — valida token e retorna o subject (email) ou string vazia/indicador de inválido.
     - `Instant expirationTime()` — calcula expiração (ex: 15 minutos).
  2. Melhoria recomendada: não usar literal da chave no código — usar `@Value("${jwt.secret}") private String jwtSecret;` e configurar em `application.properties`.
  3. Porque: separa lógica de token e facilita reutilização no filtro.

Passo 7 — AuthController (endpoints)
  1. Implementar `controller/AuthController` com:
     - `POST /auth/register` — recebe `UsuarioRequest`/DTO, codifica senha com `passwordEncoder`, salva via `UsuarioRepository` e retorna `UsuarioResponse`.
     - `POST /auth/login` — recebe credenciais, cria `UsernamePasswordAuthenticationToken`, chama `authenticationManager.authenticate(...)`, se ok gera token via `TokenService.generationToken` e retorna `TokenDTO`.
  2. Garantir validação de entrada (Ex: `@Valid`).
  3. Porque: controller expõe a API; register/login devem ficar no controller, não em services de token.

Passo 8 — Filtro JWT: VerificarToken
  1. Criar `config/security/VerificarToken extends OncePerRequestFilter` com fluxo:
     - Ler header `Authorization`.
     - Se começar com `Bearer `, extrair token.
     - Chamar `TokenService.validateToken(token)` para obter email (subject).
     - Se email válido, usar `UsuarioRepository.findByEmail(email)` para obter `UserDetails` (ou `AuthorizationService`), criar `UsernamePasswordAuthenticationToken` e setar `SecurityContextHolder.getContext().setAuthentication(...)`.
     - Caso contrário, deixar requisição sem autenticação (seguir e deixar o `SecurityConfig` bloquear se necessário).
  2. Registrar o filtro na `SecurityConfig`.
  3. Porque: autentica automaticamente cada requisição com token.

Passo 9 — Regras de acesso e roles
  1. Verificar endpoints e mapear permissões (ex: `POST /games` => ROLE_ADMIN).
  2. Testar com: token válido, token inválido, sem token.

Passo 10 — Testes e endurecimento
  1. Escrever testes unitários para `TokenService` (geração e validação) e `AuthorizationService`.
  2. Teste de integração para `AuthController` (login flow).
  3. Melhorias: refresh token, rotação de chaves, limitar expiração, logs de segurança, monitoramento.

---

DIAGRAMA ASCII (fluxo simplificado)

Cliente
  |
  | POST /auth/login {email,senha}
  v
AuthController --(authenticate via AuthenticationManager)--> AuthenticationManager
  |                                                           |
  |<-- se ok: TokenService.generationToken(usuario) ---------- |
  v                                                           v
Cliente recebe { token }

Subsequent Request:
Cliente --(Header: Authorization: Bearer <token>)--> VerificarToken (filter) --(TokenService.validateToken)-> TokenService
  
Se token valido: VerificarToken -> UsuarioRepository.findByEmail(email) -> cria Authentication -> SecurityContextHolder
  
Controllers protegidos -> executam com Authentication presente

---

CHECKLIST DE MELHORIAS (prioridade e justificativa)

1) Mover `register` de `TokenService` para `AuthController` (Prioridade: alta)
   - Justificativa: separar controller (camada HTTP) de serviços de negócio. Evita que classes `@Service` exponham endpoints.

2) Externalizar segredo JWT (Prioridade: alta)
   - Justificativa: segredos não devem ficar hard-coded; usar `application.properties` e `@Value` ou `Environment`.

3) Declarar `PasswordEncoder` como `@Bean` em `SecurityConfig` (Prioridade: média)
   - Justificativa: permite injeção e consistência no projeto.

4) Tornar `TokenService.validateToken` não-estático e usar bean injetado no filtro (Prioridade: média)
   - Justificativa: melhora testabilidade e segue princípios de design orientado a objetos e DI.

5) Limpar warnings/inspeções (Ex: inicialização redundante de variáveis no filtro) (Prioridade: baixa)
   - Justificativa: remove ruído em IDE e melhora qualidade do código.

6) Adicionar testes unitários e integração (Prioridade: alta)
   - Justificativa: garante regressões menores ao evoluir segurança.

---

Exemplos rápidos de configuração recomendada (resumo, não código completo)
- `application.properties`
  - `jwt.secret=uma-chave-secreta-efetiva` (usar variável de ambiente em produção)
  - `jwt.expiration-minutes=15`

- `SecurityConfig` itens a confirmar:
  - `http.csrf().disable()` e `sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)`
  - `authorizeHttpRequests()` para liberar `/auth/login`, `/auth/register` e rotas públicas.
  - registrar `VerificarToken` antes de `UsernamePasswordAuthenticationFilter`.

---

Próximas ações que eu posso executar para você (escolha uma):
1) Aplicar automaticamente as melhorias prioritárias: mover `register` para `AuthController`, externalizar `jwt.secret`, tornar `passwordEncoder()` um `@Bean`, transformar `validateToken` em instância e adaptar `VerificarToken`. (+ revalidação do build)
2) Gerar um arquivo `docs/seguranca-diagram.txt` com diagrama ASCII mais detalhado e exemplos de payloads/token.
3) Criar testes unitários básicos para `TokenService`.
4) Nada — apenas salvar este arquivo e parar.

Diga qual opção prefere (ex: `1` para aplicar as melhorias agora).
