# Implementação do Módulo de Segurança - RoomiesApp (`app-roomiesapp`)

Este documento descreve todo o progresso e os detalhes técnicos da implementação da camada de segurança e autenticação para o backend do **RoomiesApp** (Gestor de Repúblicas e Condomínios), utilizando o framework da aplicação (`gogather-framework`), especificamente o módulo de segurança (`gogather-framework-security`), bem como a configuração do banco de dados PostgreSQL padronizada com o ecossistema GoGather.

---

## 1. Verificação de Módulos e Dependências

> [!IMPORTANT]
> **Aviso de Módulos:** Conforme solicitado ("*Caso seja necessário mais algum módulo, avise antes de, de fato, implementar*"), foi realizada uma análise rigorosa das dependências do projeto antes e durante a implementação. **Nenhum módulo adicional do framework fora do escopo de segurança foi necessário.**
>
> O `pom.xml` do backend do `app-roomiesapp` já contava com os módulos `gogather-framework-core`, `gogather-framework-billing`, `gogather-framework-polling` e `gogather-framework-chat`. A única adição ao arquivo foi a inclusão explícita do módulo alvo **`gogather-framework-security`**:
> ```xml
> <dependency>
>     <groupId>br.com.gogather</groupId>
>     <artifactId>gogather-framework-security</artifactId>
>     <version>1.0.0-SNAPSHOT</version>
> </dependency>
> ```

---

## 2. Configuração de Banco de Dados PostgreSQL e Container Docker (`secret.yaml` + `compose.yaml`)

Alinhado com o padrão arquitetural do **GoGather**, o RoomiesApp foi configurado para utilizar o banco de dados relacional **PostgreSQL**, isolando as credenciais sensíveis através do arquivo `secret.yaml`:

### 2.1. Isolamento de Credenciais (`secret.yaml` e `application.yaml`)
No arquivo de configuração principal (`application.yaml`), a importação do segredo é declarada de forma opcional no classpath:
```yaml
spring:
  config:
    import: optional:classpath:secret.yaml
  datasource:
    url: jdbc:postgresql://localhost:5432/roomiesapp
    username: ${db.username:postgres}
    password: ${db.password:password}
    driver-class-name: org.postgresql.Driver
```
O arquivo de segredo real (`src/main/resources/secret.yaml`) define as credenciais injetadas nas propriedades do Spring:
```yaml
db:
  username: pedro
  password: pp1234
```
> [!NOTE]
> Para garantir a segurança do repositório, o arquivo `secret.yaml` (e os arquivos `.env`) está devidamente ignorado no `.gitignore`. Um arquivo modelo seguro **`secret.yaml.example`** foi disponibilizado em `src/main/resources` para guiar novos desenvolvedores.

### 2.2. Container de Banco de Dados (`compose.yaml` e `.env`)
Para subir o banco de dados rapidamente via Docker Compose sem necessidade de instalação local do servidor PostgreSQL, foram criados arquivos `compose.yaml` (e seus respectivos `.env`) na raiz do projeto e em `/backend`:
```yaml
services:
  db:
    image: postgres:latest
    container_name: roomiesapp-db
    restart: always
    environment:
      POSTGRES_USER: ${DB_USER:-pedro}
      POSTGRES_PASSWORD: ${DB_PASS:-pp1234}
      POSTGRES_DB: roomiesapp
    ports:
      - "5432:5432"
```

---

## 3. Adequação ao Domínio - Repúblicas e Condomínios (RoomiesApp)

O **RoomiesApp** foi planejado ao redor de 5 pilares fundamentais:
1. **Domínio Central (PV1):** Unidade Habitacional (moradores de repúblicas/condomínios).
2. **Gestor de Sequência (PV2):** Mural de regras e checklist de convivência.
3. **Fluxo Financeiro (PV3):** Rateio de contas de casa (aluguel, água, luz, internet, contas variáveis).
4. **Comunicação (PF3):** Chat da casa e mural de avisos.
5. **Tomada de Decisão (PF4):** Votação de regras e decisões financeiras/reformas.

Para suportar esse ecossistema habitacional desde a raiz da identidade, a entidade **`User`** (que representa um morador) foi enriquecida além dos campos tradicionais de autenticação, incorporando o campo **`phoneNumber`** (telefone/WhatsApp). Esse atributo é vital para futuras integrações de notificações de cobranças no **Fluxo Financeiro (PV3)** e alertas urgentes no **Chat da casa (PF3)**.

---

## 4. Arquitetura do Sistema de Segurança (Padrão de Hollywood)

A integração com o ecossistema `gogather-framework` segue o **Princípio de Hollywood** (*"Don't call us, we'll call you"*), dividindo as responsabilidades entre pontos congelados e quentes:

