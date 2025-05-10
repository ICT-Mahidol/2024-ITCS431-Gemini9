public class Main {
    public static void main(String[] args) {
        Customer customer = new Customer("C001", "John Doe", "123 Main St", "555-1234");
        Vehicle vehicle = new Vehicle("V001", "RV Model X", "X-2024", 2024, "TravelCorp", 50000.0);
        Invoice invoice = new Invoice("INV1001", customer, vehicle, 55000.0);

        invoice.displayInvoice();
    } // ✅ 确保这个大括号存在
} // ✅ 这个也是必须的
