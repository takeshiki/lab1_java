package lab.src;

import java.util.ArrayList;
import java.util.HashMap;

public class Pizzeria implements JsonSerializable<Pizzeria> {
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

    public HashMap<Integer, Customer> getCustomers() {
        return customers;
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
            // Extract menu section
            int menuStartIndex = json.indexOf("\"menu\": [");
            if (menuStartIndex != -1) {
                int menuContentStart = json.indexOf('[', menuStartIndex) + 1;
                int menuContentEnd = findMatchingCloseBracket(json, menuContentStart - 1);

                if (menuContentEnd != -1) {
                    String menuContent = json.substring(menuContentStart, menuContentEnd).trim();

                    // Process each pizza in the menu
                    if (!menuContent.isEmpty()) {
                        ArrayList<String> pizzaJsons = splitJsonObjects(menuContent);

                        for (String pizzaJson : pizzaJsons) {
                            try {
                                Pizza pizza = new Pizza();
                                pizza = pizza.fromJson(pizzaJson);
                                pizzeria.addPizzaToMenu(pizza);
                            } catch (Exception e) {
                                System.err.println("Error parsing pizza: " + e.getMessage());
                            }
                        }
                    }
                }
            }

            // Extract customers section
            int customersStartIndex = json.indexOf("\"customers\": {");
            if (customersStartIndex != -1) {
                int customersContentStart = json.indexOf('{', customersStartIndex) + 1;
                int customersContentEnd = findMatchingCloseBracket(json, customersContentStart - 1);

                if (customersContentEnd != -1) {
                    String customersContent = json.substring(customersContentStart, customersContentEnd).trim();

                    if (!customersContent.isEmpty()) {
                        HashMap<String, String> customerEntries = extractJsonKeyValuePairs(customersContent);

                        for (String customerId : customerEntries.keySet()) {
                            try {
                                String customerJson = customerEntries.get(customerId);
                                Customer customer = new Customer();
                                customer = customer.fromJson(customerJson);
                                pizzeria.registerCustomer(customer);
                            } catch (Exception e) {
                                System.err.println("Error parsing customer: " + e.getMessage());
                            }
                        }
                    }
                }
            }

            // Extract totalSoldPizzas
            int totalSoldStartIndex = json.indexOf("\"totalSoldPizzas\":");
            if (totalSoldStartIndex != -1) {
                int valueStart = totalSoldStartIndex + "\"totalSoldPizzas\":".length();
                int valueEnd = json.indexOf(",", valueStart);
                if (valueEnd == -1) {
                    valueEnd = json.indexOf("}", valueStart);
                }
                if (valueEnd != -1) {
                    String valueStr = json.substring(valueStart, valueEnd).trim();
                    pizzeria.totalSoldPizzas = Integer.parseInt(valueStr);
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing pizzeria JSON: " + e.getMessage());
        }

        return pizzeria;
    }


    // Helper method to find the matching closing bracket (either } or ])
    private int findMatchingCloseBracket(String json, int openBracketPos) {
        char openBracket = json.charAt(openBracketPos);
        char closeBracket = (openBracket == '{') ? '}' : ']';

        int depth = 1;
        for (int i = openBracketPos + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == openBracket) depth++;
            else if (c == closeBracket) {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1; // No matching bracket found
    }

    // Helper method to split a JSON string into individual objects
    private ArrayList<String> splitJsonObjects(String jsonContent) {
        ArrayList<String> result = new ArrayList<>();

        int startPos = 0;
        int depth = 0;
        boolean inObject = false;

        for (int i = 0; i < jsonContent.length(); i++) {
            char c = jsonContent.charAt(i);

            if (c == '{') {
                if (!inObject) {
                    startPos = i;
                    inObject = true;
                }
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && inObject) {
                    result.add(jsonContent.substring(startPos, i + 1));
                    inObject = false;
                }
            }
        }

        return result;
    }

    // Helper method to extract key-value pairs from a JSON object
    private HashMap<String, String> extractJsonKeyValuePairs(String jsonContent) {
        HashMap<String, String> result = new HashMap<>();

        int pos = 0;
        while (pos < jsonContent.length()) {
            // Find the key (enclosed in quotes)
            int keyStart = jsonContent.indexOf("\"", pos);
            if (keyStart == -1) break;

            int keyEnd = jsonContent.indexOf("\"", keyStart + 1);
            if (keyEnd == -1) break;

            String key = jsonContent.substring(keyStart + 1, keyEnd);

            // Find the value (which should be an object)
            int colonPos = jsonContent.indexOf(":", keyEnd);
            if (colonPos == -1) break;

            int valueStart = jsonContent.indexOf("{", colonPos);
            if (valueStart == -1) break;

            int valueEnd = findMatchingCloseBracket(jsonContent, valueStart);
            if (valueEnd == -1) break;

            String value = jsonContent.substring(valueStart, valueEnd + 1);
            result.put(key, value);

            pos = valueEnd + 1;
        }

        return result;
    }
}
