import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RBACSystem {
    private UserManager userManager;
    private RoleManager roleManager;
    private AssignmentManager assignmentManager;
    private String currentUser;
    private AuditLog AuditLog;

    public RBACSystem(UserManager userManager, RoleManager roleManager, AssignmentManager assignmentManager, String currentUser) {
        this.userManager = userManager;
        this.roleManager = roleManager;
        this.assignmentManager = assignmentManager;
        this.currentUser = currentUser;
        this.AuditLog = new AuditLog();
    }

    public AuditLog getAuditLog(){
        return AuditLog;
    }

    public UserManager getUserManager(){
        return userManager;
    }

    public RoleManager getRoleManager(){
        return roleManager;
    }

    public AssignmentManager getAssignmentManager(){
        return assignmentManager;
    }

    public void setCurrentUser(String username){
        currentUser = username;
    }

    public String getCurrentUser(){
        return currentUser;
    }

    public void initialize(){

        Permission readUsers = new Permission("READ", "users", "Read users");
        Permission writeUsers = new Permission("WRITE", "users", "Write users");
        Permission deleteUsers = new Permission("DELETE", "users", "Delete users");

        Permission readRoles = new Permission("READ", "roles", "Read roles");
        Permission writeRoles = new Permission("WRITE", "roles", "Write roles");
        Permission deleteRoles = new Permission("DELETE", "roles", "Delete roles");

        Permission readAssignments = new Permission("READ", "assignments", "Read assignments");
        Permission writeAssignments = new Permission("WRITE", "assignments", "Write assignments");
        Permission deleteAssignments = new Permission("DELETE", "assignments", "Delete assignments");

        Set<Permission> adminPerm = new HashSet<>();
        adminPerm.add(readUsers);
        adminPerm.add(readRoles);
        adminPerm.add(readAssignments);
        adminPerm.add(writeUsers);
        adminPerm.add(writeRoles);
        adminPerm.add(writeAssignments);
        adminPerm.add(deleteUsers);
        adminPerm.add(deleteRoles);
        adminPerm.add(deleteAssignments);

        Set<Permission> viewerPerm = new HashSet<>();
        viewerPerm.add(readUsers);
        viewerPerm.add(readRoles);
        viewerPerm.add(readAssignments);

        Set<Permission> managerPerm = new HashSet<>();
        managerPerm.add(readUsers);
        managerPerm.add(readRoles);
        managerPerm.add(readAssignments);
        managerPerm.add(writeUsers);
        managerPerm.add(writeRoles);
        managerPerm.add(writeAssignments);

        User useradmin = User.create("ADMIN", "ADMIN", "admin@gmail.com");
        Role roleadmin = new Role("Admin", "Created by system", adminPerm);
        Role roleviewer = new Role("Viewer", "Created by system", viewerPerm);
        Role rolemanager = new Role("Manager", "Created by system", managerPerm);

        roleManager.add(roleadmin);
        roleManager.add(rolemanager);
        roleManager.add(roleviewer);
        userManager.add(useradmin);

        AssignmentMetadata metadata = AssignmentMetadata.now("System", "System initialization");
        RoleAssignment adminAssignment = new PermanentAssignment(useradmin, roleadmin, metadata);
        assignmentManager.add(adminAssignment);
    }


    public String generateStatistics(){
        StringBuilder stat = new StringBuilder();
        stat.append("\n\n\nSystem statistic:\n");
        stat.append("Total users: ").append(userManager.count()).append("\n");
        stat.append("Users:\n");
        List<User> users = userManager.findAll();
        for (int i = 0; i < users.size(); i++) {
            stat.append(i+1).append(") ");
            stat.append(users.get(i).format()).append("\n");
        }

        stat.append("\n\n");

        stat.append("Total roles: ").append(roleManager.count()).append("\n");
        stat.append("Roles:\n");
        List<Role> roles = roleManager.findAll();
        for (int i = 0; i < roles.size(); i++){
            stat.append(i+1).append(")");
            stat.append(roles.get(i).format()).append("\n");
        }

        stat.append("\n\n");

        stat.append("Total assignments: ").append(assignmentManager.count()).append("\n");
        stat.append("Assignments:\n");
        List<RoleAssignment> assignments = assignmentManager.findAll();
        for (int i = 0; i < assignments.size(); i++) {
            stat.append("  ").append(i + 1).append(") ");
            stat.append(assignments.get(i).user().username()).append(" -> ");
            stat.append(assignments.get(i).role().name()).append(" [");
            stat.append(assignments.get(i).assignmentType()).append("]");
            stat.append(assignments.get(i).isActive() ? " (ACTIVE)" : " (INACTIVE)");
            stat.append("\n");
        }

        return stat.toString();
    }


}