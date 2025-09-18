```java
package com.vaadin.starter.bakery.app;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.starter.bakery.backend.data.OrderState;
import com.vaadin.starter.bakery.backend.data.Role;
import com.vaadin.starter.bakery.backend.data.entity.Customer;
import com.vaadin.starter.bakery.backend.data.entity.HistoryItem;
import com.vaadin.starter.bakery.backend.data.entity.Order;
import com.vaadin.starter.bakery.backend.data.entity.OrderItem;
import com.vaadin.starter.bakery.backend.data.entity.PickupLocation;
import com.vaadin.starter.bakery.backend.data.entity.Product;
import com.vaadin.starter.bakery.backend.data.entity.User;
import com.vaadin.starter.bakery.backend.repositories.OrderRepository;
import com.vaadin.starter.bakery.backend.repositories.PickupLocationRepository;
import com.vaadin.starter.bakery.backend.repositories.ProductRepository;
import com.vaadin.starter.bakery.backend.repositories.UserRepository;

/**
 * DataGenerator is responsible for populating the application database with demo data.
 * It creates users, products, pickup locations, and a realistic set of demo orders for testing purposes.
 */
@SpringComponent
public class DataGenerator implements HasLogger {

	private static final String[] FILLING = new String[] { "Strawberry", "Chocolate", "Blueberry", "Raspberry",
			"Vanilla" };
	private static final String[] TYPE = new String[] { "Cake", "Pastry", "Tart", "Muffin", "Biscuit", "Bread", "Bagel",
			"Bun", "Brownie", "Cookie", "Cracker", "Cheese Cake" };
	private static final String[] FIRST_NAME = new String[] { "Ori", "Amanda", "Octavia", "Laurel", "Lael", "Delilah",
			"Jason", "Skyler", "Arsenio", "Haley", "Lionel", "Sylvia", "Jessica", "Lester", "Ferdinand", "Elaine",
			"Griffin", "Kerry", "Dominique" };
	private static final String[] LAST_NAME = new String[] { "Carter", "Castro", "Rich", "Irwin", "Moore", "Hendricks",
			"Huber", "Patton", "Wilkinson", "Thornton", "Nunez", "Macias", "Gallegos", "Blevins", "Mejia", "Pickett",
			"Whitney", "Farmer", "Henry", "Chen", "Macias", "Rowland", "Pierce", "Cortez", "Noble", "Howard", "Nixon",
			"Mcbride", "Leblanc", "Russell", "Carver", "Benton", "Maldonado", "Lyons" };

	private final Random random = new Random(1L);

	private OrderRepository orderRepository;
	private UserRepository userRepository;
	private ProductRepository productRepository;
	private PickupLocationRepository pickupLocationRepository;
	private PasswordEncoder passwordEncoder;

	/**
	 * Constructs a new DataGenerator with required repositories and password encoder.
	 *
	 * @param orderRepository            the order repository
	 * @param userRepository             the user repository
	 * @param productRepository          the product repository
	 * @param pickupLocationRepository   the pickup location repository
	 * @param passwordEncoder            the password encoder
	 */
	@Autowired
	public DataGenerator(OrderRepository orderRepository, UserRepository userRepository,
			ProductRepository productRepository, PickupLocationRepository pickupLocationRepository,
			PasswordEncoder passwordEncoder) {
		this.orderRepository = orderRepository;
		this.userRepository = userRepository;
		this.productRepository = productRepository;
		this.pickupLocationRepository = pickupLocationRepository;
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * Loads demo data into the database if not already populated.
	 * This method is automatically called after dependency injection.
	 */
	@PostConstruct
	public void loadData() {
		if (userRepository.count() != 0L) {
			getLogger().info("Using existing database");
			return;
		}

		getLogger().info("Generating demo data");

		getLogger().info("... generating users");
		User baker = createBaker(userRepository, passwordEncoder);
		User barista = createBarista(userRepository, passwordEncoder);
		createAdmin(userRepository, passwordEncoder);
		// A set of products without constrains that can be deleted
		createDeletableUsers(userRepository, passwordEncoder);

		getLogger().info("... generating products");
		// A set of products that will be used for creating orders.
		Supplier<Product> productSupplier = createProducts(productRepository, 8);
		// A set of products without relationships that can be deleted
		createProducts(productRepository, 4);

		getLogger().info("... generating pickup locations");
		Supplier<PickupLocation> pickupLocationSupplier = createPickupLocations(pickupLocationRepository);

		getLogger().info("... generating orders");
		createOrders(orderRepository, productSupplier, pickupLocationSupplier, barista, baker);

		getLogger().info("Generated demo data");
	}

	/**
	 * Fills the given customer with random full name and phone number.
	 * Occasionally marks the customer as "Very important".
	 *
	 * @param customer the customer to fill
	 */
	private void fillCustomer(Customer customer) {
		String first = getRandom(FIRST_NAME);
		String last = getRandom(LAST_NAME);
		customer.setFullName(first + " " + last);
		customer.setPhoneNumber(getRandomPhone());
		if (random.nextInt(10) == 0) {
			customer.setDetails("Very important customer");
		}
	}

	/**
	 * Generates a random phone number.
	 *
	 * @return random phone number string
	 */
	private String getRandomPhone() {
		return "+1-555-" + String.format("%04d", random.nextInt(10000));
	}

	/**
	 * Creates demo orders for a date range and saves them to the repository.
	 *
	 * @param orderRepo               the order repository
	 * @param productSupplier         supplier for random products
	 * @param pickupLocationSupplier  supplier for random pickup locations
	 * @param barista                 barista user
	 * @param baker                   baker user
	 */
	private void createOrders(OrderRepository orderRepo, Supplier<Product> productSupplier,
			Supplier<PickupLocation> pickupLocationSupplier, User barista, User baker) {
		int yearsToInclude = 2;
		LocalDate now = LocalDate.now();
		LocalDate oldestDate = LocalDate.of(now.getYear() - yearsToInclude, 1, 1);
		LocalDate newestDate = now.plusMonths(1L);

		// Create first today's order
		Order order = createOrder(productSupplier, pickupLocationSupplier, barista, baker, now);
		order.setDueTime(LocalTime.of(8, 0));
		order.setHistory(order.getHistory().subList(0, 1));
		order.setItems(order.getItems().subList(0, 1));
		orderRepo.save(order);

		for (LocalDate dueDate = oldestDate; dueDate.isBefore(newestDate); dueDate = dueDate.plusDays(1)) {
			// Create a slightly upwards trend - everybody wants to be
			// successful
			int relativeYear = dueDate.getYear() - now.getYear() + yearsToInclude;
			int relativeMonth = relativeYear * 12 + dueDate.getMonthValue();
			double multiplier = 1.0 + 0.03 * relativeMonth;
			int ordersThisDay = (int) (random.nextInt(10) + 1 * multiplier);
			for (int i = 0; i < ordersThisDay; i++) {
				orderRepo.save(createOrder(productSupplier, pickupLocationSupplier, barista, baker, dueDate));
			}
		}
	}

	/**
	 * Creates a random order for a given date, including items and order history.
	 *
	 * @param productSupplier         supplier for random products
	 * @param pickupLocationSupplier  supplier for random pickup locations
	 * @param barista                 barista user
	 * @param baker                   baker user
	 * @param dueDate                 due date for the order
	 * @return newly created order
	 */
	private Order createOrder(Supplier<Product> productSupplier, Supplier<PickupLocation> pickupLocationSupplier,
			User barista, User baker, LocalDate dueDate) {
		Order order = new Order(barista);

		fillCustomer(order.getCustomer());
		order.setPickupLocation(pickupLocationSupplier.get());
		order.setDueDate(dueDate);
		order.setDueTime(getRandomDueTime());
		order.changeState(barista, getRandomState(order.getDueDate()));

		int itemCount = random.nextInt(3);
		List<OrderItem> items = new ArrayList<>();
		for (int i = 0; i <= itemCount; i++) {
			OrderItem item = new OrderItem();
			Product product;
			do {
				product = productSupplier.get();
			} while (containsProduct(items, product));
			item.setProduct(product);
			item.setQuantity(random.nextInt(10) + 1);
			if (random.nextInt(5) == 0) {
				if (random.nextBoolean()) {
					item.setComment("Lactose free");
				} else {
					item.setComment("Gluten free");
				}
			}
			items.add(item);
		}
		order.setItems(items);

		order.setHistory(createOrderHistory(order, barista, baker));

		return order;
	}

	/**
	 * Creates the order history for a given order, simulating different order state changes.
	 *
	 * @param order    the order
	 * @param barista  barista user
	 * @param baker    baker user
	 * @return list of history items for the order
	 */
	private List<HistoryItem> createOrderHistory(Order order, User barista, User baker) {
		ArrayList<HistoryItem> history = new ArrayList<>();
		HistoryItem item = new HistoryItem(barista, "Order placed");
		item.setNewState(OrderState.NEW);
		LocalDateTime orderPlaced = order.getDueDate().minusDays(random.nextInt(5) + 2L).atTime(random.nextInt(10) + 7,
				00);
		item.setTimestamp(orderPlaced);
		history.add(item);
		if (order.getState() == OrderState.CANCELLED) {
			item = new HistoryItem(barista, "Order cancelled");
			item.setNewState(OrderState.CANCELLED);
			item.setTimestamp(orderPlaced.plusDays(random
					.nextInt((int) orderPlaced.until(order.getDueDate().atTime(order.getDueTime()), ChronoUnit.DAYS))));
			history.add(item);
		} else if (order.getState() == OrderState.CONFIRMED || order.getState() == OrderState.DELIVERED
				|| order.getState() == OrderState.PROBLEM || order.getState() == OrderState.READY) {
			item = new HistoryItem(baker, "Order confirmed");
			item.setNewState(OrderState.CONFIRMED);
			item.setTimestamp(orderPlaced.plusDays(random.nextInt(2)).plusHours(random.nextInt(5)));
			history.add(item);

			if (order.getState() == OrderState.PROBLEM) {
				item = new HistoryItem(baker, "Can't make it. Did not get any ingredients this morning");
				item.setNewState(OrderState.PROBLEM);
				item.setTimestamp(order.getDueDate().atTime(random.nextInt(4) + 4, 0));
				history.add(item);
			} else if (order.getState() == OrderState.READY || order.getState() == OrderState.DELIVERED) {
				item = new HistoryItem(baker, "Order ready for pickup");
				item.setNewState(OrderState.READY);
				item.setTimestamp(order.getDueDate().atTime(random.nextInt(2) + 8, random.nextBoolean() ? 0 : 30));
				history.add(item);
				if (order.getState() == OrderState.DELIVERED) {
					item = new HistoryItem(baker, "Order delivered");
					item.setNewState(OrderState.DELIVERED);
					item.setTimestamp(order.getDueDate().atTime(order.getDueTime().minusMinutes(random.nextInt(120))));
					history.add(item);
				}
			}
		}

		return history;
	}

	/**
	 * Checks whether the list of order items already contains a given product.
	 *
	 * @param items   list of order items
	 * @param product product to check
	 * @return true if product is already in the list, false otherwise
	 */
	private boolean containsProduct(List<OrderItem> items, Product product) {
		for (OrderItem item : items) {
			if (item.getProduct() == product) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Generates a random due time for an order (between 8:00 and 16:00).
	 *
	 * @return random due time
	 */
	private LocalTime getRandomDueTime() {
		int time = 8 + 4 * random.nextInt(3);

		return LocalTime.of(time, 0);
	}

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
	
	/**
	 * Returns a random order state depending on the due date.
	 *
	 * @param due the due date
	 * @return randomly selected order state
	 */
	private OrderState getRandomState(LocalDate due) {
		LocalDate today = LocalDate.now();
		LocalDate tomorrow = today.plusDays(1);
		LocalDate twoDays = today.plusDays(2);

		if (due.isBefore(today)) {
			if (random.nextDouble() < 0.9) {
				return OrderState.DELIVERED;
			} else {
				return OrderState.CANCELLED;
			}
		} else {
			if (due.isAfter(twoDays)) {
				return OrderState.NEW;
			} else if (due.isAfter(tomorrow)) {
				// in 1-2 days
				double resolution = random.nextDouble();
				if (resolution < 0.8) {
					return OrderState.NEW;
				} else if (resolution < 0.9) {
					return OrderState.PROBLEM;
				} else {
					return OrderState.CANCELLED;
				}
			} else {
				double resolution = random.nextDouble();
				if (resolution < 0.6) {
					return OrderState.READY;
				} else if (resolution < 0.8) {
					return OrderState.DELIVERED;
				} else if (resolution < 0.9) {
					return OrderState.PROBLEM;
				} else {
					return OrderState.CANCELLED;
				}
			}

		}
	}

	/**
	 * Returns a random element from the given array.
	 *
	 * @param array array to select from
	 * @param <T>   type of array elements
	 * @return random element from array
	 */
	private <T> T getRandom(T[] array) {
		return array[random.nextInt(array.length)];
	}

	/**
	 * Creates several pickup locations and returns a supplier for random pickup locations.
	 *
	 * @param pickupLocationRepository the repository to save pickup locations
	 * @return supplier for random pickup locations
	 */
	private Supplier<PickupLocation> createPickupLocations(PickupLocationRepository pickupLocationRepository) {
		List<PickupLocation> pickupLocations = Arrays.asList(
				pickupLocationRepository.save(createPickupLocation("Store")),
				pickupLocationRepository.save(createPickupLocation("Bakery")));
		return () -> pickupLocations.get(random.nextInt(pickupLocations.size()));
	}

	/**
	 * Creates a new PickupLocation with the given name.
	 *
	 * @param name name of the pickup location
	 * @return new PickupLocation instance
	 */
	private PickupLocation createPickupLocation(String name) {
		PickupLocation store = new PickupLocation();
		store.setName(name);
		return store;
	}

	/**
	 * Creates the specified number of products and returns a supplier for random products.
	 *
	 * @param productsRepo    the product repository
	 * @param numberOfItems   number of products to create
	 * @return supplier for random products
	 */
	private Supplier<Product> createProducts(ProductRepository productsRepo, int numberOfItems) {
		List<Product> products  = new ArrayList<>();
		for (int i = 0; i < numberOfItems; i++) {
			Product product = new Product();
			product.setName(getRandomProductName());
			double doublePrice = 2.0 + random.nextDouble() * 100.0;
			product.setPrice((int) (doublePrice * 100.0));
			products.add(productsRepo.save(product));
		}
		return () -> {
			double cutoff = 2.5;
			double g = random.nextGaussian();
			g = Math.min(cutoff, g);
			g = Math.max(-cutoff, g);
			g += cutoff;
			g /= (cutoff * 2.0);
			return products.get((int) (g * (products.size() - 1)));
		};
	}

	/**
	 * Generates a random product name by combining fillings and types.
	 *
	 * @return random product name
	 */
	private String getRandomProductName() {
		String firstFilling = getRandom(FILLING);
		String name;
		if (random.nextBoolean()) {
			String secondFilling;
			do {
				secondFilling = getRandom(FILLING);
			} while (secondFilling.equals(firstFilling));

			name = firstFilling + " " + secondFilling;
		} else {
			name = firstFilling;
		}
		name += " " + getRandom(TYPE);

		return name;
	}

	/**
	 * Creates and saves a demo baker user.
	 *
	 * @param userRepository    the user repository
	 * @param passwordEncoder   the password encoder
	 * @return created baker user
	 */
	private User createBaker(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return userRepository.save(
				createUser("baker@vaadin.com", "Heidi", "Carter", passwordEncoder.encode("baker"), Role.BAKER, false));
	}

	/**
	 * Creates and saves a demo barista user.
	 *
	 * @param userRepository    the user repository
	 * @param passwordEncoder   the password encoder
	 * @return created barista user
	 */
	private User createBarista(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return userRepository.save(createUser("barista@vaadin.com", "Malin", "Castro",
				passwordEncoder.encode("barista"), Role.BARISTA, true));
	}

	/**
	 * Creates and saves a demo admin user.
	 *
	 * @param userRepository    the user repository
	 * @param passwordEncoder   the password encoder
	 * @return created admin user
	 */
	private User createAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return userRepository.save(
				createUser("admin@vaadin.com", "Göran", "Rich", passwordEncoder.encode("admin"), Role.ADMIN, true));
	}

	/**
	 * Creates and saves a set of deletable demo users.
	 *
	 * @param userRepository    the user repository
	 * @param passwordEncoder   the password encoder
	 */
	private void createDeletableUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		userRepository.save(
				createUser("peter@vaadin.com", "Peter", "Bush", passwordEncoder.encode("peter"), Role.BARISTA, false));
		userRepository
				.save(createUser("mary@vaadin.com", "Mary", "Ocon", passwordEncoder.encode("mary"), Role.BAKER, true));
	}

	/**
	 * Creates a new User entity with the given properties.
	 *
	 * @param email        user's email
	 * @param firstName    user's first name
	 * @param lastName     user's last name
	 * @param passwordHash encoded password
	 * @param role         user role
	 * @param locked       whether the account is locked
	 * @return new User entity
	 */
	private User createUser(String email, String firstName, String lastName, String passwordHash, String role,
			boolean locked) {
		User user = new User();
		user.setEmail(email);
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setPasswordHash(passwordHash);
		user.setRole(role);
		user.setLocked(locked);
		return user;
	}
}

```
