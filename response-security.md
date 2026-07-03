# Relatório Técnico: Análise e Refatoração Arquitetural do Módulo Security (`gogather-framework-security`)

Este documento apresenta a análise diagnóstica, as decisões de design arquitetural e a refatoração completa realizada no módulo `gogather-framework-security`, alinhando-o aos padrões de engenharia do ecossistema **GoGather** e à **Regra de Hollywood ("Don't call us, we'll call you")**.

O objetivo primordial desta refatoração é garantir que o módulo atue como um **Ponto Fixo / Congelado (*Frozen Spot*)** robusto e reutilizável para a criação de novos aplicativos no ecossistema (bem como a integração com o app original), delegando às aplicações consumidoras apenas os **Pontos Quentes (*Hot Spots*)** essenciais.

---

## 1. Análise Diagnóstica do Módulo Original

### 1.1. Por que não respeitava o Princípio de Hollywood e divergia dos outros módulos?
Na sua implementação original, o módulo `gogather-framework-security` possuía apenas quatro artefatos dispostos de forma estruturalmente incompatível com o restante do framework (`group`, `billing`, `chat`, `polling`):
* `SecurityAutoConfiguration.java` (alocado incorretamente no pacote `.config`);
* `JwtAuthenticationFilter.java`;
* `TokenService.java`;
* `JwtProperties.java`.

A análise dessa estrutura revelou falhas críticas de design arquitetural:

1. **Ausência da Regra de Hollywood (Inversão de Controle):**
   Um framework se diferencia de uma biblioteca pela Inversão de Controle. Em um framework bem desenhado, o núcleo assume o controle do fluxo de execução (*Frozen Spot*) e convoca o código da aplicação consumidora (*Hot Spot*) apenas nos momentos de fornecer dados ou customizações.
   No modelo anterior, o `JwtAuthenticationFilter` injetava diretamente a interface `UserDetailsService` do Spring Security e executava a lógica de autenticação dentro do método `doFilterInternal(...)`. Não existia um ponto de extensão do próprio framework para a aplicação fornecer suas credenciais ou regras de autorização, forçando o acoplamento direto com detalhes do Spring Security no filtro web.

2. **Falta de um Orquestrador Centrado no Domínio:**
   Em todos os outros módulos do ecossistema GoGather, existe um orquestrador central (como `GroupMembershipOrchestrator`, `BillingOrchestrator`, `ChatOrchestrator` e `PollingOrchestrator`) que encapsula a regra de negócio do framework. O módulo de segurança carecia dessa entidade, deixando a lógica de verificação de token e autenticação dispersa e acoplada ao encadeamento de filtros HTTP.

3. **Inconsistência de Pacotes e Acoplamento:**
   Enquanto os demais módulos isolam a integração do Spring Boot no pacote `gogather.framework.<modulo>.autoconfigure`, o módulo de segurança colocou sua configuração em `gogather.framework.security.config`, além de não possuir o pacote `.core` para contratos de domínio e não ter **nenhum teste unitário implementado**.

---

## 2. A Segurança como Ponto Congelado (*Frozen Spot*) e a Regra de Hollywood

Tendo em vista que este framework será a base arquitetural para a criação de **dois novos aplicativos** (além de padronizar o `app-gogather-original`), a segurança da informação e o ciclo de vida de autenticação precisam funcionar como um alicerce imutável e padronizado.

### 2.1. O que é o Frozen Spot (Ponto Fixo) de Segurança?
O fluxo geral de segurança web em uma API REST moderna é invariavelmente o mesmo para qualquer aplicativo do ecossistema:
1. Interceptação da requisição HTTP;
2. Extração do token JWT (seja de um cookie `accessToken` ou do cabeçalho `Authorization: Bearer <token>`);
3. Verificação de assinatura criptográfica HMAC256 e checagem de expiração temporal do JWT;
4. Extração da identificação principal do usuário (*subject*);
5. Consulta à base de dados para certificar a existência da conta e validar se ela está habilitada/ativa;
6. Registro da autenticação no contexto da requisição (`SecurityContextHolder`).

Esse fluxo foi encapsulado na nova classe **`SecurityOrchestrator`** (no pacote `gogather.framework.security.orchestrator`). A aplicação consumidora não precisa (e nem deve) reescrever a manipulação de tokens ou filtros HTTP; o framework gerencia tudo.

### 2.2. O Gancho da Aplicação: `SecurityDataProvider` (Hot Spot)
Para aplicar a Regra de Hollywood (*"Não nos chame, nós chamaremos você"*), criamos a interface `SecurityDataProvider` no pacote `gogather.framework.security.core`. 

Cada novo aplicativo criado com o framework terá seu próprio banco de dados, tabelas de usuários ou provedores de identidade. Para se conectar ao motor de segurança do framework, basta a aplicação implementar este gancho:

