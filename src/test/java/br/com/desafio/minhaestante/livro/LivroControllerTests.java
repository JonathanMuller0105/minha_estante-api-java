package br.com.desafio.minhaestante.livro;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercita a integração Controller → Service → Repository com H2 real em memória.
 * {@code @SpringBootTest} carrega o contexto e {@code @AutoConfigureMockMvc}
 * prepara o MockMvc, que percorre a infraestrutura HTTP do Spring sem abrir uma
 * porta de rede. Assim são verificados JSON, validação e tratamento de exceções.
 */
@SpringBootTest
@AutoConfigureMockMvc
class LivroControllerTests {

	/** Cliente de testes injetado pelo Spring para executar as requisições simuladas. */
	@Autowired
	private MockMvc mvc;

	/** Repositório real usado para preparar e conferir registros dos cenários. */
	@Autowired
	private LivroRepository repository;

	/** Remove dados entre cenários para que os resultados não dependam da ordem de execução. */
	@BeforeEach
	void limparDados() {
		repository.deleteAll();
	}

	/** Uma coleção vazia deve ser JSON vazio com 200, sem ser confundida com erro 404. */
	@Test
	void listaVaziaCom200() throws Exception {
		mvc.perform(get("/api/livros"))
				.andExpect(status().isOk())
				.andExpect(content().json("[]"));
	}

