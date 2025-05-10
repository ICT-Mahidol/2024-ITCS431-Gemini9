// Salesperson.java
public class Salesperson extends Person {
    private String employeeID;

    public Salesperson(String name, String contactInfo, String employeeID) {
        super(name, contactInfo);
        this.employeeID = employeeID;
    }

    public String getEmployeeID() {
        return employeeID;
    }

    @Override
    public void displayInfo() {
        super.displayInfo();
        System.out.println("Employee ID: " + employeeID);
    }
}
