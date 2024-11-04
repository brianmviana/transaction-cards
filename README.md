# Cards Transaction

## Descrição

Autorizador HTTP de transações de cartão, em varios leveis diferentes, desde um autorizado simples até um autorizador completo com Lock.

## Tecnologias Usadas

- **Linguagens**: Kotlin
- **Frameworks**: Spring Boot
- **Banco de Dados**: DynamoDB, Redis
- **Testes**: Kotest, MockK
- **Gerenciamento de Dependências**: Maven
- **Infraestutura**: Docker

## Funcionalidades

- **Autorização de Transações**
- **Gerenciamento de Cartões**
- **Integração com Merchants**
- **Persistência de Dados**
- **Bloqueio de transações**

## Estrutura do Projeto

Estrutura de diretórios do projeto (principais pacotes):

```markdown
app
 ├─ src 
 |   ├── main
 │   │   ├── kotlin
 │   │   │   ├── br.com.caju.card
 │   │   │   │   ├── application (Pacote onde ficam centralizado o fluxo da aplicação)
 │   │   │   │   ├── adapter (Classes de entrada e saida de dados da aplicação)
 │   │   │   │   ├── domain (Entidades de domínio)
 │   │   │   │   └── common (Utilitários e classes gerais da aplicação)
 │   │   └── resources (Pacote das propriedades e configurações da aplicação)
 │   └── test (Pacote de testes unitários da aplicação)
 ├─ ci
 │   └─ localstack
 │       └─ make-dynamodb.sh (script para criar e preencher o banco de dados local)
 ├── docker-compose.yml
 └── pom.xml
```

## Pre-requisitos

- Git
- Docker

## Instalação

Passo a passo para executar o projeto localmente:

1. Clone o repositório:

```bash
git clone https://github.com/brianmviana/transaction-cards.git
cd transaction-cards
cd app
```

2. Instale as dependências usando Maven:

```bash
mvn clean install
```

3. Execute o projeto com docker:

```bash
docker-compose up -d --build
```

## Testes Unitários

Para executar os testes use o comando maven:

```bash
  mvn test
```

## Uso

É possível executar os teste local pelo swagger acessando a url abaixo.

```bash
localhost:8080/swagger-ui.html
```
Outra alternativa para realizar os teste das APIs seria por meio de uma ferramente, postman ou insomnia.
Deixei collections para ambas as ferramentas disponíveis na pasta abaixo:

```bash
docs/collections
```


## Endpoints:

### L1. Autorizador simples

#### URL
- **/api/transactions/l1**

#### Descrição

- O autorizador simples funciona da seguinte forma:
  -  Recebe a transação
  -  Usa apenas a MCC para mapear a transação para uma categoria de benefícios
  -  Aprova ou rejeita a transação
  -  Caso a transação seja aprovada, o saldo da categoria mapeada deverá ser diminuído no valor da transação.

#### cURL

```bash
curl --request POST \
  --url http://localhost:8080/api/transactions/l1 \
  --header 'Content-Type: application/json' \
  --data '{
	"accountId": "123",
	"totalAmount": 10,
	"mcc": "2521",
	"merchant": "PADARIA DO ZE               SAO PAULO BR"
}'
```

### L2. Autorizador com fallback

#### URL:

- **/api/transactions/l2**

#### Descrição:

- Para despesas não relacionadas a benefícios, exite outra categoria, chamada CASH.
- O autorizador com fallback funciona como o autorizador simples, com a seguinte diferença:
  - Se a MCC não puder ser mapeado para uma categoria de benefícios
    - Verifica o saldo de CASH e se for suficiente, debita esse saldo
  - Se o saldo da categoria fornecida não for suficiente para pagar a transação inteira,
    - Verifica o saldo de CASH e se for suficiente, debita esse saldo

#### cURL

```bash
curl --request POST \
  --url http://localhost:8080/api/transactions/l2 \
  --header 'Content-Type: application/json' \
  --data '{
	"accountId": "123",
	"totalAmount": 100.00,
	"mcc": "5812",
	"merchant": "PADARIA DO ZE               SAO PAULO BR"
}'
```

### L3. Dependente do comerciante

#### URL:

- **/api/transactions/l3**

#### Descrição:

- As vezes, os MCCs estão incorretos e uma transação deve ser processada conforme os dados do comerciante.
  - Nesse cenario, existe um mecanismo que substituir MCCs com base no nome do comerciante cadastrado
    - O nome do comerciante tem maior precedência sobre as MCCs.
- Ex:
  - O comeciante "**PADARIA DO ZE               SAO PAULO BR**" está cadastrado com o MCC "***5812***"
  - Então, caso solicite alguma transação para esse comerciante nesse endpoint para outro MCC
  - O sistema ira utilizar o MCC cadastrado "***5812***"

#### cURL

```bash
curl --request POST \
  --url http://localhost:8080/api/transactions/l3 \
  --header 'Content-Type: application/json' \
  --data '{
	"accountId": "123",
	"totalAmount": 100.00,
	"mcc": "1234",
	"merchant": "PADARIA DO ZE               SAO PAULO BR"
}'
```

### L4. Autorizador com Lock de transação

#### URL:

- **/api/transactions/l4**

#### Descrição:

- Existe um cenario pouco provável, mas que pode ocorrer, onde um mesmo cartão pode ser utilizados em 2 ou mais serviços online diferentes ao mesmo tempo.
- Para evitar esse cenario onde ocorra 2 ou mais transações simutanteas, foi implementado um mecanimos de ***LOCK*** da transação.
  - Esse mecanismo, funciona da seguinte forma.
    - O ***LOCK*** deve conter uma chave unica que idenfitica o recuros que iremos travar.
      - No cenario em questão, utilizamos o *número da conta*, pois uma conta so pode realizar uma trasação por vez.
    - Quando uma transação é iniciada, o primeiro passo deve ser ligar o ***LOCK***
      - Caso consiga executar o ***LOCK***, a transação pode seguir normalmente
        - Ao final da transação, é liberado o ***LOCK***
      - Caso contrario, já existe uma trasanção sendo executada, então devemos tratar o erro conforme as regras de negócio.
    - O ***LOCK*** sempre deve ser liberado ao final da transação
    - Caso tenha algum erro no processamento e a transação não consegui liberar o ***LOCK***, não tem problema, pois existe o ***LOCK*** implementa um mecanismo de timeout para que o recurso não fique travado infinitamente.
    
#### LOCK:

- Para o mecanismo de ***LOCK*** implementado, utilizamos o ***Redis**.
- A escolha do ***Redis***, se deu, pois o ***Redis*** é um banco de dados em memória com uma perfomace muito alta.
  - Geralmente o ***Redis*** é ultilizado para cache de dados, porem tambem se encaixa perfeitamente nesse cenario de lock.
- Embora o banco de dados para armazenamento escolhido foi o DynamoDB e ele tambem é um forte candidado a implementação do ***LOCK***.
  - O ***Redis*** tem um custo menor além de um pouco mais de performace nesse contexto.

#### cURL

```bash
curl --request POST \
  --url http://localhost:8080/api/transactions/l4 \
  --header 'Content-Type: application/json' \
  --data '{
	"accountId": "123",
	"totalAmount": 100.00,
	"mcc": "1234",
	"merchant": "PADARIA DO ZE               SAO PAULO BR"
}'
```

## Desenvolvedor

- [Brian Marques Viana](https://www.linkedin.com/in/brianmviana/)
  - Github: [https://github.com/brianmviana](https://github.com/brianmviana)
  - LinkedIn: [https://www.linkedin.com/in/brianmviana/](https://www.linkedin.com/in/brianmviana/)

