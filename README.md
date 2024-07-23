# Translator Document Management API

API para gerenciamento de tradutores e documentos conforme o desafio proposto.

## Tecnologias Utilizadas

- **Java 17**
- **Spring Boot 3.3.1**
    - Spring Data JPA
    - Spring Web
- **PostgreSQL**
- **Maven**
- **Docker**
- **Docker Compose**
- **OpenAPI**

## Funcionalidades do Sistema

1. **Gerenciamento de Tradutores**
    - Cadastro de novos tradutores
    - Atualização de dados de tradutores
    - Listagem paginada de todos os tradutores
    - Remoção de tradutores
    - Consulta de tradutores através de texto
2. **Gerenciamento de Documentos**
    - Cadastro de novos documentos
    - Atribuição de documentos a tradutores
    - Atualização de dados de documentos
    - Listagem paginada de todos os documentos
    - Remoção de documentos
    - Consulta de documentos através de texto
3. **Processo de importação de documentos**
    - Importação de documentos em massa através de um arquivo CSV
    - Cadastro de um processo de importação
    - Listagem paginada de todos os processos de importação
    - Detalhes do processo de importação
    - Consulta de importação através de texto

## Configurações

### Banco de Dados

O sistema utiliza o PostgreSQL como banco de dados. As configurações de conexão são definidas através de variáveis de ambiente:

- `SPRING_DATASOURCE_URL`: URL de conexão do banco de dados
- `SPRING_DATASOURCE_USERNAME`: Nome de usuário do banco de dados
- `SPRING_DATASOURCE_PASSWORD`: Senha do banco de dados
- `SPRING_DATASOURCE_DRIVER_CLASS_NAME`: Classe do driver JDBC

### Docker

O projeto inclui um `Dockerfile` para criação de uma imagem Docker e um `docker-compose.yml` para orquestração dos serviços.

### OpenAPI

Utilização do Springdoc OpenAPI para geração da documentação da API.

## Como Executar a Aplicação

### Pré-requisitos

- **Java 17**
- **Maven**
- **Docker**
- **Docker Compose**

### Passos para execução

1. **Clone o repositório:**

    ```bash
    git clone https://github.com/yurikaffer/translator_document_management_api
    cd translator_document_management_api
    ```

2. **Configure as variáveis de ambiente:**

   Se você não for utilizar o docker compose, edite o arquivo `application.properties` com as seguintes configurações:

    ```
    spring.datasource.url=jdbc:postgresql://localhost:5432/translator_document_management
    spring.datasource.username=postgres
    spring.datasource.password=postgres
    spring.datasource.driver-class-name=org.postgresql.Driver
    
    openai.api.key=
    ```

   Se você for utilizar o docker compose, apenas adicione a sua chave de api da OpenAI.

3. **Construa e execute os serviços com Docker Compose:**

```bash
docker-compose up --build
```

1. **Acesse a aplicação:**

   A aplicação estará disponível em `http://localhost:8080`.


## Documentação da API

A documentação da API gerada pelo OpenAPI pode ser acessada em `http://localhost:8080/swagger-ui.html`.

### Notas do desenvolvedor:

1. Utilizei processamento assíncrono na operação de importação de documentos através do arquivo CSV para que seja realizada em segundo plano sem bloquear o fluxo principal.
2. Existia uma lacuna aberta de como eu poderia retornar ao usuário informações e erros que uma importação de documentos poderia gerar. Resolvi esse problema criando a central de importações.
3. Utilizei índices para otimização de consultas.
4. Utilizei paginação para atender um volume grande de dados.
5. Faltou implementar caching com Redis para reduzir a carga no banco de dados e acelerar o acesso aos dados frequentemente solicitados.
6. Preciso corrigir a exclusão de um tradutor quando existe um documento relacionado a ele, atualmente está deletando o documento em cascata, o certo seria retornar uma mensagem de erro.
7. Apesar de feliz e orgulhoso com o resultado final, ainda há muitas oportunidades de melhorias e boas praticas para uma aplicação robusta e escalável