```java
package gogather.framework.security.core;

import java.util.Optional;
import org.springframework.security.core.userdetails.UserDetails;

public interface SecurityDataProvider {
    /**
     * O framework chama a aplicação para obter os dados do usuário a partir do identificador (e-mail, ID, username).
     */
    Optional<UserDetails> loadUserByUsername(String username);

    /**
     * Gancho opcional para verificar se a conta do usuário está autorizada/ativa para autenticação.
     * O comportamento padrão valida as flags de status do UserDetails.
     */
    default boolean isUserAuthorized(UserDetails userDetails) {
        return userDetails != null && userDetails.isEnabled() && userDetails.isAccountNonExpired() 
                && userDetails.isAccountNonLocked() && userDetails.isCredentialsNonExpired();
    }
}
```

### 2.3. O Motor de Orquestração: `SecurityOrchestrator`
Com a separação de papéis, o orquestrador assume o controle absoluto da verificação e autenticação, coordenando o `TokenService` e chamando o gancho `SecurityDataProvider`:

```java
public class SecurityOrchestrator {
    private final TokenService tokenService;
    private final SecurityDataProvider dataProvider;

    public Optional<Authentication> authenticateToken(String token) {
        if (token == null || token.isBlank()) return Optional.empty();

        // 1. O framework valida a assinatura criptográfica e expiração
        Optional<String> subjectOpt = tokenService.validateTokenAndGetSubject(token);
        if (subjectOpt.isEmpty()) return Optional.empty();

        // 2. O framework invoca a aplicação (Hot Spot) para buscar o usuário
        String username = subjectOpt.get();
        Optional<UserDetails> userDetailsOpt = dataProvider.loadUserByUsername(username);

        // 3. O framework verifica autorização e monta o contexto de segurança
        if (userDetailsOpt.isPresent()) {
            UserDetails userDetails = userDetailsOpt.get();
            if (dataProvider.isUserAuthorized(userDetails)) {
                return Optional.of(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
            }
        }
        return Optional.empty();
    }
}
```

---

## 3. Refatoração e Desacoplamento dos Componentes

### 3.1. Evolução do `TokenService`
Anteriormente, o `TokenService` exigia obrigatoriamente um objeto `UserDetails` do Spring Security para gerar um JWT. Ele foi refatorado para ser mais flexível e reutilizável:
* Adicionado o método `generateToken(String subject)`, permitindo a emissão de tokens agnósticos baseados em qualquer identificador de usuário;
* Mantido o método `generateToken(UserDetails userDetails)` como sobrecarga de conveniência que delega para o novo método, preservando compatibilidade.

### 3.2. Limpeza do `JwtAuthenticationFilter`
O filtro HTTP foi reestruturado para atuar exclusivamente como um adaptador de infraestrutura web. Ele não possui mais nenhuma dependência de acesso a dados ou serviços de token; sua única função é extrair o cabeçalho/cookie e delegar a decisão de autenticação ao `SecurityOrchestrator`:
```java
@Override
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
        throws ServletException, IOException {
    String token = recoverToken(request);
    if (token != null) {
        securityOrchestrator.authenticateToken(token)
            .ifPresent(auth -> SecurityContextHolder.getContext().setAuthentication(auth));
    }
    filterChain.doFilter(request, response);
}
```

### 3.3. Padronização e Adaptador de Auto-Configuração
1. **Pacote Correto:** A classe `SecurityAutoConfiguration` foi movida para o pacote `gogather.framework.security.autoconfigure`, padronizando-a em relação aos módulos `billing` e `group`. O arquivo de registro `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` foi atualizado em concordância.
2. **Remoção de Redundância:** A anotação `@Configuration` foi removida da classe `JwtProperties`, mantendo apenas `@ConfigurationProperties(prefix = "framework.security.jwt")` e `@Data`, eliminando avisos e dupla instanciação de beans em Spring Boot.
3. **Adaptador de Compatibilidade Retroativa:**
   Para garantir que aplicativos legados ou construídos no modelo padrão do Spring (que definem um `@Bean UserDetailsService`) possam utilizar o framework sem precisar escrever uma nova classe para implementar `SecurityDataProvider`, adicionamos um bean adaptador inteligente na auto-configuração:
   ```java
   @Bean
   @ConditionalOnBean(UserDetailsService.class)
   @ConditionalOnMissingBean(SecurityDataProvider.class)
   public SecurityDataProvider defaultUserDetailsServiceAdapter(UserDetailsService userDetailsService) {
       return new SecurityDataProvider() {
           @Override
           public Optional<UserDetails> loadUserByUsername(String username) {
               try {
                   return Optional.ofNullable(userDetailsService.loadUserByUsername(username));
               } catch (Exception ex) {
                   return Optional.empty();
               }
           }
       };
   }
   ```
   Dessa forma, o framework se adapta automaticamente tanto a novas aplicações (que implementam `SecurityDataProvider` diretamente) quanto a aplicações existentes (que possuem `UserDetailsService`).