	/** O cadastro deve persistir, devolver 201 e apontar para uma consulta válida em Location. */
	@Test
	void cadastraCom201ELocationConsultavel() throws Exception {
		var resultado = mvc.perform(post("/api/livros").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"titulo":"Dom Casmurro","autor":"Machado de Assis","lido":false}
						"""))
				.andExpect(status().isCreated())
				.andExpect(header().string(HttpHeaders.LOCATION, matchesPattern("http://localhost/api/livros/\\d+")))
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.titulo").value("Dom Casmurro"))
				.andExpect(jsonPath("$.autor").value("Machado de Assis"))
				.andExpect(jsonPath("$.lido").value(false))
				.andReturn();
		Livro salvo = repository.findAll().getFirst();
		assertThat(repository.count()).isEqualTo(1);
		String location = resultado.getResponse().getHeader(HttpHeaders.LOCATION);
		assertThat(location).endsWith("/api/livros/" + salvo.getId());
		mvc.perform(get(location)).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(salvo.getId()));
	}

	/** Confere que a listagem devolve os registros persistidos, sem filtros adicionais. */
	@Test
	void listaLivrosExistentes() throws Exception {
		repository.save(new Livro("Primeiro", "Autor A", false));
		repository.save(new Livro("Segundo", "Autor B", true));
		mvc.perform(get("/api/livros")).andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)));
	}

	/** A consulta individual deve serializar os quatro atributos da entidade encontrada. */
	@Test
	void consultaPorIdCom200() throws Exception {
		Livro salvo = repository.save(new Livro("Titulo", "Autor", true));
		mvc.perform(get("/api/livros/{id}", salvo.getId())).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(salvo.getId()))
				.andExpect(jsonPath("$.titulo").value("Titulo"))
				.andExpect(jsonPath("$.autor").value("Autor"))
				.andExpect(jsonPath("$.lido").value(true));
	}

	/** PUT deve atualizar os dados no banco e preservar a identidade do registro. */
	@Test
	void atualizaCom200EPreservaId() throws Exception {
		Livro salvo = repository.save(new Livro("Antigo", "Autor antigo", true));
		mvc.perform(put("/api/livros/{id}", salvo.getId()).contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"titulo":"Novo","autor":"Novo autor","lido":false}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(salvo.getId()))
				.andExpect(jsonPath("$.titulo").value("Novo"))
				.andExpect(jsonPath("$.autor").value("Novo autor"))
				.andExpect(jsonPath("$.lido").value(false));
		Livro atualizado = repository.findById(salvo.getId()).orElseThrow();
		assertThat(atualizado.getTitulo()).isEqualTo("Novo");
		assertThat(atualizado.getAutor()).isEqualTo("Novo autor");
		assertThat(atualizado.isLido()).isFalse();
		assertThat(repository.count()).isEqualTo(1);
	}

	/** PATCH sem corpo deve alterar somente lido, mantendo os demais campos persistidos. */
	@Test
	void marcaComoLidoSemCorpoJson() throws Exception {
		Livro salvo = repository.save(new Livro("Titulo", "Autor", false));
		mvc.perform(patch("/api/livros/{id}/lido", salvo.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(salvo.getId()))
				.andExpect(jsonPath("$.titulo").value("Titulo"))
				.andExpect(jsonPath("$.autor").value("Autor"))
				.andExpect(jsonPath("$.lido").value(true));
		Livro atualizado = repository.findById(salvo.getId()).orElseThrow();
		assertThat(atualizado.getTitulo()).isEqualTo("Titulo");
		assertThat(atualizado.getAutor()).isEqualTo("Autor");
		assertThat(atualizado.isLido()).isTrue();
	}

	/** DELETE deve remover do banco e responder 204 com corpo efetivamente vazio. */
	@Test
	void excluiCom204SemCorpo() throws Exception {
		Livro salvo = repository.save(new Livro("Titulo", "Autor", false));
		mvc.perform(delete("/api/livros/{id}", salvo.getId()))
				.andExpect(status().isNoContent()).andExpect(content().string(""));
		assertThat(repository.existsById(salvo.getId())).isFalse();
	}

	/**
	 * As quatro operações por ID devem usar o tratamento central de ausência.
	 *
	 * @param metodo verbo HTTP exercitado
	 */
	@ParameterizedTest
	@ValueSource(strings = {"GET", "PUT", "PATCH", "DELETE"})
	void retorna404ComErroParaIdInexistente(String metodo) throws Exception {
		String caminho = "/api/livros/999999" + (metodo.equals("PATCH") ? "/lido" : "");
		var requisicao = request(HttpMethod.valueOf(metodo), caminho);
		if (metodo.equals("PUT")) {
			requisicao.contentType(MediaType.APPLICATION_JSON)
					.content("{\"titulo\":\"Titulo\",\"autor\":\"Autor\",\"lido\":false}");
		}
		mvc.perform(requisicao).andExpect(status().isNotFound())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.erro").value("Livro com id 999999 nao encontrado."));
	}

	/**
	 * Cadastro e atualização devem rejeitar nulo, vazio e espaços em título ou autor.
	 * Também confere que uma atualização inválida não modifica o registro existente.
	 *
	 * @param metodo POST ou PUT
	 * @param titulo valor de título usado no cenário
	 * @param autor valor de autor usado no cenário
	 */
	@ParameterizedTest
	@MethodSource("dadosInvalidos")
	void retorna400SemPersistirDadosInvalidos(String metodo, String titulo, String autor) throws Exception {
		Livro existente = metodo.equals("PUT") ? repository.save(new Livro("Original", "Autor", false)) : null;
		String caminho = "/api/livros" + (existente == null ? "" : "/" + existente.getId());
		// Os valores são controlados pelo teste; nulo deve ser JSON nulo, sem aspas.
		String tituloJson = titulo == null ? "null" : "\"" + titulo + "\"";
		String autorJson = autor == null ? "null" : "\"" + autor + "\"";
		String corpo = "{\"titulo\":%s,\"autor\":%s,\"lido\":true}".formatted(tituloJson, autorJson);
		mvc.perform(request(HttpMethod.valueOf(metodo), caminho).contentType(MediaType.APPLICATION_JSON).content(corpo))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.erro").value("Informe titulo e autor validos."));
		assertThat(repository.count()).isEqualTo(existente == null ? 0 : 1);
		if (existente != null) {
			Livro preservado = repository.findById(existente.getId()).orElseThrow();
			assertThat(preservado.getTitulo()).isEqualTo("Original");
			assertThat(preservado.getAutor()).isEqualTo("Autor");
			assertThat(preservado.isLido()).isFalse();
		}
	}

	/**
	 * Combina os dois verbos com seis entradas inválidas para cobrir ambas as propriedades.
	 *
	 * @return argumentos dos doze cenários de validação HTTP
	 */
	static Stream<Arguments> dadosInvalidos() {
		return Stream.of("POST", "PUT").flatMap(metodo -> Stream.of(
				Arguments.of(metodo, "", "Autor"),
				Arguments.of(metodo, " ", "Autor"),
				Arguments.of(metodo, null, "Autor"),
				Arguments.of(metodo, "Titulo", ""),
				Arguments.of(metodo, "Titulo", " "),
				Arguments.of(metodo, "Titulo", null)));
	}

}
