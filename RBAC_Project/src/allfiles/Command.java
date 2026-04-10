package allfiles;

import java.util.Scanner;

@FunctionalInterface
public interface Command {
    void execute(Scanner scanner, String args, RBACSystem system);
}
