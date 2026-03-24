import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class ReportGenerator {
    public ReportGenerator(){
    }
    public static String generateUserReport(UserManager userManager, AssignmentManager assignmentManager){
        StringBuilder res = new StringBuilder();
        ValidationUtils val = new ValidationUtils();
        res.append("Users Report:").append('\n');
        res.append("-".repeat(80)).append('\n');
        for (User user : userManager.findAll()){
            List<RoleAssignment> assignments = assignmentManager.findByUser(user);
            res.append(user.username()).append(", ");
            res.append(user.fullName()).append(", ");
            res.append(user.email()).append(", Roles: ");
            for(RoleAssignment ra : assignments){
                val.requireNonEmpty(ra.role().name, "Roles is empty");
                res.append(ra.role().name).append(", ");
            }
            res.append('\n');
        }
        res.append("-".repeat(80)).append('\n');
        return res.toString();
    }

    public static String generateRoleReport(RoleManager roleManager, AssignmentManager assignmentManager){
        StringBuilder res = new StringBuilder();
        res.append("Roles Report:").append('\n');
        res.append("-".repeat(80)).append('\n');
        for (Role role : roleManager.findAll()){
            List<RoleAssignment> assignments = assignmentManager.findByRole(role);
            res.append("Role: ");
            res.append(role.name).append(", Count users:");
            res.append(assignments.stream().count()).append('\n');
        }
        res.append("-".repeat(80)).append('\n');
        return res.toString();
    }

    public static String generatePermissionMatrix(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder report = new StringBuilder();

        report.append("Permission Matrix:").append('\n');
        report.append("-".repeat(80)).append('\n');
        List<User> users = userManager.findAll();
        Set<String> resources = new TreeSet<>();

        for (User user : users) {
            for (Permission p : assignmentManager.getUserPermissions(user)) {
                resources.add(p.resource());
            }
        }

        List<String> resourceList = new ArrayList<>(resources);

        report.append(String.format("%-20s", "User/Resource"));
        for (String r : resourceList) {
            report.append(String.format(" | %-10s", r));
        }
        report.append("\n");
        report.append("-".repeat(100)).append("\n");

        for (User user : users) {
            Set<Permission> perms = assignmentManager.getUserPermissions(user);
            Map<String, String> userPerms = new HashMap<>();

            for (Permission p : perms) {
                String existing = userPerms.get(p.resource());
                userPerms.put(p.resource(), existing == null ? p.name() : existing + "," + p.name());
            }

            report.append(String.format("%-20s", user.username()));
            for (String r : resourceList) {
                String perm = userPerms.getOrDefault(r, "-");
                report.append(String.format(" | %-10s", perm));
            }
            report.append("\n");
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
