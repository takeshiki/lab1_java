package lab.src;

import java.util.ArrayList;
import java.util.HashMap;

public class Pizzeria implements JsonSerializable {
    private ArrayList<Pizza> menu = new ArrayList<>();
    private HashMap<Integer, Customer> customers = new HashMap<>();
    private int totalSoldPizzas = 0;

    public void addPizzaToMenu(Pizza pizza) {
        menu.add(pizza);
    }

    public void removePizzaFromMenu(Pizza pizza) {
        menu.remove(pizza);
    }

    public ArrayList<Pizza> getMenu() {
        return menu;
    }

    public void registerCustomer(Customer customer) {
        customers.put(customer.getId(), customer);
    }

    public void removeCustomer(int customerId) {
        customers.remove(customerId);
    }

    public Customer getCustomer(int customerId) {
        return customers.get(customerId);
    }

    public float processOrder(int customerId) {
        if (!customers.containsKey(customerId)) {
            throw new IllegalArgumentException("Customer not found");
        }

        float totalPrice = 0;
        for (Pizza pizza : customers.get(customerId).getPizzasToOrder()) {
            pizza.cook();
            totalSoldPizzas++;
            totalPrice += pizza.getPrice();
        }

        return totalPrice;
    }

    public int getTotalSoldPizzas() {
        return totalSoldPizzas;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Pizzeria)) {
            return false;
        }

        Pizzeria other = (Pizzeria) obj;

        if (this.totalSoldPizzas != other.totalSoldPizzas) {
            return false;
        }

        if (this.menu.size() != other.menu.size()) {
            return false;
        }
        for (int i = 0; i < this.menu.size(); i++) {
            if (!this.menu.get(i).equals(other.menu.get(i))) {
                return false;
            }
        }

        if (this.customers.size() != other.customers.size()) {
            return false;
        }
        for (Integer key : this.customers.keySet()) {
            if (!this.customers.get(key).equals(other.customers.get(key))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 17;

        result = 31 * result + totalSoldPizzas;
        result = 31 * result + (menu != null ? menu.hashCode() : 0);
        result = 31 * result + (customers != null ? customers.hashCode() : 0);

        return result;
    }

    @Override
    public String toJson() {
        StringBuilder json = new StringBuilder();
        json.append("{\n");

        // Menu
        json.append("\"menu\": [\n");
        for (int i = 0; i < menu.size(); i++) {
            json.append(menu.get(i).toJson());
            if (i < menu.size() - 1) json.append(",\n");
        }
        json.append("\n],\n");

        // Customers
        json.append("\"customers\": {\n");
        for (Integer id : customers.keySet()) {
            json.append("\"" + id + "\": " + customers.get(id).toJson() + ",\n");
        }
        if (!customers.isEmpty()) {
            json.setLength(json.length() - 2); // Remove trailing comma
        }
        json.append("\n},\n");

        // Total sold pizzas
        json.append("\"totalSoldPizzas\": " + totalSoldPizzas + "\n");

        json.append("}");

        return json.toString();
    }

    @Override
    public Pizzeria fromJson(String json) {
        Pizzeria pizzeria = new Pizzeria();

        try {
            // Parse menu
            String menuStr = json.split("\"menu\": \\[")[1].split("\\]")[0].trim();
            String[] pizzaJsons = menuStr.split("},\\{");
            for (String pizzaJson : pizzaJsons) {
                if (!pizzaJson.startsWith("{")) pizzaJson = "{" + pizzaJson;
                if (!pizzaJson.endsWith("}")) pizzaJson = pizzaJson + "}";
                Pizza pizza =  new Pizza();
                pizza.fromJson(pizzaJson);
                pizzeria.addPizzaToMenu(pizza);
            }

            // Parse customers
            String customersStr = json.split("\"customers\": \\{")[1].split("\\}")[0].trim();
            String[] customerEntries = customersStr.split(",\\n");
            for (String customerEntry : customerEntries) {
                String customerId = customerEntry.split(":")[0].replaceAll("\"", "").trim();
                String customerJson = customerEntry.split(":")[1].trim();
                Customer customer = new Customer();
                customer.fromJson(customerJson);
                pizzeria.registerCustomer(customer);
            }

            // Parse totalSoldPizzas
            String totalSoldPizzasStr = json.split("\"totalSoldPizzas\": ")[1].split("\n")[0].trim();
            pizzeria.totalSoldPizzas = Integer.parseInt(totalSoldPizzasStr);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return pizzeria;
    }
}
