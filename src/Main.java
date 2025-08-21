import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        ContactDAO dao = new ContactDAO();
        Scanner scanner = new Scanner(System.in);

        // Insert new contact
        System.out.println("Enter contact details:");
        System.out.print("Name: ");
        String name = scanner.nextLine();
        System.out.print("Nick Name: ");
        String nickName = scanner.nextLine();
        System.out.print("Address: ");
        String address = scanner.nextLine();
        System.out.print("Home Phone: ");
        String homePhone = scanner.nextLine();
        System.out.print("Work Phone: ");
        String workPhone = scanner.nextLine();
        System.out.print("Cell Phone: ");
        String cellPhone = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Birthday (yyyy-mm-dd): ");
        String birthdayInput = scanner.nextLine();
        Date birthday = java.sql.Date.valueOf(birthdayInput);
        System.out.print("Website: ");
        String website = scanner.nextLine();
        System.out.print("Profession: ");
        String profession = scanner.nextLine();

        ContactPerson newContact = new ContactPerson(0, name, nickName, address, homePhone,
                workPhone, cellPhone, email, birthday, website, profession);
        if (dao.insertContactPerson(newContact)) {
            System.out.println("Contact inserted successfully!");
        } else {
            System.out.println("Failed to insert contact.");
        }

        // Retrieve all contacts
        System.out.println("\n=== All Contacts ===");
        List<ContactPerson> contacts = dao.getContacts();
        for (ContactPerson c : contacts) {
            System.out.println(c);
        }

        // Demonstrate JDBCRowSet method
        System.out.println("\n=== All Contacts using JDBCRowSet ===");
        List<ContactPerson> contactsUsingRowSet = dao.getAllContactsUsingRowSet();
        for (ContactPerson c : contactsUsingRowSet) {
            System.out.println(c);
        }

        // Demonstrate WebRowSet method with XML export
        System.out.println("\n=== All Contacts using WebRowSet (saved to XML) ===");
        List<ContactPerson> contactsUsingWebRowSet = dao.getAllContactsUsingWebRowSet("dataproj.xml");
        for (ContactPerson c : contactsUsingWebRowSet) {
            System.out.println(c);
        }

        // Demonstrate loading from XML file
        System.out.println("\n=== Loading Contacts from XML file ===");
        List<ContactPerson> contactsFromXML = dao.loadContactsFromXML("dataproj.xml");
        for (ContactPerson c : contactsFromXML) {
            System.out.println(c);
        }

        // Retrieve by name
        System.out.print("\nEnter name to search for contacts: ");
        String searchName = scanner.nextLine();
        List<ContactPerson> namedContacts = dao.getContactsForName(searchName);
        System.out.println("Contacts with name containing '" + searchName + "': " + namedContacts);

        // Update a contact
        if (!contacts.isEmpty()) {
            System.out.print("\nEnter contact ID to update: ");
            int id = scanner.nextInt();
            scanner.nextLine(); // Consume the newline after nextInt()
            System.out.print("Enter new profession: ");
            String professionDetails = scanner.nextLine();
            System.out.println("Updating contact's profession...");

            ContactPerson up = null;
            for (ContactPerson contact : contacts) {
                if (contact.getId() == id) {
                    up = contact;
                    break;
                }
            }

            if (up != null) {
                up.setProfession(professionDetails);
                if (dao.updateContact(up)) {
                    System.out.println("Contact updated successfully!");
                } else {
                    System.out.println("Failed to update contact.");
                }
            } else {
                System.out.println("Contact with ID " + id + " not found.");
            }
        }

        // Batch update emails by IDs
        System.out.println("\n=== Batch Update Emails ===");
        System.out.print("How many contacts do you want to update emails for? ");
        int numContacts = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        if (numContacts > 0) {
            int[] contactIds = new int[numContacts];
            String[] newEmails = new String[numContacts];

            for (int i = 0; i < numContacts; i++) {
                System.out.print("Enter contact ID " + (i + 1) + ": ");
                contactIds[i] = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                System.out.print("Enter new email for contact ID " + contactIds[i] + ": ");
                newEmails[i] = scanner.nextLine();
            }

            dao.batchUpdateEmailsByIds(contactIds, newEmails);
        }
        System.out.println("do you want to delete any one");
        System.out.println("yes or no");
        String deleteChoice = scanner.nextLine();
        if (!deleteChoice.equalsIgnoreCase("yes")) {
            System.out.println("Exiting without deletion.");
            dao.close();
            scanner.close();
            return;
        }
        // Delete a contact by id
        if (!contacts.isEmpty()) {
            System.out.print("\nEnter ID of contact to delete: ");
            int idToDelete = scanner.nextInt();
            scanner.nextLine();

            if (dao.deleteContact(idToDelete)) {
                System.out.println("Contact with ID " + idToDelete + " deleted successfully!");
            } else {
                System.out.println("Failed to delete contact with ID " + idToDelete + ". Contact may not exist.");
            }
        }

        // Close the database connection
        dao.close();
        scanner.close();
        System.out.println("Database connection closed. Program ended.");
    }
}