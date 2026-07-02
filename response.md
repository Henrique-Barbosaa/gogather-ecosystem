# Relatório Técnico: Refatoração Arquitetural do Módulo Billing

O módulo `gogather-framework-billing` foi analisado, reestruturado e refatorado para se comportar como um verdadeiro módulo de framework dentro do ecossistema **GoGather**.

Este documento detalha o diagnóstico inicial, as decisões arquiteturais tomadas (incluindo a aplicação da **Regra de Hollywood** e a refatoração de *Strategy* para *Template Method*), a justificativa sobre o desacoplamento do Spring e o resumo das implementações realizadas.

---

## 1. Análise Diagnóstica do Módulo Billing

### 1.1. Por que não se comportava como um Framework?
Na sua implementação original, o módulo `gogather-framework-billing` consistia basicamente em:
* Uma interface de estratégia: `ExpenseSplitStrategy`;
* Uma classe concreta: `SimpleEqualSplitStrategy`;
* Uma classe de configuração Spring: `BillingAutoConfiguration`;
* DTOs de dados: `Contribution`, `DebtDistribution` e `ParticipantValue`.

Do ponto de vista de engenharia de software e arquitetura de frameworks, essa estrutura **não se comportava como um módulo de framework**, mas sim como uma simples **biblioteca utilitária de cálculo financeiro**.

A principal diferença entre uma biblioteca e um framework reside na **Inversão de Controle (IoC)** — o que chamamos de **Regra de Hollywood ("Don't call us, we'll call you")**:
* **Em uma Biblioteca:** A aplicação consome a biblioteca chamando suas funções diretamente. A aplicação comanda o fluxo de execução (busca os dados no banco, invoca a função de cálculo e depois salva o resultado manualmente).
* **Em um Framework:** O framework fornece a estrutura e o fluxo de controle do ciclo de vida da operação (*Frozen Spot* / Ponto Congelado). É o framework que comanda a execução, convocando o código do usuário/aplicação (*Hot Spot* / Ponto Quente) apenas nos momentos de fornecer dados ou executar customizações específicas.

No modelo anterior, o módulo `billing` apenas expunha o algoritmo de cálculo via `SimpleEqualSplitStrategy`. Faltava o motor de orquestração do fluxo que caracterizava outros módulos do ecossistema, como o `gogather-framework-group` (que possui o `GroupMembershipOrchestrator` atuando como controlador de fluxo do convite de usuários e o `GroupDataProvider` como gancho para o sistema consumidor).

---

## 2. Aplicação da Regra de Hollywood (Inversão de Controle)

Para transformar o `billing` em um verdadeiro módulo de framework, aplicamos os conceitos de arquitetura de pontos quentes e congelados (*Hot Spots & Frozen Spots*):

### 2.1. O Gancho de Dados: `BillingDataProvider` (Hot Spot)
Criamos a interface `BillingDataProvider` no pacote `gogather.framework.billing.core`. Ela atua como o ponto de extensão no qual a aplicação consumidora implementa a comunicação com seu próprio banco de dados ou camada de persistência:
```java
public interface BillingDataProvider {
    Long getTotalCents(String expenseId);
    List<Participant> getParticipants(String expenseId);
    List<Contribution> getContributions(String expenseId);
    void saveDistributions(String expenseId, List<DebtDistribution> distributions);
}
```

### 2.2. O Motor de Orquestração: `BillingOrchestrator` (Frozen Spot)
Criamos a classe `BillingOrchestrator` no pacote `gogather.framework.billing.orchestrator`. O orquestrador assume total controle sobre o ciclo de vida de liquidação e rateio de uma despesa:
```java
public class BillingOrchestrator {
    private final BillingDataProvider dataProvider;
    private final AbstractExpenseSplitter expenseSplitter;

    public List<DebtDistribution> settleExpense(String expenseId) {
        // 1. O framework invoca a aplicação para obter as entradas
        Long totalCents = dataProvider.getTotalCents(expenseId);
        List<Participant> participants = dataProvider.getParticipants(expenseId);
        List<Contribution> contributions = dataProvider.getContributions(expenseId);

        // 2. O framework processa o algoritmo de rateio (Template Method)
        List<DebtDistribution> distributions = expenseSplitter.calculateSplit(totalCents, participants, contributions);

        // 3. O framework invoca a aplicação para persistir o resultado
        dataProvider.saveDistributions(expenseId, distributions);

        return distributions;
    }
}
```
Com essa mudança, a aplicação consumidora não precisa mais gerenciar o fluxo de rateio: basta implementar o `BillingDataProvider` e chamar `billingOrchestrator.settleExpense("id-da-despesa")`. O framework orquestra todo o restante.

