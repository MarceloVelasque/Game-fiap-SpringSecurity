# Seguranca - passo a passo da implementacao

Este documento descreve, em alto nivel, como a camada de seguranca foi implementada neste projeto e a ordem recomendada para evolucao.

## Resumo rapido (o que existe hoje)
- Dependencias: Spring Security, com.auth0 java-jwt, Spring Data JPA, Flyway, MySQL driver.
- Entidade `Usuario` com integracao ao Spring Security (`UserDetails`).
- `UsuarioRepository` com `findByEmail`.
- `AuthorizationService` implementando `UserDetailsService`.
- `SecurityConfig` com `SecurityFilterChain` e regras de acesso.
- `TokenService` para gerar/validar JWT (hardeada key `fiap`).
- `AuthController` com endpoints de login e (atualmente) register.
- `VerificarToken` (filtro) para extrair e validar token em requisicoes.

---

## Passo a passo numerado (o que foi feito, porque e quando usar)

1) Dependencias (pom.xml)
   - O que foi adicionado:
     - `spring-boot-starter-security` — fornece a infraestrutura de autenticação/autorização.
     - `com.auth0:java-jwt` — utilitario para criar e validar JWTs.
     - `spring-boot-starter-data-jpa`, `mysql-connector-j` — persistencia do `Usuario`.
     - `spring-boot-starter-flyway` — para rodar migrations (V1, V2 já presentes).
   - Por que: sem elas nao ha como autenticar (Security), gerar tokens (java-jwt) nem persistir usuarios (JPA).

2) Entidade `Usuario` e `Papel` (model)
   - O que: classe `Usuario` com campos id, nome, email, senha e `Papel` (enum) que mapeia perfis de acesso.
   - Porque: representa o usuario no banco e integra com Spring Security via `UserDetails`.
   - Observacao: `getUsername()` devolve email para usar o email como identificador.

3) Repositorio `UsuarioRepository`
   - O que: interface JPA com metodo `findByEmail(String email)`.
   - Porque: necessario para `AuthorizationService` e para validar/login e também usado pelo filtro para carregar UserDetails.

4) `AuthorizationService` (UserDetailsService)
   - O que: implementa `loadUserByUsername` buscando usuario pelo email e devolvendo um `UserDetails`.
   - Porque: `AuthenticationManager` precisa deste servico para checar credenciais durante o login.

5) `BCryptPasswordEncoder`
   - O que: encoder usado para criptografar senhas em `register` e para comparacao no login.
   - Porque: bcrypt é o padrão seguro para armazenar senhas; sem encoding a senha ficaria em texto plano.
   - Observacao: recomendo expor como `@Bean` em `SecurityConfig`.

6) `SecurityConfig` (regras e beans)
   - O que: define `securityFilterChain` (CSRF off, stateless, regras por rota), `AuthenticationManager` bean.
   - Porque: centraliza regras e permite injetar `AuthenticationManager` no `AuthController` para autenticar manualmente.

7) `TokenService` (JWT)
   - O que: gera token (HMAC256 com key "fiap", issuer "game", subject=email, expiracao 15min) e valida token.
   - Porque: JWTs permitem stateless authentication; o servidor nao precisa manter sessao.
   - Observacao: mover a chave para `application.properties` é recomendado.
   - Por que cada metodo existe:
     - `generationToken`: separar a criacao do token da logica do controller.
     - `validateToken`: abstrair a validacao e extracao do subject para ser reutilizavel no filtro.
     - `expirationTime`: util para manter politica de expiracao centralizada.

8) `AuthController` (login/register)
   - O que: endpoints para `POST /auth/login` e `POST /auth/register`.
   - Porque: controllers expõem a API; `login` realiza authenticação e chama `TokenService.generationToken`.
   - Observacao: atualmente `register` esta implementado no `TokenService` (com `@PostMapping`) — isso é atípico: endpoints devem ficar em controllers.

