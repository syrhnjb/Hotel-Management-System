package com.abhiram.hotel.cli;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Thin wrapper around {@link Scanner} that centralizes input parsing and validation so the rest
 * of the CLI never has to handle a raw {@code InputMismatchException} or non-numeric input
 * crashing the loop.
 */
final class ConsoleIO {

    private final Scanner scanner;

    ConsoleIO(Scanner scanner) {
        this.scanner = scanner;
    }

    void print(String message) {
        System.out.print(message);
    }

    void println(String message) {
        System.out.println(message);
    }

    String readLine(String prompt) {
        print(prompt);
        if (!scanner.hasNextLine()) {
            throw new NoSuchElementException("No more input available");
        }
        return scanner.nextLine().trim();
    }

    /** Reads an integer, re-prompting on non-numeric input rather than crashing. */
    int readInt(String prompt) {
        while (true) {
            String line = readLine(prompt);
            try {
                return Integer.parseInt(line.trim());
            } catch (NumberFormatException e) {
                println("Please enter a whole number.");
            }
        }
    }

    /** Reads a date in ISO {@code yyyy-MM-dd} format, re-prompting on unparseable input. */
    LocalDate readDate(String prompt) {
        while (true) {
            String line = readLine(prompt);
            try {
                return LocalDate.parse(line.trim());
            } catch (DateTimeParseException e) {
                println("Please enter a date in yyyy-MM-dd format, e.g. 2026-09-16.");
            }
        }
    }

    boolean readYesNo(String prompt) {
        while (true) {
            String line = readLine(prompt).toLowerCase();
            if (line.equals("y") || line.equals("yes")) {
                return true;
            }
            if (line.equals("n") || line.equals("no")) {
                return false;
            }
            println("Please answer y or n.");
        }
    }
}
