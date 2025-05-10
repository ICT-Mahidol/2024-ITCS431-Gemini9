public class Customer extends Person {
    private String customerID;

    public Customer(String name, String phoneNumber, String customerID) {
        super(name, phoneNumber);
        this.customerID = customerID;
    }

    public String getCustomerID() {
        return customerID;
    }

    @Override
    public void displayInfo() {
        super.displayInfo();
        System.out.println("Customer ID: " + customerID);
    }
}
