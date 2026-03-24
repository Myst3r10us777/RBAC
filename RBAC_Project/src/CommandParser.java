import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CommandParser {
    Map<String, Command> commands = new HashMap<>();
    Map<String, String> commandDescriptions = new HashMap<>();

    public CommandParser() {
    }

    public void registerCommand(String name, String description, Command command){
        commands.put(name, command);
        commandDescriptions.put(name, description);
    }

    public void executeCommand(String commandName, String args, Scanner scanner, RBACSystem system){
        Command command = commands.get(commandName);
        if (command == null) {
            System.out.println("Unknown command: " + commandName);
            return;
        }
        command.execute(scanner, args, system);
    }

    public void printHelp(){
        int i = 1;
        for (Map.Entry<String, String> entry : commandDescriptions.entrySet()) {
            System.out.println(i + ") " + entry.getKey() + ": " + entry.getValue() + '\n');
            i++;
        }
    }

    public void parseAndExecute(String input, Scanner scanner, RBACSystem system){
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        String[] parts = input.trim().split("\\s+", 2);
        String commandName = parts[0];

        String args = parts.length > 1 ? parts[1] : null;

        Command command = commands.get(commandName);

        executeCommand(commandName, args, scanner, system);
    }
}
