public class Invoice {
    private String invoiceNumber;
    private Customer customer;
    private Vehicle vehicle;
    private double finalPrice;

    public Invoice(String invoiceNumber, Customer customer, Vehicle vehicle, double finalPrice) {
        this.invoiceNumber = invoiceNumber;
        this.customer = customer;
        this.vehicle = vehicle;
        this.finalPrice = finalPrice;
    }

    public void displayInvoice() {
        System.out.println("Invoice Number: " + invoiceNumber);
        customer.displayCustomerInfo();
        vehicle.displayVehicleInfo();
        System.out.println("Final Price: $" + finalPrice);
    }
}