---

## 3. Refatoração: De *Strategy* para *Template Method*

### 3.1. Por que o *Strategy* era inadequado neste cenário?
O padrão *Strategy* (`ExpenseSplitStrategy`) tratava o cálculo de rateio como uma caixa preta. Se um desenvolvedor quisesse adicionar uma nova estratégia ao framework (ex: divisão proporcional à renda, divisão por porcentagens ou divisão itemizada), seria obrigado a reescrever:
1. As validações de entrada (checar se a soma das contribuições bate com o total, se a lista não está vazia, etc.);
2. O somatório e agrupamento de quanto cada participante pagou;
3. O cálculo de saldos líquidos e classificação de credores vs. devedores;
4. **O algoritmo ganancioso (*greedy*) de conciliação de dívidas** (o laço `while` que cruza quem tem saldo positivo com quem tem saldo negativo para emitir as transferências `DebtDistribution`).

### 3.2. A Superioridade do *Template Method* para o Rateio de Contas
A análise matemática e conceitual do problema revelou que **as etapas de validação, cálculo de saldo líquido e conciliação de dívidas são 100% invariantes** para qualquer regra de divisão de despesas. A **única** coisa que muda entre um algoritmo igualitário e um algoritmo proporcional é **como calcular a cota devida por cada participante** (*how much each person owes*).

O padrão **Template Method** foi aplicado com perfeição através da nova classe abstrata `AbstractExpenseSplitter`:

```java
public abstract class AbstractExpenseSplitter {

    // Template Method final: garante a estrutura invariante do algoritmo
    public final List<DebtDistribution> calculateSplit(
        Long totalCents, List<Participant> participants, List<Contribution> contributions
    ) {
        validate(totalCents, participants, contributions);
        Map<String, Long> paidAmounts = calculatePaidAmounts(contributions);
        
        // HOOK METHOD (Gancho): única parte que varia entre subclasses
        Map<String, Long> owedAmounts = calculateOwedAmounts(totalCents, participants, contributions);

        List<ParticipantValue> receivers = new ArrayList<>();
        List<ParticipantValue> payers = new ArrayList<>();
        categorizeParticipants(participants, paidAmounts, owedAmounts, receivers, payers);

        return settleDebts(receivers, payers);
    }

    // Métodos essenciais protegidos como comportamento padrão
    protected void validate(...) { ... }
    protected Map<String, Long> calculatePaidAmounts(...) { ... }
    protected void categorizeParticipants(...) { ... }
    protected List<DebtDistribution> settleDebts(...) { ... }

    // Gancho abstrato a ser implementado pelas customizações
    protected abstract Map<String, Long> calculateOwedAmounts(
        Long totalCents, List<Participant> participants, List<Contribution> contributions
    );
}
```

### 3.3. Sobre a Existência da Classe `SimpleEqualSplitStrategy`
Em resposta ao questionamento sobre a necessidade da implementação `SimpleEqualSplitStrategy` existir dentro do framework:
Em arquiteturas de frameworks modernas, adota-se o princípio de **Convenção sobre Configuração (*Convention over Configuration*)**. Embora uma aplicação consumidora (instância do framework) possa criar subclasses customizadas de `AbstractExpenseSplitter` para suas regras de negócio específicas, é uma boa prática da indústria que o framework forneça **pelo menos uma implementação padrão (*plug-and-play*)** para o seu gancho mais comum.
Mantemos a classe `SimpleEqualSplitStrategy` (agora estendendo `AbstractExpenseSplitter`) como a estratégia padrão (*default fallback*) fornecida pelo framework para divisão igualitária. Com o *Template Method*, ela foi reduzida a poucas linhas, contendo exclusivamente a lógica de divisão das cotas individuais:
```java
public class SimpleEqualSplitStrategy extends AbstractExpenseSplitter {
    @Override
    protected Map<String, Long> calculateOwedAmounts(Long totalCents, List<Participant> participants, List<Contribution> contributions) {
        int membersAmount = participants.size();
        long individualQuota = totalCents / membersAmount;
        int remainingCents = Math.toIntExact(totalCents % membersAmount);
        // ... distribui cotas e centavos restantes ...
        return owedAmounts;
    }
}
```

---

## 4. Desacoplamento do Spring e Arquitetura Limpa

