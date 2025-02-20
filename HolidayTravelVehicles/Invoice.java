public class Invoice {
    private String invoiceNumber;
    private Customer customer;
    private Vehicle vehicle;
    private Salesperson salesperson;

    public Invoice(String invoiceNumber, Customer customer, Vehicle vehicle, Salesperson salesperson) {
        this.invoiceNumber = invoiceNumber;
        this.customer = customer;
        this.vehicle = vehicle;
        this.salesperson = salesperson;
    }

    public void displayInvoice() {
        System.out.println("\n===== Invoice Details =====");
        System.out.println("Invoice Number: " + invoiceNumber);
        System.out.println("\n--- Customer Info ---");
        customer.displayInfo();
        System.out.println("\n--- Vehicle Info ---");
        vehicle.displayInfo();
        System.out.println("\n--- Salesperson Info ---");
        salesperson.displayInfo();
    }
}
