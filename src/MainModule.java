import dao.OrderProcessor;
import entity.*;


import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainModule {

    public static void main(String[] args) {
        try (Connection connection = DBConn.getConnection();
             Scanner scanner = new Scanner(System.in)) {

            OrderProcessor orderProcessor = new OrderProcessor(connection);

            while (true) {
                System.out.println("Select an action:");
                System.out.println("1. Create User");
                System.out.println("2. Create Product (Admin only)");
                System.out.println("3. Place Order");
                System.out.println("4. View All Products");
                System.out.println("5. View User Orders");
                System.out.println("6. Cancel Order");
                System.out.println("7. Exit");

                int choice = scanner.nextInt();
                scanner.nextLine();  // Consume newline

                switch (choice) {
                    case 1:
                        // Create User
                        System.out.println("Enter User ID:");
                        int userId = scanner.nextInt();
                        scanner.nextLine();  // Consume newline

                        System.out.println("Enter Username:");
                        String username = scanner.nextLine();

                        System.out.println("Enter Password:");
                        String password = scanner.nextLine();

                        System.out.println("Enter Role (User/Admin):");
                        String role = scanner.nextLine();

                        User user = new User(userId, username, password, role);
                        orderProcessor.createUser(user);
                        break;

                    case 2:

                        System.out.println("Enter Admin ID:");
                        int adminId = scanner.nextInt();
                        scanner.nextLine();

                        User admin = new User(adminId, "admin", "adminpass", "Admin");  // Assuming fixed admin for simplicity

                        System.out.println("Enter Product ID:");
                        int productId = scanner.nextInt();
                        scanner.nextLine();

                        System.out.println("Enter Product Name:");
                        String productName = scanner.nextLine();

                        System.out.println("Enter Product Description:");
                        String description = scanner.nextLine();

                        System.out.println("Enter Price:");
                        double price = scanner.nextDouble();

                        System.out.println("Enter Quantity in Stock:");
                        int quantity = scanner.nextInt();
                        scanner.nextLine();

                        System.out.println("Enter Product Type (Electronics/Clothing):");
                        String type = scanner.nextLine();

                        Product product;
                        if ("Electronics".equalsIgnoreCase(type)) {
                            System.out.println("Enter Brand:");
                            String brand = scanner.nextLine();

                            System.out.println("Enter Warranty Period (months):");
                            int warranty = scanner.nextInt();

                            product = new Electronics(productId, productName, description, price, quantity, brand, warranty);
                        } else if ("Clothing".equalsIgnoreCase(type)) {
                            System.out.println("Enter Size:");
                            String size = scanner.nextLine();

                            System.out.println("Enter Color:");
                            String color = scanner.nextLine();

                            product = new Clothing(productId, productName, description, price, quantity, size, color);
                        } else {
                            System.out.println("Invalid product type. Try again.");
                            break;
                        }

                        orderProcessor.createProduct(admin, product);
                        break;

                    case 3:

                        System.out.println("Enter User ID:");
                        userId = scanner.nextInt();
                        scanner.nextLine();  // Consume newline

                        User orderUser = new User(userId, "user", "password", "User");

                        System.out.println("Enter Product ID to order:");
                        productId = scanner.nextInt();

                        Product orderedProduct = orderProcessor.getAllProducts().stream()
                                .filter(p -> p.getProductId() == productId)
                                .findFirst()
                                .orElse(null);

                        if (orderedProduct == null) {
                            System.out.println("Product not found.");
                            break;
                        }

                        List<Product> products = new ArrayList<>();
                        products.add(orderedProduct);
                        orderProcessor.createOrder(orderUser, products);
                        break;

                    case 4:

                        List<Product> allProducts = orderProcessor.getAllProducts();
                        for (Product p : allProducts) {
                            System.out.println("Product ID: " + p.getProductId() + ", Name: " + p.getProductName() + ", Type: " + p.getType());
                        }
                        break;

                    case 5:

                        System.out.println("Enter User ID:");
                        userId = scanner.nextInt();
                        scanner.nextLine();

                        User viewUser = new User(userId, "user", "password", "User");
                        List<Product> userOrders = orderProcessor.getOrderByUser(viewUser);
                        for (Product orderedProductView : userOrders) {
                            System.out.println("Ordered Product: " + orderedProductView.getProductName());
                        }
                        break;

                    case 6:
                        System.out.println("Enter User ID:");
                        userId = scanner.nextInt();

                        System.out.println("Enter Order ID to cancel:");
                        int orderId = scanner.nextInt();

                        orderProcessor.cancelOrder(userId, orderId);
                        break;

                    case 7:
                        
                        System.out.println("Exiting...");
                        return;

                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}