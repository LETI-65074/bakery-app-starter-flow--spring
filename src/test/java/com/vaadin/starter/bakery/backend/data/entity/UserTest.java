package com.vaadin.starter.bakery.backend.data.entity;

import org.junit.Assert;
import org.junit.Test;

/**
 * Serviço responsável por determinar e simular o estado de pedidos.
 *
 * <p>Esta classe gera estados aleatórios de pedidos de acordo com a data de vencimento,
 * permitindo simular diferentes situações como pedido novo, pronto, entregue, com problema
 * ou cancelado. Pode ser utilizada em testes, geração de dados fictícios ou simulações
 * de fluxo de pedidos.
 *
 * <h2>Lógica principal</h2>
 * <ul>
 *   <li>Datas já vencidas têm alta probabilidade de estarem entregues ou canceladas.</li>
 *   <li>Datas futuras distantes indicam pedido novo.</li>
 *   <li>Datas próximas podem resultar em estados intermediários como READY ou PROBLEM,
 *       de acordo com probabilidades definidas.</li>
 * </ul>
 *
 * <p>Exemplo de uso:
 * <pre>{@code
 * OrderService service = new OrderService();
 * OrderState state = service.getRandomState(LocalDate.now().plusDays(1));
 * System.out.println("Estado gerado: " + state);
 * }</pre>
 *
 * @author SeuNome
 * @version 1.0
 */
public class OrderService {

    private final Random random = new Random();

    /**
     * Gera um estado aleatório para um pedido com base na data de vencimento informada.
     *
     * <p>As probabilidades variam conforme a diferença entre a data atual e a data de vencimento:
     *
     * <ul>
     *   <li><b>Data de vencimento anterior a hoje:</b><br>
     *       90% de chance de {@link OrderState#DELIVERED}, 10% de {@link OrderState#CANCELLED}.
     *   </li>
     *
     *   <li><b>Vencimento em mais de 2 dias:</b><br>
     *       Sempre retorna {@link OrderState#NEW}.
     *   </li>
     *
     *   <li><b>Vencimento entre 1 e 2 dias a partir de hoje:</b><br>
     *       80% {@link OrderState#NEW}, 10% {@link OrderState#PROBLEM}, 10% {@link OrderState#CANCELLED}.
     *   </li>
     *
     *   <li><b>Vencimento para hoje ou amanhã:</b><br>
     *       60% {@link OrderState#READY}, 20% {@link OrderState#DELIVERED},
     *       10% {@link OrderState#PROBLEM}, 10% {@link OrderState#CANCELLED}.
     *   </li>
     * </ul>
     *
     * @param due data de vencimento do pedido
     * @return um {@link OrderState} selecionado aleatoriamente de acordo com as regras acima
     */

public class UserTest {

	@Test
	public void equalsTest() {
		User o1 = new User();
		o1.setPasswordHash("hash");
		o1.setEmail("abc@vaadin.com");
		o1.setFirstName("first");
		o1.setLastName("last");
		o1.setRole("role");

		User o2 = new User();
		o2.setPasswordHash("anotherhash");
		o2.setEmail("abc@vaadin.com");
		o2.setFirstName("anotherName");
		o2.setLastName("last");
		o2.setRole("role");

		Assert.assertNotEquals(o1, o2);

		o2.setFirstName("first");
		Assert.assertEquals(o1, o2);
	}
}
