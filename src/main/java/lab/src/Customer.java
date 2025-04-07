package lab.src;

import java.util.ArrayList;
import java.util.Objects;

public class Customer implements JsonSerializable {
    private int id;
    private String name;
    private String phone;
    private ArrayList<Pizza> pizzasToOrder = new ArrayList<>();

    public  Customer() {}
    public Customer(int id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public ArrayList<Pizza> getPizzasToOrder() { return pizzasToOrder; }

    public void addToOrder(Pizza pizza) {
        pizzasToOrder.add(pizza);
    }
    public void removeFromOrder(Pizza pizza) {
        pizzasToOrder.remove(pizza);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer customer)) return false;
        return id == customer.id &&
                Objects.equals(name, customer.name) &&
                Objects.equals(phone, customer.phone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, phone);
    }

    @Override
    public String toJson() {
        StringBuilder json = new StringBuilder("{");
        json.append("\"id\": ").append(id).append(", ");
        json.append("\"name\": \"").append(name).append("\", ");
        json.append("\"phone\": \"").append(phone).append("\", ");
        json.append("\"pizzasToOrder\": [");

        for (int i = 0; i < pizzasToOrder.size(); i++) {
            Pizza pizza = pizzasToOrder.get(i);
            json.append(pizza.toJson());
            if (i < pizzasToOrder.size() - 1) {
                json.append(", ");
            }
        }

        json.append("]}");
        return json.toString();
    }

    @Override
    public Customer fromJson(String json) {
        // Basic parsing logic, assuming valid JSON format
        int id = Integer.parseInt(json.split("\"id\": ")[1].split(",")[0].trim());
        String name = json.split("\"name\": \"")[1].split("\"")[0];
        String phone = json.split("\"phone\": \"")[1].split("\"")[0];

        Customer customer = new Customer(id, name, phone);

        // Extracting pizzas
        String pizzasJson = json.split("\"pizzasToOrder\": \\[")[1].split("]")[0].trim();
        if (!pizzasJson.isEmpty()) {
            String[] pizzas = pizzasJson.split("\\}, \\{");
            for (String pizzaJson : pizzas) {
                Pizza pizza = new Pizza().fromJson("{" + pizzaJson + "}");
                customer.addToOrder(pizza);
            }
        }

        return customer;
    }
}
