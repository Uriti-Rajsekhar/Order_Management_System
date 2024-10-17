package dao;

import entity.Product;
import entity.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderProcessor implements IOrderManagementRepository {

    private Connection connection;

    public OrderProcessor(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createOrder(User user, List<Product> products) {
        try {

            String checkUserQuery = "select * from users where userId = ?";
            PreparedStatement checkUserStmt = connection.prepareStatement(checkUserQuery);
            checkUserStmt.setInt(1, user.getUserId());
            ResultSet rs = checkUserStmt.executeQuery();

            if (!rs.next()) {
                createUser(user);
            }

            String createOrderQuery = "insert into orders (userId) values (?)";
            PreparedStatement createOrderStmt = connection.prepareStatement(createOrderQuery, Statement.RETURN_GENERATED_KEYS);
            createOrderStmt.setInt(1, user.getUserId());
            createOrderStmt.executeUpdate();
            ResultSet generatedKeys = createOrderStmt.getGeneratedKeys();
            int orderId = 0;
            if (generatedKeys.next()) {
                orderId = generatedKeys.getInt(1);
            }


            String addProductToOrderQuery = "insert into order_items (orderId, productId, quantity) values (?, ?, ?)";
            PreparedStatement addProductToOrderStmt = connection.prepareStatement(addProductToOrderQuery);
            for (Product product : products) {
                addProductToOrderStmt.setInt(1, orderId);
                addProductToOrderStmt.setInt(2, product.getProductId());
                addProductToOrderStmt.setInt(3, 1); // Default quantity is 1 for now
                addProductToOrderStmt.executeUpdate();
            }

            System.out.println("Order created successfully with Order ID: " + orderId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cancelOrder(int userId, int orderId) {
        try {

            String checkOrderQuery = "select * from orders where orderId = ? and user_id = ?";
            PreparedStatement checkOrderStmt = connection.prepareStatement(checkOrderQuery);
            checkOrderStmt.setInt(1, orderId);
            checkOrderStmt.setInt(2, userId);
            ResultSet rs = checkOrderStmt.executeQuery();

            if (!rs.next()) {
                throw new SQLException("Order not found for user ID: " + userId + " and order ID: " + orderId);
            }


            String cancelOrderQuery = "delete from orders where order_id = ?";
            PreparedStatement cancelOrderStmt = connection.prepareStatement(cancelOrderQuery);
            cancelOrderStmt.setInt(1, orderId);
            cancelOrderStmt.executeUpdate();

            System.out.println("Order canceled successfully for Order ID: " + orderId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createProduct(User user, Product product) {
        try {

            if (!"Admin".equals(user.getRole())) {
                throw new SQLException("Only admins can create products.");
            }


            String createProductQuery = "insert into products (product_name, description, price, quantity_in_stock, type) values (?, ?, ?, ?, ?)";
            PreparedStatement createProductStmt = connection.prepareStatement(createProductQuery, Statement.RETURN_GENERATED_KEYS);
            createProductStmt.setString(1, product.getProductName());
            createProductStmt.setString(2, product.getDescription());
            createProductStmt.setDouble(3, product.getPrice());
            createProductStmt.setInt(4, product.getQuantityInStock());
            createProductStmt.setString(5, product.getType());
            createProductStmt.executeUpdate();


            ResultSet generatedKeys = createProductStmt.getGeneratedKeys();
            int productId = 0;
            if (generatedKeys.next()) {
                productId = generatedKeys.getInt(1);
            }


            if ("Electronics".equals(product.getType())) {
                String createElectronicsQuery = "insert into electronics (product_id, brand, warranty_period) values (?, ?, ?)";
                PreparedStatement createElectronicsStmt = connection.prepareStatement(createElectronicsQuery);
                createElectronicsStmt.setInt(1, productId);
                createElectronicsStmt.setString(2, ((entity.Electronics) product).getBrand());
                createElectronicsStmt.setInt(3, ((entity.Electronics) product).getWarrantyPeriod());
                createElectronicsStmt.executeUpdate();
            } else if ("Clothing".equals(product.getType())) {
                String createClothingQuery = "insert into clothing (product_id, size, color) values (?, ?, ?)";
                PreparedStatement createClothingStmt = connection.prepareStatement(createClothingQuery);
                createClothingStmt.setInt(1, productId);
                createClothingStmt.setString(2, ((entity.Clothing) product).getSize());
                createClothingStmt.setString(3, ((entity.Clothing) product).getColor());
                createClothingStmt.executeUpdate();
            }

            System.out.println("Product created successfully with Product ID: " + productId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createUser(User user) {
        try {

            String createUserQuery = "insert into users (username, password, role) values (?, ?, ?)";
            PreparedStatement createUserStmt = connection.prepareStatement(createUserQuery);
            createUserStmt.setString(1, user.getUsername());
            createUserStmt.setString(2, user.getPassword());
            createUserStmt.setString(3, user.getRole());
            createUserStmt.executeUpdate();

            System.out.println("User created successfully with User ID: " + user.getUserId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        try {

            String getAllProductsQuery = "select * from products";
            PreparedStatement getAllProductsStmt = connection.prepareStatement(getAllProductsQuery);
            ResultSet rs = getAllProductsStmt.executeQuery();

            while (rs.next()) {
                Product product;
                if ("Electronics".equals(rs.getString("type"))) {
                    product = new entity.Electronics(rs.getInt("product_id"), rs.getString("product_name"),
                            rs.getString("description"), rs.getDouble("price"), rs.getInt("quantity_in_stock"),
                            "Electronics", 0);
                } else {
                    product = new entity.Clothing(rs.getInt("product_id"), rs.getString("product_name"),
                            rs.getString("description"), rs.getDouble("price"), rs.getInt("quantity_in_stock"),
                            "M", "Blue"); // Placeholder values for clothing-specific fields
                }
                products.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    @Override
    public List<Product> getOrderByUser(User user) {
        List<Product> products = new ArrayList<>();
        try {

            String getOrderByUserQuery = "select p.* from products p join order_items oi on p.productId= oi.productId join orders o on o.orderId = oi.orderId where o.userId = ?";
            PreparedStatement getOrderByUserStmt = connection.prepareStatement(getOrderByUserQuery);
            getOrderByUserStmt.setInt(1, user.getUserId());
            ResultSet rs = getOrderByUserStmt.executeQuery();

            while (rs.next()) {
                Product product;
                if ("Electronics".equals(rs.getString("type"))) {
                    product = new entity.Electronics(rs.getInt("product_id"), rs.getString("product_name"),
                            rs.getString("description"), rs.getDouble("price"), rs.getInt("quantity_in_stock"),
                            "Electronics", 0);
                } else {
                    product = new entity.Clothing(rs.getInt("product_id"), rs.getString("product_name"),
                            rs.getString("description"), rs.getDouble("price"), rs.getInt("quantity_in_stock"),
                            "M", "Blue"); // Placeholder values
                }
                products.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }
}