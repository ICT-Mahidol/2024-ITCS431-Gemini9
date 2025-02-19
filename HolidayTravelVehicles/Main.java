// Main.java
public class Main {
    public static void main(String[] args) {
        // Create objects
        Customer customer = new Customer("John Doe", "john@example.com", "CUST123");
        Salesperson salesperson = new Salesperson("Alice Smith", "alice@example.com", "EMP456");
        Vehicle vehicle = new Vehicle("Toyota", "RAV4", 2024, 30000);

        // Create invoice and display details
        Invoice invoice = new Invoice(customer, salesperson, vehicle);
        invoice.displayInvoice();
    }
}
