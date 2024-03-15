import java.sql.*;
import java.util.Scanner;

import static java.sql.DriverManager.getConnection;

public class Main {
    // Database connection parameters
    public static final String DB_URL = "jdbc:mysql://localhost:3306/invoice?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
    public static final String DB_USER = "fruee";
    public static final String DB_PASSWORD = "1234";

    // Method to display the menu
    public static void displayMenu() {
        System.out.println("Choose an option:");
        System.out.println("1. Client Management");
        System.out.println("2. Service Management");
        System.out.println("3. Invoice Management");
        System.out.println("4. Analytics");
        System.out.println("0. Exit");
        System.out.print("Enter your choice: ");
    }

    // Client Management: Add a new client
    public static void addClient() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter client details:");
        System.out.print("Client Name: ");
        String clientName = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Phone Number: ");
        String phoneNumber = scanner.nextLine();
        System.out.print("Address: ");
        String address = scanner.nextLine();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO clients (name, email, phone, address) VALUES (?, ?, ?, ?)")) {
            stmt.setString(1, clientName);
            stmt.setString(2, email);
            stmt.setString(3, phoneNumber);
            stmt.setString(4, address);
            stmt.executeUpdate();
            System.out.println("Client added successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // Client Management: View all clients
    public static void viewClients() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM clients")) {

            if (!rs.isBeforeFirst()) {
                System.out.println("No clients found.");
                return;
            }

            System.out.println("List of Clients:");
            System.out.println("Name\t\tEmail\t\tPhone Number\tAddress");
            System.out.println("-----------------------------------------------------");
            while (rs.next()) {
                String name = rs.getString("name");
                String email = rs.getString("email");
                String phone = rs.getString("phone");
                String address = rs.getString("address");

                System.out.println(name + "\t\t" + email + "\t\t" + phone + "\t" + address);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    // Client Management: Delete an existing client
    public static void deleteClient() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter Client ID to delete:");
        int clientId = scanner.nextInt();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM clients WHERE id = ?")) {
            stmt.setInt(1, clientId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Client with ID " + clientId + " deleted successfully.");
            } else {
                System.out.println("No client found with ID " + clientId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



   /* public static void viewClientBilling() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Client ID: ");
        int clientId = scanner.nextInt();

        try (Connection conn = getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("SELECT SUM(total_amount) AS total_billed_amount FROM invoices WHERE client_id = ?")) {
            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double totalBilledAmount = rs.getDouble("total_billed_amount");
                System.out.println("Total Billed Amount for Client ID " + clientId + ": " + totalBilledAmount);
            } else {
                System.out.println("No billed amount found for Client ID " + clientId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }*/

    // Main method for user input
    // Main method for user input
    public static void main(String[] args) {
        System.out.println("Successful connection");
        Scanner scanner = new Scanner(System.in);
        int choice = -1;

        while (choice != 0) {
            displayMenu();
            choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline character

            switch (choice) {
                case 1:
                    System.out.println("Client Management selected.");
                    clientManagementMenu();
                    break;
                case 2:
                    System.out.println("Service Management selected.");
                    // Call service management methods
                    break;
                case 3:
                    System.out.println("Invoice Management selected.");
                    // Call invoice management methods
                    break;
                case 4:
                    System.out.println("Analytics selected.");
                    // Call analytics methods
                    break;
                case 0:
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a valid option.");
            }
        }

        scanner.close();
    }

    // Method to display the client management submenu
    public static void clientManagementMenu() {
        Scanner scanner = new Scanner(System.in);
        int choice = -1;

        while (choice != 0) {
            System.out.println("Client Management Menu:");
            System.out.println("1. Add Client");
            System.out.println("2. Delete Client");
            System.out.println("3. View Clients");
            System.out.println("0. Go back to main menu");
            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline character

            switch (choice) {
                case 1:
                    addClient();
                    break;
                case 2:
                    deleteClient();
                    break;
                case 3:
                    viewClients();
                    break;
                case 0:
                    System.out.println("Returning to main menu...");
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a valid option.");
            }
        }
    }}
