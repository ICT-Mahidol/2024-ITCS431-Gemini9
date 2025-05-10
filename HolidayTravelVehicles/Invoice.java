// Invoice.java
public class Invoice {
    private Customer customer;
    private Salesperson salesperson;
    private Vehicle vehicle;
    private double totalPrice;

    public Invoice(Customer customer, Salesperson salesperson, Vehicle vehicle) {
        this.customer = customer;
        this.salesperson = salesperson;
        this.vehicle = vehicle;
        this.totalPrice = vehicle.getPrice();
    }

    public void displayInvoice() {
        System.out.println("----- Invoice Details -----");
        customer.displayInfo();
        salesperson.displayInfo();
        vehicle.displayInfo();
        System.out.println("Total Price: $" + totalPrice);
        System.out.println("---------------------------");
    }
}
