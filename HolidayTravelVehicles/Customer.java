// Customer.java
public class Customer extends Person {
    private String customerID;

    public Customer(String name, String contactInfo, String customerID) {
        super(name, contactInfo);
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
