public class Salesperson extends Person {
    private String employeeID;
    private double salesAmount;

    public Salesperson(String name, String phoneNumber, String employeeID, double salesAmount) {
        super(name, phoneNumber);
        this.employeeID = employeeID;
        this.salesAmount = salesAmount;
    }

    public String getEmployeeID() {
        return employeeID;
    }

    public double getSalesAmount() {
        return salesAmount;
    }

    @Override
    public void displayInfo() {
        super.displayInfo();
        System.out.println("Employee ID: " + employeeID);
        System.out.println("Sales Amount: $" + salesAmount);
    }
}
