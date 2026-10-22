package br.com.desafio.minhaestante.livro;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Expõe as seis operações REST e delega o trabalho ao {@link LivroService}.
 * {@code @RestController} registra o componente web e faz os retornos comporem
 * o corpo da resposta, convertido em JSON quando há um objeto.
 * {@code @RequestMapping} define o caminho comum {@code /api/livros}.
 *
 * <p>{@code ResponseEntity} permite definir explicitamente código HTTP, corpo
 * e cabeçalhos. Exceções propagam para o tratamento central já existente, que
 * produz 400 para validação inválida e 404 para livro não encontrado.</p>
 */
@RestController
@RequestMapping("/api/livros")
public class LivroController {

	/** Serviço usado pelo controlador, sem acesso direto ao repositório. */
	private final LivroService service;

	/**
	 * Recebe o serviço pelo único construtor. A injeção feita pelo Spring explicita
	 * a dependência e mantém a referência disponível durante a vida do controlador.
	 *
	 * @param service serviço responsável pelas operações da aplicação
	 */
	public LivroController(LivroService service) {
		this.service = service;
	}

	/**
	 * {@code @GetMapping} associa a consulta da coleção ao método HTTP GET.
	 *
	 * @return resposta 200 com a lista JSON, inclusive quando vazia
	 */
	@GetMapping
	public ResponseEntity<List<Livro>> listarTodos() {
		return ResponseEntity.ok(service.listarTodos());
	}

	/**
	 * Consulta um recurso. {@code @PathVariable} vincula o trecho {@code {id}}
	 * da URL ao argumento Java, convertido para {@code Long} pelo Spring.
	 *
	 * @param id identificador presente no caminho da requisição
	 * @return resposta 200 com o livro; a ausência é tratada como 404
	 */
	@GetMapping("/{id}")
	public ResponseEntity<Livro> buscarPorId(@PathVariable Long id) {
		return ResponseEntity.ok(service.buscarPorId(id));
	}

	/**
	 * {@code @PostMapping} recebe o cadastro via POST. {@code @RequestBody}
	 * converte o JSON em DTO; {@code @Valid} solicita a Jakarta Validation antes
	 * da execução do método, aplicando as restrições de título e autor.
	 *
	 * @param dados dados validados do novo livro
	 * @return resposta 201 com o livro e Location indicando onde consultá-lo
	 */
	@PostMapping
	public ResponseEntity<Livro> cadastrar(@Valid @RequestBody LivroRequisicao dados) {
		Livro livro = service.cadastrar(dados);
		// Usa a URL da requisição e o ID gerado para identificar o recurso criado.
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}").buildAndExpand(livro.getId()).toUri();
		return ResponseEntity.created(location).body(livro);
	}

	/**
	 * {@code @PutMapping} associa PUT à substituição dos campos editáveis.
	 * O ID vem da URL; o corpo é validado antes de chegar ao serviço.
	 *
	 * @param id identificador do livro existente
	 * @param dados novos dados do livro
	 * @return resposta 200 com o livro atualizado; erros usam 400 ou 404
	 */
	@PutMapping("/{id}")
	public ResponseEntity<Livro> atualizar(@PathVariable Long id, @Valid @RequestBody LivroRequisicao dados) {
		return ResponseEntity.ok(service.atualizar(id, dados));
	}

	/**
	 * {@code @PatchMapping} representa uma alteração parcial: somente a marcação
	 * de leitura. Não recebe corpo JSON, pois o próprio caminho define a ação.
	 *
	 * @param id identificador do livro a marcar como lido
	 * @return resposta 200 com o livro; a ausência é tratada como 404
	 */
	@PatchMapping("/{id}/lido")
	public ResponseEntity<Livro> marcarComoLido(@PathVariable Long id) {
		return ResponseEntity.ok(service.marcarComoLido(id));
	}

	/**
	 * {@code @DeleteMapping} associa DELETE à exclusão. O código 204 confirma
	 * o sucesso sem corpo de resposta; um livro ausente resulta em 404.
	 *
	 * @param id identificador do livro a excluir
	 * @return resposta 204 sem conteúdo
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> excluir(@PathVariable Long id) {
		service.excluir(id);
		return ResponseEntity.noContent().build();
	}

}
