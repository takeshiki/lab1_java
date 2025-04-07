import lab.src.Customer;
import lab.src.Pizza;
import lab.src.Pizzeria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PizzeriaTests {

    private Pizza margarita;
    private Pizza pepperoni;
    private Pizza veggie;
    private Customer customer;
    private Pizzeria pizzeria;

    @BeforeEach
    public void setup() {
        HashMap<String, Float> margheritaToppings = new HashMap<>();
        margheritaToppings.put("Cheese", 2.0f);
        margheritaToppings.put("Tomato", 1.0f);
        margarita = new Pizza("Margherita", 10.0f, margheritaToppings, false); // Price 10

        HashMap<String, Float> pepperoniToppings = new HashMap<>();
        pepperoniToppings.put("Cheese", 2.0f);
        pepperoniToppings.put("Pepperoni", 3.0f);
        pepperoni = new Pizza("Pepperoni", 12.0f, pepperoniToppings, false); // Price 12

        HashMap<String, Float> veggieToppings = new HashMap<>();
        veggieToppings.put("Mushrooms", 1.5f);
        veggieToppings.put("Olives", 1.0f);
        veggie = new Pizza("Veggie", 9.0f, veggieToppings, false); // Price 9

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
        customer = new Customer(1, "Taras", "+380551338696");

        customer.addToOrder(margarita);
        customer.addToOrder(pepperoni);
        pizzeria.registerCustomer(customer);

        assertFalse(margarita.isCooked());
        assertFalse(pepperoni.isCooked());

        float expectedTotalPrice = margarita.getPrice() + pepperoni.getPrice();
        float totalPrice = pizzeria.processOrder(1);

        assertEquals(expectedTotalPrice, totalPrice, 0.001f, "Total price should be sum of pizza prices.");
        assertEquals(2, pizzeria.getTotalSoldPizzas(), "Total sold pizzas should increment correctly.");

        assertTrue(margarita.isCooked(), "Margarita should be marked as cooked after processing.");
        assertTrue(pepperoni.isCooked(), "Pepperoni should be marked as cooked after processing.");

    }

    @Test
    public void testProcessOrderWithInvalidCustomer() {
        assertThrows(IllegalArgumentException.class, () -> {
            pizzeria.processOrder(999);
        }, "Processing order for non-existent customer should throw IllegalArgumentException.");
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

        margarita.AddTopping("Cheese", 2.0f); // Assuming "Cheese" already exists

        assertEquals(initialToppingsCount, margarita.getToppings().size(), "Topping count should not change when adding duplicate.");
        assertEquals(initialPrice, margarita.getPrice(), 0.001f, "Price should not change when adding duplicate topping.");
    }

    @Test
    public void testPizzaRemoveTopping() {
        float initialPrice = pepperoni.getPrice();
        assertTrue(pepperoni.getToppings().containsKey("Pepperoni"), "Pepperoni topping should exist initially.");

        pepperoni.RemoveTopping("Pepperoni", 3.0f); // Price should decrease

        assertFalse(pepperoni.getToppings().containsKey("Pepperoni"), "Pepperoni topping should be removed.");
        assertEquals(initialPrice - 3.0f, pepperoni.getPrice(), 0.001f, "Price should decrease after removing topping.");
    }


    @Test
    public void testSortMenuByName() {
        pizzeria.addPizzaToMenu(margarita);  // Margherita
        pizzeria.addPizzaToMenu(pepperoni);  // Pepperoni
        pizzeria.addPizzaToMenu(veggie);     // Veggie

        pizzeria.sortMenuByName();

        List<Pizza> sortedMenu = pizzeria.getMenu();
        assertEquals(3, sortedMenu.size());
        assertEquals("Margherita", sortedMenu.get(0).getName());
        assertEquals("Pepperoni", sortedMenu.get(1).getName());
        assertEquals("Veggie", sortedMenu.get(2).getName());
    }

    @Test
    public void testSortMenuByPrice() {
        pizzeria.addPizzaToMenu(margarita);  // Price 10.0
        pizzeria.addPizzaToMenu(pepperoni);  // Price 12.0
        pizzeria.addPizzaToMenu(veggie);     // Price 9.0

        pizzeria.sortMenuByPrice();

        List<Pizza> sortedMenu = pizzeria.getMenu();
        assertEquals(3, sortedMenu.size());
        assertEquals("Veggie", sortedMenu.get(0).getName());     // 9.0
        assertEquals("Margherita", sortedMenu.get(1).getName()); // 10.0
        assertEquals("Pepperoni", sortedMenu.get(2).getName());  // 12.0
    }

    @Test
    public void testPizzeriaToJsonAndFromJson() {
        pizzeria.addPizzaToMenu(margarita);
        pizzeria.addPizzaToMenu(veggie);
        pizzeria.registerCustomer(customer);


        pizzeria.sortMenuByName();
        String jsonSortedByName = pizzeria.toJson();

        assertTrue(jsonSortedByName.contains("\"menu\": ["), "JSON should contain menu array.");
        assertTrue(jsonSortedByName.contains("\"name\": \"Margherita\""), "JSON should contain Margherita.");
        assertTrue(jsonSortedByName.contains("\"name\": \"Veggie\""), "JSON should contain Veggie.");
        assertTrue(jsonSortedByName.contains("\"customers\": {"), "JSON should contain customers object.");
        assertTrue(jsonSortedByName.contains("\"id\": 1"), "JSON should contain customer ID 1.");
        assertTrue(jsonSortedByName.contains("\"totalSoldPizzas\": 0"), "JSON should contain totalSoldPizzas.");

        int margheritaIndex = jsonSortedByName.indexOf("\"name\": \"Margherita\"");
        int veggieIndex = jsonSortedByName.indexOf("\"name\": \"Veggie\"");
        assertTrue(margheritaIndex > 0 && veggieIndex > 0, "Both pizzas should be found in JSON");
        assertTrue(margheritaIndex < veggieIndex, "Margherita should appear before Veggie in JSON when sorted by name.");

        Pizzeria deserializedPizzeria = new Pizzeria().fromJson(jsonSortedByName);
        assertNotNull(deserializedPizzeria, "Deserialized pizzeria should not be null.");
        assertEquals(2, deserializedPizzeria.getMenu().size(), "Deserialized menu should have 2 pizzas.");
        assertEquals(1, deserializedPizzeria.getCustomers().size(), "Deserialized customers map should have 1 customer.");
        assertEquals(0, deserializedPizzeria.getTotalSoldPizzas(), "Deserialized total sold pizzas should be 0.");

        List<String> deserializedNames = deserializedPizzeria.getMenu().stream().map(Pizza::getName).toList();
        assertTrue(deserializedNames.contains("Margherita"), "Deserialized menu should contain Margherita.");
        assertTrue(deserializedNames.contains("Veggie"), "Deserialized menu should contain Veggie.");
        assertNotNull(deserializedPizzeria.getCustomer(1), "Deserialized pizzeria should contain customer with ID 1.");

    }

    @Test
    public void testCustomerToJsonAndFromJson() {
        customer = new Customer(1, "Taras", "+380551338696");
        customer.addToOrder(margarita);

        String json = customer.toJson();
        Customer deserializedCustomer = new Customer().fromJson(json);

        assertNotNull(deserializedCustomer, "Deserialized customer should not be null.");
        assertEquals(customer.getId(), deserializedCustomer.getId(), "IDs should match.");
        assertEquals(customer.getName(), deserializedCustomer.getName(), "Names should match.");
        assertEquals(customer.getPhone(), deserializedCustomer.getPhone(), "Phones should match.");

        assertEquals(1, deserializedCustomer.getPizzasToOrder().size(), "Deserialized order should have 1 pizza.");
        Pizza orderedPizza = deserializedCustomer.getPizzasToOrder().get(0);
        assertEquals(margarita.getName(), orderedPizza.getName(), "Pizza name in order should match.");
        assertEquals(margarita.getPrice(), orderedPizza.getPrice(), 0.001f, "Pizza price in order should match.");
    }
}