import lab.src.Customer;
import lab.src.Pizza;
import lab.src.Pizzeria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class PizzeriaTests {

    private Pizza margarita;
    private Pizza pepperoni;
    private Customer customer;
    private Pizzeria pizzeria;

    @BeforeEach
    public void setup() {
        // Initialize test objects before each test
        HashMap<String, Float> margheritaToppings = new HashMap<>();
        margheritaToppings.put("Cheese", 2.0f);
        margheritaToppings.put("Tomato", 1.0f);
        margarita = new Pizza("Margherita", 10.0f, margheritaToppings, false);

        HashMap<String, Float> pepperoniToppings = new HashMap<>();
        pepperoniToppings.put("Cheese", 2.0f);
        pepperoniToppings.put("Pepperoni", 3.0f);
        pepperoni = new Pizza("Pepperoni", 12.0f, pepperoniToppings, false);

        customer = new Customer(1, "Taras", "+380551338696");

        pizzeria = new Pizzeria();
    }

    @Test
    public void testAddPizzaToMenu() {
        pizzeria.addPizzaToMenu(margarita);
        assertEquals(1, pizzeria.getMenu().size());
        assertTrue(pizzeria.getMenu().contains(margarita));
    }

    @Test
    public void testRemovePizzaFromMenu() {
        pizzeria.addPizzaToMenu(margarita);
        pizzeria.addPizzaToMenu(pepperoni);
        assertEquals(2, pizzeria.getMenu().size());

        pizzeria.removePizzaFromMenu(margarita);
        assertEquals(1, pizzeria.getMenu().size());
        assertFalse(pizzeria.getMenu().contains(margarita));
        assertTrue(pizzeria.getMenu().contains(pepperoni));
    }

    @Test
    public void testRegisterAndGetCustomer() {
        pizzeria.registerCustomer(customer);
        assertEquals(customer, pizzeria.getCustomer(1));
    }

    @Test
    public void testRemoveCustomer() {
        pizzeria.registerCustomer(customer);
        assertNotNull(pizzeria.getCustomer(1));

        pizzeria.removeCustomer(1);
        assertNull(pizzeria.getCustomer(1));
    }

    @Test
    public void testProcessOrderWithValidCustomer() {
        // Initialize pizzasToOrder as it appears to be null in the constructor
        customer = new Customer(1, "Taras", "+380551338696") {
            private ArrayList<Pizza> pizzasToOrder = new ArrayList<>();

            @Override
            public ArrayList<Pizza> getPizzasToOrder() {
                return pizzasToOrder;
            }

            @Override
            public void addToOrder(Pizza pizza) {
                pizzasToOrder.add(pizza);
            }
        };

        customer.addToOrder(margarita);
        customer.addToOrder(pepperoni);
        pizzeria.registerCustomer(customer);

        float totalPrice = pizzeria.processOrder(1);
        assertEquals(22.0f, totalPrice, 0.001f);
        assertEquals(2, pizzeria.getTotalSoldPizzas());

        // Verify that pizzas are cooked
        for (Pizza pizza : customer.getPizzasToOrder()) {
            assertTrue(pizza.isCooked());
        }
    }

    @Test
    public void testProcessOrderWithInvalidCustomer() {
        assertThrows(IllegalArgumentException.class, () -> {
            pizzeria.processOrder(999); // Non-existent customer ID
        });
    }

    @Test
    public void testPizzaAddTopping() {
        float initialPrice = margarita.getPrice();
        margarita.AddTopping("Mushroom", 2.5f);

        assertTrue(margarita.getToppings().containsKey("Mushroom"));
        assertEquals(2.5f, margarita.getToppings().get("Mushroom"), 0.001f);
        assertEquals(initialPrice + 2.5f, margarita.getPrice(), 0.001f);
    }

    @Test
    public void testPizzaAddDuplicateTopping() {
        float initialPrice = margarita.getPrice();
        int initialToppingsCount = margarita.getToppings().size();

        // Try to add cheese again
        margarita.AddTopping("Cheese", 2.0f);

        // Verify topping wasn't added again and price didn't change
        assertEquals(initialToppingsCount, margarita.getToppings().size());
        assertEquals(initialPrice, margarita.getPrice(), 0.001f);
    }

    @Test
    public void testPizzaRemoveTopping() {
        float initialPrice = pepperoni.getPrice();
        pepperoni.RemoveTopping("Pepperoni", 3.0f);

        assertFalse(pepperoni.getToppings().containsKey("Pepperoni"));
        assertEquals(initialPrice - 3.0f, pepperoni.getPrice(), 0.001f);
    }

    @Test
    public void testPizzaToJsonAndFromJson() {
        String json = margarita.toJson();
        Pizza deserializedPizza = new Pizza().fromJson(json);

        assertEquals(margarita.getName(), deserializedPizza.getName());
        assertEquals(margarita.getPrice(), deserializedPizza.getPrice(), 0.001f);
        assertEquals(margarita.isCooked(), deserializedPizza.isCooked());
        assertEquals(margarita.getToppings().size(), deserializedPizza.getToppings().size());

        for (String topping : margarita.getToppings().keySet()) {
            assertTrue(deserializedPizza.getToppings().containsKey(topping));
            assertEquals(margarita.getToppings().get(topping), deserializedPizza.getToppings().get(topping), 0.001f);
        }
    }

    @Test
    public void testPizzeriaToJsonAndFromJson() {
        pizzeria.addPizzaToMenu(margarita);
        pizzeria.registerCustomer(customer);

        String json = pizzeria.toJson();
        Pizzeria deserializedPizzeria = new Pizzeria().fromJson(json);

        assertEquals(pizzeria.getMenu().size(), deserializedPizzeria.getMenu().size());
        assertEquals(pizzeria.getTotalSoldPizzas(), deserializedPizzeria.getTotalSoldPizzas());
    }

    @Test
    public void testCustomerToJsonAndFromJson() {
        customer = new Customer(1, "Taras", "+380551338696") {
            private ArrayList<Pizza> pizzasToOrder = new ArrayList<>();

            @Override
            public ArrayList<Pizza> getPizzasToOrder() {
                return pizzasToOrder;
            }

            @Override
            public void addToOrder(Pizza pizza) {
                pizzasToOrder.add(pizza);
            }
        };

        customer.addToOrder(margarita);

        String json = customer.toJson();
        Customer deserializedCustomer = new Customer().fromJson(json);

        assertEquals(customer.getId(), deserializedCustomer.getId());
        assertEquals(customer.getName(), deserializedCustomer.getName());
        assertEquals(customer.getPhone(), deserializedCustomer.getPhone());
    }
}