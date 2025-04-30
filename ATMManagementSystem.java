package com.karthik.HibernataConfig;


import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Scanner;

/**
 * ATM Management System
 * - Login
 * - Check Balance
 * - Withdraw Money
 * - Deposit Money
 * - Exit
 */
public class ATMManagementSystem {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=== Welcome to the ATM Management System ===");

        System.out.print("Enter Card Number: ");
        String cardNumber = scanner.nextLine();

        System.out.print("Enter PIN: ");
        String pin = scanner.nextLine();

        UserAccount userAccount = authenticateUser(cardNumber, pin);

        if (userAccount != null) {
            System.out.println("\nLogin Successful!\n");
            showMainMenu(userAccount);
        } else {
            System.out.println("\nInvalid Card Number or PIN. Exiting...");
        }

        HibernateUtil.shutdown(); // Close Hibernate SessionFactory
        scanner.close(); // Close scanner
    }

    /**
     * Authenticates the user based on card number and pin
     */
    private static UserAccount authenticateUser(String cardNumber, String pin) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        UserAccount account = null;

        try {
            account = session.createQuery(
                    "FROM UserAccount WHERE cardNumber = :cardNumber AND pin = :pin", UserAccount.class)
                    .setParameter("cardNumber", cardNumber)
                    .setParameter("pin", pin)
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }

        return account;
    }

    /**
     * Displays the ATM Main Menu
     */
    private static void showMainMenu(UserAccount account) {
        boolean isRunning = true;

        while (isRunning) {
            System.out.println("\n=== ATM Main Menu ===");
            System.out.println("1. Check Balance");
            System.out.println("2. Withdraw Money");
            System.out.println("3. Deposit Money");
            System.out.println("4. Exit");
            System.out.print("Select an option (1-4): ");

            int choice = readIntegerInput();

            switch (choice) {
                case 1:
                    checkBalance(account);
                    break;

                case 2:
                    withdrawMoney(account);
                    break;

                case 3:
                    depositMoney(account);
                    break;

                case 4:
                    System.out.println("Thank you for using the ATM. Goodbye!");
                    isRunning = false; // Exit loop
                    break;

                default:
                    System.out.println("Invalid option. Please choose between 1 and 4.");
            }
        }
    }

    /**
     * Reads integer input safely
     */
    private static int readIntegerInput() {
        int input = -1;
        try {
            input = scanner.nextInt();
        } catch (Exception e) {
            System.out.println("Invalid input! Please enter a number.");
        } finally {
            scanner.nextLine(); // Clear input buffer
        }
        return input;
    }

    /**
     * Checks and displays the current balance
     */
    private static void checkBalance(UserAccount account) {
        System.out.println("\nYour Current Balance: ₹" + account.getBalance() + "\n");
    }

    /**
     * Withdraws money if balance is sufficient
     */
    private static void withdrawMoney(UserAccount account) {
        System.out.print("Enter amount to withdraw: ₹");
        double amount = readDoubleInput();

        if (amount <= 0) {
            System.out.println("Invalid amount! Please enter a positive value.");
            return;
        }

        if (amount > account.getBalance()) {
            System.out.println("Insufficient balance.");
        } else {
            account.setBalance(account.getBalance() - amount);
            updateAccount(account);
            System.out.println("Withdrawal successful! New Balance: ₹" + account.getBalance());
        }
    }

    /**
     * Deposits money into the account
     */
    private static void depositMoney(UserAccount account) {
        System.out.print("Enter amount to deposit: ₹");
        double amount = readDoubleInput();

        if (amount <= 0) {
            System.out.println("Invalid amount! Please enter a positive value.");
            return;
        }

        account.setBalance(account.getBalance() + amount);
        updateAccount(account);
        System.out.println("Deposit successful! New Balance: ₹" + account.getBalance());
    }

    /**
     * Reads double input safely
     */
    private static double readDoubleInput() {
        double input = -1;
        try {
            input = scanner.nextDouble();
        } catch (Exception e) {
            System.out.println("Invalid input! Please enter a number.");
        } finally {
            scanner.nextLine(); // Clear input buffer
        }
        return input;
    }

    /**
     * Updates the account balance in the database
     */
    private static void updateAccount(UserAccount account) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            session.update(account);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }
}

