package br.com.desafio.minhaestante.exception;

/**
 * Sinaliza que uma operação não encontrou o livro solicitado.
 * A herança com {@code extends RuntimeException} especializa uma exceção não
 * verificada: o compilador não exige declará-la com {@code throws} nem capturá-la.
 *
 * <p>Uma exceção interrompe o fluxo normal até encontrar um tratamento adequado.
 * Esta classe descreve o problema; a tradução para HTTP 404 fica no
 * {@link ApiExceptionHandler}, mantendo separadas essas responsabilidades.</p>
 */
public class LivroNaoEncontradoException extends RuntimeException {

	/**
	 * Cria a exceção com o identificador procurado. A chamada {@code super}
	 * entrega a mensagem ao construtor da classe pai para disponibilizá-la por
	 * {@code getMessage()}.
	 *
	 * @param id identificador do livro que não foi encontrado
	 */
	public LivroNaoEncontradoException(Long id) {
		super("Livro com id " + id + " nao encontrado.");
	}

}
