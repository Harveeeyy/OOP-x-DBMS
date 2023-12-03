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

        retrieveBooksFromDatabase();

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

    private static void initializeDefaultUsers() {
        User librarian = new User("librarian", "librarian123", "librarian");
        User reader = new User("reader", "reader123", "reader");
        librarianCredentials.put(librarian.getUsername(), librarian);
        librarianCredentials.put(reader.getUsername(), reader);
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
                    updateBook(catalog);
                    waitForEnter();
                    break;
                case 3:
                    clearScreen();
                    retrieveBooksFromDatabase();
                    listBooks();
                    waitForEnter();
                    break;
                case 4:
                    clearScreen();
                    searchBooks(catalog.getBooks());
                    waitForEnter();
                    break;
                case 5:
                    System.out.println("Exiting the system. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    waitForEnter();
                    break;
            }
        } while (choice != 5);
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
                case 3:
                    retrieveBooksFromDatabase();
                    listBooks();
                    waitForEnter();
                    break;
                case 4:
                    searchBooks(catalog.getBooks());
                    waitForEnter();
                    break;
                case 5:
                    System.out.println("Exiting the system. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    waitForEnter();
                    break;
                    
            }
        } while (choice != 5);
    }

    private static void displayAdminOptions() {
        clearScreen();
        System.out.println("Library Catalog and Book Checkout System\n");
        System.out.println("Add Book      [Press 1]");
        System.out.println("Update Book   [Press 2]");
        System.out.println("List of Books [Press 3]");
        System.out.println("Search Books  [Press 4]");
        System.out.println("Exit          [Press 5]\n");
        System.out.print("Enter your choice: ");
    }

    private static void displayReaderOptions() {
        clearScreen();
        System.out.println("Library Catalog and Book Checkout System\n");
        System.out.println("List of Books [Press 3]");
        System.out.println("Search Books  [Press 4]");
        System.out.println("Exit          [Press 5]\n");
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
        scanner.nextLine();

        Book newBook = new Book(title, author);
        newBook.setGenre(genre);
        newBook.setQuantity(quantity);
        catalog.addBook(newBook);
        addBookToDatabase(newBook);
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

    //Function to Update book
    private static void updateBook(LibraryCatalog catalog) {
        System.out.println("Updating the book...");
        System.out.println("Looking for the book...\n");
        System.out.print("Enter the title of the book: ");
        String title = scanner.nextLine();
        System.out.print("Enter the author of the book: ");
        String author = scanner.nextLine();

        ArrayList<Book> matchingBooks = searchByTitleAndAuthor(title, author);

        if (matchingBooks.isEmpty()) {
            System.out.println("No books found matching the search criteria.");
            waitForEnter();
        } else {
            displayBooks(matchingBooks);
            System.out.println("\nUpdating book by:\n");
            System.out.println("Update Title                     [T]");
            System.out.println("Update Author                    [A]");
            System.out.println("Update Genre                     [G]");
            System.out.println("How many copies are checked out? [H]");
            System.out.println("Delete Book?                     [D]");
            System.out.println("Back to Menu                     [B]");
            System.out.print("\nEnter your choice: ");

            String choice = scanner.nextLine().toUpperCase();

            switch (choice) {
                case "T":
                    System.out.print("Enter the new title: ");
                    String newTitle = scanner.nextLine();
                    updateBookTitle(newTitle, title, author);
                    break;
                case "A":
                    System.out.print("Enter the new author: ");
                    String newAuthor = scanner.nextLine();
                    updateBookAuthor(newAuthor, title, author);
                    break;
                case "G":
                    System.out.print("Enter the new genre: ");
                    String newGenre = scanner.nextLine();
                    updateBookGenre(newGenre, title, author);
                    break;
                case "H":
                    updateCheckedOutCopies(title, author);
                    break;
                case "B":
                    System.out.println("Returning to Menu...");
                    break;
                case "D":
                    deleteBook(title, author);
                    break;
                default:
                    System.out.println("Invalid choice.");
                    break;
            }
        }
    }

    private static ArrayList<Book> searchByTitleAndAuthor(String title, String author) {
        ArrayList<Book> foundBooks = new ArrayList<>();
        String query = "SELECT * FROM Booktbl WHERE Title = ? AND Author = ?";
        
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, title);
            preparedStatement.setString(2, author);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String bookTitle = resultSet.getString("Title");
                    String bookAuthor = resultSet.getString("Author");
                    String genre = resultSet.getString("Genre");
                    int quantity = resultSet.getInt("Quantity");
                    boolean available = resultSet.getBoolean("Availability");
    
                    Book book = new Book(bookTitle, bookAuthor);
                    book.setGenre(genre);
                    book.setQuantity(quantity);
                    book.setAvailable(available);
                    foundBooks.add(book);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return foundBooks;
    }

    //Function to Update book by title
    private static void updateBookTitle(String newTitle, String currentTitle, String author) {
        String query = "UPDATE Booktbl SET Title = ? WHERE Title = ? AND Author = ?";
        
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, newTitle);
            preparedStatement.setString(2, currentTitle);
            preparedStatement.setString(3, author);
    
            int rowsUpdated = preparedStatement.executeUpdate();
            
            if (rowsUpdated > 0) {
                System.out.println("Book title updated successfully!");
            } else {
                System.out.println("No matching book found to update the title.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    //Function to Update book by author
    private static void updateBookAuthor(String newAuthor, String title, String currentAuthor) {
        String query = "UPDATE Booktbl SET Author = ? WHERE Title = ? AND Author = ?";
        
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, newAuthor);
            preparedStatement.setString(2, title);
            preparedStatement.setString(3, currentAuthor);
    
            int rowsUpdated = preparedStatement.executeUpdate();
            
            if (rowsUpdated > 0) {
                System.out.println("Book author updated successfully!");
            } else {
                System.out.println("No matching book found to update the author.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Function to Update book by genre
    private static void updateBookGenre(String newGenre, String title, String author) {
        String query = "UPDATE Booktbl SET Genre = ? WHERE Title = ? AND Author = ?";
        
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, newGenre);
            preparedStatement.setString(2, title);
            preparedStatement.setString(3, author);
    
            int rowsUpdated = preparedStatement.executeUpdate();
            
            if (rowsUpdated > 0) {
                System.out.println("Book genre updated successfully!");
            } else {
                System.out.println("No matching book found to update the genre.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Function to Update book by the copies that have been checked out
    private static void updateCheckedOutCopies(String title, String author) {
        System.out.print("Enter the number of copies checked out: ");
        int copiesCheckedOut = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character
    
        updateBookQuantity(copiesCheckedOut, title, author);
    }
    
    private static void updateBookQuantity(int copiesCheckedOut, String title, String author) {
        String JDBC_URL = LoginManager.JDBC_URL;
        String query = "UPDATE Booktbl SET Quantity = Quantity - ? WHERE Title = ? AND Author = ?";
    
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, copiesCheckedOut);
            preparedStatement.setString(2, title);
            preparedStatement.setString(3, author);
            int updatedRows = preparedStatement.executeUpdate();
    
            if (updatedRows > 0) {
                System.out.println("Quantity updated successfully!");
            } else {
                System.out.println("No books found matching the search criteria.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    //Function to delete the book from the entire system/database
    private static void deleteBook(String deleteTitle, String deleteAuthor) {
        String JDBC_URL = LoginManager.JDBC_URL;
        String query = "DELETE FROM Booktbl WHERE Title = ? AND Author = ?";
    
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, deleteTitle);
            preparedStatement.setString(2, deleteAuthor);
            int deletedRows = preparedStatement.executeUpdate();
    
            if (deletedRows > 0) {
                System.out.println("Book deleted successfully!");
            } else {
                System.out.println("No books found matching the search criteria.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }   

    //Function see the list of book available in the library
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

    //Search book Function
    private static void searchBooks(ArrayList<Book> books) {
    System.out.println("Searching Book by:\n");
    System.out.println("Enter Title      [Press 1]");
    System.out.println("Enter Author     [Press 2]");
    System.out.println("Enter Genre      [Press 3]");
    System.out.println("Back to Menu     [Press 4]\n");

    System.out.print("Enter your choice: ");
    int choice = scanner.nextInt();
    scanner.nextLine();

    clearScreen();
    System.out.println("Searching book by:\n");

    switch (choice) {
        case 1:
            System.out.print("Enter Title: ");
            String titleSearch = scanner.nextLine();
            displayBooks(searchByTitle(titleSearch, books));
            break;
        case 2:
            System.out.print("Enter Author: ");
            String authorSearch = scanner.nextLine();
            displayBooks(searchByAuthor(authorSearch, books));
            break;
        case 3:
            System.out.print("Enter Genre: ");
            String genreSearch = scanner.nextLine();
            displayBooks(searchByGenre(genreSearch, books));
            break;
        case 4:
            System.out.println("Returning to Menu...");
            break;
        default:
            System.out.println("Invalid choice.");
            break; }
    }

    //By title
    private static ArrayList<Book> searchByTitle(String title, ArrayList<Book> books) {
        ArrayList<Book> foundBooks = new ArrayList<>();
        String query = "SELECT * FROM Booktbl WHERE Title = ?";
        
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, title);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String bookTitle = resultSet.getString("Title");
                    String author = resultSet.getString("Author");
                    String genre = resultSet.getString("Genre");
                    int quantity = resultSet.getInt("Quantity");
                    boolean available = resultSet.getBoolean("Availability");
    
                    Book book = new Book(bookTitle, author);
                    book.setGenre(genre);
                    book.setQuantity(quantity);
                    book.setAvailable(available);
                    foundBooks.add(book);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return foundBooks;
    }

    //By author
    private static ArrayList<Book> searchByAuthor(String author, ArrayList<Book> books) {
        ArrayList<Book> foundBooks = new ArrayList<>();
        String query = "SELECT * FROM Booktbl WHERE Author = ?";
        
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, author);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String bookTitle = resultSet.getString("Title");
                    String bookAuthor = resultSet.getString("Author");
                    String genre = resultSet.getString("Genre");
                    int quantity = resultSet.getInt("Quantity");
                    boolean available = resultSet.getBoolean("Availability");
    
                    Book book = new Book(bookTitle, bookAuthor);
                    book.setGenre(genre);
                    book.setQuantity(quantity);
                    book.setAvailable(available);
                    foundBooks.add(book);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return foundBooks;
    }

    //By genre
    private static ArrayList<Book> searchByGenre(String genre, ArrayList<Book> books) {
        ArrayList<Book> foundBooks = new ArrayList<>();
        String query = "SELECT * FROM Booktbl WHERE Genre = ?";
        
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, genre);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String bookTitle = resultSet.getString("Title");
                    String bookAuthor = resultSet.getString("Author");
                    String bookGenre = resultSet.getString("Genre");
                    int quantity = resultSet.getInt("Quantity");
                    boolean available = resultSet.getBoolean("Availability");
    
                    Book book = new Book(bookTitle, bookAuthor);
                    book.setGenre(bookGenre);
                    book.setQuantity(quantity);
                    book.setAvailable(available);
                    foundBooks.add(book);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return foundBooks;
    }

    private static void displayBooks(ArrayList<Book> books) {
    if (books.isEmpty()) {
        System.out.println("No books found matching the search criteria.");
    } else {
        clearScreen();
        System.out.println("Books found:\n");
        for (Book book : books) {
            String availability = book.isAvailable() ? " (Available)" : " (Checked Out)";
            System.out.println(book.getTitle() + " by " + book.getAuthor() + availability);
        }
    }
}
}
