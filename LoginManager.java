package src;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class LoginManager {
    public static final String JDBC_URL = "jdbc:mysql://localhost:3306/librarydb";
    private static final String USERNAME = "Harvs";
    private static final String PASSWORD = "harvsdb";
    private static Scanner scanner = new Scanner(System.in);

    public static String login() {
        System.out.print("Enter Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();

        if (checkUserInDatabase(username, password)) {
            String role = getUserRoleFromDatabase(username);
            return role;
        } else {
            System.out.println("User account does not exist!");
            waitForEnter();
            return registerNewUser();
        }
    }

    private static boolean checkUserInDatabase(String username, String password) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String query = "SELECT Password, Role FROM Usertbl WHERE Username = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, username);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        String hashedPasswordFromDB = resultSet.getString("Password");
                        String hashedEnteredPassword = hashPassword(password); // Hash the entered password
    
                        if (hashedEnteredPassword != null && hashedEnteredPassword.equals(hashedPasswordFromDB)) {
                            String userRole = resultSet.getString("Role");
                            if (userRole != null && (userRole.equalsIgnoreCase("Admin") || userRole.equalsIgnoreCase("Reader"))) {
                                return true;
                            }
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
            String hashedPassword = hashPassword(password); // Hash the password
            String insertQuery = "INSERT INTO Usertbl (FirstName, LastName, Username, Password, Role) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                preparedStatement.setString(1, firstName);
                preparedStatement.setString(2, lastName);
                preparedStatement.setString(3, username);
                preparedStatement.setString(4, hashedPassword); // Use the hashed password in the query
                preparedStatement.setString(5, role);
    
                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("User added successfully.");
                    waitForEnter();
                } else {
                    System.out.println("Failed to add user.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String registerNewUser() {
        clearScreen();
        System.out.println("Creating a new account...");
        System.out.print("Enter First Name: ");
        String firstName = scanner.nextLine().trim();
    
        System.out.print("Enter Last Name: ");
        String lastName = scanner.nextLine().trim();
    
        System.out.print("Enter Username: ");
        String username = scanner.nextLine().trim();
    
        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();
    
        String role;
    do {
        System.out.print("Enter Role [Admin/Reader]: ");
        role = scanner.nextLine().trim(); // Remove toLowerCase() conversion
        
        if (!role.equals("Admin") && !role.equals("Reader")) {
            System.out.println("Wrong input! Please type [Admin/Reader]");
        }
    } while (!role.equals("Admin") && !role.equals("Reader"));

    addUserToDatabase(firstName, lastName, username, password, role);
    return role; // Return the role as is
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

    private static String hashPassword(String password) {
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
    
    private static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private static void waitForEnter() {
        System.out.println("Press Enter to continue...");
        scanner.nextLine();
    }
}
