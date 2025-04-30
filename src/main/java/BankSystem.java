
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;
import java.util.Arrays;

public class BankSystem {

    private static final String URL = "jdbc:mysql://localhost:3306/bank_system";
    private static final String USER = "root"; // Change to your MySQL username
    private static final String PASSWORD = "password123"; // Change to your MySQL password

    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        // Sample customers
        System.out.println("Welcome to the ATU Bank System");

        while (true) {
            System.out.println("\n1. Create Account");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Select an option: ");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1: {
                    System.out.println("Create Acc - call you Create Account Method here..");
                    createAccount();
                }
                case 2: {
                    System.out.println("Login Acc - call you Login Method here..");
                    login();
                }
                case 3: {
                    System.out.println("Thank you for using the ATU Bank System. Goodbye!");
                    return;
                }
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    // this method will create a users account
    public static void createAccount() {

        //create variables to store the user details
        String accountNo = null;
        String password = null;
        double balance = 0.0;

        //loop until the user enters a valid credentials
        //enter AccountNo
        while (true) {
            System.out.print("Enter AccountNo: ");
            accountNo = scanner.next();
            if (accountNo.trim().isEmpty()) {
                System.out.println("Account number cannot be empty. Please try again.");
            } else {
                break;
            }
        }
        //enter Password
        //Password must be at least 8 characters long, include at least one number and one special character
        while (true) {
            System.out.print("Enter Password (must be at least 8 characters, include a number and a special character): ");
            password = scanner.next();
            if (password.length() < 8 || !password.matches(".*\\d.*") || !password.matches(".*[!@#$%^&*()].*")) {
                System.out.println("Password must be at least 8 characters long, include at least one number, and one special character. Please try again.");
            } else {
                break;
            }
        }
        //enter initial deposit
        //initial deposit must be a positive number
        //if deposit is less than 0, prompt the user to enter a valid number then loop
        while (true) {
            System.out.print("Enter initial deposit (must be a positive number): ");
            if (scanner.hasNextDouble()) {
                balance = scanner.nextDouble();
                if (balance < 0) {
                    System.out.println("Initial deposit must be a positive number. Please try again.");
                } else {
                    scanner.nextLine(); // Consume newline
                    break;
                }
            } else {
                System.out.println("Invalid input. Please enter a valid number.");
                scanner.next(); // Clear invalid input
            }
        }

        // Encrypt the password
        byte[] salt;
        byte[] encryptedPassword;
        try {
            salt = PasswordEncryptionService.generateSalt();
            encryptedPassword = PasswordEncryptionService.getEncryptedPassword(password, salt);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.out.println("Error encrypting password: " + e.getMessage());
            return;
        }

    // Convert salt and encrypted password to strings for storage
    String saltString = Arrays.toString(salt);
    String encryptedPasswordString = Arrays.toString(encryptedPassword);
        

        // try connect to database
        // add the user details
        // catch any exceptions
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                Statement stmt = conn.createStatement();) {

            String sql = "INSERT INTO customers (accountNo, password, salt, balance) VALUES ('" + accountNo + "', '" + encryptedPasswordString + "', '" + saltString + "', '" + balance + "');";
            stmt.executeUpdate(sql);
            System.out.println("Account successfully created for " + accountNo);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // this method will valid the users login
    public static void login() {

        // enter username
        // enter password
        // try connect to database
        // validate the AccountNo and password
        System.out.print("Enter AccountNo: ");
        String accountNo = scanner.next();

        System.out.print("Enter Password: ");
        String password = scanner.next();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                Statement stmt = conn.createStatement();) {

            String sql = "SELECT * FROM customers WHERE accountNo = '"+accountNo+"' AND password ='"+password+"';";
            System.out.println(sql);
            ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                System.out.println("Login successful!");
                validCustomer(accountNo); // the are a valid customer
            } else {
                System.out.println("Invalid username or password.");
            }
            

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // this method is called when the login has been successfull
    public static void validCustomer(String accountNo) {
        while (true) {
            System.out.println("\n1. Check Balance");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Logout");
            System.out.print("Select an option: ");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    checkBalance(accountNo);
                    break;
                case 2:
                    System.out.print("Enter deposit amount: ");
                    double depositAmount = scanner.nextDouble();
                    updateBalance(accountNo, depositAmount, true);
                    break;
                case 3:
                    System.out.print("Enter withdrawal amount: ");
                    double withdrawAmount = scanner.nextDouble();
                    updateBalance(accountNo, withdrawAmount, false);
                    break;
                case 4:
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    public static void checkBalance(String accountNo) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                Statement stmt = conn.createStatement();) {

            String sql = "SELECT balance FROM customers WHERE accountNo = '"+accountNo+"';";
            System.out.println(sql);
            ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                System.out.println("Current balance: $" + rs.getDouble("balance"));
            } else {
                System.out.println("User not found.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateBalance(String accountNo, double amount, boolean isDeposit) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                Statement stmt = conn.createStatement();) {

            // Check current balance
            double currentBalance = 0;
            String sql = "SELECT balance FROM customers WHERE accountNo = '"+accountNo+"';";
            System.out.println(sql);
            ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                currentBalance = rs.getDouble("balance");
            }

            if (!isDeposit && amount > currentBalance) {
                System.out.println("Insufficient funds.");
                return;
            }

            double newBalance;
            if (isDeposit) // if true
            {
                newBalance = currentBalance + amount;
            } else { // else withdraw money
                newBalance = currentBalance - amount;
            }

            // Update balance
            Statement stmtupdate = conn.createStatement();
            String sqlupdate = "UPDATE customers SET balance =" + newBalance + "WHERE accountNo = '" +accountNo+"';";

            stmtupdate.executeUpdate(sqlupdate);
            System.out.println("successful! New balance: $" + newBalance);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
