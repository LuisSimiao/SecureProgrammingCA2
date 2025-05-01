import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;

public class BankSystem {

    private static final String URL = "jdbc:mysql://localhost:3306/bank_system";
    private static final String USER = "root"; // Change to your MySQL username
    private static final String PASSWORD = "password123"; // Change to your MySQL password

    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException {
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
    public static void createAccount() throws NoSuchAlgorithmException, InvalidKeySpecException {

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
        //Password must be at least 8 characters long, include at least one number and one special character for security
        //if password is less than 8 characters, prompt the user to enter a valid password then loop
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
         // Generate a salt for the password
        byte[] salt = null;
        try {
            salt = PasswordEncryptionService.generateSalt();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        // Encrypt the password using the generated salt
        byte[] encryptedPassword = null;
        try {
            encryptedPassword = PasswordEncryptionService.getEncryptedPassword(password, salt);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }

        // Connect to the database
        // Store the account details in the database using Prepared Statement
        String sql = "INSERT INTO customers (accountNo, password, balance, salt) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, accountNo); // Store account number as a string
            pstmt.setBytes(2, encryptedPassword); // Store password as a byte array
            pstmt.setDouble(3, balance); // Store balance as a double
            pstmt.setBytes(4, salt); // Store salt as a byte array

            pstmt.executeUpdate();
            System.out.println("Account successfully created for " + accountNo);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // this method will valid the users login
    public static void login() {

        // enter username
        // enter password
        System.out.print("Enter AccountNo: ");
        String accountNo = scanner.next();

        System.out.print("Enter Password: ");
        String password = scanner.next();

        // Connect to the database and validate the user
        // Use PreparedStatement to prevent SQL injection
        String sql = "SELECT password, salt FROM customers WHERE accountNo = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, accountNo);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Get the stored hashed password and salt as byte arrays
                byte[] storedEncryptedPassword = rs.getBytes("password");
                byte[] storedSalt = rs.getBytes("salt");

                // Authenticate the entered password
                boolean isAuthenticated = PasswordEncryptionService.authenticate(password, storedEncryptedPassword, storedSalt);

                if (isAuthenticated) {
                    System.out.println("Login successful!");
                    // Call the SimpleMFA method to validate the OTP
                    if(SimpleMFA()) {
                        System.out.println("MFA successful!");
                        validCustomer(accountNo); // Proceed to the next step
                    } else {
                        System.out.println("MFA failed. Exiting...");
                        login(); // Call the login method again
                    } 
                } else {
                    System.out.println("Invalid username or password.");
                    login(); // Call the login method again
                }
            } else {
                System.out.println("Invalid username or password.");
                login(); // Call the login method again
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

    public static boolean SimpleMFA(){
        System.out.print("Enter the OTP sent to your email: ");
        String otp = scanner.next();
        // Simulate OTP validation (in a real-world scenario, you would send an OTP to the user's email or phone)

        if (validateOTP(otp)) {
            System.out.println("OTP validated successfully!");
            return true;
        } else {
            System.out.println("Invalid OTP.");
            return false;
        } 
    }

    // Simulate OTP validation
    public static boolean validateOTP(String otp) {
        
        return otp.equals("12345"); // Replace with actual OTP validation logic
    }
}
