package br.com.desafio.minhaestante.livro;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Representa um livro da estante e seu estado de leitura.
 * Uma classe define a estrutura e os comportamentos; cada objeto criado a partir
 * dela representa um livro específico, com seus próprios valores.
 *
 * <p>{@code @Entity} identifica uma entidade JPA, cujos objetos podem ser
 * associados a registros do banco pelo provedor de persistência, o Hibernate.
 * {@code @Table} define explicitamente o nome da tabela: {@code livros}.</p>
 *
 * <p>Os atributos privados aplicam encapsulamento: o acesso externo ocorre pelos
 * métodos públicos. Como {@code @Id} está no atributo, a JPA usa acesso direto
 * aos campos e não precisa de um método público para alterar o identificador.</p>
 */
@Entity
@Table(name = "livros")
public class Livro {

	/**
	 * Identificador único do registro. {@code @Id} define a chave primária e
	 * {@code @GeneratedValue(IDENTITY)} delega sua geração à coluna de identidade
	 * do banco. O tipo {@code Long} permite que o valor seja nulo antes da inserção.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * Título do livro. {@code @Column} orienta o mapeamento e a geração da coluna:
	 * não aceita nulo e comporta até 120 caracteres. Essa restrição do banco não
	 * substitui a validação da requisição e não impede, por si só, texto em branco.
	 */
	@Column(nullable = false, length = 120)
	private String titulo;

	/** Autor do livro, mapeado para uma coluna não nula de até 120 caracteres. */
	@Column(nullable = false, length = 120)
	private String autor;

	/** Estado de leitura; por ser {@code boolean} primitivo, não representa nulo. */
	private boolean lido;

	/**
	 * Construtor sem argumentos exigido pela JPA para recriar objetos ao consultar
	 * registros. A visibilidade protegida atende à JPA e restringe seu uso externo.
	 */
	protected Livro() {
	}

	/**
	 * Cria um objeto com os dados do livro. Construir o objeto em memória não o
	 * salva automaticamente; a persistência é uma operação separada.
	 *
	 * @param titulo título do livro
	 * @param autor autor do livro
	 * @param lido indica se o livro já foi lido
	 */
	public Livro(String titulo, String autor, boolean lido) {
		this.titulo = titulo;
		this.autor = autor;
		this.lido = lido;
	}

	/**
	 * Expõe a identidade para leitura, sem oferecer um {@code setId()} que permita
	 * substituí-la externamente.
	 *
	 * @return identificador gerado pelo banco, ou nulo antes de sua atribuição
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Consulta o título por meio de um método de leitura, chamado de getter.
	 *
	 * @return título atual
	 */
	public String getTitulo() {
		return titulo;
	}

	/**
	 * Atualiza o título por meio de um método de escrita, chamado de setter.
	 * Este método apenas atribui o valor; não executa validação nem salva no banco.
	 *
	 * @param titulo novo título
	 */
	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	/**
	 * Permite consultar o autor sem acesso direto ao atributo privado.
	 *
	 * @return autor atual
	 */
	public String getAutor() {
		return autor;
	}

	/**
	 * Atualiza o autor em memória; não realiza persistência por conta própria.
	 *
	 * @param autor novo autor
	 */
	public void setAutor(String autor) {
		this.autor = autor;
	}

	/**
	 * Consulta o estado de leitura. O prefixo {@code is} é uma convenção de
	 * métodos de leitura para propriedades booleanas.
	 *
	 * @return verdadeiro se o livro foi marcado como lido
	 */
	public boolean isLido() {
		return lido;
	}

	/**
	 * Altera a marcação de leitura do objeto.
	 *
	 * @param lido novo estado de leitura
	 */
	public void setLido(boolean lido) {
		this.lido = lido;
	}

}