9) `VerificarToken` (filtro JWT)
   - O que: `OncePerRequestFilter` que extrai o token do header Authorization, chama `TokenService.validateToken` e popula SecurityContext.
   - Porque: middleware que garante que os controllers protegidos vejam o usuario autenticado.

10) Regras de acesso por rota
    - O que: definido em `SecurityConfig` (ex: `GET /games` publico, `POST /games` apenas `ROLE_ADMIN`).
    - Porque: controlar quem pode criar/editar recursos.

---

## Ordem recomendada para implementacao da camada de seguranca (pratica)

Siga esta ordem para implementar ou revisar a seguranca no projeto (cada passo compacto com objetivo claro):

Passo 1 — Dependencias
- Objetivo: ter todas as libs necessarias no classpath.
- Acoes: conferir `pom.xml` (já possui as dependencias listadas).

Passo 2 — Modelo e repositorio
- Objetivo: ter `Usuario` persistivel e buscavel por email.
- Acoes: criar ou revisar `Usuario` e `UsuarioRepository.findByEmail`.

Passo 3 — AuthorizationService
- Objetivo: prover `UserDetails` ao `AuthenticationManager`.
- Acoes: implementar `loadUserByUsername`.

Passo 4 — Password encoder
- Objetivo: garantir hashing seguro das senhas.
- Acoes: adicionar `BCryptPasswordEncoder` e idealmente expor como `@Bean`.

Passo 5 — SecurityConfig
- Objetivo: definir regras centrais de seguranca e beans como `AuthenticationManager`.
- Acoes: implementar `securityFilterChain` com CSRF off, stateless e regras por rota. Registrar filtros (ex: `VerificarToken`).

Passo 6 — TokenService
- Objetivo: ter utilitarios para gerar e validar JWTs.
- Acoes: implementar `generationToken`, `validateToken` e `expirationTime`. Externalizar secret.

Passo 7 — AuthController
- Objetivo: expor endpoints de login e register.
- Acoes: `POST /auth/login` deve autenticar e retornar `TokenDTO`. `POST /auth/register` deve criptografar e salvar usuario.

Passo 8 — VerificarToken (filtro)
- Objetivo: autenticar requisicoes por token automaticamente.
- Acoes: implementar filtro e registrá-lo na cadeia do `SecurityConfig`.

Passo 9 — Regras de acesso e testes
- Objetivo: assegurar que endpoints estao corretamente protegidos.
- Acoes: testar fluxos com token valido/invalido/ausente e ajustar regras por role.

Passo 10 — Melhorias e seguranca adicional
- Objetivo: endurecer a seguranca e operacionalizar.
- Acoes: externalizar secrets, considerar refresh tokens, reduzir expiracao, adicionar logs de seguranca e testes automatizados.

---

## Mapeamento rápido (lista de classes/pacotes criados e motivo)
- `config.security.SecurityConfig` — configura cadeia de seguranca e beans.
- `config.security.TokenService` — gera e valida JWTs.
- `config.security.VerificarToken` — filtro para extrair e validar token em cada requisicao.
- `service.AuthorizationService` — implementa `UserDetailsService` para integra com AuthenticationManager.
- `controller.AuthController` — endpoints de login e registro (controlador HTTP).
- `model.Usuario` e `mode.enums.Papel` — representa usuario e papeis/roles.
- `repository.UsuarioRepository` — operações de consulta ao BD (findByEmail).
- `dto.TokenDTO`, `dto.UserDTO` — objetos de transferencia usados pelo AuthController.

---

Se quiser, eu:
- gero um novo arquivo `docs/seguranca-mapping.md` com esse conteudo em formato mais visual ou um diagrama ASCII mais detalhado,
- ou aplico automaticamente as melhorias listadas (mover register para `AuthController`, externalizar a chave `fiap` para `application.properties`, transformar `passwordEncoder()` em `@Bean` e limpar warnings) e revalido o build.

Qual preferencia?
