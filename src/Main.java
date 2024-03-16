import java.sql.*;
import java.util.Scanner;
import java.sql.Statement;
import static java.sql.DriverManager.getConnection;

public class Main {
    // DATABASE
    public static final String DB_URL = "jdbc:mysql://localhost:3306/invoice?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
    public static final String DB_USER = "fruee";
    public static final String DB_PASSWORD = "1234";

    // MAIN MENU
    public static void displayMenu() {
        System.out.println("Choose an option:");
        System.out.println("1. Client Management");
        System.out.println("2. Service Management");
        System.out.println("3. Invoice Management");
        System.out.println("4. Analytics");
        System.out.println("0. Exit");
        System.out.print("Enter your choice: ");
    }

    //==================================================================================================================

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
            System.out.println("-------------------------------------------------------------------------------------------------------------------------");
            System.out.printf("| %-5s | %-20s | %-30s | %-15s | %-40s |\n", "client_id", "Name", "Email", "Phone Number", "Address");
            System.out.println("-------------------------------------------------------------------------------------------------------------------------");
            while (rs.next()) {
                int id = rs.getInt("client_id");
                String name = rs.getString("name");
                String email = rs.getString("email");
                String phone = rs.getString("phone");
                String address = rs.getString("address");

                System.out.printf("| %-5d | %-20s | %-30s | %-15s | %-40s |\n", id, name, email, phone, address);
            }
            System.out.println("-------------------------------------------------------------------------------------------------------------------------");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Client Management: Update client details
    public static void updateClient(int clientId) {
        Scanner scanner = new Scanner(System.in);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Check if the client exists
            PreparedStatement checkStmt = conn.prepareStatement("SELECT * FROM clients WHERE client_id = ?");
            checkStmt.setInt(1, clientId);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.isBeforeFirst()) {
                System.out.println("No client found with ID " + clientId);
                rs.close(); // Close the result set before returning
                return;
            }

            // Client exists, prompt for new details
            System.out.println("Enter new client details:");
            System.out.print("Client Name: ");
            String clientName = scanner.nextLine();
            System.out.print("Email: ");
            String email = scanner.nextLine();
            System.out.print("Phone Number: ");
            String phoneNumber = scanner.nextLine();
            System.out.print("Address: ");
            String address = scanner.nextLine();

