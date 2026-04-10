package allfiles;

import java.util.List;
import java.util.Scanner;

public class ConsoleUtils {
    public static String promptString(Scanner scanner, String message, boolean required) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();
            if (!required || !input.isEmpty()) {
                return input;
            }
            System.out.println("This field is required!");
        }
    }

    public static int promptInt(Scanner scanner, String message, int min, int max) {
        while (true) {
            System.out.print(message);
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("Enter number between " + min + " and " + max);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number!");
            }
        }
    }

    public static boolean promptYesNo(Scanner scanner, String message) {
        while (true) {
            System.out.print(message + " (y/n): ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("y") || input.equals("yes")) return true;
            if (input.equals("n") || input.equals("no")) return false;
            System.out.println("Enter y or n");
        }
    }

    public static <T> T promptChoice(Scanner scanner, String message, List<T> options) {
        if (options.isEmpty()) return null;

        while (true) {
            System.out.println("\n" + message);
            for (int i = 0; i < options.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + options.get(i));
            }
            System.out.print("Choose (1-" + options.size() + "): ");

            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice >= 1 && choice <= options.size()) {
                    return options.get(choice - 1);
                }
                System.out.println("Invalid choice!");
            } catch (NumberFormatException e) {
                System.out.println("Enter a number!");
            }
        }
    }

}
