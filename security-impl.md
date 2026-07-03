# Implementação do Módulo de Segurança - RoomiesApp (`app-roomiesapp`)

Este documento descreve todo o progresso e os detalhes técnicos da implementação da camada de segurança e autenticação para o backend do **RoomiesApp**, utilizando o framework da aplicação (`gogather-framework`), especificamente o módulo de segurança (`gogather-framework-security`).

---

## 1. Verificação de Módulos e Dependências

> [!IMPORTANT]
> **Aviso de Módulos:** Conforme solicitado, foi realizada uma análise de dependências antes da implementação. **Nenhum módulo adicional do framework** ou biblioteca externa fora do padrão do ecossistema foi necessário para esta fase de segurança. As únicas adições no `pom.xml` da aplicação foram a inclusão explícita do módulo **`gogather-framework-security`** (o módulo alvo) e as dependências padrão do Spring Boot e Java JWT já existentes no ecossistema GoGather.

---

## 2. Arquitetura do Sistema de Segurança (Padrão de Hollywood)

A integração com o ecossistema `gogather-framework` segue o **Princípio de Hollywood** (*"Don't call us, we'll call you"*), dividindo as responsabilidades entre pontos congelados e quentes:

* **Frozen Spot (Framework de Segurança):**
  * **`SecurityOrchestrator` & `JwtAuthenticationFilter`**: Gerenciam o fluxo de verificação de tokens JWT nas requisições HTTP, inspecionando tanto os headers `Authorization: Bearer <token>` quanto os cookies `accessToken`.
  * **`TokenService` (Framework)**: Responsável pela assinatura e validação criptográfica (HMAC256) dos tokens JWT de acesso (`accessToken`).

* **Hot Spot (Aplicação Consumidora - RoomiesApp):**
  * **`AppSecurityDataProvider`**: Implementa a interface `SecurityDataProvider` do framework. Quando o framework recebe um token válido, ele invoca o gancho `loadUserByUsername` desta classe, que consulta o banco de dados do RoomiesApp via `UserRepository` aceitando tanto o **username** quanto o **e-mail** do usuário.

---

## 3. Funcionalidades e Rotas Implementadas

Todas as rotas foram expostas sob o prefixo `/auth` no controlador `AuthController`:

### 3.1. Registro de Usuários
* **Rota:** `POST /auth/register`
* **Acesso:** Público (`permitAll`)
* **Descrição:** Recebe username, e-mail, senha, nome de exibição e data de nascimento. Valida a unicidade de e-mail e username (retornando erro `400 Bad Request` caso já existam), criptografa a senha com `BCryptPasswordEncoder` e persiste a entidade `User` com status ativo no banco de dados.

### 3.2. Autenticação e Login (Suporte a Cookies)
* **Rota:** `POST /auth/login`
* **Acesso:** Público (`permitAll`)
* **Descrição:** Autentica as credenciais fornecidas (suportando login por **username** ou **e-mail**). Em caso de sucesso:
  1. Gera um `accessToken` JWT (com validade padrão de 2 horas) via framework.
  2. Gera e persiste no banco de dados um `refreshToken` UUID (com validade de 7 dias) associado ao usuário.
  3. Retorna os tokens no corpo da resposta (JSON) e também nos headers HTTP `Set-Cookie` com as flags de segurança:
     * `HttpOnly=true` (proteção contra XSS / leitura via JavaScript)
     * `SameSite=Lax` (proteção contra CSRF e compatibilidade com navegação moderna)
     * `Path=/`

### 3.3. Renovação de Tokens (`Refresh Token`)
* **Rota:** `POST /auth/refresh`
* **Acesso:** Público (`permitAll`)
* **Descrição:** Aceita o refresh token através do **Cookie HTTP (`refreshToken`)** ou via corpo da requisição JSON. Verificações realizadas:
  1. Localiza o token no banco de dados; caso não exista, retorna erro de validação.
  2. Verifica a data de expiração (`expireDate`). Se expirado, remove-o do banco e exige novo login.
  3. Realiza a **rotação do token**: deleta o refresh token anterior, emite um novo `refreshToken` e um novo `accessToken`, retornando-os no corpo e atualizando os cookies do navegador.

### 4.4. Logout e Limpeza de Sessão
* **Rota:** `POST /auth/logout`
* **Acesso:** Público (`permitAll`)
* **Descrição:** Captura o refresh token (do cookie ou do corpo), revoga-o excluindo seu registro na tabela `refresh_tokens` do banco de dados e emite cookies limpos (com `Max-Age=0`) para `accessToken` e `refreshToken`, encerrando a sessão no cliente.

