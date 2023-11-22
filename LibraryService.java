package src;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class LibraryService {
    private static HashMap<String, User> librarianCredentials = new HashMap<>();
    private static HashMap<String, User> readerCredentials = new HashMap<>();
    private static Scanner scanner = new Scanner(System.in);
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/librarydb";
    private static final String USERNAME= "Harvs";
    private static final String PASSWORD = "harvsdb";
    private static ArrayList<Book> books = new ArrayList<>();

    public static void main(String[] args) {
        initializeDefaultUsers();
        System.out.println("Welcome to the Library System!");
        String role = login();

        if (role.equals("invalid")) {
            System.out.println("Login failed. Exiting the system.");
            return;
        }

        System.out.println("Login successful!");

        if (role.equals("Admin")) {
            System.out.println("Welcome Admin!");
            LibraryCatalog catalog = new LibraryCatalog();
            handleLibrarianActions(catalog);
        } else if (role.equals("Reader")) {
            System.out.println("Hello Reader!");
        }
    }

    private static void initializeDefaultUsers() {
        User librarian = new User("librarian", "librarian123", "librarian");
        User reader = new User("reader", "reader123", "reader");
        librarianCredentials.put(librarian.getUsername(), librarian);
        readerCredentials.put(reader.getUsername(), reader);
    }

    private static void handleLibrarianActions(LibraryCatalog catalog) {
        int choice;
        do {
            displayMenuOptions();
            choice = scanner.nextInt();
            scanner.nextLine();
            switch (choice) {
                case 1:
                    addBook(catalog);
                    break;
                case 2:
                    retrieveBooksFromDatabase(); // Retrieve books from the database
                    listBooks(); // List books after retrieval
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
        } while (choice != 4);
        scanner.close();
    }
    
    private static void retrieveBooksFromDatabase() {
        books.clear(); // Clear the existing books
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

    private static String login() {
        System.out.print("Enter Username: ");
        String username = scanner.nextLine().trim();
    
        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();
    
        // Check if the username and password are valid
        if (checkUserInDatabase(username, password)) {
            // Retrieve the user's role from the database
            String role = getUserRoleFromDatabase(username);
    
            if (role.equals("Admin")) {
                return "Admin"; // Return 'librarian' for admin
            } else if (role.equals("Reader")) {
                return "Reader"; // Return 'reader' for reader
            } else {
                return "invalid"; // Return 'invalid' for any other role or unexpected situation
            }
        } else {
            System.out.println("User account does not exist.");
            System.out.println("Are you an (Admin/Reader): Press A/R");
            String roleInput = scanner.nextLine().trim().toLowerCase();
    
            if (roleInput.equals("a")) {
                recordUserRoleInDatabase(username, "Admin");
                return "librarian"; // Return 'librarian' for admin
            } else if (roleInput.equals("r")) {
                recordUserRoleInDatabase(username, "Reader");
                return "reader"; // Return 'reader' for reader
            } else {
                return "invalid"; // Return 'invalid' for any other input
            }
        }
    }

    private static boolean checkUserInDatabase(String username, String password) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String query = "SELECT Username FROM Usertbl WHERE Username = ? AND Password = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, hashFunction (password));
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    return resultSet.next(); // Returns true if a user with the given username and password exists
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Return false in case of any database error
    }
    

    private static void recordUserRoleInDatabase(String username, String role) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String updateQuery = "UPDATE Usertbl SET Role = ? WHERE Username = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                preparedStatement.setString(1, role);
                preparedStatement.setString(2, username);
    
                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("User role updated successfully.");
                } else {
                    System.out.println("Failed to update user role.");
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
        return "invalid"; // Return 'invalid' if the user's role is not found or in case of any database error
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
        addBookToDatabase(newBook); // Add book to the database
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

    private static String hashFunction(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());

            // Convert byte array to a string representation
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null; // Return null in case of any error
    }
}
