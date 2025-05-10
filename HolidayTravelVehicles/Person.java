public class Person {
    private String name;
    private String phoneNumber;

    public Person(String name, String phoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void displayInfo() {
        System.out.println("Person Name: " + name);
        System.out.println("Phone Number: " + phoneNumber);
    }
}
