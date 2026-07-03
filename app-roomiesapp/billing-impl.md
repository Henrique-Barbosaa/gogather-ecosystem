# Módulo Financeiro e Divisão de Contas - RoomiesApp (`billing-impl`)

Este documento descreve em detalhes a arquitetura, as regras de negócio, a modelagem de dados e os endpoints REST da implementação do módulo financeiro (Billing) do **RoomiesApp**, estendendo o `gogather-framework-billing`.

---

## 1. Visão Geral da Arquitetura

O sistema financeiro do **RoomiesApp** foi planejado especificamente para o contexto de moradias compartilhadas e repúblicas ("Roomies"), onde a gestão de despesas se divide em dois grandes cenários:
1. **Despesas Normais/Imprevisíveis**: Compras de supermercado, delivery, consertos pontuais na casa ou itens adquiridos em conjunto.
2. **Despesas Recorrentes/Fixas**: Aluguel, condomínio, IPTU, contas de luz, água, internet, serviços de streaming ou taxas de manutenção periódica (mensais, anuais ou intervalos customizados em dias).

Para garantir o reuso de algoritmos complexos de divisão de contas, o RoomiesApp integra-se ao motor central do ecossistema GoGather através do módulo `gogather-framework-billing`, implementando o padrão de provedor de dados (`BillingDataProvider`).

```
+-----------------------------------------------------------------------------+
|                               RoomiesApp API                                |
|  +--------------------+      +--------------------+      +---------------+  |
|  | BillingController  | ---> | RoomiesBillingServ | ---> | UserPixService|  |
|  +--------------------+      +--------------------+      +---------------+  |
|                                        |                                    |
|                                        v                                    |
|                         +------------------------------+                    |
|                         |  RoomiesBillingDataProvider  |                    |
|                         +------------------------------+                    |
+----------------------------------------|------------------------------------+
                                         | implements
                                         v
+-----------------------------------------------------------------------------+
|                          gogather-framework-billing                         |
|  +---------------------+      +---------------------+      +-------------+  |
|  | BillingDataProvider | <--- | BillingOrchestrator | ---> | PixProvider |  |
|  +---------------------+      +---------------------+      +-------------+  |
+-----------------------------------------------------------------------------+
```

---

## 2. Tipos de Contas e Lógica de Divisão

O modelo de contas da casa (`HouseBill`) suporta duas modalidades de rateio com regras distintas de credores e devedores:

### A. Contas Normais (`BillType.NORMAL`)
- **Característica**: Representam gastos pontuais e variáveis (ex: compra no supermercado por um morador para a casa toda).
- **Atribuição do Credor**: O morador que realizou o gasto é definido como **Credor/Contribuidor** (`contributor`). O sistema **impede** o cadastro ou atribuição de um credor que não tenha uma chave Pix cadastrada em seu perfil.
- **Participantes/Devedores**: Uma lista de moradores (`participants`) entre os quais o valor total será dividido.
- **Divisão**: Utiliza a estratégia padrão de rateio igualitário do framework (`EqualExpenseSplitter`). Se o próprio contribuidor estiver entre os participantes, a sua parte é subtraída automaticamente no rateio líquido, gerando dívidas (`HouseDebt`) apenas para os demais devedores perante o contribuidor.

### B. Contas Recorrentes (`BillType.RECURRING`)
- **Característica**: Representam despesas fixas ou periódicas do espaço de convívio (ex: conta de energia elétrica, aluguel).
- **Atribuição Opcional de Contribuidor**: Diferente das contas normais, o pagador/contribuidor inicial é **totalmente opcional**. Isso ocorre porque:
  - Uma conta como luz ou condomínio pode chegar da imobiliária ou concessionária sem um pagador designado logo no início.
  - O valor pode ser posteriormente assumido por um morador ou descontado no balanço financeiro global da casa.
- **Rateio e Dívidas Sintéticas**:
  - Quando um `contributor` é atribuído (assumindo o pagamento da fatura recorrente), geram-se dívidas normais dos demais moradores com esse credor.
  - Quando **não** há um `contributor` definido (`contributor == null`), o sistema gera **Dívidas Coletivas/Sintéticas** perante a casa (com `creditor == null`), representando a obrigação de cada morador de contribuir para o montante geral daquela fatura recorrente.
