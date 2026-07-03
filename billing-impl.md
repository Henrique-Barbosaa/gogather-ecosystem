# Implementação do Módulo Financeiro no Tripmaker (Integração com `gogather-framework-billing`)

Este documento detalha a arquitetura, modelagem, decisões de design e implementação completa da gestão financeira e divisão de contas na aplicação **Tripmaker**, utilizando como base o módulo de cobrança (`gogather-framework-billing`) do ecossistema GoGather.

---

## 1. Resumo Executivo e Ideias Integradas

A implementação seguiu estritamente os requisitos solicitados e integrou com sucesso as três ideias propostas na análise inicial:

1. **Ideia 1 - Base de Interface e Padronização (`PixRecipient` no Framework):**  
   Foi criada a interface `gogather.framework.billing.pix.PixRecipient` no framework, definindo os métodos essenciais (`getPixKey()`, `getMerchantName()`, `getMerchantCity()`). Os DTOs e entidades aplicacionais agora implementam essa interface, garantindo que o `PixCodeGenerator` do framework possa ser consumido de forma padronizada em todo o ecossistema.
2. **Ideia 2 - Status de Dívidas Expandido no Framework (`DebtStatus`):**  
   O enum `DebtStatus` no framework foi expandido para conter os estados:
   - `PENDING`: Dívida recém-calculada pelo orquestrador e aguardando pagamento.
   - `AWAITING_CONFIRMATION`: O devedor informou que realizou o pagamento (ex: enviou o Pix).
   - `PAID`: O credor analisou e confirmou o recebimento do valor.
   - `CANCELLED`: Dívida cancelada por acordo mútuo ou remoção de despesa.
3. **Ideia 3 - Rateio Flexível (Grupo Completo ou Participantes Específicos na Aplicação):**  
   A aplicação foi arquitetada para suportar tanto a divisão simples entre todos os membros de uma viagem quanto a divisão granular entre **participantes específicos** de uma despesa. O provedor `TripBillingDataProvider` inspeciona a despesa: se houver participantes definidos para aquela despesa específica, o rateio ocorre exclusivamente entre eles; caso contrário, abrange todos os membros ativos do grupo.

---

## 2. Modificações no Framework (`gogather-framework`)

### 2.1. Interface `PixRecipient` e Atualização de DTOs
* **Arquivo:** `gogather-framework-billing/.../pix/PixRecipient.java`
* Definição do contrato para beneficiários Pix. O record `DebtDistribution` foi estendido com o campo opcional `pixRecipient` e o utilitário de BR Code (`PixCodeGenerator`) foi configurado como um Bean Spring via `BillingAutoConfiguration`.

### 2.2. Expansão de Status (`DebtStatus`)
* **Arquivo:** `gogather-framework-billing/.../dto/DebtStatus.java`
* Substituição dos status simples por um ciclo de vida financeiro completo (`PENDING`, `AWAITING_CONFIRMATION`, `PAID`, `CANCELLED`).

---

## 3. Implementação no Backend do Tripmaker (`app-tripmaker`)

### 3.1. Validação e Cadastro Obrigatório de Chave Pix
Para garantir a segurança financeira e impedir cobranças inválidas, foi implementada uma regra de negócio que exige que qualquer usuário adicionado como **contribuinte (quem pagou a conta)** possua uma chave Pix ativa e configurada.

* **Entidade `PixInfo`:** Implementa `PixRecipient` e armazena chave Pix, nome do beneficiário e cidade, vinculada 1:1 com a entidade `User`.
* **Endpoint de Cadastro:** `PATCH /users/pix`  
  Permite ao usuário autenticado cadastrar ou atualizar seus dados Pix (`RegisterPixKeyRequest`).
* **Trava de Validação no Rateio:** No momento da criação da despesa (`TripBillingService.createExpense`), o sistema verifica se todos os contribuintes possuem chave Pix. Caso contrário, a requisição é bloqueada com erro explicativo:
  > *"O contribuinte [Nome] não possui uma chave Pix cadastrada. É necessário cadastrar a chave Pix antes de adicioná-lo como contribuinte."*

---

### 3.2. Modelagem de Dados da Despesa e Divisão
Foram criadas as seguintes entidades JPA no pacote `com.role.net.tripmaker.entity`:

