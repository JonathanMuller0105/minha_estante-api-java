# Minha Estante — API REST

API REST para organizar uma estante de livros e acompanhar o estado de leitura.
O projeto acadêmico individual aplica orientação a objetos em Java, separação de
responsabilidades, persistência com JPA, validação de dados e testes de API.

O objetivo é permitir que um cliente, como o Postman, envie requisições HTTP e
receba respostas JSON para cadastrar, consultar, atualizar e excluir livros.

Repositório: [minha_estante-api-java no GitHub](https://github.com/JonathanMuller0105/minha_estante-api-java).

## Tecnologias utilizadas

| Tecnologia | Uso no projeto |
|---|---|
| Java 21 | Linguagem e ambiente de execução |
| Spring Boot 4.1.1 | Inicialização e configuração da aplicação |
| Spring Web MVC | Endpoints HTTP, conversão de JSON e integração web |
| Spring Data JPA e Hibernate | Mapeamento de objetos e acesso ao banco |
| Jakarta Validation | Validação dos dados recebidos |
| H2 em memória | Banco relacional utilizado durante a execução |
| Maven e Maven Wrapper | Dependências, compilação, testes e inicialização |
| JUnit, AssertJ e Mockito | Testes automatizados e simulação de dependências |
| MockMvc | Testes de integração da camada HTTP do Spring |
| Postman | Collections, requisições e verificações funcionais |
| Git e GitHub | Histórico local de versões e repositório remoto |

O Wrapper está configurado para Maven 3.10.0. Não é necessário instalar Maven
globalmente: os arquivos `mvnw.cmd`, `mvnw` e `.mvn/wrapper/` estão no projeto.

## Funcionalidades

- **Cadastrar:** informa título, autor e situação de leitura; o banco gera o ID.
- **Listar:** consulta todos os livros; uma estante vazia retorna `[]`.
- **Buscar por ID:** consulta um livro específico ou informa que ele não existe.
- **Atualizar:** substitui título, autor e situação de leitura, preservando o ID.
- **Marcar como lido:** altera somente `lido` para `true`, mantendo os demais dados.
- **Excluir:** remove um livro existente e retorna uma resposta sem corpo.

## Arquitetura e responsabilidades

O pacote principal é `br.com.desafio.minhaestante`.

| Componente | Responsabilidade |
|---|---|
| `MinhaEstanteApplication` | Inicia o contexto Spring e o servidor embutido. `@SpringBootApplication` habilita a configuração e a descoberta dos componentes. |
| `Livro` | Entidade JPA associada à tabela `livros`, com quatro atributos privados: `id`, `titulo`, `autor` e `lido`. Possui construtores e métodos de acesso, sem `setId()`. |
| `LivroRequisicao` | DTO de entrada implementado como `record`. Transporta `titulo`, `autor` e `lido`, separando o contrato da requisição da entidade persistida. |
| `LivroRepository` | Interface que estende `JpaRepository<Livro, Long>`. Disponibiliza operações de persistência sem consultas personalizadas. |
| `LivroService` | Executa as seis operações da aplicação, busca entidades antes de alterá-las e sinaliza IDs inexistentes. Recebe o repositório por construtor. |
| `LivroController` | Expõe as seis rotas em `/api/livros`, valida a entrada, chama o serviço e define as respostas HTTP. Recebe o serviço por construtor. |
| `LivroNaoEncontradoException` | Exceção que estende `RuntimeException` e identifica a ausência do livro solicitado. |
| `ApiExceptionHandler` | Usa `@RestControllerAdvice` e `@ExceptionHandler` para traduzir ausência de livro em 404 e falhas de validação em 400. |

### Fluxo de uma requisição

```text
Postman → LivroController → LivroService → LivroRepository → H2
Postman ← resposta HTTP/JSON ← Controller ← Service ← Repository ← H2
```

Por exemplo, em um cadastro, o Controller converte o JSON em `LivroRequisicao` e
solicita sua validação. O Service cria um objeto `Livro`, o Repository solicita a
persistência e o H2 gera o identificador. O resultado volta pelas camadas até o
Controller responder com HTTP 201, o objeto criado e o cabeçalho `Location`.
Se ocorrer uma exceção prevista, o tratamento central prepara a resposta de erro.

## Estrutura do projeto

Estrutura dos arquivos de código, configuração, testes e documentação. A pasta
`target/`, gerada pelo Maven, é ignorada pelo Git e foi omitida abaixo.

```text
.
├── README.md
├── pom.xml
├── mvnw
├── mvnw.cmd
├── .gitattributes
├── .gitignore
├── .mvn/
│   └── wrapper/
│       └── maven-wrapper.properties
├── src/
│   ├── main/
│   │   ├── java/br/com/desafio/minhaestante/
│   │   │   ├── MinhaEstanteApplication.java
│   │   │   ├── livro/
│   │   │   │   ├── Livro.java
│   │   │   │   ├── LivroRequisicao.java
│   │   │   │   ├── LivroRepository.java
│   │   │   │   ├── LivroService.java
│   │   │   │   └── LivroController.java
│   │   │   └── exception/
│   │   │       ├── LivroNaoEncontradoException.java
│   │   │       └── ApiExceptionHandler.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/java/br/com/desafio/minhaestante/
│       ├── MinhaEstanteApplicationTests.java
│       └── livro/
│           ├── ComponentesLivroTests.java
│           ├── LivroServiceTests.java
│           └── LivroControllerTests.java
└── postman/
    ├── Minha Estante API.postman_collection.json
    ├── Demonstracao pessoal.postman_collection.json
    ├── GUIA-DE-EXECUCAO.md
    ├── validar-http.ps1
    └── evidencias/
        ├── RELATORIO-BLOCO-5.md
        ├── aplicacao.json
        ├── collections-validacao.json
        ├── git-final.txt
        ├── http-e-h2.json
        ├── maven-resumo.txt
        ├── maven.json
        └── tentativa-inicial-http-e-h2.json
```

## Pré-requisitos

- JDK 21, necessário para compilar e executar Java.
- Windows com PowerShell para os comandos apresentados abaixo.
- Git, caso utilize clonagem e controle de versões.
- Visual Studio Code ou ambiente equivalente para leitura e edição do projeto.
- Postman para executar as collections e os testes manuais.
- Acesso à internet na primeira execução do Wrapper ou quando faltarem dependências
  no cache Maven.

Confira a versão disponível com `java -version`. Essa verificação não substitui
a conferência do Java usado pelo Maven: o Wrapper também utiliza `JAVA_HOME`,
quando essa variável está definida.
Ela deve apontar para a **raiz do JDK 21**, sem terminar em `bin`. Confira o ambiente
efetivamente utilizado pelo Wrapper com:

```powershell
.\mvnw.cmd --version
```

## Como compilar, testar e executar

Abra o terminal **na raiz do projeto**, onde estão `pom.xml` e `mvnw.cmd`.

Para limpar os arquivos de build anteriores e compilar a aplicação:

```powershell
.\mvnw.cmd clean compile
```

Para executar os testes automatizados:

```powershell
.\mvnw.cmd test
```

Para iniciar a API:

```powershell
.\mvnw.cmd spring-boot:run
```

Aguarde a mensagem de inicialização concluída e a confirmação da porta **8080**.
A base dos recursos é `http://localhost:8080/api/livros`. O comando mantém o
servidor em execução no terminal; use outro terminal ou o Postman para as chamadas.
Para encerrar, pressione `Ctrl+C` no terminal do servidor. Não inicie outra
instância enquanto essa porta estiver ocupada.

## Modelo de dados e JSON

| Campo | Tipo | Significado |
|---|---|---|
| `id` | `Long` | Identificador gerado pelo banco com estratégia `IDENTITY`; não faz parte do DTO de entrada. |
| `titulo` | `String` | Nome do livro. |
| `autor` | `String` | Nome do autor. |
| `lido` | `boolean` | `false` para não lido; `true` para lido. |

Exemplo de corpo válido para POST ou PUT, com `Content-Type: application/json`:

```json
{
  "titulo": "O Hobbit",
  "autor": "J. R. R. Tolkien",
  "lido": false
}
```

O cadastro devolve os mesmos dados e o `id` gerado. Utilize o ID efetivamente
retornado nas próximas chamadas; não dependa de uma numeração fixa.

As colunas de título e autor são não nulas e possuem comprimento de 120 caracteres
no mapeamento JPA. Esse limite do banco é distinto da validação do DTO: não há
`@Size` na requisição nem resposta 400 específica implementada para excesso de
comprimento.

## Endpoints e respostas

Todos os caminhos abaixo usam a base `http://localhost:8080`.

| Método | Caminho | Operação e sucesso | Erros tratados |
|---|---|---|---|
| GET | `/api/livros` | Lista todos os livros: **200**, array JSON, inclusive `[]`. | Não retorna 404 apenas por a coleção estar vazia. |
| GET | `/api/livros/{id}` | Busca um livro: **200**, objeto JSON. | **404** se o ID não existir. |
| POST | `/api/livros` | Cadastra: **201**, objeto criado e `Location` apontando para `/api/livros/{id}`. | **400** para título ou autor inválidos. |
| PUT | `/api/livros/{id}` | Atualiza título, autor e lido: **200**, objeto atualizado com o mesmo ID. | **400** para dados inválidos; **404** para ID inexistente com entrada válida. |
| PATCH | `/api/livros/{id}/lido` | Marca somente lido como `true`: **200**, objeto atualizado. **Não exige corpo JSON.** | **404** se o ID não existir. |
| DELETE | `/api/livros/{id}` | Exclui: **204**, sem corpo de resposta. | **404** se o ID não existir. |

O `Location` do POST é construído a partir da URL da requisição e do identificador
gerado, permitindo consultar diretamente o novo recurso. O PATCH preserva título,
autor e ID; repeti-lo mantém o livro marcado como lido. O PUT recebe os três campos
editáveis, enquanto o ID permanece definido pela URL e pelo registro existente.

### Validação e tratamento de erros

`LivroRequisicao` aplica `@NotBlank` a `titulo` e `autor`: nulo, texto vazio e texto
formado apenas por espaços em branco são rejeitados. As mensagens das restrições
são `Titulo obrigatorio` e `Autor obrigatorio`.

No Controller, `@RequestBody` converte o JSON em DTO e `@Valid` aciona sua validação
antes da chamada ao serviço. Criar o record diretamente com `new` não executa
essa validação automaticamente.

Para essa falha de validação, `ApiExceptionHandler` devolve **400 Bad Request**:

```json
{
  "erro": "Informe titulo e autor validos."
}
```

O serviço consulta IDs com `findById(...).orElseThrow(...)`. Quando não encontra o
livro, lança `LivroNaoEncontradoException`, tratada como **404 Not Found**.
Exemplo ilustrativo para uma consulta ao ID 99, caso ele não exista:

```json
{
  "erro": "Livro com id 99 nao encontrado."
}
```

Esses formatos correspondem às duas exceções tratadas explicitamente. Outros
erros, como JSON malformado ou tipo de ID inválido, podem usar o tratamento padrão
do Spring e não têm o mesmo corpo personalizado garantido pela implementação.

## H2 em memória

A aplicação usa `jdbc:h2:mem:estante`, com `spring.jpa.hibernate.ddl-auto=create-drop`.
O Hibernate cria a tabela no início e solicita sua remoção no encerramento.
Os dados existem somente enquanto a JVM da aplicação estiver ativa e são perdidos
ao encerrar a aplicação. Reiniciar inicia uma nova base vazia, sem os cadastros da
execução anterior.

Com o servidor ativo, acesse `http://localhost:8080/h2-console` e informe:

| Configuração | Valor |
|---|---|
| JDBC URL | `jdbc:h2:mem:estante` |
| Usuário | `sa` |
| Senha | Vazia; deixe o campo em branco. |

Use exatamente a URL JDBC acima para consultar o mesmo banco utilizado pela API.
A tabela `LIVROS` contém `ID`, `TITULO`, `AUTOR` e `LIDO`. Exemplo de leitura:

```sql
SELECT * FROM LIVROS;
```

Os nomes em maiúsculas no H2 correspondem aos campos JSON em minúsculas.
Compare pelo ID, pois a ordem de linhas sem `ORDER BY` não é garantida.

## Testar com Postman

Arquivos prontos para importação:

- [Collection principal — Minha Estante API](<postman/Minha Estante API.postman_collection.json>).
- [Collection de demonstração pessoal](<postman/Demonstracao pessoal.postman_collection.json>).
- [Guia detalhado de execução](postman/GUIA-DE-EXECUCAO.md).
- [Relatório de testes e evidências do Bloco 5](postman/evidencias/RELATORIO-BLOCO-5.md).

1. Inicie a aplicação com o H2 vazio, reiniciando o servidor quando necessário.
2. Importe a collection principal e confira `baseUrl = http://localhost:8080`,
   sem barra final. Evite variáveis de ambiente conflitantes.
3. Execute uma iteração dos dez cenários, na ordem numerada: listar vazio,
   cadastrar dois livros, listar, buscar, atualizar, marcar como lido, tentar
   cadastro inválido, excluir e buscar o excluído.
4. Os scripts capturam `livroId` e `segundoLivroId` das respostas. Confira as
   verificações de status, campos JSON, ID, `Location` e corpo vazio.
5. Execute a collection pessoal separadamente, depois da principal ou em outra
   inicialização. Ela contém três POSTs com os títulos e autores confirmados pelo
   aluno: Harry Potter — JK Rowling; Cronicas — C.S Lewis; Automação — M. Jonathan.
6. Para comparar um livro lido no H2, consulte o banco após o cenário 07 e antes
   da exclusão. Após o cenário 10, permanece somente Dom Casmurro, não lido.

O roteiro opcional `postman/validar-http.ps1` reproduz verificações por HTTP e
compara o H2, usando PowerShell 7. Ele é um cliente alternativo e não executa os
scripts do Postman. Consulte o guia antes de executá-lo: ele exige H2 vazio e
atualiza o arquivo de evidências da execução.

## Testes e resultados registrados

Os números abaixo correspondem às validações registradas durante o desenvolvimento,
com execução técnica em 26/10/2026 e confirmação dos resultados Postman pelo aluno
no encerramento de 27/10/2026. Não representam uma nova execução a cada leitura
deste README.

| Validação | Resultado registrado | Origem |
|---|---|---|
| Maven | **37 testes aprovados**, 0 falhas, 0 erros, 0 ignorados; `BUILD SUCCESS`. | Relatórios de execução: 10 testes do Service, 23 do Controller e 4 anteriores. |
| Collection principal | **42 verificações aprovadas**, 0 falhas, em 10 requisições. | Execução no Postman confirmada pelo aluno. |
| Collection pessoal | **12 verificações aprovadas**, 0 falhas, em 3 POSTs com 4 verificações cada. | Execução no Postman confirmada pelo aluno. |
| H2 | **12 consultas de leitura realizadas e conferidas**, em três momentos. | Comparação dos resultados SQL com as respostas da API. |

Os testes automatizados abrangem validação do DTO, tratamento de exceções,
operações do Service e integração do Controller com o H2. Os cenários HTTP incluem
cadastro, atualização, PATCH parcial, exclusão, dados inválidos e IDs inexistentes.

As evidências HTTP/H2 estão registradas em arquivos JSON. Os resultados Postman
foram informados pelo aluno; capturas de tela ou exportações do Runner não estão
anexadas ao repositório. O relatório diferencia essas origens e preserva a tentativa
inicial interrompida por um problema no leitor de evidências, posteriormente
corrigido. Registros históricos de Postman pendente descrevem o momento anterior
à confirmação do aluno; consulte a atualização no relatório para o encerramento.

## O que aprendi

Durante o desenvolvimento do projeto Minha Estante, pude estudar como funciona uma API REST utilizando Java 21 e Spring Boot.

**Programação Orientada a Objetos:** compreendi que uma classe funciona como um modelo para criar objetos. No projeto, a classe `Livro` representa os livros cadastrados, com atributos como título, autor e situação de leitura. O encapsulamento utiliza atributos privados e métodos públicos para controlar o acesso aos dados.

**Arquitetura em camadas:** aprendi a diferenciar as responsabilidades das principais classes. O `Controller` recebe as requisições HTTP, o `Service` executa as regras da aplicação e o `Repository` realiza a comunicação com o banco de dados.

**DTO e validação:** conheci a utilização de um `record` para receber os dados enviados em JSON. Também utilizei as anotações `@NotBlank` e `@Valid` para impedir cadastros com título ou autor vazios.

**Persistência de dados:** entendi que o Spring Data JPA permite salvar objetos Java em tabelas de um banco relacional. Neste projeto, utilizei o H2 em memória e observei que os registros desaparecem quando a aplicação é encerrada.

**API REST e métodos HTTP:** pratiquei o funcionamento de GET, POST, PUT, PATCH e DELETE, compreendendo como cada método representa uma operação diferente. Também estudei os códigos HTTP 200, 201, 204, 400 e 404.

**Testes:** utilizei o Postman para enviar requisições, conferir respostas JSON e validar os comportamentos da API. Também acompanhei os testes automatizados do projeto para verificar se as funcionalidades continuavam funcionando corretamente.

**Git e GitHub:** aprendi a importância do controle de versão, dos commits descritivos e da organização de um repositório para registrar a evolução do desenvolvimento.

O principal aprendizado deste desafio foi compreender como as diferentes camadas de uma aplicação Spring Boot trabalham juntas, desde o recebimento de uma requisição até o armazenamento e a consulta das informações no banco de dados.

## Autoria

Projeto acadêmico individual de **Jonathan Muller**, conforme o nome de autoria
configurado no Git.

[Repositório do projeto no GitHub](https://github.com/JonathanMuller0105/minha_estante-api-java).
