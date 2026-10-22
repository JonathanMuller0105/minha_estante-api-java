package br.com.desafio.minhaestante.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centraliza a tradução de exceções da camada web em respostas HTTP padronizadas.
 * {@code @RestControllerAdvice} registra um tratamento compartilhado entre
 * controladores e faz os valores retornados comporem o corpo da resposta.
 * A anotação não cria um controlador com rotas de negócio.
 *
 * <p>{@code ResponseEntity} reúne o código HTTP, os cabeçalhos e o corpo da
 * resposta. Aqui, o corpo é um mapa de textos, convertido em JSON pela
 * infraestrutura web, com a propriedade {@code erro}.</p>
 */
@RestControllerAdvice
public class ApiExceptionHandler {

	/**
	 * Trata a ausência de um livro. {@code @ExceptionHandler} associa o tipo da
	 * exceção a este método, e o Spring fornece a exceção capturada como argumento.
	 * O código HTTP 404 indica que o recurso solicitado não foi encontrado.
	 *
	 * @param exception exceção que contém a mensagem com o identificador procurado
	 * @return resposta 404 com a mensagem na propriedade {@code erro}
	 */
	@ExceptionHandler(LivroNaoEncontradoException.class)
	public ResponseEntity<Map<String, String>> tratarLivroNaoEncontrado(LivroNaoEncontradoException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", exception.getMessage()));
	}

	/**
	 * Trata uma falha de validação de argumentos sinalizada pelo Spring MVC.
	 * A associação com {@code MethodArgumentNotValidException} é definida na
	 * anotação; o método não precisa receber a exceção, pois usa mensagem fixa.
	 * O código HTTP 400 informa que a requisição contém dados inválidos.
	 *
	 * @return resposta 400 com a orientação padronizada sobre título e autor
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> tratarValidacao() {
		return ResponseEntity.badRequest().body(Map.of("erro", "Informe titulo e autor validos."));
	}

}