---

## 4. Resumo das Alterações e Mapeamento de Arquivos

| Arquivo / Componente | Ação | Descrição |
| :--- | :--- | :--- |
| `SecurityDataProvider.java` | **CRIADO** | Interface *Hot Spot* no pacote `.core` para a aplicação fornecer acesso aos dados e status do usuário (Regra de Hollywood). |
| `SecurityOrchestrator.java` | **CRIADO** | Orquestrador *Frozen Spot* no pacote `.orchestrator` que gerencia todo o ciclo de verificação, validação e autenticação de tokens. |
| `TokenService.java` | **MODIFICADO** | Refatorado para aceitar `String subject` ou `UserDetails`, melhorando a flexibilidade e desacoplamento do serviço JWT. |
| `JwtProperties.java` | **MODIFICADO** | Removida a anotação `@Configuration` redundante em favor das boas práticas do `@ConfigurationProperties`. |
| `JwtAuthenticationFilter.java` | **MODIFICADO** | Desacoplado do `UserDetailsService` e `TokenService`; agora delega a lógica de autenticação 100% para o `SecurityOrchestrator`. |
| `SecurityAutoConfiguration.java` | **MOVIDO / MODIFICADO** | Movido do pacote `.config` para `.autoconfigure`. Adicionado `@Bean SecurityOrchestrator` e o `@Bean defaultUserDetailsServiceAdapter` para compatibilidade com Spring Security. |
| `AutoConfiguration.imports` | **MODIFICADO** | Atualizado o caminho da classe de auto-configuração no diretório `META-INF/spring/`. |
| `TokenServiceTest.java` | **CRIADO** | Suíte de testes unitários em `src/test/java` validando a criação, assinatura, expiração e validação de tokens JWT. |
| `SecurityOrchestratorTest.java` | **CRIADO** | Suíte de testes unitários comprovando o funcionamento da Regra de Hollywood, injeção de provedores anônimos, autenticação com sucesso e rejeição de usuários inativos/desabilitados. |
| `pom.xml` (security) | **MODIFICADO** | Adicionadas as dependências `spring-boot-starter-test` e `spring-security-test` em escopo `test`. |

---

## 5. Validação e Resultados dos Testes

A refatoração foi validada de forma exaustiva através de compilação e execução de testes automatizados via Maven. O módulo, que antes não possuía cobertura de testes, agora conta com uma suíte dedicada que comprova o isolamento dos contratos e a eficácia da Inversão de Controle.

### 5.1. Suíte do Módulo (`gogather-framework-security`)
1. **`TokenServiceTest`**: Validou emissão e validação de JWT via `subject` puro e via `UserDetails`, além de certificar a rejeição de tokens malformados/inválidos.
2. **`SecurityOrchestratorTest`**: Demonstrado o Princípio de Hollywood injetando implementações anônimas de `SecurityDataProvider`. Comprovou que:
   * O orquestrador autentica com sucesso quando o *Hot Spot* localiza e autoriza o usuário;
   * O orquestrador recusa a autenticação quando o *Hot Spot* não localiza o usuário;
   * O orquestrador recusa automaticamente a autenticação quando a flag `enabled=false` é retornada pelo *Hot Spot* (usuário desabilitado/bloqueado).
* **Resultado:** `Tests run: 6, Failures: 0, Errors: 0, Skipped: 0` (`BUILD SUCCESS` em **9.58s**).

### 5.2. Suíte Global do Ecossistema (`gogather-framework`)
Executado `mvn test` na raiz do ecossistema do framework para comprovar a interoperabilidade com todos os demais módulos (`core`, `billing`, `group`, `group-jpa`, `group-web`, `polling` e `chat`).
* **Resultado do Reator Maven:**
  ```
  [INFO] Reactor Summary for GoGather Framework 1.0.0-SNAPSHOT:
  [INFO] GoGather Framework ................................. SUCCESS
  [INFO] GoGather Framework - Core .......................... SUCCESS
  [INFO] GoGather Framework - Billing ....................... SUCCESS
  [INFO] GoGather Framework - Security ...................... SUCCESS
  [INFO] GoGather Framework - Group ......................... SUCCESS
  [INFO] GoGather Framework - Group JPA ..................... SUCCESS
  [INFO] GoGather Framework - Group Web ..................... SUCCESS
  [INFO] GoGather Framework - Polling ....................... SUCCESS
  [INFO] GoGather Framework - Chat .......................... SUCCESS
  [INFO] BUILD SUCCESS
  ```
  Nenhuma regressão, quebra de contrato ou incompatibilidade de auto-configuração foi detectada em todo o ecossistema do framework.