- **Periodicidade**: Suporta intervalos padronizados (`MONTHLY`, `ANNUAL`) e também intervalos personalizados (`CUSTOM` com `customIntervalDays`), possibilitando ciclos flexíveis (ex: faxina quinzenal a cada 15 dias).

---

## 3. Integração e Validação Pix (Copia e Cola)

O pagamento de dívidas entre moradores no RoomiesApp é facilitado via **Pix Copia e Cola (BR Code / EMV QRCPS)**, operado de forma integrada com a especificação do Banco Central:

1. **Obrigatoriedade de Chave Pix**:
   - Através do `UserPixService`, qualquer tentativa de criar uma conta normal ou atribuir um credor a uma conta recorrente verifica se o morador possui dados bancários Pix cadastrados (`user_pix_info`).
   - Caso o usuário não tenha Pix cadastrado, a API retorna erro de validação (`400 Bad Request` ou `404 Not Found` para chave não cadastrada), incentivando o pré-cadastro.

2. **Geração do BR Code**:
   - A entidade `PixInfo` estende `BaseEntity` e implementa a interface do framework `gogather.framework.billing.pix.PixRecipient`, fornecendo chave Pix (`pixKey`), nome do beneficiário (`merchantName`) e cidade (`merchantCity`).
   - Ao requisitar o código Pix de uma dívida (`GET /api/v1/billing/debts/{id}/pix`), o `RoomiesBillingService` invoca o `PixProvider` do framework, passando o valor exato da dívida em centavos (`amountInCents`) e o código identificador da dívida como identificador de transação (`txid`).
   - O payload gerado (formato padrão EMV QRCPS "000201...") é retornado ao usuário para cópia e também armazenado em cache (`pixCodeCache`) na entidade `HouseDebt`.

---

## 4. Modelagem de Dados e Entidades (JPA)

As entidades do módulo financeiro relacionam-se diretamente com o modelo de usuários e grupos da casa:

```
+------------------+         +-------------------+         +------------------+
|      Group       | 1     N |     HouseBill     | 1     N |    HouseDebt     |
| (households)     | <------ | (house_bills)     | <------ | (house_debts)    |
+------------------+         +-------------------+         +------------------+
                               |               |             |              |
                             N |               | N           | N          N |
                               v               v             v              v
                             +-------------------+         +------------------+
                             |       User        | <------ |     User         |
                             |  (contributor)    |         | (debtor/creditor)|
                             +-------------------+         +------------------+
                                       ^ 1
                                       |
                                       | 1
                             +-------------------+
                             |      PixInfo      |
                             | (user_pix_info)   |
                             +-------------------+
```

- **`HouseBill`**: Armazena título, descrição, valor total em centavos (`totalCents`), tipo de conta (`NORMAL` / `RECURRING`), intervalo de recorrência, data de vencimento (`dueDate`), grupo (casa), credor (`contributor`) e lista de moradores participantes (`participants`).
- **`HouseDebt`**: Representa a fração da dívida gerada após o rateio. Armazena a conta de origem, morador devedor (`debtor`), morador credor (`creditor`, podendo ser nulo em contas fixas da casa), valor em centavos, status de pagamento (`PENDING`, `AWAITING_CONFIRMATION`, `PAID`, `CANCELLED`) e cache do código Pix.
- **`PixInfo`**: Tabela 1:1 com `User` contendo os dados do recebedor para emissão do Pix.
- **`User`**: Adaptado para estender `BaseUser` do framework, sincronizando automaticamente propriedades como `name` e `externalId` para compatibilidade com o motor de rateio do ecossistema.

---

## 5. Referência dos Endpoints REST (`BillingController`)

Todas as rotas de faturamento estão disponíveis sob o prefixo `/api/v1/billing` e exigem autenticação via Token JWT Bearer.

