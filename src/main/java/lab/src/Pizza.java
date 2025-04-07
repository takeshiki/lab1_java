package lab.src;

import java.util.HashMap;
import java.util.Map;

public class Pizza implements JsonSerializable {
    private String name;
    private boolean isCooked;
    private float price;
    private HashMap<String, Float> toppings = new HashMap<>();

    public Pizza() {}
    public Pizza(String name, float price, HashMap<String, Float> toppings, boolean isCooked) {
        this.name = name;
        this.price = price;
        this.toppings = toppings != null ? toppings : new HashMap<>();
        this.isCooked = isCooked;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public float getPrice() {
        return price;
    }
    public void setPrice(float price) {
        this.price = price;
    }

    public HashMap<String, Float> getToppings() {
        return toppings;
    }
    public void setToppings(HashMap<String, Float> toppings) {
        this.toppings = toppings;
    }

    public void cook() {
        isCooked = true;
    }

    public boolean isCooked() {
        return isCooked;
    }

    public void AddTopping(String topping, float toppingPrice) {
        if (!toppings.containsKey(topping)) {
            toppings.put(topping, toppingPrice);
            price += toppingPrice;
        }
    }
    public void RemoveTopping(String topping, float toppingPrice) {
        if (toppings.containsKey(topping)) {
            toppings.remove(topping);
            price -= toppingPrice;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Pizza pizza = (Pizza) obj;
        return Float.compare(pizza.price, price) == 0 &&
                (name == null ? pizza.name == null : name.equals(pizza.name)) &&
                (toppings == null ? pizza.toppings == null : toppings.equals(pizza.toppings));
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + Float.hashCode(price);
        result = 31 * result + (toppings != null ? toppings.hashCode() : 0);
        return result;
    }

    @Override
    public String toJson() {
        StringBuilder json = new StringBuilder("{\n");
        json.append("  \"name\": \"").append(name).append("\",\n");
        json.append("  \"price\": ").append(String.format("%.2f", price)).append(",\n");
        json.append("  \"toppings\": {\n");

        int count = 0;
        for (Map.Entry<String, Float> entry : toppings.entrySet()) {
            json.append("    \"").append(entry.getKey()).append("\": ")
                    .append(String.format("%.2f", entry.getValue()));
            count++;
            if (count < toppings.size()) json.append(",");
            json.append("\n");
        }

        json.append("  },\n");
        json.append("  \"isCooked\": ").append(isCooked).append("\n");
        json.append("}");
        return json.toString();
    }

    @Override
    public Pizza fromJson(String json) {
        String name = null;
        float price = 0;
        boolean isCooked = false;
        HashMap<String, Float> toppings = new HashMap<>();

        try {
            name = json.split("\"name\": \"")[1].split("\"")[0];
            price = Float.parseFloat(json.split("\"price\": ")[1].split(",")[0]);
            isCooked = Boolean.parseBoolean(json.split("\"isCooked\": ")[1].split("\n")[0].trim());

            if (json.contains("\"toppings\": {")) {
                String toppingsStr = json.split("\"toppings\": \\{")[1].split("\\}")[0].trim();

                if (!toppingsStr.isEmpty()) {
                    String[] toppingEntries = toppingsStr.split("\\s*,\\s*|\\s*\n\\s*");

                    for (String entry : toppingEntries) {
                        if (entry.trim().isEmpty()) continue;

                        String[] parts = entry.split(":");
                        if (parts.length >= 2) {
                            String toppingName = parts[0].replaceAll("\"", "").trim();
                            StringBuilder priceStr = new StringBuilder();
                            for (int i = 1; i < parts.length; i++) {
                                priceStr.append(parts[i]);
                            }
                            Float toppingPrice = Float.parseFloat(priceStr.toString().trim());
                            toppings.put(toppingName, toppingPrice);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error parsing Pizza JSON: " + e.getMessage(), e);
        }

        return new Pizza(name, price, toppings, isCooked);
    }
}