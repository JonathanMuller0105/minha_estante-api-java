package br.com.desafio.minhaestante.livro;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.desafio.minhaestante.exception.LivroNaoEncontradoException;

/**
 * Coordena as operações sobre livros, sem depender dos detalhes de HTTP.
 * O fluxo Controller → Service → Repository separa a entrada web, as operações
 * da aplicação e o acesso ao banco. {@code @Service} registra esta classe como
 * componente gerenciado pelo Spring.
 */
@Service
public class LivroService {

	/** Dependência de persistência; a referência não pode ser reatribuída. */
	private final LivroRepository repository;

	/**
	 * Recebe o repositório por injeção de dependência. O Spring fornece o objeto
	 * pelo único construtor, dispensando {@code @Autowired} e instanciação manual.
	 *
	 * @param repository contrato de persistência dos livros
	 */
	public LivroService(LivroRepository repository) {
		this.repository = repository;
	}

	/**
	 * Consulta os livros sem aplicar filtros ou paginação.
	 *
	 * @return lista dos livros, possivelmente vazia
	 */
	public List<Livro> listarTodos() {
		return repository.findAll();
	}

	/**
	 * Localiza um livro ou sinaliza sua ausência. {@code findById} devolve um
	 * {@code Optional}, que representa um valor presente ou ausente.
	 * {@code orElseThrow} devolve o valor presente; caso contrário, executa a
	 * expressão lambda {@code () -> ...} para criar a exceção sob demanda.
	 *
	 * @param id identificador procurado
	 * @return livro encontrado
	 * @throws LivroNaoEncontradoException quando o identificador não existe
	 */
	public Livro buscarPorId(Long id) {
		return repository.findById(id).orElseThrow(() -> new LivroNaoEncontradoException(id));
	}

	/**
	 * Converte os dados de entrada em entidade e solicita sua persistência.
	 *
	 * @param dados título, autor e estado de leitura recebidos
	 * @return entidade salva, com identificador atribuído pelo banco
	 */
	public Livro cadastrar(LivroRequisicao dados) {
		Livro livro = new Livro(dados.titulo(), dados.autor(), dados.lido());
		return repository.save(livro);
	}

	/**
	 * Substitui os três dados editáveis do livro encontrado. Reutilizar a entidade
	 * existente preserva seu identificador; nenhum novo ID é atribuído pelo código.
	 *
	 * @param id identificador do livro a atualizar
	 * @param dados novos valores dos campos editáveis
	 * @return livro salvo após a atualização
	 * @throws LivroNaoEncontradoException quando o livro não existe
	 */
	public Livro atualizar(Long id, LivroRequisicao dados) {
		Livro livro = buscarPorId(id);
		livro.setTitulo(dados.titulo());
		livro.setAutor(dados.autor());
		livro.setLido(dados.lido());
		return repository.save(livro);
	}

	/**
	 * Marca somente o estado de leitura. Essa alteração parcial, exposta por PATCH,
	 * preserva título, autor e identificador. Repeti-la mantém o estado como lido.
	 *
	 * @param id identificador do livro
	 * @return livro salvo com {@code lido} igual a verdadeiro
	 * @throws LivroNaoEncontradoException quando o livro não existe
	 */
	public Livro marcarComoLido(Long id) {
		Livro livro = buscarPorId(id);
		livro.setLido(true);
		return repository.save(livro);
	}

	/**
	 * Confirma a existência antes de excluir, permitindo distinguir livro ausente
	 * de exclusão bem-sucedida no tratamento de exceções da camada web.
	 *
	 * @param id identificador do livro a excluir
	 * @throws LivroNaoEncontradoException quando o livro não existe
	 */
	public void excluir(Long id) {
		Livro livro = buscarPorId(id);
		repository.delete(livro);
	}

}
