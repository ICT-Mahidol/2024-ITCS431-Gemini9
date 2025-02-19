// Person.java
public class Person {
    private String name;
    private String contactInfo;

    public Person(String name, String contactInfo) {
        this.name = name;
        this.contactInfo = contactInfo;
    }

    public String getName() {
        return name;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void displayInfo() {
        System.out.println("Name: " + name);
        System.out.println("Contact Info: " + contactInfo);
    }
}
