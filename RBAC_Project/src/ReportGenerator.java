import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;


public class ReportGenerator {
    public ReportGenerator(){
    }
    public static String generateUserReport(UserManager userManager, AssignmentManager assignmentManager){
        StringBuilder res = new StringBuilder();
        ValidationUtils val = new ValidationUtils();
        List<User> users = userManager.findAll();

        res.append("Users Report:").append('\n');
        res.append("-".repeat(80)).append('\n');

        List<String> userLine = users.parallelStream().map(user -> {
            List<RoleAssignment> assignments = assignmentManager.findByUser(user);
            StringBuilder line = new StringBuilder();

            line.append(user.username()).append(", ");
            line.append(user.fullName()).append(", ");
            line.append(user.email()).append(", Roles: ");
            for(RoleAssignment ra : assignments){
                val.requireNonEmpty(ra.role().name, "Roles is empty");
                line.append(ra.role().name).append(", ");
            }
            line.append('\n');
            return line.toString();
        }).collect(Collectors.toList());

        for (String line : userLine){
            res.append(line);
        }

        res.append("-".repeat(80)).append('\n');
        return res.toString();
    }

    public static String generateRoleReport(RoleManager roleManager, AssignmentManager assignmentManager){
        StringBuilder res = new StringBuilder();
        res.append("Roles Report:").append('\n');
        res.append("-".repeat(80)).append('\n');

        List<Role> roles = roleManager.findAll();
        List<String> roleLine = roles.parallelStream().map(role -> {
            List<RoleAssignment> assignments = assignmentManager.findByRole(role);
            StringBuilder line = new StringBuilder();

            line.append("Role: ");
            line.append(role.name).append(", Count users:");
            line.append(assignments.size()).append('\n');
            return line.toString();
        }).collect(Collectors.toList());

        for (String line : roleLine){
            res.append(line);
        }
        res.append("-".repeat(80)).append('\n');
        return res.toString();
    }

    public static String generatePermissionMatrix(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder report = new StringBuilder();

        report.append("Permission Matrix:").append('\n');
        report.append("-".repeat(80)).append('\n');

        List<User> users = userManager.findAll();

        Set<String> resources = users.parallelStream()
                .flatMap(user -> assignmentManager.getUserPermissions(user).stream())
                .map(Permission::resource)
                .collect(Collectors.toCollection(TreeSet::new));

        List<String> resourceList = new ArrayList<>(resources);

        report.append(String.format("%-20s", "User/Resource"));
        for (String r : resourceList) {
            report.append(String.format(" | %-10s", r));
        }
        report.append("\n");
        report.append("-".repeat(100)).append("\n");

        List<String> rows = users.parallelStream()
                .map(user -> {
                    StringBuilder line = new StringBuilder();
                    Set<Permission> perms = assignmentManager.getUserPermissions(user);
                    Map<String, String> userPerms = new HashMap<>();

                    for (Permission p : perms) {
                        String existing = userPerms.get(p.resource());
                        userPerms.put(p.resource(), existing == null ? p.name() : existing + "," + p.name());
                    }

                    line.append(String.format("%-20s", user.username()));
                    for (String r : resourceList) {
                        String perm = userPerms.getOrDefault(r, "-");
                        line.append(String.format(" | %-10s", perm));
                    }
                    line.append("\n");
                    return line.toString();
                }).collect(Collectors.toList());
        for (String line : rows){
            report.append(line);
        }
        return report.toString();
    }

    public static void exportToFile(String report, String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.print(report);
            System.out.println("Report saved to: " + filename);
        } catch (IOException e) {
            System.out.println("Error saving report: " + e.getMessage());
        }
    }
}