            // Update the client
            PreparedStatement updateStmt = conn.prepareStatement("UPDATE clients SET name = ?, email = ?, phone = ?, address = ? WHERE client_id = ?");
            updateStmt.setString(1, clientName);
            updateStmt.setString(2, email);
            updateStmt.setString(3, phoneNumber);
            updateStmt.setString(4, address);
            updateStmt.setInt(5, clientId);
            int rowsAffected = updateStmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Client with ID " + clientId + " updated successfully.");
            } else {
                System.out.println("Failed to update client with ID " + clientId);
            }

            rs.close(); // Close the result set after updating the client
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    //Client Management : delete a client
    public static void deleteClient() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter Client ID to delete:");
        int client_Id = scanner.nextInt();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM clients WHERE client_id = ?")) {
            stmt.setInt(1, client_Id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("client_id " + client_Id + " deleted successfully.");
            } else {
                System.out.println("No client found with ID " + client_Id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void totalAmountEachClientBilled() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("SELECT c.client_id, c.name, SUM(s.price * i.hours_billed) AS total_amount " +
                     "FROM clients c " +
                     "JOIN invoices inv ON c.client_id = inv.client_id " +
                     "JOIN invoice_services i ON inv.invoice_id = i.invoice_id " +
                     "JOIN service_management s ON i.service_id = s.service_id " +
                     "GROUP BY c.client_id, c.name")) {

            ResultSet rs = stmt.executeQuery();

            System.out.println("Total Amount Billed for Each Client:");
            System.out.println("-------------------------------------");
            System.out.printf("| %-10s | %-20s | %-15s |\n", "Client ID", "Name", "Total Amount");
            System.out.println("-------------------------------------");

            while (rs.next()) {
                int clientId = rs.getInt("client_id");
                String clientName = rs.getString("name");
                double totalAmount = rs.getDouble("total_amount");

                System.out.printf("| %-10d | %-20s | %-15.2f |\n", clientId, clientName, totalAmount);
            }

            System.out.println("-------------------------------------");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //==================================================================================================================

    //Service Management: add service
    public static void addService() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter service details:");
        System.out.print("Service Name: ");
        String serviceName = scanner.nextLine();
        System.out.print("Service Description: ");
        String serviceDescription = scanner.nextLine();
        System.out.print("Service Price: ");
        double servicePrice = scanner.nextDouble();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO service_management (service_name, description, price) VALUES (?, ?, ?)")) {
            stmt.setString(1, serviceName);
            stmt.setString(2, serviceDescription);
            stmt.setDouble(3, servicePrice);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Service added successfully.");
            } else {
                System.out.println("Failed to add service.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Service Management: View services
    public static void viewServices() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM service_management")) {

            if (!rs.isBeforeFirst()) {
                System.out.println("No services found.");
                return;
            }

            System.out.println("List of Services:");
            System.out.println("----------------------------------------------------------------------------------------------------------");
            System.out.printf("| %-10s | %-30s | %-40s | %10s |\n", "Service ID", "Name", "Description", "Price");
            System.out.println("----------------------------------------------------------------------------------------------------------");
            while (rs.next()) {
                int id = rs.getInt("service_id");
                String name = rs.getString("service_name");
                String description = rs.getString("description");
                double price = rs.getDouble("price");

                System.out.printf("| %-10d | %-30s | %-40s | %10.2f |\n", id, name, description, price);
            }
            System.out.println("----------------------------------------------------------------------------------------------------------");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Service Management: delete service
    public static void deleteService() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter Service ID to delete:");
        int serviceId = scanner.nextInt();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM service_management WHERE service_id = ?")) {
            stmt.setInt(1, serviceId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Service with ID " + serviceId + " deleted successfully.");
            } else {
                System.out.println("No service found with ID " + serviceId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateService() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter Service ID to update:");
        int serviceId = scanner.nextInt();
        scanner.nextLine(); // Consume newline character

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Check if the service exists
            PreparedStatement checkStmt = conn.prepareStatement("SELECT * FROM service_management WHERE service_id = ?");
            checkStmt.setInt(1, serviceId);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.isBeforeFirst()) {
                System.out.println("No service found with ID " + serviceId);
                return;
            }

            // Service exists, prompt for new details
            System.out.println("Enter new service details:");
            System.out.print("Service Name: ");
            String serviceName = scanner.nextLine();
            System.out.print("Service Description: ");
            String serviceDescription = scanner.nextLine();
            System.out.print("Service Price: ");
            double servicePrice = scanner.nextDouble();

            // Update the service
            PreparedStatement updateStmt = conn.prepareStatement("UPDATE service_management SET service_name = ?, description = ?, price = ? WHERE service_id = ?");
            updateStmt.setString(1, serviceName);
            updateStmt.setString(2, serviceDescription);
            updateStmt.setDouble(3, servicePrice);
            updateStmt.setInt(4, serviceId);
            int rowsAffected = updateStmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Service with ID " + serviceId + " updated successfully.");
            } else {
                System.out.println("Failed to update service with ID " + serviceId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }




    // Invoice Management: Create new invoice
    public static void createInvoice(int clientId) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO invoices (client_id) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, clientId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int invoiceId = generatedKeys.getInt(1);
                    System.out.println("Invoice created successfully. Invoice ID: " + invoiceId);
                }
            } else {
                System.out.println("Failed to create invoice.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Invoice Management: Add service to an invoice
    public static void addServiceToInvoice(int invoiceId, int serviceId, double hoursBilled) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO invoice_services (invoice_id, service_id, hours_billed) VALUES (?, ?, ?)")) {
            stmt.setInt(1, invoiceId);
            stmt.setInt(2, serviceId);
            stmt.setDouble(3, hoursBilled);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Service added to the invoice successfully.");
            } else {
                System.out.println("Failed to add service to the invoice.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Invoice Management: Update service hours in an invoice
    public static void updateServiceHours(int invoiceId, int serviceId, double newHoursBilled) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("UPDATE invoice_services SET hours_billed = ? WHERE invoice_id = ? AND service_id = ?")) {
            stmt.setDouble(1, newHoursBilled);
            stmt.setInt(2, invoiceId);
            stmt.setInt(3, serviceId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Service hours updated successfully.");
            } else {
                System.out.println("Failed to update service hours.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Invoice Management: Delete an invoice
    public static void deleteInvoice(int invoiceId) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM invoices WHERE invoice_id = ?")) {
            stmt.setInt(1, invoiceId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Invoice deleted successfully.");
            } else {
                System.out.println("No invoice found with the given ID.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Invoice Management: View all invoices for a particular client
    public static void viewInvoicesForClient(int clientId) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM invoices WHERE client_id = ?");
             PreparedStatement stmt2 = conn.prepareStatement("SELECT * FROM invoice_services WHERE invoice_id = ?");
             PreparedStatement stmt3 = conn.prepareStatement("SELECT price FROM services WHERE service_id = ?");
        ) {
            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int invoiceId = rs.getInt("invoice_id");
                double totalAmount = 0.0;
                stmt2.setInt(1, invoiceId);
                ResultSet rs2 = stmt2.executeQuery();
                while (rs2.next()) {
                    double hoursBilled = rs2.getDouble("hours_billed");
                    int serviceId = rs2.getInt("service_id");
                    stmt3.setInt(1, serviceId);
                    ResultSet rs3 = stmt3.executeQuery();
                    if (rs3.next()) {
                        double servicePrice = rs3.getDouble("price");
                        totalAmount += hoursBilled * servicePrice;
                    }
                }
                System.out.println("Invoice ID: " + invoiceId + ", Total Amount: " + totalAmount);
            }
        } catch (SQLException e) {
            if (e instanceof SQLSyntaxErrorException) {
                System.err.println("Error: Table 'invoice_services' or 'services' doesn't exist.");
            } else {
                e.printStackTrace();
            }
        }
    }





    //==================================================================================================================

    public static void main(String[] args) {
        System.out.println("-S H O E  C A R E  M A N A G E M E N T  S Y S T E M-");
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
                    serviceManagementMenu();
                    break;
                case 3:
                    System.out.println("Invoice Management selected.");
                    invoiceManagementMenu();
                    break;
                case 4:
                    viewAnalytics();
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


    public static void viewTotalHoursBilledforEachService() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT s.service_id, s.service_name, SUM(i.hours_billed) AS total_hours_billed " +
                     "FROM service_management s " +
                     "JOIN invoice_services i ON s.service_id = i.service_id " +
                     "GROUP BY s.service_id, s.service_name")) {

            System.out.println("Total Hours Billed for Each Service:");
            System.out.println("-------------------------------------");
            System.out.printf("| %-10s | %-30s | %-20s |\n", "Service ID", "Service Name", "Total Hours Billed");
            System.out.println("-------------------------------------");

            while (rs.next()) {
                int serviceId = rs.getInt("service_id");
                String serviceName = rs.getString("service_name");
                double totalHoursBilled = rs.getDouble("total_hours_billed");

                System.out.printf("| %-10d | %-30s | %-20.2f |\n", serviceId, serviceName, totalHoursBilled);
            }

            System.out.println("-------------------------------------");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //==================================================================================================================
    // client management submenu
    public static void clientManagementMenu() {
        Scanner scanner = new Scanner(System.in);
        int choice = -1;

        while (choice != 0) {
            System.out.println("Client Management Menu:");
            System.out.println("1. Add Client");
            System.out.println("2. Delete Client");
            System.out.println("3. View Clients");
            System.out.println("4.Total Amount Each Client Billed");
            System.out.println("5.Update Client");
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
                case 4:
                    totalAmountEachClientBilled();
                    break;
                case 5:
                    System.out.println("Enter client ID to update:");
                    int updateClientId = scanner.nextInt();
                    updateClient(updateClientId);
                    break;
                case 0:
                    System.out.println("Returning to main menu...");
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a valid option.");
            }
        }
    }
    //==================================================================================================================
    // Service management submenu
    public static void serviceManagementMenu() {
        Scanner scanner = new Scanner(System.in);
        int choice = -1;

        while (choice != 0) {
            System.out.println("Service Management Menu:");
            System.out.println("1. Add Service");
            System.out.println("2. View Services");
            System.out.println("3. Update Service");
            System.out.println("4. Delete Service");
            System.out.println("5. View Total Hours Billed for Each Service");
            System.out.println("0. Go back to main menu");
            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline character

            switch (choice) {
                case 1:
                    addService();
                    break;
                case 2:
                    viewServices();
                    break;
                case 3:
                    updateService();
                    break;
                case 4:
                    deleteService();
                    break;
                case 5:
                    viewTotalHoursBilledforEachService();
                    break;
                case 0:
                    System.out.println("Returning to main menu...");
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a valid option.");
            }
        }
    }

    //==================================================================================================================
    // Add invoice management submenu
    public static void invoiceManagementMenu() {
        Scanner scanner = new Scanner(System.in);
        int choice = -1;

        while (choice != 0) {
            System.out.println("Invoice Management Menu:");
            System.out.println("1. Create Invoice");
            System.out.println("2. Add Service to Invoice");
            System.out.println("3. Update Service Hours");
            System.out.println("4. Delete Invoice");
            System.out.println("5. View Invoices for Client");
            System.out.println("0. Go back to main menu");
            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline character

            switch (choice) {
                case 1:
                    System.out.println("Enter client ID for the invoice:");
                    int clientId = scanner.nextInt();
                    createInvoice(clientId);
                    break;
                case 2:
                    System.out.println("Enter invoice ID to add service:");
                    int invoiceId = scanner.nextInt();
                    System.out.println("Enter service ID to add:");
                    int serviceId = scanner.nextInt();
                    System.out.println("Enter hours billed:");
                    double hoursBilled = scanner.nextDouble();
                    addServiceToInvoice(invoiceId, serviceId, hoursBilled);
                    break;
                case 3:
                    System.out.println("Enter invoice ID to update service hours:");
                    int invId = scanner.nextInt();
                    System.out.println("Enter service ID to update:");
                    int servId = scanner.nextInt();
                    System.out.println("Enter new hours billed:");
                    double newHoursBilled = scanner.nextDouble();
                    updateServiceHours(invId, servId, newHoursBilled);
                    break;
                case 4:
                    System.out.println("Enter invoice ID to delete:");
                    int delInvoiceId = scanner.nextInt();
                    deleteInvoice(delInvoiceId);
                    break;
                case 5:
                    System.out.println("Enter client ID to view invoices:");
                    int viewClientId = scanner.nextInt();
                    viewInvoicesForClient(viewClientId);
                    break;
                case 0:
                    System.out.println("Returning to main menu...");
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a valid option.");
            }
        }
    }
    //==================================================================================================================
    public static void viewAnalytics() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM analytics")) {

            System.out.println("Analytics Table:");
            System.out.println("-------------------------------------------------------------------------------------------------------------------------");
            System.out.printf("| %-10s | %-12s | %-12s | %-15s | %-30s | %-20s |\n",
                    "Analytics ID", "Start Date", "End Date", "Total Income", "Most Popular Service", "Top Client");
            System.out.println("-------------------------------------------------------------------------------------------------------------------------");

            while (rs.next()) {
                int analyticsId = rs.getInt("analytics_id");
                Date startDate = rs.getDate("start_date");
                Date endDate = rs.getDate("end_date");
                double totalIncome = rs.getDouble("total_income");
                String mostPopularService = rs.getString("most_popular_service");
                String topClient = rs.getString("top_client");

                System.out.printf("| %-12d | %-12s | %-12s | %-15.2f | %-30s | %-20s |\n",
                        analyticsId, startDate, endDate, totalIncome, mostPopularService, topClient);
            }
            System.out.println("-------------------------------------------------------------------------------------------------------------------------");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
