public class Main {
    public static void main(String[] args) {
        // Create Vehicle object
        Vehicle car = new Vehicle("Toyota", "Camry", 2023, 30000);

        // Create Customer object
        Customer customer = new Customer("John Doe", "123-456-7890", "CUST001");

        // Create Salesperson object
        Salesperson salesperson = new Salesperson("Jane Smith", "987-654-3210", "EMP001", 150000);

        // Create Invoice object
        Invoice invoice = new Invoice("INV12345", customer, car, salesperson);

        // Display Invoice details
        invoice.displayInvoice();
    }
}
