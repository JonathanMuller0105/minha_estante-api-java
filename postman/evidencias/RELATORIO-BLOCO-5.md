# Relatório do Bloco 5 — validação funcional

Data local: 26/10/2026, America/Sao_Paulo. Execução HTTP iniciada em 2026-10-26T19:01:21.3943915-03:00 e encerrada em 2026-10-26T19:01:26.7322980-03:00.

Atualização de encerramento: 27/10/2026. Os resultados Postman abaixo foram
confirmados pelo aluno na solicitação de versionamento. Não foram fornecidas
capturas nem exportações do Runner nesta pasta para inspeção pelo agente.

## 1. Escopo executado

Inspeção, testes existentes, inicialização com Java 21 na porta 8080, duas collections reutilizáveis, sequência funcional e consultas H2 somente de leitura. A execução foi por PowerShell 7, não pelo aplicativo Postman.

## 2. Arquivos preparados

- ../Minha Estante API.postman_collection.json: dez requisições obrigatórias.
- ../Demonstracao pessoal.postman_collection.json: três cadastros escolhidos pelo aluno.
- ../validar-http.ps1: reprodução HTTP e comparação com H2.
- ../GUIA-DE-EXECUCAO.md: importação, variáveis, sequência e informações para o README posterior.
- http-e-h2.json: requisições, corpos, códigos, Location, asserções e resultados SQL reais.
- maven.json e maven-resumo.txt: resultados da execução Maven desta etapa.
- collections-validacao.json: schema, sintaxe e hashes das collections.
- tentativa-inicial-http-e-h2.json: tentativa inicial interrompida por erro do leitor HTML, preservada sem ser tratada como aprovação.
- git-final.txt e aplicacao.json: conferências finais do repositório e do encerramento.

Nenhuma classe Java, pom.xml ou application.properties foi modificada.

## 3. Testes Maven

Comando: `./mvnw.cmd test`. Java 21.0.10; Maven 3.10.0. **BUILD SUCCESS**: 37 testes, 0 falhas, 0 erros e 0 ignorados. Os dados vêm dos relatórios Surefire gerados nesta execução. Não foi repetido clean compile, pois a atividade exigiu test e nenhuma classe Java foi alterada.

## 4. Dez testes obrigatórios

| Nº | Cenário | Método | Esperado | Observado | Resultado |
|---|---|---|---:|---:|---|
| 1 | Listar banco vazio | GET | 200 | 200 | Aprovado por HTTP |
| 2 | Cadastrar primeiro livro | POST | 201 | 201 | Aprovado por HTTP |
| 3 | Cadastrar segundo livro | POST | 201 | 201 | Aprovado por HTTP |
| 4 | Listar livros | GET | 200 | 200 | Aprovado por HTTP |
| 5 | Buscar por ID | GET | 200 | 200 | Aprovado por HTTP |
| 6 | Atualizar livro | PUT | 200 | 200 | Aprovado por HTTP |
| 7 | Marcar como lido | PATCH | 200 | 200 | Aprovado por HTTP |
| 8 | Cadastro inválido | POST | 400 | 400 | Aprovado por HTTP |
| 9 | Excluir livro | DELETE | 204 | 204 | Aprovado por HTTP |
| 10 | Buscar livro excluído | GET | 404 | 404 | Aprovado por HTTP |

A primeira chamada encontrou []. O primeiro cadastro capturou dinamicamente o ID 1; o segundo capturou ID 2. Esses valores são evidência desta execução e não estão fixados nas requisições reutilizáveis.

POST conferiu os dados e Location. PUT preservou ID. PATCH foi enviado sem corpo e alterou somente lido. DELETE respondeu com corpo vazio. Os erros 400/404 contêm a propriedade erro com a mensagem prevista. A sequência obrigatória foi concluída antes dos cadastros pessoais.

## 5. Execução efetiva no Postman

O aluno confirmou a execução no Postman na solicitação de encerramento do Bloco 5:

| Collection | Requisições | Verificações aprovadas | Falhas | Origem do registro |
|---|---:|---:|---:|---|
| Minha Estante API | 10 | 42 | 0 | Resultado informado pelo aluno |
| Demonstração pessoal | 3 POST | 12 | 0 | Resultado informado pelo aluno |

Na collection pessoal, cada POST contém quatro verificações: código HTTP 201,
ID positivo, dados pessoais preservados e Location correto. São 12 verificações,
não 12 requisições. Na principal, os dez cenários contêm 42 verificações.

Não houve operação do aplicativo Postman pelo agente. Os resultados acima são
registrados com base na confirmação expressa do aluno; as capturas mencionadas
na solicitação não estão em `postman/` e não foram inspecionadas pelo agente.
Nenhuma captura de tela ou exportação do Runner é declarada versionada.

As collections passaram na validação do schema oficial v2.1, e seus 17 scripts
passaram na análise de sintaxe JavaScript. Essa verificação estática é independente
da execução no Postman e não executa `pm.test`. Os JSONs de HTTP/H2 e de validação
estática foram preservados como registros históricos de 26/10; suas indicações de
Postman pendente descrevem aquele momento, anterior à confirmação do aluno.

## 6. H2 e comparação com a API

Console acessado por HTTP 200, com JDBC jdbc:h2:mem:estante, usuário sa e senha vazia. Tabela LIVROS confirmada com ID, TITULO, AUTOR e LIDO. As consultas foram executadas em cada momento:

```sql
SELECT * FROM LIVROS;
SELECT ID, TITULO, AUTOR, LIDO FROM LIVROS;
SELECT * FROM LIVROS WHERE LIDO = TRUE;
SELECT COUNT(*) FROM LIVROS;
```

