# Transfer API

API REST para transferências entre contas, construída com Java 21 e Spring Boot.

## Arquitetura

- `controller`: recebe requisições HTTP e valida o corpo da transferência.
- `dto`: records `TransferRequest` e `TransferResponse` para entrada e saída da API.
- `service`: executa as regras da transferência e coordena as gravações.
- `domain`: entidades JPA `Account` e `Transaction`, além do enum `TransactionStatus`.
- `repository`: interfaces Spring Data JPA para acesso aos dados.
- `exception`: exceções de negócio e tratamento global de erros em JSON.
- `config`: `DataSeeder` cria as contas iniciais quando a tabela de contas está vazia.

## Garantias para transferências

- **Precisão monetária:** valores são representados por `BigDecimal`, evitando erros de ponto flutuante.
- **Atomicidade:** `@Transactional` mantém débito, crédito e registro da transação na mesma transação de banco de dados. Falhas de negócio ou de persistência desfazem as alterações.
- **Concorrência:** `@Version` na entidade `Account` habilita lock otimista. Conflitos de atualização são respondidos com HTTP `409 Conflict`.
- **Idempotência:** `idempotencyKey` é obrigatório e único no banco. Uma chave já registrada retorna a transação existente sem repetir a movimentação.

## Stack

- Java 21
- Spring Boot 4.2.0, Spring Web MVC, Spring Data JPA e Jakarta Bean Validation
- Maven, usando o Maven Wrapper incluído no projeto
- H2 em memória (`jdbc:h2:mem:fintechdb`)
- Springdoc OpenAPI 2.8.5 e Swagger UI

Como o H2 está configurado em memória, os dados são reiniciados quando a aplicação para. Ao iniciar, o `DataSeeder` cria Alice com `1000.00` e Bob com `500.00` se não houver contas.

## Executar localmente

É necessário ter o JDK 21 instalado. Na pasta `transfer-api`, inicie a aplicação:

```powershell
.\mvnw.cmd spring-boot:run
```

No macOS ou Linux:

```bash
./mvnw spring-boot:run
```

Por padrão, a API inicia em `http://localhost:8080`.

Em ambientes como o Render, a aplicação usa a porta definida pela variável de ambiente `PORT`; se ela não estiver definida, usa `8080`.

A rota raiz (`/`) redireciona para a interface Swagger. Os endpoints REST continuam disponíveis em suas rotas próprias, como `POST /transfers`.

## Testar pelo Swagger UI

1. Com a aplicação em execução, abra [`http://localhost:8080/swagger-ui/index.html`](http://localhost:8080/swagger-ui/index.html).
2. Expanda `POST /transfers` e selecione **Try it out**.
3. Use o corpo abaixo. As contas `1` e `2` correspondem a Alice e Bob em uma base H2 recém-iniciada:

   ```json
   {
     "sourceAccountId": 1,
     "targetAccountId": 2,
     "amount": 75.50,
     "idempotencyKey": "demo-alice-bob-001"
   }
   ```

4. Selecione **Execute**. Uma transferência aceita retorna HTTP `201 Created` e um `TransferResponse` com o ID da transação, as contas, o valor, o status e a data de criação.
5. Para testar a idempotência, envie novamente a requisição com a mesma `idempotencyKey`; a transação já registrada será retornada sem repetir o débito e o crédito.

A especificação OpenAPI também está disponível em [`http://localhost:8080/v3/api-docs`](http://localhost:8080/v3/api-docs).

### Respostas de erro

- `400 Bad Request`: dados inválidos ou contas de origem e destino iguais.
- `404 Not Found`: conta inexistente.
- `409 Conflict`: conflito de lock otimista.
- `422 Unprocessable Content`: saldo insuficiente.

## Executar os testes

Na pasta `transfer-api`:

```powershell
.\mvnw.cmd test
```

No macOS ou Linux:

```bash
./mvnw test
```

## Executar com Docker

O Dockerfile pode ser usado para construir e iniciar a aplicação:

```bash
docker build -t transfer-api .
docker run --rm -p 8080:8080 transfer-api
```

Com o container em execução, acesse o Swagger UI em `http://localhost:8080/swagger-ui/index.html`.
