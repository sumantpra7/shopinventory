import java.sql.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ShopInventoryManager {
    static final int ROWS = 10;
    static final int COLUMNS = 12;
    static Product[][] inventory = new Product[ROWS][COLUMNS];

    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        createDatabase();
        while (true) {
            System.out.println("\n1. Add Product\n2. View Product\n3. Delete Product\n4. Process Sale (Generate Bill)\n5. Exit");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1: addProduct(); break;
                case 2: viewProduct(); break;
                case 3: deleteProduct(); break;
                case 4: generateBill(); break;
                case 5: return;
                default: System.out.println("Invalid choice");
            }
        }
    }

    static void addProduct() {
        System.out.print("Enter row (0-9): ");
        int row = scanner.nextInt();
        System.out.print("Enter column (0-11): ");
        int col = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter Product ID: ");
        String id = scanner.nextLine();
        System.out.print("Enter Product Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter MFD Date: ");
        String mfd = scanner.nextLine();
        System.out.print("Enter EXP Date: ");
        String exp = scanner.nextLine();
        System.out.print("Enter Price per unit: ");
        double price = scanner.nextDouble();
        System.out.print("Enter Quantity: ");
        int quantity = scanner.nextInt();

        inventory[row][col] = new Product(id, name, mfd, exp, price, quantity);
        System.out.println("Product added successfully.");
    }

    static void viewProduct() {
        System.out.print("Enter row and column (e.g., 2 3): ");
        int row = scanner.nextInt();
        int col = scanner.nextInt();

        Product p = inventory[row][col];
        if (p != null) {
            System.out.println(p);
        } else {
            System.out.println("No product found.");
        }
    }

    static void deleteProduct() {
        System.out.print("Enter row and column to delete (e.g., 2 3): ");
        int row = scanner.nextInt();
        int col = scanner.nextInt();

        inventory[row][col] = null;
        System.out.println("Product deleted.");
    }

    static void generateBill() {
        scanner.nextLine();
        System.out.print("Enter customer name: ");
        String customerName = scanner.nextLine();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dateTime = now.format(formatter);

        double total = 0;
        List<String> items = new ArrayList<>();

        while (true) {
            System.out.print("Enter row and column of product (or -1 to finish): ");
            int row = scanner.nextInt();
            if (row == -1) break;
            int col = scanner.nextInt();

            Product p = inventory[row][col];
            if (p == null) {
                System.out.println("No product at this location.");
                continue;
            }

            System.out.print("Enter quantity: ");
            int qty = scanner.nextInt();
            if (qty > p.quantity) {
                System.out.println("Not enough stock.");
                continue;
            }

            double price = p.price * qty;
            total += price;
            p.quantity -= qty;
            items.add(p.name + " x " + qty + " @ Rs. " + p.price + " = Rs. " + price);
        }

        System.out.println("\n===== BILL =====");
        System.out.println("Customer: " + customerName);
        System.out.println("Date/Time: " + dateTime);
        for (String item : items) System.out.println(item);
        System.out.println("Total: Rs. " + total);

        // Save bill in DB
        saveBillToDatabase(customerName, dateTime, total);
    }

    static void createDatabase() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:shop.db")) {
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS bills (id INTEGER PRIMARY KEY AUTOINCREMENT, customer TEXT, date TEXT, total REAL)");
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    static void saveBillToDatabase(String customer, String date, double total) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:shop.db")) {
            PreparedStatement pstmt = conn.prepareStatement("INSERT INTO bills (customer, date, total) VALUES (?, ?, ?)");
            pstmt.setString(1, customer);
            pstmt.setString(2, date);
            pstmt.setDouble(3, total);
            pstmt.executeUpdate();
            System.out.println("Bill saved to database.");
        } catch (SQLException e) {
            System.out.println("Error saving bill: " + e.getMessage());
        }
    }
}

class Product {
    String id, name, mfd, exp;
    double price;
    int quantity;

    Product(String id, String name, String mfd, String exp, double price, int quantity) {
        this.id = id;
        this.name = name;
        this.mfd = mfd;
        this.exp = exp;
        this.price = price;
        this.quantity = quantity;
    }

    public String toString() {
        return "Product ID: " + id + ", Name: " + name + ", MFD: " + mfd + ", EXP: " + exp + ", Price: Rs. " + price + ", Qty: " + quantity;
    }
}
