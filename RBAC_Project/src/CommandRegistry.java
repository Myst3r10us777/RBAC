import java.util.*;

public class CommandRegistry {
    public void RegistryAllCommands(CommandParser pars){
        pars.registerCommand("user-list", "Output all users", ((scanner, args, system) -> {
            List<User> users = system.getUserManager().findAll();
            if (users.isEmpty()) {
                System.out.println("No users found");
                return;
            }

            String[] headers = {"№", "Username", "Full name", "Email"};
            List<String[]> rows = new ArrayList<>();

            for (int i = 0; i < users.size(); i++) {
                User u = users.get(i);
                rows.add(new String[]{
                        String.valueOf(i + 1),
                        u.username(),
                        u.fullName() != null ? u.fullName() : "-",
                        u.email()
                });
            }

            System.out.println(FormatUtils.formatTable(headers, rows));
        }));

        pars.registerCommand("user-create", "Create new user", ((scanner, args, system) -> {
            String username = ConsoleUtils.promptString(scanner, "Input username: ", true);
            String fullName = ConsoleUtils.promptString(scanner, "Input FullName: ", true);
            String email = ConsoleUtils.promptString(scanner, "Input email: ", true);

            try{
                User user = User.create(username, fullName, email);
                system.getUserManager().add(user);
                system.getAuditLog().log("Created user", system.getCurrentUser(), username, "user-create created: " + username);
            } catch (Exception e){
                System.out.println("Error: " + e.getMessage());
            }
        }));

        pars.registerCommand("user-view", "User information", ((scanner, args, system) -> {
            String username = ConsoleUtils.promptString(scanner, "Input username: ", true);

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
                    System.out.println(FormatUtils.formatHeader(ra.role().format()));
                }
            } catch (Exception e){
                System.out.println("Error: " + e.getMessage());
            }
        }));

        pars.registerCommand("user-update", "Update user information", ((scanner, args, system) -> {
            String username = ConsoleUtils.promptString(scanner, "Input username: ", true);
            String newfullName = ConsoleUtils.promptString(scanner, "Input FullName: ", true);
            String newEmail = ConsoleUtils.promptString(scanner, "Input email: ", true);

            try{
                system.getUserManager().update(username, newfullName, newEmail);
            } catch (Exception e){
                System.out.println("Error: " + e.getMessage());
            }
        }));

        pars.registerCommand("user-delete", "Delete user", ((scanner, args, system) -> {
            String username = ConsoleUtils.promptString(scanner, "Input username: ", true);

            if (!ConsoleUtils.promptYesNo(scanner, "Are you sure?")){
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
            int choice = ConsoleUtils.promptInt(scanner, "\nВыберите фильтр:\n" +
                    "1 - По username (содержит)\n" +
                    "2 - По email (содержит)\n" +
                    "3 - По домену email\n" +
                    "4 - По полному имени (содержит)\n" +
                    "0 - Отмена\n" +
                    "Ваш выбор: ", 0, 4);

            UserFilter filter = null;
            String searchValue = null;

            switch (choice) {
                case 1:
                    searchValue = ConsoleUtils.promptString(scanner, "Input part of username: ", true);
                    filter = UserFilters.byUsernameContains(searchValue);
                    break;
                case 2:
                    searchValue = ConsoleUtils.promptString(scanner, "Input email: ", true);
                    filter = UserFilters.byEmail(searchValue);
                    break;
                case 3:
                    searchValue = ConsoleUtils.promptString(scanner, "Input domen: ", true);
                    filter = UserFilters.byEmailDomain(searchValue);
                    break;
                case 4:
                    searchValue = ConsoleUtils.promptString(scanner, "Input part of full name: ", true);
                    filter = UserFilters.byFullNameContains(searchValue);
                    break;
                case 0:
                    System.out.println("Поиск отменен");
                    return;
                default:
                    System.out.println("Неверный выбор");
                    return;
            }

            List<User> users = system.getUserManager().findByFilter(filter);
            String[] headers = {"№", "Username", "Full name", "Email"};
            List<String[]> rows = new ArrayList<>();
            for (int i = 0; i < users.size(); i++) {
                User u = users.get(i);
                rows.add(new String[]{
                        String.valueOf(i + 1),
                        u.username(),
                        u.fullName() != null ? u.fullName() : "-",
                        u.email()
                });
            }
            System.out.println(FormatUtils.formatTable(headers, rows));
        }));


        pars.registerCommand("role-list", "Output all roles", ((scanner, args, system) -> {
            List<Role> roles = system.getRoleManager().findAll();
            if (roles.isEmpty()) {
                System.out.println("No roles found");
                return;
            }

            String[] headers = {"№", "Name", "Description", "Permissions"};
            List<String[]> rows = new ArrayList<>();
            for (int i = 0; i < roles.size(); i++) {
                Role r = roles.get(i);
                rows.add(new String[]{
                        String.valueOf(i + 1),
                        r.name(),
                        r.description != null ? r.description : "-",
                        String.valueOf(r.getPermissions().size())
                });
            }
            System.out.println(FormatUtils.formatTable(headers, rows));
        }));

        pars.registerCommand("role-create", "Create new role", ((scanner, args, system) -> {
            String roleName = ConsoleUtils.promptString(scanner, "Input roleName: ", true);
            String description = ConsoleUtils.promptString(scanner, "Input description: ", true);

            try {
                Role role = new Role(roleName, description, new HashSet<>());
                system.getRoleManager().add(role);
                System.out.println("Role created successfully!");

                while (true) {
                    if (!ConsoleUtils.promptYesNo(scanner, "Add permission? ")) {
                        break;
                    }

                    String permName = ConsoleUtils.promptString(scanner, "Permission name: ", true);
                    String resource = ConsoleUtils.promptString(scanner, "Resource: ", true);
                    String permDesc = ConsoleUtils.promptString(scanner, "Description: ", true);

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
            String roleName = ConsoleUtils.promptString(scanner, "Input role name: ", true);

            Optional<Role> roleOpt = system.getRoleManager().findByName(roleName);
            if (!roleOpt.isPresent()) {
                System.out.println("Role not found: " + roleName);
                return;
            }
            Role role = roleOpt.get();
            System.out.println(FormatUtils.formatBox(role.format()));
        }));

        pars.registerCommand("role-update", "Update role information", ((scanner, args, system) -> {
            String roleName = ConsoleUtils.promptString(scanner, "Input role name: ", true);

            Optional<Role> roleOpt = system.getRoleManager().findByName(roleName);
            if (!roleOpt.isPresent()) {
                System.out.println("Role not found: " + roleName);
                return;
            }

            String newName = ConsoleUtils.promptString(scanner, "Input new name: ", true);
            String newDesc = ConsoleUtils.promptString(scanner, "Input new description: ", true);

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
            String roleName = ConsoleUtils.promptString(scanner, "Input role name: ", true);

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

            if (!ConsoleUtils.promptYesNo(scanner, "Are you sure to delete role? ")) {
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
            String roleName = ConsoleUtils.promptString(scanner, "Input role name: ", true);

            Optional<Role> roleOpt = system.getRoleManager().findByName(roleName);
            if (!roleOpt.isPresent()) {
                System.out.println("Role not found: " + roleName);
                return;
            }

            String permName = ConsoleUtils.promptString(scanner, "Permission name: ", true);
            String resource = ConsoleUtils.promptString(scanner, "Resource: ", true);
            String description = ConsoleUtils.promptString(scanner, "Description: ", true);

            try {
                Permission perm = new Permission(permName, resource, description);
                system.getRoleManager().addPermissionToRole(roleName, perm);
                System.out.println("Permission added to role '" + roleName + "'");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }));

        pars.registerCommand("role-remove-permission", "Remove permission from role", ((scanner, args, system) -> {
            String roleName = ConsoleUtils.promptString(scanner, "Input role name: ", true);

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

            List<Permission> permList = new ArrayList<>(permissions);
            String[] headers = {"№", "Name", "Resource", "Description"};
            List<String[]> rows = new ArrayList<>();
            for (int i = 0; i < permList.size(); i++) {
                Permission p = permList.get(i);
                rows.add(new String[]{
                        String.valueOf(i + 1),
                        p.name(),
                        p.resource(),
                        p.description() != null ? p.description() : "-"
                });
            }
            System.out.println(FormatUtils.formatTable(headers, rows));

            int choice = ConsoleUtils.promptInt(scanner, "Select permission number to remove: ", 1, permList.size());

            Permission toRemove = permList.get(choice - 1);
            try {
                system.getRoleManager().removePermissionFromRole(roleName, toRemove);
                System.out.println("Permission removed from role '" + roleName + "'");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }));

        pars.registerCommand("role-search", "Search roles", ((scanner, args, system) -> {
            int choice = ConsoleUtils.promptInt(scanner, "\nВыберите фильтр:\n" +
                    "1 - По имени (содержит)\n" +
                    "2 - По наличию конкретного права\n" +
                    "3 - По минимальному количеству прав\n" +
                    "0 - Отмена\n" +
                    "Ваш выбор: ", 0, 3);

            List<Role> results = new ArrayList<>();

            switch (choice) {
                case 1:
                    String nameSub = ConsoleUtils.promptString(scanner, "Введите часть имени роли: ", true);
                    results = system.getRoleManager().findByFilter(role ->
                            role.name().toLowerCase().contains(nameSub.toLowerCase()));
                    break;
                case 2:
                    String permName = ConsoleUtils.promptString(scanner, "Введите имя права: ", true);
                    String resource = ConsoleUtils.promptString(scanner, "Введите ресурс: ", true);
                    results = system.getRoleManager().findRolesWithPermission(permName, resource);
                    break;
                case 3:
                    int minPerms = ConsoleUtils.promptInt(scanner, "Минимальное количество прав: ", 0, Integer.MAX_VALUE);
                    results = system.getRoleManager().findByFilter(role ->
                            role.getPermissions().size() >= minPerms);
                    break;
                case 0:
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

            String[] headers = {"№", "Name", "Description", "Permissions"};
            List<String[]> rows = new ArrayList<>();
            for (int i = 0; i < results.size(); i++) {
                Role r = results.get(i);
                rows.add(new String[]{
                        String.valueOf(i + 1),
                        r.name(),
                        r.description != null ? r.description : "-",
                        String.valueOf(r.getPermissions().size())
                });
            }
            System.out.println(FormatUtils.formatTable(headers, rows));
        }));


        pars.registerCommand("assign-role", "Assign role to user", ((scanner, args, system) -> {
            String username = ConsoleUtils.promptString(scanner, "Input username: ", true);

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

            String[] headers = {"№", "Name", "Description"};
            List<String[]> rows = new ArrayList<>();
            for (int i = 0; i < roles.size(); i++) {
                Role r = roles.get(i);
                rows.add(new String[]{
                        String.valueOf(i + 1),
                        r.name(),
                        r.description != null ? r.description : "-"
                });
            }
            System.out.println(FormatUtils.formatTable(headers, rows));

            int roleChoice = ConsoleUtils.promptInt(scanner, "Select role number: ", 1, roles.size());
            Role role = roles.get(roleChoice - 1);

            String type = ConsoleUtils.promptString(scanner, "Assignment type (PERMANENT/TEMPORARY): ", true).toUpperCase();
            if (!type.equals("PERMANENT") && !type.equals("TEMPORARY")) {
                System.out.println("Invalid type. Using PERMANENT");
                type = "PERMANENT";
            }

            String expiresAt = null;
            if (type.equals("TEMPORARY")) {
                expiresAt = ConsoleUtils.promptString(scanner, "Expiration date (yyyy-MM-ddTHH:mm:ss): ", true);
            }

            String reason = ConsoleUtils.promptString(scanner, "Reason for assignment: ", false);

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
            String username = ConsoleUtils.promptString(scanner, "Input username: ", true);

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

            String[] headers = {"№", "Role", "Type"};
            List<String[]> rows = new ArrayList<>();
            for (int i = 0; i < activeAssignments.size(); i++) {
                RoleAssignment ra = activeAssignments.get(i);
                rows.add(new String[]{
                        String.valueOf(i + 1),
                        ra.role().name(),
                        ra.assignmentType()
                });
            }
            System.out.println(FormatUtils.formatTable(headers, rows));

            int choice = ConsoleUtils.promptInt(scanner, "Select assignment number to revoke: ", 1, activeAssignments.size());

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

            String[] headers = {"ID", "Username", "Role", "Type", "Status", "Assigned At"};
            List<String[]> rows = new ArrayList<>();
            for (RoleAssignment ra : assignments) {
                rows.add(new String[]{
                        ra.assignmentId(),
                        ra.user().username(),
                        ra.role().name(),
                        ra.assignmentType(),
                        ra.isActive() ? "ACTIVE" : "INACTIVE",
                        ra.metadata().assignedAt()
                });
            }
            System.out.println(FormatUtils.formatTable(headers, rows));
        }));

        pars.registerCommand("assignment-list-user", "List assignments for specific user", ((scanner, args, system) -> {
            String username = ConsoleUtils.promptString(scanner, "Input username: ", true);

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

            System.out.println(FormatUtils.formatHeader("Assignments for " + username));

            String[] headers = {"Role", "Type", "Status", "Assigned By", "Assigned At", "Reason", "Expires At"};
            List<String[]> rows = new ArrayList<>();
            for (RoleAssignment ra : assignments) {
                rows.add(new String[]{
                        ra.role().name(),
                        ra.assignmentType(),
                        ra.isActive() ? "ACTIVE" : "INACTIVE",
                        ra.metadata().assignedBy(),
                        ra.metadata().assignedAt(),
                        ra.metadata().reason() != null ? ra.metadata().reason() : "-",
                        ra instanceof TemporaryAssignment ? ((TemporaryAssignment) ra).expiresAt : "-"
                });
            }
            System.out.println(FormatUtils.formatTable(headers, rows));
        }));

        pars.registerCommand("assignment-list-role", "List users with specific role", ((scanner, args, system) -> {
            String roleName = ConsoleUtils.promptString(scanner, "Input role name: ", true);

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

            System.out.println(FormatUtils.formatHeader("Users with role '" + roleName + "'"));

            String[] headers = {"Username", "Type", "Status"};
            List<String[]> rows = new ArrayList<>();
            for (RoleAssignment ra : assignments) {
                rows.add(new String[]{
                        ra.user().username(),
                        ra.assignmentType(),
                        ra.isActive() ? "ACTIVE" : "INACTIVE"
                });
            }
            System.out.println(FormatUtils.formatTable(headers, rows));
            System.out.println("Total: " + assignments.size());
        }));

        pars.registerCommand("assignment-active", "Show active assignments", ((scanner, args, system) -> {
            List<RoleAssignment> active = system.getAssignmentManager().getActiveAssignments();
            if (active.isEmpty()) {
                System.out.println("No active assignments");
                return;
            }

            System.out.println(FormatUtils.formatHeader("Active assignments"));

            String[] headers = {"Username", "Role", "Type"};
            List<String[]> rows = new ArrayList<>();
            for (RoleAssignment ra : active) {
                rows.add(new String[]{
                        ra.user().username(),
                        ra.role().name(),
                        ra.assignmentType()
                });
            }
            System.out.println(FormatUtils.formatTable(headers, rows));
            System.out.println("Total: " + active.size());
        }));

        pars.registerCommand("assignment-expired", "Show expired assignments", ((scanner, args, system) -> {
            List<RoleAssignment> expired = system.getAssignmentManager().getExpiredAssignments();
            if (expired.isEmpty()) {
                System.out.println("No expired assignments");
                return;
            }

            System.out.println(FormatUtils.formatHeader("Expired assignments"));

            String[] headers = {"Username", "Role", "Type"};
            List<String[]> rows = new ArrayList<>();
            for (RoleAssignment ra : expired) {
                rows.add(new String[]{
                        ra.user().username(),
                        ra.role().name(),
                        ra.assignmentType()
                });
            }
            System.out.println(FormatUtils.formatTable(headers, rows));
            System.out.println("Total: " + expired.size());
        }));

        pars.registerCommand("assignment-extend", "Extend temporary assignment", ((scanner, args, system) -> {
            List<String> options = Arrays.asList("Assignment ID", "Username + Role");
            String method = ConsoleUtils.promptChoice(scanner, "Find by:", options);

            RoleAssignment toExtend = null;

            if (method.equals("Assignment ID")) {
                String assignmentId = ConsoleUtils.promptString(scanner, "Input Assignment ID: ", true);
                Optional<RoleAssignment> raOpt = system.getAssignmentManager().findById(assignmentId);
                if (!raOpt.isPresent()) {
                    System.out.println("Assignment not found");
                    return;
                }
                toExtend = raOpt.get();
            } else {
                String username = ConsoleUtils.promptString(scanner, "Input username: ", true);
                String roleName = ConsoleUtils.promptString(scanner, "Input role name: ", true);

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

            String newExpiration = ConsoleUtils.promptString(scanner, "New expiration date (yyyy-MM-ddTHH:mm:ss): ", true);

            try {
                system.getAssignmentManager().extendTemporaryAssignment(toExtend.assignmentId(), newExpiration);
                System.out.println("Assignment extended successfully");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }));

        pars.registerCommand("assignment-search", "Search assignments by filters", ((scanner, args, system) -> {
            int choice = ConsoleUtils.promptInt(scanner, "\nВыберите фильтр:\n" +
                    "1 - По пользователю\n" +
                    "2 - По роли\n" +
                    "3 - По типу (PERMANENT/TEMPORARY)\n" +
                    "4 - По статусу (ACTIVE/INACTIVE)\n" +
                    "5 - Назначенные после даты\n" +
                    "6 - Истекающие до даты\n" +
                    "0 - Отмена\n" +
                    "Ваш выбор: ", 0, 6);

            AssignmentFilter filter = null;

            switch (choice) {
                case 1:
                    String username = ConsoleUtils.promptString(scanner, "Введите username: ", true);
                    filter = AssignmentFilters.byUsername(username);
                    break;
                case 2:
                    String roleName = ConsoleUtils.promptString(scanner, "Введите имя роли: ", true);
                    filter = AssignmentFilters.byRoleName(roleName);
                    break;
                case 3:
                    String type = ConsoleUtils.promptString(scanner, "Введите тип (PERMANENT/TEMPORARY): ", true).toUpperCase();
                    filter = AssignmentFilters.byType(type);
                    break;
                case 4:
                    String status = ConsoleUtils.promptString(scanner, "Введите статус (ACTIVE/INACTIVE): ", true).toUpperCase();
                    if (status.equals("ACTIVE")) {
                        filter = AssignmentFilters.activeOnly();
                    } else {
                        filter = AssignmentFilters.inactiveOnly();
                    }
                    break;
                case 5:
                    String date = ConsoleUtils.promptString(scanner, "Введите дату (yyyy-MM-ddTHH:mm:ss): ", true);
                    filter = AssignmentFilters.assignedAfter(date);
                    break;
                case 6:
                    String expDate = ConsoleUtils.promptString(scanner, "Введите дату (yyyy-MM-ddTHH:mm:ss): ", true);
                    filter = AssignmentFilters.expiringBefore(expDate);
                    break;
                case 0:
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

            String[] headers = {"ID", "Username", "Role", "Type", "Status"};
            List<String[]> rows = new ArrayList<>();
            for (RoleAssignment ra : results) {
                rows.add(new String[]{
                        ra.assignmentId(),
                        ra.user().username(),
                        ra.role().name(),
                        ra.assignmentType(),
                        ra.isActive() ? "ACTIVE" : "INACTIVE"
                });
            }
            System.out.println(FormatUtils.formatTable(headers, rows));
            System.out.println("Found: " + results.size() + "\n");
        }));


        pars.registerCommand("permissions-user", "All permissions of a user", ((scanner, args, system) -> {
            String username = ConsoleUtils.promptString(scanner, "Input username: ", true);

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

            System.out.println(FormatUtils.formatHeader("Permissions for user: " + username));

            String[] headers = {"Resource", "Permissions"};
            List<String[]> rows = new ArrayList<>();
            for (Map.Entry<String, List<Permission>> entry : groupedByResource.entrySet()) {
                StringBuilder perms = new StringBuilder();
                for (Permission p : entry.getValue()) {
                    if (perms.length() > 0) perms.append(", ");
                    perms.append(p.name()).append(" - ").append(p.description());
                }
                rows.add(new String[]{entry.getKey(), perms.toString()});
            }
            System.out.println(FormatUtils.formatTable(headers, rows));
            System.out.println("Total permissions: " + permissions.size() + "\n");
        }));

        pars.registerCommand("permissions-check", "Check if user has specific permission", ((scanner, args, system) -> {
            String username = ConsoleUtils.promptString(scanner, "Input username: ", true);

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (!userOpt.isPresent()) {
                System.out.println("User not found: " + username);
                return;
            }
            User user = userOpt.get();

            String permName = ConsoleUtils.promptString(scanner, "Permission name: ", true);
            String resource = ConsoleUtils.promptString(scanner, "Resource: ", true);

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
            if (ConsoleUtils.promptYesNo(scanner, "Are you sure? ")) {
                System.out.println("Goodbye!");
                System.exit(0);
            } else {
                System.out.println("Exit cancelled");
            }
        }));

        pars.registerCommand("audit-log", "Show log", ((scanner, args, system) -> {
            system.getAuditLog().printLog();
        }));


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


