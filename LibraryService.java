package src;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class LibraryService {
    private static Scanner scanner = new Scanner(System.in);
    private static HashMap<String, User> librarianCredentials = new HashMap<>();
    private static ArrayList<Book> books = new ArrayList<>();
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/librarydb";
    private static final String USERNAME = "Harvs";
    private static final String PASSWORD = "harvsdb";


    public static void main(String[] args) {
        clearScreen();
        System.out.println("Welcome to the Library System!\n");
        String role = LoginManager.login();

        if (role.equals("invalid")) {
            System.out.println("Login failed. Exiting the system.");
            return;
        }

        System.out.println("Login successful!");

        initializeDefaultUsers();

        if (role.equals("Admin")) {
            System.out.println("Welcome Admin!\n");
            LibraryCatalog catalog = new LibraryCatalog();
            handleAdminActions(catalog, role);
        
        } else if (role.equals("Reader")) {
            System.out.println("Hello Reader!\n");
            LibraryCatalog catalog = new LibraryCatalog();
            handleReaderActions(catalog);
        }
        scanner.close();
    }

    private static void handleAdminActions(LibraryCatalog catalog, String role) {
        int choice = -1;
        do {
            displayAdminOptions();
            choice = scanner.nextInt();
            scanner.nextLine();
            
            clearScreen();

            switch (choice) {
                case 1:
                clearScreen();
                    addBook(catalog);
                    waitForEnter();
                    break;
                case 2:
                clearScreen();
                    retrieveBooksFromDatabase();
                    listBooks();
                    waitForEnter();
                    break;
                case 3:
                clearScreen();
                    searchBooks(catalog.getBooks());
                    waitForEnter();
                    break;
                case 4:
                    System.out.println("Exiting the system. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    waitForEnter();
                    break;
            }
        } while (choice != 4);
    }

    private static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private static void waitForEnter() {
        System.out.println("Press Enter to continue...");
        scanner.nextLine();
    }

    private static void handleReaderActions(LibraryCatalog catalog) {
        int choice = -1;
        do {
            displayReaderOptions();
            choice = scanner.nextInt();
            scanner.nextLine();

            clearScreen();

            switch (choice) {
                case 2:
                    retrieveBooksFromDatabase();
                    listBooks();
                    waitForEnter();
                    break;
                case 3:
                    searchBooks(catalog.getBooks());
                    waitForEnter();
                    break;
                case 4:
                    System.out.println("Exiting the system. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    waitForEnter();
                    break;
                    
            }
        } while (choice != 4);
    }

    private static void displayAdminOptions() {
        clearScreen();
        System.out.println("Library Catalog and Book Checkout System\n");
        System.out.println("Add Book      [Press 1]");
        System.out.println("List of Books [Press 2]");
        System.out.println("Search Books  [Press 3]");
        System.out.println("Exit          [Press 4]\n");
        System.out.print("Enter your choice: ");
    }

    private static void displayReaderOptions() {
        clearScreen();
        System.out.println("Library Catalog and Book Checkout System\n");
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

    private static void addBookToDatabase(Book book) {
        String JDBC_URL = LoginManager.JDBC_URL;
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

    private static void listBooks() {
        if (books.isEmpty()) {
            System.out.println("No books available in the catalog.");
        } else {
            System.out.println("Books in Catalog:\n");
            for (Book book : books) {
                System.out.println(book.getTitle() + " by " + book.getAuthor() +
                        (book.isAvailable() ? " (Available)" : " (Checked Out)"));
            }
        }
    }
    
    private static void initializeDefaultUsers() {
        User librarian = new User("librarian", "librarian123", "librarian");
        User reader = new User("reader", "reader123", "reader");
        librarianCredentials.put(librarian.getUsername(), librarian);
        librarianCredentials.put(reader.getUsername(), reader);
    }

    private static void searchBooks(ArrayList<Book> books) {
        // Implement logic to search books based on title, author, or genre
        // Display search results using listBooks method
    }
}
