package br.com.desafio.minhaestante.livro;

import java.util.Map;

import jakarta.validation.Validation;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import br.com.desafio.minhaestante.exception.ApiExceptionHandler;
import br.com.desafio.minhaestante.exception.LivroNaoEncontradoException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica isoladamente a validação do DTO e as respostas produzidas pelo
 * tratamento de exceções, sem iniciar servidor ou cadastrar dados no banco.
 * As verificações com {@code assertThat} comparam resultados reais e esperados.
 *
 * <p>Os métodos anotados com {@code @Test} são executados pelo JUnit. As chamadas
 * diretas ao tratador verificam código HTTP e corpo, mas não simulam requisições
 * HTTP nem comprovam, sozinhas, o encaminhamento de exceções pelo Spring MVC.</p>
 */
class ComponentesLivroTests {

	/**
	 * Confirma que título em branco e autor nulo geram exatamente as mensagens
	 * contratadas, enquanto uma requisição válida não gera violações.
	 * A validação é acionada explicitamente pelo validador Jakarta, e a fábrica
	 * é fechada automaticamente ao sair do bloco de gerenciamento de recursos.
	 * Isso protege o contrato sem depender de uma futura rota REST.
	 */
	@Test
	void rejeitaTituloEAutorEmBrancoComAsMensagensDoContrato() {
		try (var factory = Validation.buildDefaultValidatorFactory()) {
			var validator = factory.getValidator();
			var violations = validator.validate(new LivroRequisicao(" ", null, false));

			assertThat(violations).extracting(violation -> violation.getMessage())
					.containsExactlyInAnyOrder("Titulo obrigatorio", "Autor obrigatorio");
			assertThat(validator.validate(new LivroRequisicao("Titulo", "Autor", false))).isEmpty();
		}
	}

	/**
	 * Confirma que a exceção de um livro ausente resulta em 404 e que a mensagem
	 * identifica o livro procurado na propriedade JSON esperada, {@code erro}.
	 * Compara o mapa completo para detectar alterações no contrato de resposta.
	 */
	@Test
	void retorna404ComAMensagemDoLivroNaoEncontrado() {
		var response = new ApiExceptionHandler().tratarLivroNaoEncontrado(new LivroNaoEncontradoException(42L));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isEqualTo(Map.of("erro", "Livro com id 42 nao encontrado."));
	}

	/**
	 * Confirma que o tratamento de validação produz 400 e a orientação fixa do
	 * curso, preservando a distinção entre dados inválidos e recurso ausente.
	 */
	@Test
	void retorna400ComAMensagemDeValidacao() {
		var response = new ApiExceptionHandler().tratarValidacao();

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isEqualTo(Map.of("erro", "Informe titulo e autor validos."));
	}

}
