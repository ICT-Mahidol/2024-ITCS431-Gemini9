public class Salesperson extends Person {
    private String salespersonID;

    public Salesperson(String salespersonID, String name, String address, String phoneNumber) {
        super(name, address, phoneNumber);
        this.salespersonID = salespersonID;
    }

    public void displaySalespersonInfo() {
        System.out.println("Salesperson ID: " + salespersonID);
        System.out.println("Name: " + name);
        System.out.println("Address: " + address);
        System.out.println("Phone Number: " + phoneNumber);
    }
}