### 4.5. Troca de Senha
* **Rota:** `POST /auth/change-password`
* **Acesso:** Autenticado (`anyRequest().authenticated()`)
* **Descrição:** Rota protegida por JWT. Requer o envio da senha antiga e da nova senha no corpo da requisição. Valida se a senha antiga confere com o hash armazenado e, em caso afirmativo, criptografa e atualiza a nova senha no banco de dados.

### 4.6. Verificação de Sessão e Perfil
* **Rotas:** `GET /auth/me` e `GET /auth/verify`
* **Acesso:** Autenticado
* **Descrição:** Retorna os dados públicos (ID, username, e-mail, displayName, birthDate) do usuário autenticado no contexto de segurança da requisição atual.

---

## 4. Estrutura de Arquivos Criados / Modificados

Abaixo está o resumo dos arquivos criados na estrutura do projeto `/app-roomiesapp/backend`:

```text
app-roomiesapp/backend/
├── pom.xml                                               # Modificado: Adicionado gogather-framework-security
└── src/main/
    ├── resources/
    │   ├── application.yaml                              # Criado: Configuração PostgreSQL e propriedades do Framework JWT
    │   └── application-local.yaml                        # Criado: Configuração H2 (em memória) para desenvolvimento/testes locais
    └── java/com/role/net/roomiesapp/
        ├── config/
        │   └── SecurityConfig.java                       # Criado: SecurityFilterChain, CORS, permissões de rota, filtros JWT, BCrypt
        ├── controller/
        │   └── AuthController.java                       # Criado: Endpoints REST para login, register, logout, refresh, change-password
        ├── dto/
        │   ├── auth/
        │   │   ├── ChangePasswordRequest.java            # Criado: DTO de troca de senha
        │   │   ├── LoginRequest.java                     # Criado: DTO de login
        │   │   ├── RefreshRequest.java                   # Criado: DTO de solicitação de refresh
        │   │   ├── RegisterUserRequest.java              # Criado: DTO de registro com validações Jakarta Validation
        │   │   ├── RegisterUserResponse.java             # Criado: DTO de resposta de registro
        │   │   └── TokenResponse.java                    # Criado: DTO contendo accessToken e refreshToken
        │   ├── error/
        │   │   └── StandardErrorDTO.java                 # Criado: DTO padronizado para mensagens de erro HTTP
        │   └── user/
        │       └── UserResponse.java                     # Criado: DTO de exibição de dados do usuário
        ├── entity/
        │   ├── BaseEntity.java                           # Criado: Superclasse JPA com ID, UUID externalId, createdAt e updatedAt
        │   ├── RefreshToken.java                         # Criado: Entidade JPA de persistência do token de renovação (relacionamento com User)
        │   └── User.java                                 # Criado: Entidade JPA de usuário implementando UserDetails
        ├── exception/
        │   ├── GlobalExceptionHandler.java               # Criado: RestControllerAdvice para interceptar erros e retornar StandardErrorDTO
        │   ├── InvalidCredentialsException.java          # Criado: Exceção para falha em autenticação ou troca de senha
        │   ├── ResourceNotFoundException.java            # Criado: Exceção para entidades ou tokens não encontrados
        │   └── UniqueDataAlreadyInUseException.java      # Criado: Exceção para colisão de e-mail ou username no cadastro
        ├── repository/
        │   ├── RefreshTokenRepository.java               # Criado: Repositório JPA para RefreshToken (inclui deleteByUserId)
        │   └── UserRepository.java                       # Criado: Repositório JPA para User (busca e verificação por email/username)
        └── service/
            ├── AuthService.java                          # Criado: Serviço de cadastro, alteração de senha e carregamento de UserDetails
            ├── TokenService.java                         # Criado: Serviço ("appTokenService") de gerenciamento de refresh tokens e cookies
            └── provider/
                └── AppSecurityDataProvider.java          # Criado: Implementação do gancho Hot Spot SecurityDataProvider do framework
```

---

## 5. Como Testar e Executar

1. **Compilação:**
   No diretório `app-roomiesapp/backend`, execute:
   ```bash
   mvn clean compile
   ```
2. **Execução Local (com banco H2 em memória):**
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```
   * O console do H2 estará acessível em `http://localhost:8080/h2-console`.
3. **Execução com PostgreSQL:**
   Configure as variáveis de ambiente `db.username` e `db.password` (ou ajuste no `application.yaml`) e inicie a aplicação normalmente:
   ```bash
   mvn spring-boot:run
   ```