* **Frozen Spot (Framework de Segurança - `gogather-framework-security`):**
  * **`SecurityOrchestrator` & `JwtAuthenticationFilter`**: Interceptam requisições HTTP e realizam a validação e extração dos claims de tokens JWT, inspecionando tanto os cabeçalhos `Authorization: Bearer <token>` quanto os cookies `accessToken`.
  * **`TokenService` (do Framework)**: Realiza a assinatura criptográfica (HMAC256) e verificação dos tokens JWT de acesso.

* **Hot Spot (Aplicação Consumidora - `app-roomiesapp`):**
  * **`AppSecurityDataProvider`**: Implementa a interface `SecurityDataProvider` do framework. Quando o filtro JWT valida um token, ele invoca o método `loadUserByUsername` nesta classe. O `AppSecurityDataProvider` consulta o banco de dados do RoomiesApp via `UserRepository`, permitindo autenticação flexível por **username** ou **e-mail**.

---

## 5. Funcionalidades e Rotas Implementadas

Todas as rotas de autenticação e gestão de sessão foram expostas sob o prefixo `/auth` no `AuthController`:

### 5.1. Registro de Moradores (`POST /auth/register`)
* **Acesso:** Público (`permitAll`)
* **Descrição:** Recebe `username`, `email`, `password`, `displayName`, `phoneNumber` e `birthDate`.
* **Validações:**
  * Verifica se `email` ou `username` já existem no banco (lançando exceção `UniqueDataAlreadyInUseException` que retorna status HTTP `400 Bad Request`).
  * Criptografa a senha utilizando **`BCryptPasswordEncoder`**.
  * Persiste o novo morador com status ativo no banco de dados.

### 5.2. Autenticação e Login com Cookies HTTP-Only (`POST /auth/login`)
* **Acesso:** Público (`permitAll`)
* **Descrição:** Autentica as credenciais via `AuthenticationManager` (suportando login por **username** ou **email**). Em caso de sucesso:
  1. Gera um `accessToken` JWT (com validade de 2 horas) por meio do `SecurityOrchestrator` do framework.
  2. Gera um `refreshToken` UUID aleatório (com validade de 7 dias) no banco de dados, revogando automaticamente eventuais tokens antigos do mesmo morador.
  3. Retorna os tokens no corpo da resposta JSON e injeta-os nos cabeçalhos `Set-Cookie` com flags de alta segurança:
     * `HttpOnly=true`: Impede leitura do cookie por scripts (proteção contra XSS).
     * `SameSite=Lax`: Mitiga ataques CSRF e mantém compatibilidade com navegação web moderna.
     * `Path=/`: Disponível para todas as rotas da API.

### 5.3. Renovação e Rotação de Tokens (`POST /auth/refresh`)
* **Acesso:** Público (`permitAll`)
* **Descrição:** Aceita o refresh token através do **Cookie HTTP (`refreshToken`)** ou via corpo JSON da requisição.
* **Mecanismo de Segurança:**
  * Localiza o token na tabela `refresh_tokens`.
  * Valida a expiração (`expireDate`). Se vencido, remove o registro e exige novo login.
  * Efetua a **rotação de tokens**: exclui o token de renovação atual e gera um novo par (`accessToken` + `refreshToken`), atualizando os cookies no cliente.

### 5.4. Logout e Encerramento de Sessão (`POST /auth/logout`)
* **Acesso:** Público (`permitAll`)
* **Descrição:** Identifica o token de renovação, remove-o definitivamente do banco de dados e emite cookies limpos (`Max-Age=0`) para `accessToken` e `refreshToken`, invalidando a sessão no navegador.

### 5.5. Troca de Senha (`POST /auth/change-password`)
* **Acesso:** Autenticado (`anyRequest().authenticated()`)
* **Descrição:** Rota protegida por JWT. Requer envio da `oldPassword` e `newPassword`. O serviço compara o hash da senha antiga com o armazenado; caso confira, aplica o hash BCrypt na nova senha e atualiza o banco de dados.

### 5.6. Verificação de Sessão e Perfil (`GET /auth/me` e `GET /auth/verify`)
* **Acesso:** Autenticado
* **Descrição:** Retorna os dados do usuário autenticado no contexto da requisição, incluindo `id` (UUID externo), `username`, `displayName`, `email`, `phoneNumber` e `birthDate`.

---

## 6. Estrutura de Arquivos e Componentes Criados

Todos os arquivos foram criados e compilados dentro do diretório `/app-roomiesapp`:

```text
app-roomiesapp/
├── compose.yaml                                          # Criado: Docker Compose para container PostgreSQL (roomiesapp-db)
├── .env                                                  # Criado: Variáveis de ambiente padrão para o Docker Compose
└── backend/
    ├── pom.xml                                           # Modificado: Adicionado gogather-framework-security
    ├── compose.yaml                                      # Criado: Espelho do Docker Compose para execução via /backend
    ├── .env                                              # Criado: Espelho do arquivo .env
    └── src/
        ├── test/java/com/role/net/roomiesapp/
        │   └── RoomiesAppApplicationTests.java           # Criado: Teste de integração para validação do contexto Spring Boot e JPA
        └── main/
            ├── resources/
            │   ├── application.yaml                      # Criado: Configuração PostgreSQL, importação do secret.yaml e porta 8081
            │   ├── application-local.yaml                # Criado: Configuração H2 (memória) para desenvolvimento local e testes
            │   ├── secret.yaml                           # Criado: Credenciais reais do PostgreSQL (ignorado no git)
            │   └── secret.yaml.example                   # Criado: Modelo seguro para controle de versão
            └── java/com/role/net/roomiesapp/
                ├── RoomiesAppApplication.java            # Existente: Classe principal Spring Boot
                ├── config/
                │   └── SecurityConfig.java               # Criado: Cadeia de filtros de segurança, CORS, rotas públicas e BCrypt
                ├── controller/
                │   └── AuthController.java               # Criado: Endpoints REST (/auth/register, login, refresh, logout, me, change-password)
                ├── dto/
                │   ├── auth/
                │   │   ├── ChangePasswordRequest.java    # Criado: DTO de solicitação de troca de senha
                │   │   ├── LoginRequest.java             # Criado: DTO de login
                │   │   ├── RefreshRequest.java           # Criado: DTO de solicitação de renovação de token
                │   │   ├── RegisterUserRequest.java      # Criado: DTO de registro com validações Jakarta e phoneNumber
                │   │   ├── RegisterUserResponse.java     # Criado: DTO de resposta do registro
                │   │   └── TokenResponse.java            # Criado: DTO contendo par accessToken/refreshToken
                │   ├── error/
                │   │   └── StandardErrorDTO.java         # Criado: DTO padrão para respostas de erro da API
                │   └── user/
                │       └── UserResponse.java             # Criado: DTO de dados do usuário (com phoneNumber)
                ├── entity/
                │   ├── BaseEntity.java                   # Criado: Superclasse JPA com ID, UUID externalId, createdAt e updatedAt
                │   ├── RefreshToken.java                 # Criado: Entidade JPA para armazenamento e controle de tokens de renovação
                │   └── User.java                         # Criado: Entidade JPA de morador implementando UserDetails
                ├── exception/
                │   ├── GlobalExceptionHandler.java       # Criado: Interceptador global de exceções HTTP (ControllerAdvice)
                │   ├── InvalidCredentialsException.java  # Criado: Exceção HTTP 401 para credenciais inválidas
                │   ├── ResourceNotFoundException.java    # Criado: Exceção HTTP 404 para entidades não encontradas
                │   └── UniqueDataAlreadyInUseException.java # Criado: Exceção HTTP 400 para colisão de email ou username
                ├── repository/
                │   ├── RefreshTokenRepository.java       # Criado: Repositório JPA para RefreshToken (inclui deleteByUserId)
                │   └── UserRepository.java               # Criado: Repositório JPA para User (busca por username e email)
                └── service/
                    ├── AuthService.java                  # Criado: Serviço de cadastro, alteração de senha e UserDetailsService
                    ├── TokenService.java                 # Criado: Serviço de geração/revogação de tokens e cookies HTTP
                    └── provider/
                        └── AppSecurityDataProvider.java  # Criado: Implementação do gancho Hot Spot do framework
```

---

## 7. Verificação de Build, Testes e Como Executar

A compilação e os testes de inicialização do contexto do Spring Boot foram executados com sucesso:

1. **Compilação (`mvn clean compile`):**
   * **Resultado:** `BUILD SUCCESS`. Todos os 23 arquivos de código fonte Java foram compilados sem erros.
2. **Execução de Testes (`mvn clean test`):**
   * **Resultado:** `BUILD SUCCESS`. O teste de contexto (`RoomiesAppApplicationTests`) inicializou o banco de dados, construiu o esquema JPA para as tabelas `users` e `refresh_tokens` e validou a injeção do `SecurityFilterChain`, `AuthService` e do framework de segurança.

### Como Executar com PostgreSQL (Via Docker Compose)

1. Suba o container do banco de dados em segundo plano:
   ```bash
   cd app-roomiesapp
   docker compose up -d
   ```
2. Inicie o servidor Spring Boot apontando para o PostgreSQL (porta **8081**):
   ```bash
   cd backend
   mvn spring-boot:run
   ```

### Como Executar com Banco em Memória H2 (Perfil Local)
Caso deseje rodar sem necessidade de subir o Docker:
```bash
cd app-roomiesapp/backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
```
O console do banco H2 estará acessível no navegador em `http://localhost:8081/h2-console`.