* **`TripExpense`:** Representa a conta/despesa na viagem. Contém descrição, valor total em centavos (`totalCents`), data, categoria (`ExpenseCategory`) e vínculos com o grupo (`Group`).
* **`TripExpenseContribution`:** Lista os contribuintes que arcaram com o pagamento da despesa e quanto cada um pagou (`amountInCents`).
* **`TripExpenseParticipant`:** Lista os participantes que desfrutaram da despesa (usado no rateio da Ideia 3).
* **`TripDebt`:** A dívida final gerada após o cálculo do framework, ligando `debtor` (devedor) a `creditor` (credor), com valor em centavos e status (`DebtStatus`).

---

### 3.3. Integração com o Orquestrador (`BillingOrchestrator`)
A ponte entre a aplicação Tripmaker e o framework é feita pelo provedor de dados:

* **`TripBillingDataProvider`:** Implementa a interface `gogather.framework.billing.core.BillingDataProvider`.  
  - `getTotalCents(expenseId)`: Retorna o total da despesa em centavos.
  - `getParticipants(expenseId)`: Retorna os participantes da despesa (ou todos os membros do grupo, caso não especificado).
  - `getContributions(expenseId)`: Mapeia os pagadores da despesa para os DTOs `Contribution` do framework.
  - `saveDistributions(expenseId, distributions)`: Recebe o resultado do `SimpleEqualSplitStrategy` calculado pelo framework e salva no banco de dados como instâncias da entidade `TripDebt`.

Quando o usuário chama `POST /groups/{tripId}/expenses`, o serviço `TripBillingService`:
1. Valida permissões e chaves Pix.
2. Salva a despesa inicial.
3. Invoca `billingOrchestrator.settleExpense(expenseId)`.
4. O framework calcula matematicamente quem deve quanto para quem e chama `saveDistributions` para persistir os resultados.

---

### 3.4. Geração de Pix Copia e Cola (BR Code EMVCo)
A geração do código copia e cola para pagamento da dívida é realizada pelo endpoint dedicado:

* **Endpoint:** `GET /groups/{tripId}/expenses/debts/{debtId}/pix-code`
* **Fluxo:**
  1. Verifica se o usuário logado pertence à viagem.
  2. Localiza a dívida e extrai o usuário credor (`creditor`).
  3. Consulta o `PixInfo` do credor.
  4. Aciona o bean `PixCodeGenerator.generatePixCode(creditor.getPixInfo(), debt.getAmountInCents())`.
  5. Retorna o DTO `PixCodeResponse`, contendo a string Pix Copia e Cola padronizada (com cálculo automático do CRC16, identificador GUI do Banco Central, moeda BRL 986 e formatação sem acentos), além dos metadados para exibição na interface (valor, chave, nome e cidade).

---

## 4. Endpoints da API de Cobranças (`ExpenseController` e `UserController`)

| Método | Rota | Descrição |
| :--- | :--- | :--- |
| **PATCH** | `/users/pix` | Cadastra ou atualiza a chave Pix, nome e cidade do beneficiário do usuário autenticado. |
| **POST** | `/groups/{tripId}/expenses` | Cria uma nova despesa, valida as chaves Pix dos pagadores e gera automaticamente as dívidas via framework. |
| **GET** | `/groups/{tripId}/expenses` | Lista todas as despesas de uma viagem (com suas contribuições, participantes e dívidas resultantes). |
| **GET** | `/groups/{tripId}/expenses/debts` | Lista todas as dívidas geradas na viagem, permitindo ver quem deve a quem e os status. |
| **PATCH** | `/groups/{tripId}/expenses/debts/{debtId}/status` | Atualiza o status da dívida (`PENDING` -> `AWAITING_CONFIRMATION` pelo devedor; `AWAITING_CONFIRMATION` -> `PAID` pelo credor). |
| **GET** | `/groups/{tripId}/expenses/debts/{debtId}/pix-code` | Gera a string Pix Copia e Cola (BR Code) e metadados para o devedor pagar o credor da dívida. |

---

## 5. Validação e Testes Executados

* **Compilação Geral do Framework (`gogather-framework`):**  
  Todos os 9 módulos do ecossistema (`core`, `billing`, `security`, `group`, `group-jpa`, `group-web`, `polling`, `chat`) foram construídos e instalados no repositório local Maven (`~/.m2/repository`) sem erros.
* **Compilação e Testes da Aplicação (`app-tripmaker`):**  
  As dependências foram atualizadas no `pom.xml`, todas as entidades JPA e controladores foram integrados e a suíte de testes unitários/de integração (`mvn test`) finalizou com **sucesso (BUILD SUCCESS - 0 falhas, 0 erros)**.

A arquitetura financeira do Tripmaker está sólida, escalável e 100% aderente aos padrões modulares do ecossistema GoGather.
