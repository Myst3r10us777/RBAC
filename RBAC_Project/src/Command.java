import java.util.Scanner;

@FunctionalInterface
interface Command {
    void execute(Scanner scanner, String args, RBACSystem system);
}
