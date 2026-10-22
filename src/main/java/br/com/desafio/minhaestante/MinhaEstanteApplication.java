package br.com.desafio.minhaestante;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada da aplicação: inicia o contêiner que gerencia os componentes
 * Spring e prepara a infraestrutura web e de persistência.
 *
 * <p>{@code @SpringBootApplication} reúne a configuração da aplicação, a
 * configuração automática baseada nas dependências e a busca de componentes.
 * Por estar no pacote principal, permite encontrar componentes nos subpacotes,
 * como o tratamento de exceções. A configuração automática também usa esse
 * pacote como referência para descobrir entidades e repositórios JPA.</p>
 */
@SpringBootApplication
public class MinhaEstanteApplication {

	/**
	 * Inicia o contexto Spring e o servidor embutido. O método é estático para
	 * que a JVM possa executá-lo sem criar previamente um objeto desta classe.
	 *
	 * @param args argumentos de inicialização recebidos pela linha de comando
	 */
	public static void main(String[] args) {
		SpringApplication.run(MinhaEstanteApplication.class, args);
	}

}
