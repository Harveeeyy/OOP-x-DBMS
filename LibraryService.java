package src;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.HashMap;

public class LibraryService {
    private static Scanner scanner = new Scanner(System.in);
    private static HashMap<String, User> librarianCredentials = new HashMap<>();
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/librarydb";
    private static final String USERNAME = "Harvs";
    private static final String PASSWORD = "harvsdb";
    private static ArrayList<Book> books = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("Welcome to the Library System!");
        String role = login();

        if (role.equals("invalid")) {
            System.out.println("Login failed. Exiting the system.");
            return;
        }

        System.out.println("Login successful!");

        initializeDefaultUsers();

        if (role.equals("Admin")) {
            System.out.println("Welcome Admin!");
            LibraryCatalog catalog = new LibraryCatalog();
            handleLibrarianActions(catalog, role); // Pass the role as well
        } else if (role.equals("Reader")) {
            System.out.println("Hello Reader!");
        }
    }

    private static void initializeDefaultUsers() {
        User librarian = new User("librarian", "librarian123", "librarian");
        User reader = new User("reader", "reader123", "reader");
        librarianCredentials.put(librarian.getUsername(), librarian);
        librarianCredentials.put(reader.getUsername(), reader);
    }

    private static void handleLibrarianActions(LibraryCatalog catalog, String role) {
        int choice = 0;
        do {
            if (role.equals("Admin")) {
                displayMenuOptions();
                choice = scanner.nextInt();
                scanner.nextLine();
                switch (choice) {
                    case 1:
                        addBook(catalog);
                        break;
                    case 2:
                        retrieveBooksFromDatabase();
                        listBooks();
                        break;
                    case 3:
                        searchBooks(catalog.getBooks());
                        break;
                    case 4:
                        System.out.println("Exiting the system. Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                        break;
                }
            } else if (role.equals("Reader")) {
                System.out.println("Hello Reader!");
                displayReaderOptions(); // Display reader-specific options
                do {
                    choice = scanner.nextInt();
                    scanner.nextLine(); // Consume newline character
                    
                    switch (choice) {
                        case 2:
                            retrieveBooksFromDatabase();
                            listBooks();
                            break;
                        case 3:
                            searchBooks(catalog.getBooks());
                            break;
                        case 4:
                            System.out.println("Exiting the system. Goodbye!");
                            return;
                        default:
                            System.out.println("Invalid choice. Please try again.");
                            break;
                    }
                } while (choice != 4);
            }
        } while (choice != 4);
    }
    
    
    private static void displayReaderOptions() {
        System.out.println("Reader Options:");
        System.out.println("List of Books [Press 2]");
        System.out.println("Search Books  [Press 3]");
        System.out.println("Exit          [Press 4]\n");
        System.out.print("Enter your choice: ");
    }

    private static void retrieveBooksFromDatabase() {
        books.clear();
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String query = "SELECT * FROM Booktbl";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String title = resultSet.getString("Title");
                        String author = resultSet.getString("Author");
                        String genre = resultSet.getString("Genre");
                        int quantity = resultSet.getInt("Quantity");
                        boolean available = resultSet.getBoolean("Availability");

                        Book book = new Book(title, author);
                        book.setGenre(genre);
                        book.setQuantity(quantity);
                        book.setAvailable(available);
                        books.add(book);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void displayMenuOptions() {
        System.out.println("Library Catalog and Book Checkout System\n");
        System.out.println("Add Book      [Press 1]");
        System.out.println("List of Books [Press 2]");
        System.out.println("Search Books  [Press 3]");
        System.out.println("Exit          [Press 4]\n");
        System.out.print("Enter your choice: ");
    }

    private static void addBook(LibraryCatalog catalog) {
        System.out.print("Enter book title: ");
        String title = scanner.nextLine();
        System.out.print("Enter book author: ");
        String author = scanner.nextLine();
        System.out.print("Enter book genre: ");
        String genre = scanner.nextLine();
        System.out.print("Enter book quantity: ");
        int quantity = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character

        Book newBook = new Book(title, author);
        newBook.setGenre(genre);
        newBook.setQuantity(quantity);
        catalog.addBook(newBook);
        addBookToDatabase(newBook);
        System.out.println("Book added successfully!\n");
    }

    private static void listBooks() {
        if (books.isEmpty()) {
            System.out.println("No books available in the catalog.");
        } else {
            System.out.println("Books in Catalog:");
            for (Book book : books) {
                System.out.println(book.getTitle() + " by " + book.getAuthor() +
                        (book.isAvailable() ? " (Available)" : " (Checked Out)"));
            }
        }
    }

    private static void searchBooks(ArrayList<Book> books) {
        // Implement logic to search books based on title, author, or genre
        // Display search results using listBooks method
    }

    private static String login() {
        System.out.print("Enter Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();

        if (checkUserInDatabase(username, password)) {
            String role = getUserRoleFromDatabase(username);

            if (role.equals("Admin")) {
                return "Admin";
            } else if (role.equals("Reader")) {
                return "Reader";
            } else {
                return "invalid";
            }
        } else {
            System.out.println("User account does not exist.");
            System.out.println("Are you an (Admin/Reader): Press A/R");
            String roleInput = scanner.nextLine().trim().toLowerCase();

            if (roleInput.equals("a")) {
                registerNewUser();
                return "Admin";
            } else if (roleInput.equals("r")) {
                registerNewUser();
                return "Reader";
            } else {
                return "invalid";
            }
        }
    }

    private static boolean checkUserInDatabase(String username, String password) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String query = "SELECT Password FROM Usertbl WHERE Username = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, username);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        String hashedPasswordFromDB = resultSet.getString("Password");
                        String hashedEnteredPassword = hashFunction(password);

                        if (hashedEnteredPassword != null && hashedEnteredPassword.equals(hashedPasswordFromDB)) {
                            return true;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void addUserToDatabase(String firstName, String lastName, String username, String password, String role) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String insertQuery = "INSERT INTO Usertbl (FirstName, LastName, Username, Password, Role) VALUES (?, ?, ?, SHA2(?, 256), ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                preparedStatement.setString(1, firstName);
                preparedStatement.setString(2, lastName);
                preparedStatement.setString(3, username);
                preparedStatement.setString(4, password);
                preparedStatement.setString(5, role);

                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("User added successfully.");
                } else {
                    System.out.println("Failed to add user.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String getUserRoleFromDatabase(String username) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String query = "SELECT Role FROM Usertbl WHERE Username = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, username);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getString("Role");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "invalid";
    }

    private static void addBookToDatabase(Book book) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String query = "INSERT INTO Booktbl (Title, Author, Genre, Quantity) VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, book.getTitle());
            preparedStatement.setString(2, book.getAuthor());
            preparedStatement.setString(3, book.getGenre());
            preparedStatement.setInt(4, book.getQuantity());
            preparedStatement.executeUpdate();
            System.out.println("Book added to the database successfully!");
        } catch (SQLException e) {
            System.out.println("Error: Unable to add book to the database.");
            e.printStackTrace();
        }
    }

    private static void registerNewUser() {
        System.out.print("Enter First Name: ");
        String firstName = scanner.nextLine().trim();

        System.out.print("Enter Last Name: ");
        String lastName = scanner.nextLine().trim();

        System.out.print("Enter Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();

        System.out.print("Enter Role (Admin/Reader): ");
        String role = scanner.nextLine().trim();

        addUserToDatabase(firstName, lastName, username, password, role);
    }

    private static String hashFunction(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