### A. Gestão de Faturas da Casa
| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| **POST** | `/api/v1/billing/groups/{groupId}/bills` | Cria uma nova fatura (Normal ou Recorrente) e realiza o rateio automático entre os participantes. |
| **GET** | `/api/v1/billing/groups/{groupId}/bills` | Lista todas as faturas cadastradas para uma determinada casa/grupo. |
| **GET** | `/api/v1/billing/bills/{billId}` | Retorna os detalhes completos de uma fatura, incluindo participantes e metadados de recorrência. |
| **PUT** | `/api/v1/billing/bills/{billId}` | Atualiza dados de uma fatura (título, descrição, data de vencimento ou intervalo). |

#### Exemplo de Payload: Criar Conta Normal (`POST /api/v1/billing/groups/{groupId}/bills`)
```json
{
  "title": "Compras Supermercado do Mês",
  "description": "Itens de limpeza e mantimentos básicos",
  "totalCents": 34500,
  "billType": "NORMAL",
  "contributorExternalId": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
  "participantExternalIds": [
    "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
    "f6e5d4c3-b2a1-0d9c-8b7a-6f5e4d3c2b1a",
    "11223344-5566-7788-9900-aabbccddeeff"
  ]
}
```

#### Exemplo de Payload: Criar Conta Recorrente sem Credor (`POST /api/v1/billing/groups/{groupId}/bills`)
```json
{
  "title": "Conta de Luz - Concessionária",
  "description": "Fatura mensal de energia elétrica",
  "totalCents": 18000,
  "billType": "RECURRING",
  "recurrenceInterval": "MONTHLY",
  "dueDate": "2026-08-10",
  "contributorExternalId": null,
  "participantExternalIds": [
    "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
    "f6e5d4c3-b2a1-0d9c-8b7a-6f5e4d3c2b1a"
  ]
}
```

---

### B. Gestão de Dívidas e Rateios (`Debts`)
| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| **GET** | `/api/v1/billing/bills/{billId}/debts` | Lista todas as dívidas individuais geradas a partir do rateio de uma fatura. |
| **GET** | `/api/v1/billing/debts/my` | Retorna todas as dívidas onde o morador autenticado é o devedor (`debtor`). |
| **GET** | `/api/v1/billing/credits/my` | Retorna todas as dívidas onde o morador autenticado é o credor a receber (`creditor`). |
| **PATCH** | `/api/v1/billing/debts/{debtId}/status?status=PAID` | Atualiza o status de pagamento da dívida (`PENDING`, `AWAITING_CONFIRMATION`, `PAID`, etc.). |

---

### C. Pagamento Pix
| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| **GET** | `/api/v1/billing/debts/{debtId}/pix` | Valida o recebedor e gera o código Pix Copia e Cola (BR Code) e QR Code Payload para quitação da dívida. |

#### Exemplo de Resposta: Pix Copia e Cola (`GET /api/v1/billing/debts/{debtId}/pix`)
```json
{
  "debtExternalId": "99887766-5544-3322-1100-ffeeddccbbaa",
  "amountInCents": 11500,
  "pixCopyAndPaste": "00020126580014br.gov.bcb.pix0136a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d5204000053039865405115.005802BR5915João Morador6009SÃO PAULO62190515ROOMIES998877666304E1A2",
  "qrCodeBase64": null
}
```

---

## 6. Sincronização de Entidades com o Framework

Para que o motor central de divisão de despesas do ecossistema (`gogather-framework-billing`) opere sem acoplamento direto com as tabelas específicas do RoomiesApp, o sistema implementa a ponte de dados no serviço `RoomiesBillingDataProvider`:
- **`getParticipant(String participantId)`**: Localiza o `User` pelo seu UUID (`externalId`) e o encapsula como `Participant` do framework.
- **`saveDebts(Expense expense, List<Debt> calculatedDebts)`**: Recebe a lista calculada pelo algoritmo de divisão (`AbstractExpenseSplitter`) e converte cada item em registros persistentes da entidade `HouseDebt` no banco de dados do RoomiesApp.

Esta arquitetura garante modularidade, permitindo futuras evoluções no algoritmo de divisão (como divisões por cotas proporcionais ou por consumo individual) sem alterar a camada de controle ou as entidades de domínio da moradia.
