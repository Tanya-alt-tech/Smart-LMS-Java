/*
 * Smart Learning Management System (LMS)
 * Mini project - Object-Oriented Programming using Java
 * Author: Tanyaradzwa Masimo, Manav Rachna
 */
package lms.util;

import java.io.Console;
import java.io.InputStream;
import java.util.Scanner;

/** Reads and validates console input, so bad input can never crash a menu. */
public class InputHelper {

    /** Raised when the input stream ends (for example Ctrl+D / Ctrl+Z). */
    public static class InputClosedException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    private final Scanner scanner;

    public InputHelper(InputStream in) {
        this.scanner = new Scanner(in);
    }

    public String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            throw new InputClosedException();
        }
        return scanner.nextLine().trim();
    }

    public String readNonEmpty(String prompt) {
        while (true) {
            String line = readLine(prompt);
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("  This cannot be empty. Please try again.");
        }
    }

    /**
     * Reads a password. In a real terminal (Command Prompt, VS Code terminal)
     * the typed characters are hidden; where no terminal is available, such
     * as some IDE consoles, it falls back to normal visible input.
     */
    public String readPassword(String prompt) {
        Console console = System.console();
        if (console == null) {
            return readNonEmpty(prompt);
        }
        while (true) {
            char[] typed = console.readPassword(prompt);
            if (typed == null) {
                throw new InputClosedException();
            }
            String password = new String(typed).trim();
            java.util.Arrays.fill(typed, ' ');
            if (!password.isEmpty()) {
                return password;
            }
            System.out.println("  This cannot be empty. Please try again.");
        }
    }

    /** Keeps asking until a whole number between min and max (inclusive) is typed. */
    public int readInt(String prompt, int min, int max) {
        while (true) {
            String line = readLine(prompt);
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException e) {
                // fall through to the message below
            }
            System.out.println("  Please enter a number from " + min + " to " + max + ".");
        }
    }

    public boolean readYesNo(String prompt) {
        while (true) {
            String line = readLine(prompt + " (y/n): ").toLowerCase();
            if (line.equals("y") || line.equals("yes")) {
                return true;
            }
            if (line.equals("n") || line.equals("no")) {
                return false;
            }
            System.out.println("  Please answer y or n.");
        }
    }
}
