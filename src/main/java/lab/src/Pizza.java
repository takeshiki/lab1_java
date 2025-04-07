package lab.src;

import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

public class Pizza implements JsonSerializable<Pizza> {
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
        json.append("  \"price\": ").append(String.format(Locale.US, "%.2f", price)).append(",\n");
        json.append("  \"toppings\": {\n");

        int count = 0;
        for (Map.Entry<String, Float> entry : toppings.entrySet()) {
            json.append("    \"").append(entry.getKey()).append("\": ")
                    .append(String.format(Locale.US, "%.2f", entry.getValue()));
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
        try {
            String name = extractJsonStringValue(json, "name");

            float price = extractJsonFloatValue(json, "price");

            boolean isCooked = extractJsonBooleanValue(json, "isCooked");

            HashMap<String, Float> toppings = new HashMap<>();
            String toppingsJson = extractJsonObjectValue(json, "toppings");

            if (toppingsJson != null && !toppingsJson.isEmpty()) {
                int pos = 0;
                while (pos < toppingsJson.length()) {
                    int keyStart = toppingsJson.indexOf("\"", pos);
                    if (keyStart == -1) break;

                    int keyEnd = toppingsJson.indexOf("\"", keyStart + 1);
                    if (keyEnd == -1) break;

                    String key = toppingsJson.substring(keyStart + 1, keyEnd);

                    int colonPos = toppingsJson.indexOf(":", keyEnd);
                    if (colonPos == -1) break;

                    int valueEnd = toppingsJson.indexOf(",", colonPos);
                    if (valueEnd == -1) {
                        valueEnd = toppingsJson.indexOf("}", colonPos);
                    }
                    if (valueEnd == -1) break;

                    String valueStr = toppingsJson.substring(colonPos + 1, valueEnd).trim();

                    float value;
                    if (valueStr.equals("true")) {
                        value = 1.0f;
                    } else if (valueStr.equals("false")) {
                        continue;
                    } else {
                        try {
                            value = Float.parseFloat(valueStr);
                        } catch (NumberFormatException e) {
                            value = 1.0f;
                        }
                    }

                    toppings.put(key, value);
                    pos = valueEnd + 1;
                }
            }

            return new Pizza(name, price, toppings, isCooked);

        } catch (Exception e) {
            throw new RuntimeException("Error parsing Pizza JSON: " + e.getMessage(), e);
        }
    }

    private String extractJsonStringValue(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]*)\"";
        java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = r.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return "";
    }

    private float extractJsonFloatValue(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*([0-9.]+)";
        java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = r.matcher(json);
        if (m.find()) {
            return Float.parseFloat(m.group(1));
        }
        return 0.0f;
    }

    private boolean extractJsonBooleanValue(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*(true|false)";
        java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = r.matcher(json);
        if (m.find()) {
            return Boolean.parseBoolean(m.group(1));
        }
        return false;
    }

    private String extractJsonObjectValue(String json, String key) {
        int keyIndex = json.indexOf("\"" + key + "\"");
        if (keyIndex == -1) return "";

        int objectStart = json.indexOf("{", keyIndex);
        if (objectStart == -1) return "";

        int objectEnd = findMatchingCloseBracket(json, objectStart);
        if (objectEnd == -1) return "";

        return json.substring(objectStart + 1, objectEnd);
    }

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
        return -1;
    }
}