package br.com.desafio.minhaestante.livro;

import jakarta.validation.constraints.NotBlank;

/**
 * Transporta os dados de entrada, separando o contrato de requisição da entidade
 * persistida. Esse é o papel de um DTO: transferir dados entre partes do sistema.
 *
 * <p>Um {@code record} declara seus componentes e recebe do Java um construtor,
 * métodos de leitura como {@code titulo()}, além de igualdade, código de
 * dispersão e representação textual. Seus campos são finais e não há setters;
 * isso não significa imutabilidade profunda para qualquer tipo de componente.</p>
 *
 * <p>{@code @NotBlank} rejeita nulo, texto vazio e texto formado apenas por
 * espaços em branco. As mensagens são usadas quando a Jakarta Validation é
 * acionada; criar o record com {@code new} não executa a validação automaticamente.
 * Não há restrição adicional para {@code lido}.</p>
 *
 * @param titulo título obrigatório recebido na requisição
 * @param autor autor obrigatório recebido na requisição
 * @param lido indica se o livro foi lido
 */
public record LivroRequisicao(
		@NotBlank(message = "Titulo obrigatorio") String titulo,
		@NotBlank(message = "Autor obrigatorio") String autor,
		boolean lido) {
}