| Momento | Registros | Lidos | Verificação |
|---|---:|---:|---|
| apos teste 7 | 2 | 1 | Quatro SELECTs comparados com JSON |
| apos teste 10 | 1 | 0 | Quatro SELECTs comparados com JSON |
| apos demonstracao pessoal | 4 | 0 | Quatro SELECTs comparados com JSON |

Os quatro campos foram comparados por ID, sem pressupor a ordem das linhas. Após 07, O Hobbit atualizado estava lido e Dom Casmurro não estava. Após 10, somente Dom Casmurro permaneceu. Após os cadastros pessoais, quatro registros não lidos estavam presentes. Não houve INSERT, UPDATE ou DELETE por SQL. Nenhuma sessão do console foi gravada em disco.

## 7. Evidências

Os arquivos JSON guardam os resultados observados pelo agente, separados de expectativas. As respostas HTTP vieram de Invoke-WebRequest; as consultas SQL vieram do console H2 da mesma aplicação. Há evidências em três momentos para que o filtro de lidos seja observado antes da exclusão.

Os resultados Postman de 42 e 12 aprovações foram acrescentados somente neste
relatório, com a origem identificada como informação do aluno. Não há arquivos de
captura de tela na pasta, e nenhum link de evidência visual foi inventado.

## 8. Personalização acadêmica

**Concluída por HTTP e confirmada pelo aluno no Postman:** os três títulos e autores foram confirmados expressamente pelo aluno e testados pelo agente com POST 201, corpo correto e Location. Grafias preservadas, sem presumir volumes ou substituir autores. O aluno informou três POSTs no Postman, com quatro verificações aprovadas por requisição e nenhuma falha.

| Título confirmado | Autor confirmado | ID observado | HTTP |
|---|---|---:|---:|
| Harry Potter | JK Rowling | 3 | 201 |
| Cronicas | C.S Lewis | 4 | 201 |
| Automação | M. Jonathan | 5 | 201 |

## 9. Critérios de aceite

- [x] Aplicação iniciada com Java 21 na porta 8080.
- [x] Testes Maven aprovados.
- [x] Collection Minha Estante API e dez cenários configurados.
- [x] Dez requisições HTTP executadas em ordem e registradas.
- [x] Códigos 200, 201, 204, 400 e 404 verificados.
- [x] IDs dinâmicos e Location conferidos.
- [x] PATCH parcial e DELETE sem corpo verificados.
- [x] LIVROS e resultados H2 comparados com a API.
- [x] Três livros pessoais definidos e testados por HTTP.
- [x] Scripts e explicações em pt-BR.
- [x] Código Java e configurações preservados; Git revisado.
- [x] Nenhum commit ou push na etapa de execução de 26/10; commit local autorizado separadamente no encerramento de 27/10.
- [x] Execução das collections no Postman confirmada pelo aluno: 42 e 12 verificações aprovadas, sem falhas.
- [ ] Capturas ou exportações do Runner arquivadas no repositório: não fornecidas nesta pasta.

## 10. Git

Branch main e remoto origin preservados. O estado anterior ao versionamento está
em git-final.txt: HEAD 74b92dc e arquivos de QA não rastreados. Esse arquivo é uma
evidência histórica da execução, não o estado posterior ao commit.

Em 27/10 foi autorizado versionar exclusivamente os arquivos de postman/ com a
mensagem `test: adicionar collections Postman e evidencias da API`. O identificador
e o estado posterior constam no histórico Git e no relatório de encerramento do
agente. Nenhum arquivo Java será incluído nessa alteração. target permanece
ignorado. Nenhum push foi autorizado ou executado nesta atividade.

## 11. Observações e pendências

- JAVA_HOME herdado ainda apontava ao JDK 15; os processos Maven usaram explicitamente C:\Program Files\Java\jdk-21.0.10, sem alterar configuração global.
- A primeira tentativa parou após a resposta do teste 07 porque o leitor esperava a classe HTML result; o H2 usa resultSet. Corrigido somente o roteiro de QA. A aplicação foi reiniciada e os dez cenários foram repetidos desde H2 vazio, todos aprovados.
- Ao interromper a primeira inicialização via Ctrl+C, o processo Maven reportou exit code 1 após o início do encerramento. Isso não é aprovação de encerramento normal dessa tentativa nem falha dos testes Maven. O resultado do encerramento da execução final está em aplicacao.json.
- A interrupção manual da execução final também fez o plugin spring-boot:run reportar BUILD FAILURE e exit code 1, após iniciar o fechamento do contexto. A porta foi conferida livre ao final; não se declara encerramento normal com BUILD SUCCESS. Os 37 testes Maven e as verificações HTTP/H2 terminaram aprovados antes dessa interrupção.
- Os dados H2 desaparecem após encerrar a JVM; as evidências são históricas, não dados persistidos em disco.
- Os resultados de execução no Postman foram confirmados pelo aluno no encerramento; capturas ou exportações do Runner não foram fornecidas para arquivamento. Isso não impede registrar a confirmação com sua origem explícita. Nenhum defeito da API foi observado nos cenários funcionais definidos em 26/10.
- Nesta atividade de versionamento não foram repetidos testes Maven, HTTP, Postman ou SQL, nem foi alterado o estado da aplicação. Os registros de inicialização e encerramento em aplicacao.json referem-se à execução de 26/10, não ao estado atual do servidor.

## 12. Resultado

**CONCLUÍDO para encerramento e versionamento do Bloco 5**, com base nos resultados
Maven, HTTP e H2 registrados pelo agente e na confirmação de execução Postman
fornecida pelo aluno. Permanece a observação documental de que as capturas ou
exportações do Postman não foram anexadas ao repositório. Não houve avanço para o
Bloco 6.
