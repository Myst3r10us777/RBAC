import java.util.*;

public class CommandRegistry {
    public void RegistryAllCommands(CommandParser pars){
        pars.registerCommand("user-list", "Output all users", ((scanner, args, system) -> {

            List<User> users = system.getUserManager().findAll();
            System.out.println("-".repeat(80));
            System.out.printf("%-4s | %-20s | %-30s | %-20s%n", "№", "Username", "Full name", "Email");
            System.out.println("-".repeat(80));
            for (int i = 0; i < users.size(); i++){
                User u = users.get(i);
                System.out.printf("%-4d | %-20s | %-30s | %-20s%n",
                        i + 1,
                        u.username(),
                        u.fullName(),
                        u.email())
                ;
            }
            System.out.println("-".repeat(80) + '\n');
        }));

        pars.registerCommand("user-create", "Create new user", ((scanner, args, system) -> {
            System.out.print("Input username:");
            String username = scanner.nextLine().trim();

            System.out.print("Input FullName:");
            String fullName = scanner.nextLine().trim();

            System.out.print("Input email:");
            String email = scanner.nextLine().trim();

            try{
                User user = User.create(username, fullName, email);
                system.getUserManager().add(user);
                system.getAuditLog().log("Created user", system.getCurrentUser(), username, "user-create created: " + username);
            } catch (Exception e){
                System.out.println("Error: " + e.getMessage());
            }
        }));

        pars.registerCommand("user-view", "User information", ((scanner, args, system) -> {
            System.out.print("Input username:");
            String username = scanner.nextLine().trim();

            try{
                Optional<User> userOpt = system.getUserManager().findByUsername(username);
                if (!userOpt.isPresent()) {
                    System.out.println("User not found");
                    return;
                }
                User user = userOpt.get();
                System.out.println("Information " + username + ": ");
                System.out.println(user.format());

                List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
                System.out.println("Roles:");
                for (RoleAssignment ra : assignments) {
                    System.out.println(ra.role().format());
                }
            } catch (Exception e){
                System.out.println("Error: " + e.getMessage());
            }
        }));

        pars.registerCommand("user-update", "Update user information", ((scanner, args, system) -> {
            System.out.print("Input username:");
            String username = scanner.nextLine().trim();

            System.out.print("Input new FullName:");
            String newfullName = scanner.nextLine().trim();

            System.out.print("Input new email:");
            String newEmail = scanner.nextLine().trim();

            try{
                system.getUserManager().update(username, newfullName, newEmail);
            } catch (Exception e){
                System.out.println("Error: " + e.getMessage());
            }
        }));

        pars.registerCommand("user-delete", "Delete user", ((scanner, args, system) -> {
            System.out.print("Input username:");
            String username = scanner.nextLine().trim();

            System.out.println("Are you sure?(y or n)");
            String confirm = scanner.nextLine().trim();
            if (!confirm.equalsIgnoreCase("y")){
                return;
            } else {
                Optional<User> userOpt = system.getUserManager().findByUsername(username);
                if (!userOpt.isPresent()) {
                    System.out.println("User not found");
                    return;
                }
                User u = userOpt.get();

                List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(u);
                for (RoleAssignment ra : assignments) {
                    system.getAssignmentManager().remove(ra);
                }
                system.getUserManager().remove(u);
                System.out.println("User: " + username + " - removed");
                system.getAuditLog().log("Delete user", system.getCurrentUser(), username, "user-delete deleted: " + username);
            }
        }));

        pars.registerCommand("user-search", "User search", ((scanner, args, system) -> {
            System.out.println("\nВыберите фильтр:");
            System.out.println("1 - По username (содержит)");
            System.out.println("2 - По email (содержит)");
            System.out.println("3 - По домену email");
            System.out.println("4 - По полному имени (содержит)");
            System.out.println("0 - Отмена");
            System.out.print("Ваш выбор: ");

            String choice = scanner.nextLine().trim();

            UserFilter filter = null;
            String searchValue = null;

            switch (choice) {
                case "1":
                    System.out.print("Введите часть username: ");
                    searchValue = scanner.nextLine().trim();
                    filter = UserFilters.byUsernameContains(searchValue);
                    break;
                case "2":
                    System.out.print("Введите весь email: ");
                    searchValue = scanner.nextLine().trim();
                    filter = UserFilters.byEmail(searchValue);
                    break;
                case "3":
                    System.out.print("Введите домен (например @gmail.com): ");
                    searchValue = scanner.nextLine().trim();
                    filter = UserFilters.byEmailDomain(searchValue);
                    break;
                case "4":
                    System.out.print("Введите часть полного имени: ");
                    searchValue = scanner.nextLine().trim();
                    filter = UserFilters.byFullNameContains(searchValue);
                    break;
                case "0":
                    System.out.println("Поиск отменен");
                    return;
                default:
                    System.out.println("Неверный выбор");
                    return;
            }

            List<User> users = system.getUserManager().findByFilter(filter);
            System.out.println("-".repeat(80));
            System.out.printf("%-4s | %-20s | %-30s | %-20s%n", "№", "Username", "Full name", "Email");
            System.out.println("-".repeat(80));
            for (int i = 0; i < users.size(); i++){
                User u = users.get(i);
                System.out.printf("%-4d | %-20s | %-30s | %-20s%n",
                        i + 1,
                        u.username(),
                        u.fullName(),
                        u.email())
                ;
            }
            System.out.println("-".repeat(80) + '\n');
        }));


        pars.registerCommand("role-list", "Output all roles", ((scanner, args, system) -> {
            List<Role> roles = system.getRoleManager().findAll();
            if (roles.isEmpty()) {
                System.out.println("No roles found");
                return;
            }
            for (int i = 0; i < roles.size(); i++) {
                Role r = roles.get(i);
                System.out.println(i + ") " + r.format());
            }
            System.out.println("-".repeat(80) + '\n');
        }));

        pars.registerCommand("role-create", "Create new role", ((scanner, args, system) -> {
            System.out.print("Input role name: ");
            String roleName = scanner.nextLine().trim();
            System.out.print("Input description: ");
            String description = scanner.nextLine().trim();

            try {
                Role role = new Role(roleName, description, new HashSet<>());
                system.getRoleManager().add(role);
                System.out.println("Role created successfully!");

                while (true) {
                    System.out.print("\nAdd permission? (y/n): ");
                    String addPerm = scanner.nextLine().trim();
                    if (!addPerm.equalsIgnoreCase("y")) {
                        break;
                    }

                    System.out.print("Permission name: ");
                    String permName = scanner.nextLine().trim();
                    System.out.print("Resource: ");
                    String resource = scanner.nextLine().trim();
                    System.out.print("Description: ");
                    String permDesc = scanner.nextLine().trim();

                    Permission perm = new Permission(permName, resource, permDesc);
                    system.getRoleManager().addPermissionToRole(roleName, perm);
                    System.out.println("Permission added!");
                }
                system.getAuditLog().log("Created role", system.getCurrentUser(), roleName, "role-create created: " + roleName);
                System.out.println("Role creation completed!");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }));

        pars.registerCommand("role-view", "View role information", ((scanner, args, system) -> {
            System.out.print("Input role name: ");
            String roleName = scanner.nextLine().trim();

            Optional<Role> roleOpt = system.getRoleManager().findByName(roleName);
            if (!roleOpt.isPresent()) {
                System.out.println("Role not found: " + roleName);
                return;
            }
            Role role = roleOpt.get();
            System.out.println(role.format());
        }));

        pars.registerCommand("role-update", "Update role information", ((scanner, args, system) -> {
            System.out.print("Input role name: ");
            String roleName = scanner.nextLine().trim();

            Optional<Role> roleOpt = system.getRoleManager().findByName(roleName);
            if (!roleOpt.isPresent()) {
                System.out.println("Role not found: " + roleName);
                return;
            }

            System.out.print("Input new name: ");
            String newName = scanner.nextLine().trim();
            System.out.print("Input new description: ");
            String newDesc = scanner.nextLine().trim();

            try {
                Role oldRole = roleOpt.get();
                String finalName = newName.isEmpty() ? oldRole.name() : newName;
                String finalDesc = newDesc.isEmpty() ? oldRole.description : newDesc;

                Role updatedRole = new Role(finalName, finalDesc, oldRole.getPermissions());
                system.getRoleManager().remove(oldRole);
                system.getRoleManager().add(updatedRole);
                System.out.println("Role updated successfully!");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }));

        pars.registerCommand("role-delete", "Delete role", ((scanner, args, system) -> {
            System.out.print("Input role name: ");
            String roleName = scanner.nextLine().trim();

            Optional<Role> roleOpt = system.getRoleManager().findByName(roleName);
            if (!roleOpt.isPresent()) {
                System.out.println("Role not found: " + roleName);
                return;
            }
            Role role = roleOpt.get();

            List<RoleAssignment> allAssignments = system.getAssignmentManager().findAll();
            List<User> assignedUsers = new ArrayList<>();
            for (RoleAssignment ra : allAssignments) {
                if (ra.role().equals(role)) {
                    assignedUsers.add(ra.user());
                }
            }

            if (!assignedUsers.isEmpty()) {
                System.out.println("WARNING: This role is assigned to users:");
                for (User u : assignedUsers) {
                    System.out.println("  - " + u.username());
                }
            }

            System.out.print("Are you sure to delete role? (y/n): ");
            String confirm = scanner.nextLine().trim();
            if (!confirm.equalsIgnoreCase("y")) {
                System.out.println("Delete cancelled");
                return;
            }

            for (RoleAssignment ra : allAssignments) {
                if (ra.role().equals(role)) {
                    system.getAssignmentManager().remove(ra);
                }
            }
            system.getAuditLog().log("Delete role", system.getCurrentUser(), roleName, "role-delete deleted: " + roleName);
            system.getRoleManager().remove(role);
            System.out.println("Role '" + roleName + "' deleted");
        }));

        pars.registerCommand("role-add-permission", "Add permission to role", ((scanner, args, system) -> {
            System.out.print("Input role name: ");
            String roleName = scanner.nextLine().trim();

            Optional<Role> roleOpt = system.getRoleManager().findByName(roleName);
            if (!roleOpt.isPresent()) {
                System.out.println("Role not found: " + roleName);
                return;
            }

            System.out.print("Permission name: ");
            String permName = scanner.nextLine().trim();
            System.out.print("Resource: ");
            String resource = scanner.nextLine().trim();
            System.out.print("Description: ");
            String description = scanner.nextLine().trim();

            try {
                Permission perm = new Permission(permName, resource, description);
                system.getRoleManager().addPermissionToRole(roleName, perm);
                System.out.println("Permission added to role '" + roleName + "'");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }));

        pars.registerCommand("role-remove-permission", "Remove permission from role", ((scanner, args, system) -> {
            System.out.print("Input role name: ");
            String roleName = scanner.nextLine().trim();

            Optional<Role> roleOpt = system.getRoleManager().findByName(roleName);
            if (!roleOpt.isPresent()) {
                System.out.println("Role not found: " + roleName);
                return;
            }
            Role role = roleOpt.get();

            Set<Permission> permissions = role.getPermissions();
            if (permissions.isEmpty()) {
                System.out.println("Role has no permissions");
                return;
            }

            System.out.println("\nPermissions:");
            System.out.println("-".repeat(60));
            List<Permission> permList = new ArrayList<>(permissions);
            for (int i = 0; i < permList.size(); i++) {
                Permission p = permList.get(i);
                System.out.printf("%d. %s on %s - %s%n", i + 1, p.name(), p.resource(), p.description());
            }
            System.out.println("-".repeat(60));

            System.out.print("Select permission number to remove: ");
            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input");
                return;
            }

            if (choice < 1 || choice > permList.size()) {
                System.out.println("Invalid selection");
                return;
            }

            Permission toRemove = permList.get(choice - 1);
            try {
                system.getRoleManager().removePermissionFromRole(roleName, toRemove);
                System.out.println("Permission removed from role '" + roleName + "'");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }));

        pars.registerCommand("role-search", "Search roles", ((scanner, args, system) -> {
            System.out.println("\nВыберите фильтр:");
            System.out.println("1 - По имени (содержит)");
            System.out.println("2 - По наличию конкретного права");
            System.out.println("3 - По минимальному количеству прав");
            System.out.println("0 - Отмена");
            System.out.print("Ваш выбор: ");

            String choice = scanner.nextLine().trim();
            List<Role> results = new ArrayList<>();

            switch (choice) {
                case "1":
                    System.out.print("Введите часть имени роли: ");
                    String nameSub = scanner.nextLine().trim();
                    results = system.getRoleManager().findByFilter(role ->
                            role.name().toLowerCase().contains(nameSub.toLowerCase()));
                    break;
                case "2":
                    System.out.print("Введите имя права: ");
                    String permName = scanner.nextLine().trim();
                    System.out.print("Введите ресурс: ");
                    String resource = scanner.nextLine().trim();
                    results = system.getRoleManager().findRolesWithPermission(permName, resource);
                    break;
                case "3":
                    System.out.print("Минимальное количество прав: ");
                    int minPerms;
                    try {
                        minPerms = Integer.parseInt(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input");
                        return;
                    }
                    results = system.getRoleManager().findByFilter(role ->
                            role.getPermissions().size() >= minPerms);
                    break;
                case "0":
                    System.out.println("Search cancelled");
                    return;
                default:
                    System.out.println("Invalid choice");
                    return;
            }

            if (results.isEmpty()) {
                System.out.println("No roles found");
                return;
            }

            System.out.println("\n" + "-".repeat(80));
            System.out.printf("%-4s | %-20s | %-30s | %-10s%n", "№", "Name", "Description", "Permissions");
            System.out.println("-".repeat(80));
            for (int i = 0; i < results.size(); i++) {
                Role r = results.get(i);
                System.out.printf("%-4d | %-20s | %-30s | %-10d%n",
                        i + 1,
                        r.name(),
                        r.description,
                        r.getPermissions().size());
            }
            System.out.println("-".repeat(80));
            System.out.println("Found: " + results.size() + "\n");
        }));



        pars.registerCommand("assign-role", "Assign role to user", ((scanner, args, system) -> {
            System.out.print("Input username: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (!userOpt.isPresent()) {
                System.out.println("User not found: " + username);
                return;
            }
            User user = userOpt.get();

            List<Role> roles = system.getRoleManager().findAll();
            if (roles.isEmpty()) {
                System.out.println("No roles available");
                return;
            }

            System.out.println("\nAvailable roles:");
            System.out.println("-".repeat(50));
            for (int i = 0; i < roles.size(); i++) {
                System.out.printf("%d. %s - %s%n", i + 1, roles.get(i).name(), roles.get(i).description);
            }
            System.out.println("-".repeat(50));

            System.out.print("Select role number: ");
            int roleChoice;
            try {
                roleChoice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input");
                return;
            }

            if (roleChoice < 1 || roleChoice > roles.size()) {
                System.out.println("Invalid selection");
                return;
            }
            Role role = roles.get(roleChoice - 1);

            System.out.print("Assignment type (PERMANENT/TEMPORARY): ");
            String type = scanner.nextLine().trim().toUpperCase();
            if (!type.equals("PERMANENT") && !type.equals("TEMPORARY")) {
                System.out.println("Invalid type. Using PERMANENT");
                type = "PERMANENT";
            }

            String expiresAt = null;
            if (type.equals("TEMPORARY")) {
                System.out.print("Expiration date (yyyy-MM-ddTHH:mm:ss): ");
                expiresAt = scanner.nextLine().trim();
            }

            System.out.print("Reason for assignment: ");
            String reason = scanner.nextLine().trim();

            try {
                AssignmentMetadata metadata = AssignmentMetadata.now(system.getCurrentUser(), reason);
                RoleAssignment assignment;
                if (type.equals("TEMPORARY")) {
                    assignment = new TemporaryAssignment(user, role, metadata, expiresAt, false);
                } else {
                    assignment = new PermanentAssignment(user, role, metadata);
                }
                system.getAssignmentManager().add(assignment);
                system.getAuditLog().log("Assign role", system.getCurrentUser(), username, "assign-role assign to: " + username);
                System.out.println("Role '" + role.name() + "' assigned to '" + username + "'");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }));

        pars.registerCommand("revoke-role", "Revoke role from user", ((scanner, args, system) -> {
            System.out.print("Input username: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (!userOpt.isPresent()) {
                System.out.println("User not found: " + username);
                return;
            }
            User user = userOpt.get();

            List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
            List<RoleAssignment> activeAssignments = new ArrayList<>();
            for (RoleAssignment ra : assignments) {
                if (ra.isActive()) {
                    activeAssignments.add(ra);
                }
            }

            if (activeAssignments.isEmpty()) {
                System.out.println("No active assignments for user: " + username);
                return;
            }

            System.out.println("\nActive assignments:");
            System.out.println("-".repeat(60));
            for (int i = 0; i < activeAssignments.size(); i++) {
                RoleAssignment ra = activeAssignments.get(i);
                System.out.printf("%d. %s [%s]%n", i + 1, ra.role().name(), ra.assignmentType());
            }
            System.out.println("-".repeat(60));

            System.out.print("Select assignment number to revoke: ");
            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input");
                return;
            }

            if (choice < 1 || choice > activeAssignments.size()) {
                System.out.println("Invalid selection");
                return;
            }

            RoleAssignment toRevoke = activeAssignments.get(choice - 1);
            try {
                if (toRevoke instanceof PermanentAssignment) {
                    ((PermanentAssignment) toRevoke).revoke();
                    system.getAuditLog().log("Revoke role", system.getCurrentUser(), username, "revoke-role revoked by: " + username);
                    System.out.println("Assignment revoked");
                } else {
                    system.getAssignmentManager().remove(toRevoke);
                    System.out.println("Temporary assignment removed");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }));

        pars.registerCommand("assignment-list", "List all assignments", ((scanner, args, system) -> {
            List<RoleAssignment> assignments = system.getAssignmentManager().findAll();
            if (assignments.isEmpty()) {
                System.out.println("No assignments found");
                return;
            }

            System.out.println("-".repeat(100));
            System.out.printf("%-36s | %-15s | %-15s | %-8s | %-10s | %-20s%n",
                    "ID", "Username", "Role", "Type", "Status", "Assigned At");
            System.out.println("-".repeat(100));
            for (RoleAssignment ra : assignments) {
                System.out.printf("%-36s | %-15s | %-15s | %-8s | %-10s | %-20s%n",
                        ra.assignmentId(),
                        ra.user().username(),
                        ra.role().name(),
                        ra.assignmentType(),
                        ra.isActive() ? "ACTIVE" : "INACTIVE",
                        ra.metadata().assignedAt());
            }
            System.out.println("-".repeat(100) + "\n");
        }));

        pars.registerCommand("assignment-list-user", "List assignments for specific user", ((scanner, args, system) -> {
            System.out.print("Input username: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (!userOpt.isPresent()) {
                System.out.println("User not found: " + username);
                return;
            }
            User user = userOpt.get();

            List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
            if (assignments.isEmpty()) {
                System.out.println("No assignments for user: " + username);
                return;
            }

            System.out.println("\nAssignments for " + username + ":");
            System.out.println("-".repeat(80));
            for (RoleAssignment ra : assignments) {
                System.out.println("  Role: " + ra.role().name());
                System.out.println("  Type: " + ra.assignmentType());
                System.out.println("  Status: " + (ra.isActive() ? "ACTIVE" : "INACTIVE"));
                System.out.println("  Assigned by: " + ra.metadata().assignedBy());
                System.out.println("  Assigned at: " + ra.metadata().assignedAt());
                System.out.println("  Reason: " + ra.metadata().reason());
                if (ra instanceof TemporaryAssignment) {
                    System.out.println("  Expires at: " + ((TemporaryAssignment) ra).expiresAt);
                }
                System.out.println("-".repeat(40));
            }
        }));

        pars.registerCommand("assignment-list-role", "List users with specific role", ((scanner, args, system) -> {
            System.out.print("Input role name: ");
            String roleName = scanner.nextLine().trim();

            Optional<Role> roleOpt = system.getRoleManager().findByName(roleName);
            if (!roleOpt.isPresent()) {
                System.out.println("Role not found: " + roleName);
                return;
            }
            Role role = roleOpt.get();

            List<RoleAssignment> assignments = system.getAssignmentManager().findByRole(role);
            if (assignments.isEmpty()) {
                System.out.println("No users with role: " + roleName);
                return;
            }

            System.out.println("\nUsers with role '" + roleName + "':");
            System.out.println("-".repeat(50));
            for (RoleAssignment ra : assignments) {
                System.out.println("  " + ra.user().username() + " [" + ra.assignmentType() + "] " +
                        (ra.isActive() ? "(ACTIVE)" : "(INACTIVE)"));
            }
            System.out.println("Total: " + assignments.size());
        }));

        pars.registerCommand("assignment-active", "Show active assignments", ((scanner, args, system) -> {
            List<RoleAssignment> active = system.getAssignmentManager().getActiveAssignments();
            if (active.isEmpty()) {
                System.out.println("No active assignments");
                return;
            }

            System.out.println("\nActive assignments:");
            System.out.println("-".repeat(80));
            System.out.printf("%-15s | %-15s | %-8s%n", "Username", "Role", "Type");
            System.out.println("-".repeat(80));
            for (RoleAssignment ra : active) {
                System.out.printf("%-15s | %-15s | %-8s%n",
                        ra.user().username(),
                        ra.role().name(),
                        ra.assignmentType());
            }
            System.out.println("-".repeat(80));
            System.out.println("Total: " + active.size() + "\n");
        }));

        pars.registerCommand("assignment-expired", "Show expired assignments", ((scanner, args, system) -> {
            List<RoleAssignment> expired = system.getAssignmentManager().getExpiredAssignments();
            if (expired.isEmpty()) {
                System.out.println("No expired assignments");
                return;
            }

            System.out.println("\nExpired assignments:");
            System.out.println("-".repeat(80));
            System.out.printf("%-15s | %-15s | %-8s%n", "Username", "Role", "Type");
            System.out.println("-".repeat(80));
            for (RoleAssignment ra : expired) {
                System.out.printf("%-15s | %-15s | %-8s%n",
                        ra.user().username(),
                        ra.role().name(),
                        ra.assignmentType());
            }
            System.out.println("-".repeat(80));
            System.out.println("Total: " + expired.size() + "\n");
        }));

        pars.registerCommand("assignment-extend", "Extend temporary assignment", ((scanner, args, system) -> {
            System.out.print("Find by (1) Assignment ID or (2) Username + Role: ");
            String method = scanner.nextLine().trim();

            String assignmentId = null;
            RoleAssignment toExtend = null;

            if (method.equals("1")) {
                System.out.print("Input Assignment ID: ");
                assignmentId = scanner.nextLine().trim();
                Optional<RoleAssignment> raOpt = system.getAssignmentManager().findById(assignmentId);
                if (!raOpt.isPresent()) {
                    System.out.println("Assignment not found");
                    return;
                }
                toExtend = raOpt.get();
            } else {
                System.out.print("Input username: ");
                String username = scanner.nextLine().trim();
                System.out.print("Input role name: ");
                String roleName = scanner.nextLine().trim();

                Optional<User> userOpt = system.getUserManager().findByUsername(username);
                Optional<Role> roleOpt = system.getRoleManager().findByName(roleName);
                if (!userOpt.isPresent() || !roleOpt.isPresent()) {
                    System.out.println("User or role not found");
                    return;
                }

                List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(userOpt.get());
                for (RoleAssignment ra : assignments) {
                    if (ra.role().equals(roleOpt.get()) && ra instanceof TemporaryAssignment) {
                        toExtend = ra;
                        break;
                    }
                }
            }

            if (toExtend == null || !(toExtend instanceof TemporaryAssignment)) {
                System.out.println("No temporary assignment found");
                return;
            }

            System.out.print("New expiration date (yyyy-MM-ddTHH:mm:ss): ");
            String newExpiration = scanner.nextLine().trim();

            try {
                system.getAssignmentManager().extendTemporaryAssignment(toExtend.assignmentId(), newExpiration);
                System.out.println("Assignment extended successfully");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }));

        pars.registerCommand("assignment-search", "Search assignments by filters", ((scanner, args, system) -> {
            System.out.println("\nВыберите фильтр:");
            System.out.println("1 - По пользователю");
            System.out.println("2 - По роли");
            System.out.println("3 - По типу (PERMANENT/TEMPORARY)");
            System.out.println("4 - По статусу (ACTIVE/INACTIVE)");
            System.out.println("5 - Назначенные после даты");
            System.out.println("6 - Истекающие до даты");
            System.out.println("0 - Отмена");
            System.out.print("Ваш выбор: ");

            String choice = scanner.nextLine().trim();
            AssignmentFilter filter = null;

            switch (choice) {
                case "1":
                    System.out.print("Введите username: ");
                    String username = scanner.nextLine().trim();
                    filter = AssignmentFilters.byUsername(username);
                    break;
                case "2":
                    System.out.print("Введите имя роли: ");
                    String roleName = scanner.nextLine().trim();
                    filter = AssignmentFilters.byRoleName(roleName);
                    break;
                case "3":
                    System.out.print("Введите тип (PERMANENT/TEMPORARY): ");
                    String type = scanner.nextLine().trim().toUpperCase();
                    filter = AssignmentFilters.byType(type);
                    break;
                case "4":
                    System.out.print("Введите статус (ACTIVE/INACTIVE): ");
                    String status = scanner.nextLine().trim().toUpperCase();
                    if (status.equals("ACTIVE")) {
                        filter = AssignmentFilters.activeOnly();
                    } else {
                        filter = AssignmentFilters.inactiveOnly();
                    }
                    break;
                case "5":
                    System.out.print("Введите дату (yyyy-MM-ddTHH:mm:ss): ");
                    String date = scanner.nextLine().trim();
                    filter = AssignmentFilters.assignedAfter(date);
                    break;
                case "6":
                    System.out.print("Введите дату (yyyy-MM-ddTHH:mm:ss): ");
                    String expDate = scanner.nextLine().trim();
                    filter = AssignmentFilters.expiringBefore(expDate);
                    break;
                case "0":
                    System.out.println("Поиск отменен");
                    return;
                default:
                    System.out.println("Неверный выбор");
                    return;
            }

            List<RoleAssignment> results = system.getAssignmentManager().findByFilter(filter);
            if (results.isEmpty()) {
                System.out.println("No assignments found");
                return;
            }

            System.out.println("\n" + "-".repeat(100));
            System.out.printf("%-36s | %-15s | %-15s | %-8s | %-10s%n",
                    "ID", "Username", "Role", "Type", "Status");
            System.out.println("-".repeat(100));
            for (RoleAssignment ra : results) {
                System.out.printf("%-36s | %-15s | %-15s | %-8s | %-10s%n",
                        ra.assignmentId(),
                        ra.user().username(),
                        ra.role().name(),
                        ra.assignmentType(),
                        ra.isActive() ? "ACTIVE" : "INACTIVE");
            }
            System.out.println("-".repeat(100));
            System.out.println("Found: " + results.size() + "\n");
        }));


        pars.registerCommand("permissions-user", "All permissions of a user", ((scanner, args, system) -> {
            System.out.print("Input username: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (!userOpt.isPresent()) {
                System.out.println("User not found: " + username);
                return;
            }
            User user = userOpt.get();

            Set<Permission> permissions = system.getAssignmentManager().getUserPermissions(user);
            if (permissions.isEmpty()) {
                System.out.println("User '" + username + "' has no permissions");
                return;
            }

            Map<String, List<Permission>> groupedByResource = new HashMap<>();
            for (Permission p : permissions) {
                groupedByResource.computeIfAbsent(p.resource(), k -> new ArrayList<>()).add(p);
            }

            System.out.println("\nPermissions for user: " + username);
            System.out.println("=".repeat(60));
            for (Map.Entry<String, List<Permission>> entry : groupedByResource.entrySet()) {
                System.out.println("\nResource: " + entry.getKey());
                System.out.println("-".repeat(40));
                for (Permission p : entry.getValue()) {
                    System.out.println("  " + p.name() + " - " + p.description());
                }
            }
            System.out.println("\n" + "=".repeat(60));
            System.out.println("Total permissions: " + permissions.size() + "\n");
        }));

        pars.registerCommand("permissions-check", "Check if user has specific permission", ((scanner, args, system) -> {
            System.out.print("Input username: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (!userOpt.isPresent()) {
                System.out.println("User not found: " + username);
                return;
            }
            User user = userOpt.get();

            System.out.print("Permission name: ");
            String permName = scanner.nextLine().trim();
            System.out.print("Resource: ");
            String resource = scanner.nextLine().trim();

            boolean hasPermission = system.getAssignmentManager().userHasPermission(user, permName, resource);

            if (hasPermission) {
                System.out.println("\nUser '" + username + "' HAS permission: " + permName + " on " + resource);

                List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
                for (RoleAssignment ra : assignments) {
                    if (ra.isActive() && ra.role().hasPermission(permName, resource)) {
                        System.out.println("  Granted via role: " + ra.role().name());
                    }
                }
            } else {
                System.out.println("\nUser '" + username + "' DOES NOT have permission: " + permName + " on " + resource);
            }
        }));

        pars.registerCommand("help", "Show help", ((scanner, args, system) -> {
            pars.printHelp();
        }));

        pars.registerCommand("stats", "System statistics", ((scanner, args, system) -> {
            UserManager um = system.getUserManager();
            RoleManager rm = system.getRoleManager();
            AssignmentManager am = system.getAssignmentManager();

            int userCount = um.count();
            int roleCount = rm.count();
            int totalAssignments = am.count();
            int activeAssignments = am.getActiveAssignments().size();
            int expiredAssignments = am.getExpiredAssignments().size();

            double avgRolesPerUser = 0;
            if (userCount > 0) {
                int totalRoleAssignments = 0;
                for (User u : um.findAll()) {
                    totalRoleAssignments += am.findByUser(u).size();
                }
                avgRolesPerUser = (double) totalRoleAssignments / userCount;
            }

            Map<String, Integer> rolePopularity = new HashMap<>();
            for (RoleAssignment ra : am.findAll()) {
                String roleName = ra.role().name();
                rolePopularity.put(roleName, rolePopularity.getOrDefault(roleName, 0) + 1);
            }

            List<Map.Entry<String, Integer>> sortedRoles = new ArrayList<>(rolePopularity.entrySet());
            sortedRoles.sort((a, b) -> b.getValue().compareTo(a.getValue()));

            System.out.println("\n" + "=".repeat(50));
            System.out.println("           SYSTEM STATISTICS");
            System.out.println("=".repeat(50));
            System.out.println("USERS:");
            System.out.println("  Total users: " + userCount);
            System.out.println("  Avg roles per user: " + String.format("%.2f", avgRolesPerUser));
            System.out.println();
            System.out.println("ROLES:");
            System.out.println("  Total roles: " + roleCount);
            System.out.println();
            System.out.println("ASSIGNMENTS:");
            System.out.println("  Total assignments: " + totalAssignments);
            System.out.println("  Active assignments: " + activeAssignments);
            System.out.println("  Expired assignments: " + expiredAssignments);
            System.out.println();
            System.out.println("TOP 3 POPULAR ROLES:");
            for (int i = 0; i < Math.min(3, sortedRoles.size()); i++) {
                Map.Entry<String, Integer> entry = sortedRoles.get(i);
                System.out.println("  " + (i + 1) + ". " + entry.getKey() + " - " + entry.getValue() + " assignments");
            }
            if (sortedRoles.isEmpty()) {
                System.out.println("  No assignments yet");
            }
            System.out.println("=".repeat(50) + "\n");
        }));

        pars.registerCommand("clear", "Clear screen", ((scanner, args, system) -> {
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
            System.out.println("=== RBAC System Console ===");
            System.out.println("Type 'help' for available commands\n");
        }));

        pars.registerCommand("exit", "Exit program", ((scanner, args, system) -> {
            System.out.print("Are you sure you want to exit? (y/n): ");
            String confirm = scanner.nextLine().trim();
            if (confirm.equalsIgnoreCase("y")) {
                System.out.println("Goodbye!");
                System.exit(0);
            } else {
                System.out.println("Exit cancelled");
            }
        }));

        pars.registerCommand("audit-log", "Show log", ((scanner, args, system) -> {
            system.getAuditLog().printLog();
        }));

        // ==================== REPORT COMMANDS ====================

        pars.registerCommand("report-users", "Generate user report", ((scanner, args, system) -> {
            String report = ReportGenerator.generateUserReport(
                    system.getUserManager(),
                    system.getAssignmentManager()
            );
            ReportGenerator.exportToFile(report, "user_report.txt");
            System.out.println(report);
        }));

        pars.registerCommand("report-roles", "Generate role report", ((scanner, args, system) -> {
            String report = ReportGenerator.generateRoleReport(
                    system.getRoleManager(),
                    system.getAssignmentManager()
            );
            ReportGenerator.exportToFile(report, "role_report.txt");
            System.out.println(report);
        }));

        pars.registerCommand("report-matrix", "Generate permission matrix", ((scanner, args, system) -> {
            String report = ReportGenerator.generatePermissionMatrix(
                    system.getUserManager(),
                    system.getAssignmentManager()
            );
            ReportGenerator.exportToFile(report, "permission_matrix.txt");
            System.out.println(report);
        }));

    }
}