Em atenção à excelente observação arquitetural: *"Não sei se acoplar esse framework com o spring é a melhor das estratégias..."* — avaliamos profundamente o design de todo o ecossistema `gogather-framework`.

A abordagem implementada segue o padrão arquitetural de ouro para frameworks na comunidade Java (padrão **Core Agnóstico + Spring Boot Starter / AutoConfiguration**):

1. **Núcleo 100% Agnóstico de Framework (Pure Java):**
   As classes e interfaces fundamentais do framework (`AbstractExpenseSplitter`, `BillingOrchestrator`, `BillingDataProvider`, `SimpleEqualSplitStrategy` e todos os DTOs) **não possuem absolutamente nenhuma anotação, importação ou dependência do Spring (`org.springframework.*`)**.
   * Eles são código Java puro;
   * Podem ser instanciados com um simples `new BillingOrchestrator(provider, splitter)` em qualquer ambiente Java (como Quarkus, Micronaut, Jakarta EE, Android ou aplicações CLI/Desktop sem Spring).

2. **Camada Isolada de Auto-Configuração (`autoconfigure`):**
   O acoplamento com o Spring foi confinado exclusivamente no pacote `gogather.framework.billing.autoconfigure`, através da classe `BillingAutoConfiguration`.
   * Essa classe só é ativada se a aplicação consumidora estiver rodando em Spring Boot (descoberta via arquivo `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`);
   * Ela registra o `BillingOrchestrator` automaticamente quando detecta que a aplicação implementou um `BillingDataProvider` (`@ConditionalOnBean(BillingDataProvider.class)`), economizando código de infraestrutura para desenvolvedores no ecossistema Spring sem contaminar o núcleo do framework.

---

## 5. Resumo das Alterações e Mapeamento de Arquivos

| Arquivo | Ação | Descrição |
| :--- | :--- | :--- |
| `ExpenseSplitStrategy.java` | **DELETADO** | Interface de Strategy substituída pelo padrão Template Method. |
| `AbstractExpenseSplitter.java` | **CRIADO** | Classe abstrata com o Template Method `calculateSplit(...)` e os métodos padrão de validação e conciliação de dívidas. |
| `BillingDataProvider.java` | **CRIADO** | Interface Hot Spot para a aplicação fornecer dados e persistir distribuições (Regra de Hollywood). |
| `BillingOrchestrator.java` | **CRIADO** | Orquestrador Frozen Spot que gerencia o fluxo `settleExpense(...)` (Regra de Hollywood). |
| `SimpleEqualSplitStrategy.java` | **MODIFICADO** | Refatorado para estender `AbstractExpenseSplitter`, eliminando código duplicado e mantendo apenas o cálculo de cotas devidas. |
| `BillingAutoConfiguration.java` | **MODIFICADO** | Configurado para expor `@Bean AbstractExpenseSplitter` padrão e `@Bean BillingOrchestrator` condicional ao provedor de dados. |
| `BillingOrchestratorTest.java` | **CRIADO** | Suíte de testes unitários comprovando o funcionamento do Template Method, de estratégias customizadas anônimas e da Regra de Hollywood no orquestrador. |
| `pom.xml` (billing) | **MODIFICADO** | Adicionada dependência `spring-boot-starter-test` em escopo de teste para validação unitária. |

---

## 6. Validação e Resultados dos Testes

A refatoração foi rigorosamente validada via Maven. Todos os testes executados compilaram sem erros e passaram com sucesso, tanto isoladamente quanto na verificação de impacto no ecossistema global:

1. **Suíte do Módulo (`gogather-framework-billing`):**
   * `testTemplateMethodWithEqualSplit`: Verificou o comportamento padrão da divisão igualitária sobre o Template Method.
   * `testTemplateMethodWithCustomProportionalSplit`: Verificou a extensibilidade do Template Method criando uma estratégia customizada (70%/30%) sem reescrever a conciliação de dívidas.
   * `testHollywoodPrincipleWithOrchestrator`: Comprovou que o `BillingOrchestrator` invoca corretamente o provedor de dados e persiste as distribuições ao final do cálculo.
   * **Resultado:** `Tests run: 3, Failures: 0, Errors: 0, Skipped: 0`

2. **Suíte Geral do Framework (`gogather-framework`):**
   * Executamos `mvn test` na raiz do framework para validar integração com `core`, `group`, `group-jpa`, `group-web` e `security`.
   * **Resultado:** `BUILD SUCCESS` (Nenhuma regressão ou quebra de contrato detectada).
