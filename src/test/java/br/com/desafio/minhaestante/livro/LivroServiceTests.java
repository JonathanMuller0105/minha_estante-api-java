package br.com.desafio.minhaestante.livro;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import br.com.desafio.minhaestante.exception.LivroNaoEncontradoException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifica as operações do serviço isoladamente do banco. {@code @ExtendWith}
 * ativa o Mockito, e {@code @Mock} fornece um repositório simulado: os resultados
 * são controlados pelo teste para exercitar também situações de ausência.
 */
@ExtendWith(MockitoExtension.class)
class LivroServiceTests {

	/** Substitui apenas a dependência de persistência, mantendo o serviço real. */
	@Mock
	private LivroRepository repository;

	/** Objeto real sob teste, construído com a dependência simulada. */
	private LivroService service;

	/** Livro de referência para conferir alterações e preservação da identidade. */
	private Livro livro;

	/** Prepara objetos independentes antes de cada cenário, sem adicionar setId à entidade. */
	@BeforeEach
	void preparar() {
		service = new LivroService(repository);
		livro = new Livro("Titulo original", "Autor original", false);
		// Simula um ID já atribuído pelo banco; a reflexão fica restrita ao teste.
		ReflectionTestUtils.setField(livro, "id", 7L);
	}

	/** Uma coleção sem registros deve ser devolvida vazia, sem criar dados. */
	@Test
	void listaVaziaQuandoNaoExistemLivros() {
		when(repository.findAll()).thenReturn(List.of());
		assertThat(service.listarTodos()).isEmpty();
		verify(repository).findAll();
	}

	/** A consulta deve devolver a entidade encontrada pelo repositório. */
	@Test
	void consultaLivroExistente() {
		when(repository.findById(7L)).thenReturn(Optional.of(livro));
		assertThat(service.buscarPorId(7L)).isSameAs(livro);
	}

	/** Um Optional vazio deve ser traduzido para a exceção específica do projeto. */
	@Test
	void consultaAusenteLancaExcecao() {
		when(repository.findById(7L)).thenReturn(Optional.empty());
		assertThatThrownBy(() -> service.buscarPorId(7L))
				.isInstanceOf(LivroNaoEncontradoException.class)
				.hasMessage("Livro com id 7 nao encontrado.");
	}

	/** Confere os dados enviados ao banco e o retorno da entidade efetivamente salva. */
	@Test
	void cadastraComOsDadosDaRequisicao() {
		when(repository.save(any(Livro.class))).thenAnswer(invocation -> {
			Livro novo = invocation.getArgument(0);
			assertThat(novo.getId()).isNull();
			assertThat(novo.getTitulo()).isEqualTo("Novo titulo");
			assertThat(novo.getAutor()).isEqualTo("Novo autor");
			assertThat(novo.isLido()).isTrue();
			return livro;
		});
		assertThat(service.cadastrar(new LivroRequisicao("Novo titulo", "Novo autor", true)))
				.isSameAs(livro);
	}

	/** A atualização substitui os três campos editáveis, preservando o ID existente. */
	@Test
	void atualizaDadosSemSubstituirIdentidade() {
		when(repository.findById(7L)).thenReturn(Optional.of(livro));
		when(repository.save(livro)).thenReturn(livro);
		assertThat(service.atualizar(7L, new LivroRequisicao("Novo titulo", "Novo autor", true)))
				.isSameAs(livro);
		assertThat(livro.getId()).isEqualTo(7L);
		assertThat(livro.getTitulo()).isEqualTo("Novo titulo");
		assertThat(livro.getAutor()).isEqualTo("Novo autor");
		assertThat(livro.isLido()).isTrue();
		verify(repository).save(livro);
	}

	/** A marcação parcial deve preservar título, autor e ID, inclusive quando repetida. */
	@Test
	void marcaComoLidoPreservandoOutrosCampos() {
		when(repository.findById(7L)).thenReturn(Optional.of(livro));
		when(repository.save(livro)).thenReturn(livro);
		assertThat(service.marcarComoLido(7L)).isSameAs(livro);
		assertThat(livro.isLido()).isTrue();
		service.marcarComoLido(7L);
		assertThat(livro.isLido()).isTrue();
		assertThat(livro.getId()).isEqualTo(7L);
		assertThat(livro.getTitulo()).isEqualTo("Titulo original");
		assertThat(livro.getAutor()).isEqualTo("Autor original");
	}

	/** A exclusão deve usar a entidade encontrada, conforme o contrato do serviço. */
	@Test
	void excluiLivroEncontrado() {
		when(repository.findById(7L)).thenReturn(Optional.of(livro));
		service.excluir(7L);
		verify(repository).delete(livro);
	}

	/**
	 * Cada operação de alteração deve falhar antes de salvar ou excluir um ID ausente.
	 * {@code @ParameterizedTest} repete o cenário para cada valor fornecido.
	 *
	 * @param operacao operação de negócio exercitada
	 */
	@ParameterizedTest
	@ValueSource(strings = {"atualizar", "marcarComoLido", "excluir"})
	void naoAlteraLivroInexistente(String operacao) {
		when(repository.findById(7L)).thenReturn(Optional.empty());
		assertThatThrownBy(() -> {
			switch (operacao) {
				case "atualizar" -> service.atualizar(7L, new LivroRequisicao("Titulo", "Autor", false));
				case "marcarComoLido" -> service.marcarComoLido(7L);
				case "excluir" -> service.excluir(7L);
				default -> throw new AssertionError("Operacao de teste desconhecida");
			}
		}).isInstanceOf(LivroNaoEncontradoException.class).hasMessage("Livro com id 7 nao encontrado.");
		verify(repository, never()).save(any(Livro.class));
		verify(repository, never()).delete(any(Livro.class));
	}

}
