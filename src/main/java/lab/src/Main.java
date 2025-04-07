package lab.src;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        Pizzeria pizzeria = new Pizzeria();
        try {
            String json = new String(Files.readAllBytes(Paths.get("basicpizzas.json")));
            pizzeria = pizzeria.fromJson(json);
        } catch (IOException e) {
            System.out.println("Error reading the JSON file: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error processing the JSON: " + e.getMessage());
        }

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n=== Піцерія Меню ===");
            System.out.println("1. Додати піцу в меню");
            System.out.println("2. Зареєструвати клієнта");
            System.out.println("3. Додати піцу до замовлення клієнта");
            System.out.println("4. Показати клієнтів");
            System.out.println("5. Показати меню піцерії");
            System.out.println("6. Експортувати піцерію в JSON");
            System.out.println("7. Імпортувати піцерію з JSON");
            System.out.println("0. Вийти");
            System.out.print("Виберіть опцію: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> {
                    System.out.print("Назва піци: ");
                    String name = scanner.nextLine();
                    System.out.print("Ціна: ");
                    float price = scanner.nextFloat();
                    scanner.nextLine();

                    HashMap<String, Float> toppings = new HashMap<>();
                    System.out.print("Додати топінги? (y/n): ");
                    if (scanner.nextLine().equalsIgnoreCase("y")) {
                        while (true) {
                            System.out.print("Назва топінгу (або 'stop'): ");
                            String topping = scanner.nextLine();
                            if (topping.equalsIgnoreCase("stop")) break;
                            System.out.print("Ціна топінгу: ");
                            float toppingPrice = scanner.nextFloat();
                            scanner.nextLine();
                            toppings.put(topping, toppingPrice);
                        }
                    }

                    Pizza pizza = new Pizza(name, price, toppings, false);
                    pizzeria.addPizzaToMenu(pizza);
                    System.out.println("Піцу додано!");
                }
                case 2 -> {
                    System.out.print("ID клієнта: ");
                    int id = scanner.nextInt();
                    scanner.nextLine();
                    System.out.print("Ім'я: ");
                    String name = scanner.nextLine();
                    System.out.print("Телефон: ");
                    String phone = scanner.nextLine();
                    Customer customer = new Customer(id, name, phone);
                    pizzeria.registerCustomer(customer);
                    System.out.println("Клієнта зареєстровано!");
                }
                case 3 -> {
                    System.out.print("ID клієнта: ");
                    int customerId = scanner.nextInt();
                    scanner.nextLine();
                    Customer customer = pizzeria.getCustomer(customerId);
                    if (customer == null) {
                        System.out.println("Клієнт не знайдений.");
                        break;
                    }
                    ArrayList<Pizza> menu = pizzeria.getMenu();
                    if (menu.isEmpty()) {
                        System.out.println("Меню порожнє.");
                        break;
                    }
                    System.out.println("Меню:");
                    for (int i = 0; i < menu.size(); i++) {
                        System.out.println(i + ". " + menu.get(i).getName());
                    }
                    System.out.print("Виберіть номер піци: ");
                    int pizzaIndex = scanner.nextInt();
                    if (pizzaIndex >= 0 && pizzaIndex < menu.size()) {
                        customer.addToOrder(menu.get(pizzaIndex));
                        System.out.println("Піцу додано до замовлення.");
                    } else {
                        System.out.println("Невірний індекс.");
                    }
                }
                case 4 -> {
                    System.out.println("Клієнти:");
                    for (Map.Entry<Integer, Customer> entry : pizzeria.getCustomers().entrySet()) {
                        Customer c = entry.getValue();
                        System.out.println(c.getId() + ": " + c.getName() + ", " + c.getPhone());
                    }
                }
                case 5 -> {
                    System.out.println("Меню піцерії:");
                    for (Pizza p : pizzeria.getMenu()) {
                        System.out.println("- " + p.getName() + " (" + p.getPrice() + " грн)");
                    }
                }
                case 6 -> {
                    System.out.print("Введіть назву файлу для експорту (наприклад, pizzeria.json): ");
                    String exportFile = scanner.nextLine();

                    System.out.print("Сортувати меню перед експортом? (y/n): ");
                    String sortChoice = scanner.nextLine();
                    if (sortChoice.equalsIgnoreCase("y")) {
                        System.out.println("Сортувати за:");
                        System.out.println("1. Назвою");
                        System.out.println("2. Ціною");
                        System.out.print("Ваш вибір: ");
                        int sortType = -1;
                        try {
                            sortType = scanner.nextInt();
                        } catch (java.util.InputMismatchException e) {
                            System.out.println("Невірний вибір сортування. Експорт без сортування.");
                            scanner.next();

                            scanner.next();

                        }
                        scanner.nextLine();

                        if (sortType == 1) {
                            pizzeria.sortMenuByName();
                            System.out.println("Меню відсортовано за назвою.");
                        } else if (sortType == 2) {
                            pizzeria.sortMenuByPrice();
                            System.out.println("Меню відсортовано за ціною.");
                        } else {
                            System.out.println("Невірний вибір критерію. Експорт без сортування.");
                        }
                    }
                    String json = pizzeria.toJson();
                    saveToFile(exportFile, json);
                }
                case 7 -> {
                    System.out.print("Введіть назву файлу для імпорту (наприклад, pizzeria.json): ");
                    String importFile = scanner.nextLine();
                    String jsonData = readFromFile(importFile);
                    pizzeria = pizzeria.fromJson(jsonData);
                    System.out.println("Піцерія імпортована успішно.");
                }
                case 0 -> {
                    running = false;
                    System.out.println("До побачення!");
                }
                default -> System.out.println("Невірний вибір.");
            }
        }
        scanner.close();
    }
    public static void saveToFile(String filename, String json) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(json);
            System.out.println("Успішно експортовано у " + filename);
        } catch (IOException e) {
            System.out.println("Помилка запису у файл: " + e.getMessage());
        }
    }

    public static String readFromFile(String filename) {
        StringBuilder json = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line).append("\n");
            }
        } catch (IOException e) {
            System.out.println("Помилка читання з файлу: " + e.getMessage());
        }
        return json.toString();
    }
}