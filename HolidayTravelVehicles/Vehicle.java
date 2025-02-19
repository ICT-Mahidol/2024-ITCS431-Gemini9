// Vehicle.java
public class Vehicle {
    private String brand;
    private String model;
    private int year;
    private double price;

    public Vehicle(String brand, String model, int year, double price) {
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.price = price;
    }

    // Getter method for price (fixes the compilation error)
    public double getPrice() {
        return price;
    }

    public void displayInfo() {
        System.out.println("Vehicle: " + brand + " " + model + " (" + year + ")");
        System.out.println("Price: $" + price);
    }
}
