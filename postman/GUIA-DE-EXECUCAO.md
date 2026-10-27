# Bloco 5 — roteiro de validação

Este diretório reúne collections e evidências de QA. Não é o README final do
projeto. Nenhum arquivo Java, dependência ou configuração de banco foi alterado.

## Executar no Postman

1. Inicie a aplicação com Java 21 e aguarde a porta 8080 responder. Para a sequência
   obrigatória, reinicie a aplicação: o H2 em memória deve estar vazio.
2. Importe `Minha Estante API.postman_collection.json` no Postman.
3. Confira a variável de collection `baseUrl = http://localhost:8080`, sem barra
   final. Desative variáveis de ambiente ou globais conflitantes, principalmente
   `baseUrl`, `livroId` e `segundoLivroId`.
4. Execute a collection de 01 a 10, na ordem, com uma iteração. Não execute antes
   cadastros de outra collection. Se o teste 01 falhar, reinicie a aplicação antes
   de repetir a sequência completa; o roteiro não apaga dados silenciosamente.
5. Confira os resultados de cada script de pós-resposta. Exporte o resultado do
   Runner ou registre capturas reais para confirmar a execução no Postman.
6. Para observar um livro lido no console H2, execute manualmente até 07, faça as
   consultas e depois continue com 08, 09 e 10. Após 10, somente o segundo livro
   deve existir e nenhum deve estar marcado como lido.

As duas collections foram verificadas contra o schema oficial v2.1, e a sintaxe
JavaScript dos scripts foi analisada. Essas verificações não substituem a
importação nem a execução no Postman. O relatório separa os clientes utilizados.

### Variáveis dinâmicas

O teste 02 salva o identificador retornado em `livroId`; o teste 03 usa
`segundoLivroId`. As requisições posteriores referenciam os IDs capturados, sem
presumir que serão 1 e 2. O teste 01 limpa apenas variáveis de execuções anteriores,
sem modificar o banco. Os scripts verificam o objeto, o cabeçalho `Location`, o
corpo vazio do DELETE e a propriedade `erro` das respostas 400 e 404.

## Demonstração pessoal separada

Importe `Demonstracao pessoal.postman_collection.json` e execute somente após os
dez testes, ou em outra inicialização. Os títulos e autores foram confirmados pelo
aluno e suas grafias foram preservadas:

| Título | Autor |
|---|---|
| Harry Potter | JK Rowling |
| Cronicas | C.S Lewis |
| Automação | M. Jonathan |

As seis variáveis de título e autor podem ser editadas para corrigir uma escolha.
O corpo é serializado no script de pré-requisição, preservando acentos e aspas.
Se título ou autor estiver vazio, a requisição é interrompida, sem inventar dados.
Cada cadastro captura seu próprio ID e confere o `Location`. Executar novamente
essa collection cria novos registros; ela não implementa deduplicação.

## Reproduzir as verificações por HTTP, fora do Postman

Com a aplicação recém-iniciada e H2 vazio, execute na raiz, pelo PowerShell 7:

```powershell
.\postman\validar-http.ps1 -IncluirDemonstracaoPessoal
```

O roteiro lê métodos, URLs e corpos da collection obrigatória e faz as verificações
por `Invoke-WebRequest`. Ele não executa scripts `pm.*` e não é Newman nem Postman.
IDs são capturados das respostas. As evidências ficam em
`evidencias/http-e-h2.json`, incluindo uma falha parcial se a execução parar.
Copie evidências que queira preservar antes de repetir, pois esse arquivo é
atualizado em cada execução. Nenhuma ferramenta é instalada pelo roteiro.

## Consultar o H2 sem modificar registros

Enquanto a aplicação estiver ativa, abra `http://localhost:8080/h2-console`:

- JDBC URL: `jdbc:h2:mem:estante`
- Usuário: `sa`
- Senha: vazia

```sql
SELECT * FROM LIVROS;
SELECT ID, TITULO, AUTOR, LIDO FROM LIVROS;
SELECT * FROM LIVROS WHERE LIDO = TRUE;
SELECT COUNT(*) FROM LIVROS;
```

O `id` JSON corresponde a `ID`; `titulo` a `TITULO`; `autor` a `AUTOR`; e `lido`
a `LIDO`. A ordem das linhas não é garantida sem `ORDER BY`; compare pelos IDs.
O roteiro executa apenas esses SELECTs e compara os quatro campos com as respostas
da API. As sessões do console não são salvas nas evidências.

| Momento | Total esperado | Livros lidos |
|---|---:|---:|
| Após 07 | 2 | 1 |
| Após 10 | 1 | 0 |
| Após os três cadastros pessoais | 4 | 0 |

Os quatro registros finais incluem Dom Casmurro e os três livros pessoais.
Encerrar a aplicação elimina o banco em memória; os resultados salvos continuam
servindo como evidência histórica, sem prometer dados disponíveis após reiniciar.

## Informações para o README final, em etapa posterior

- Java 21, Spring Boot 4.1.1, Maven Wrapper, Spring MVC, JPA, Validation e H2.
- Testes: `./mvnw.cmd test`; inicialização: `./mvnw.cmd spring-boot:run`.
- Base local: `http://localhost:8080/api/livros`.
- GET coleção e GET por ID: 200; POST: 201 e Location; PUT: 200;
  PATCH `/{id}/lido` sem corpo: 200; DELETE: 204 sem corpo.
- Validação inválida: 400 com `erro`; ID ausente: 404 com `erro`.
- H2 em memória com `create-drop`; sem persistência entre inicializações.
- Consultar o relatório desta etapa para resultados observados e pendências.

## Referências técnicas dos artefatos

- [Formato oficial de collection v2.1](https://schema.postman.com/collection/json/v2.1.0/draft-07/docs/index.html).
- [Variáveis em scripts do Postman](https://learning.postman.com/docs/tests-and-scripts/write-scripts/postman-sandbox-reference/pm-variables).
- [Scripts para validar respostas](https://learning.postman.com/docs/tests-and-scripts/write-scripts/test-scripts/).
