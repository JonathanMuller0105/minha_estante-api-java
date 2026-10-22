package br.com.desafio.minhaestante.livro;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Contrato de acesso à persistência dos livros.
 * Uma interface declara operações disponíveis sem fornecer aqui uma classe
 * concreta que as implemente. O Spring Data cria a implementação em execução.
 *
 * <p>A herança de interface com {@code extends JpaRepository} disponibiliza
 * operações de persistência, consulta, exclusão e paginação já definidas pelo
 * Spring Data JPA. Os parâmetros genéricos indicam a entidade {@code Livro} e
 * o tipo de seu identificador, {@code Long}. Herdar essas operações não cria
 * rotas HTTP nem exige escrever consultas personalizadas neste contrato.</p>
 */
public interface LivroRepository extends JpaRepository<Livro, Long> {
}
