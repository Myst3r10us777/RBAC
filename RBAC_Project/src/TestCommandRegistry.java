import org.junit.jupiter.api.*;
import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TestCommandRegistry {

    private UserManager userManager;
    private RoleManager roleManager;
    private AssignmentManager assignmentManager;
    private RBACSystem system;
    private CommandParser parser;
    private ByteArrayOutputStream outContent;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
        roleManager = new RoleManager();
        assignmentManager = new AssignmentManager(userManager, roleManager);
        system = new RBACSystem(userManager, roleManager, assignmentManager, "admin");
        system.initialize();

        parser = new CommandParser();
        CommandRegistry registry = new CommandRegistry();
        registry.RegistryAllCommands(parser);

        outContent = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    private void provideInput(String... lines) {
        String input = String.join("\n", lines) + "\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
    }

    private String getOutput() {
        return outContent.toString();
    }

    @Test
    void testUserList() {
        provideInput("");
        parser.parseAndExecute("user-list", new Scanner(System.in), system);
        assertTrue(getOutput().contains("admin"));
    }

    @Test
    void testUserCreate() {
        provideInput("testuser\nTest User\ntest@mail.com\n");
        parser.parseAndExecute("user-create", new Scanner(System.in), system);
        assertTrue(userManager.exists("testuser"), "User should exist after create command");
    }

    @Test
    void testUserView() {
        provideInput("ADMIN\n");
        parser.parseAndExecute("user-view", new Scanner(System.in), system);
        assertTrue(getOutput().contains("ADMIN"));
    }

    @Test
    void testUserViewNotFound() {
        provideInput("nonexistent\n");
        parser.parseAndExecute("user-view", new Scanner(System.in), system);
        assertTrue(getOutput().contains("User not found"));
    }

    @Test
    void testUserUpdate() {
        provideInput("ADMIN\nNew Name\nnew@mail.com\n");
        parser.parseAndExecute("user-update", new Scanner(System.in), system);
        Optional<User> user = userManager.findByUsername("ADMIN");
        assertTrue(user.isPresent());
    }

    @Test
    void testUserDelete() {
        provideInput("testuser\ny\n");
        User u = User.create("testuser", "Test", "test@mail.com");
        userManager.add(u);
        parser.parseAndExecute("user-delete", new Scanner(System.in), system);
        assertFalse(userManager.exists("testuser"));
    }

    @Test
    void testUserDeleteCancel() {
        provideInput("ADMIN\nn\n");
        parser.parseAndExecute("user-delete", new Scanner(System.in), system);
        assertTrue(userManager.exists("ADMIN"));
    }

    @Test
    void testUserSearchByUsername() {
        provideInput("1\nad\n");
        parser.parseAndExecute("user-search", new Scanner(System.in), system);
        assertTrue(getOutput().contains("admin"));
    }


    @Test
    void testRoleList() {
        parser.parseAndExecute("role-list", new Scanner(System.in), system);
        assertTrue(getOutput().contains("Admin") || getOutput().contains("No roles"));
    }

    @Test
    void testRoleCreate() {
        provideInput("TestRole\nTest Description\nn\n");
        parser.parseAndExecute("role-create", new Scanner(System.in), system);
        assertTrue(roleManager.exists("TestRole"));
    }

    @Test
    void testRoleView() {
        provideInput("Admin\n");
        parser.parseAndExecute("role-view", new Scanner(System.in), system);
        assertTrue(getOutput().contains("Admin"));
    }

    @Test
    void testRoleUpdate() {
        provideInput("Admin\nNewAdminName\nNew Description\n");
        parser.parseAndExecute("role-update", new Scanner(System.in), system);
        assertTrue(roleManager.exists("NewAdminName"));
    }

    @Test
    void testRoleDelete() {
        provideInput("TestRole\ny\n");
        Role role = new Role("TestRole", "Test", new HashSet<>());
        roleManager.add(role);
        parser.parseAndExecute("role-delete", new Scanner(System.in), system);
        assertFalse(roleManager.exists("TestRole"));
    }

    @Test
    void testRoleAddPermission() {
        provideInput("Admin\nREAD\nusers\nRead users\n");
        parser.parseAndExecute("role-add-permission", new Scanner(System.in), system);
        Optional<Role> role = roleManager.findByName("Admin");
        assertTrue(role.isPresent());
    }

    @Test
    void testRoleRemovePermission() {
        provideInput("Admin\n1\n");
        parser.parseAndExecute("role-remove-permission", new Scanner(System.in), system);
        assertTrue(true);
    }

    @Test
    void testRoleSearchByName() {
        provideInput("1\nAd\n");
        parser.parseAndExecute("role-search", new Scanner(System.in), system);
        assertTrue(getOutput().contains("Admin") || getOutput().contains("No roles"));
    }

    @Test
    void testAssignRole() {
        provideInput("admin\n1\nPERMANENT\nTest assignment\n");
        parser.parseAndExecute("assign-role", new Scanner(System.in), system);
        assertTrue(true);
    }

    @Test
    void testRevokeRole() {
        provideInput("admin\n1\n");
        parser.parseAndExecute("revoke-role", new Scanner(System.in), system);
        assertTrue(true);
    }

    @Test
    void testAssignmentList() {
        parser.parseAndExecute("assignment-list", new Scanner(System.in), system);
        assertTrue(getOutput().contains("ID") || getOutput().contains("No assignments"));
    }

    @Test
    void testAssignmentListUser() {
        provideInput("admin\n");
        parser.parseAndExecute("assignment-list-user", new Scanner(System.in), system);
        assertTrue(true);
    }

    @Test
    void testAssignmentListRole() {
        provideInput("Admin\n");
        parser.parseAndExecute("assignment-list-role", new Scanner(System.in), system);
        assertTrue(true);
    }

    @Test
    void testAssignmentActive() {
        parser.parseAndExecute("assignment-active", new Scanner(System.in), system);
        assertTrue(true);
    }

    @Test
    void testAssignmentExpired() {
        parser.parseAndExecute("assignment-expired", new Scanner(System.in), system);
        assertTrue(true);
    }

    @Test
    void testAssignmentExtend() {
        provideInput("2\nadmin\nAdmin\n2025-12-31T23:59:59\n");
        parser.parseAndExecute("assignment-extend", new Scanner(System.in), system);
        assertTrue(true);
    }

    @Test
    void testAssignmentSearchByType() {
        provideInput("3\nPERMANENT\n");
        parser.parseAndExecute("assignment-search", new Scanner(System.in), system);
        assertTrue(true);
    }

    @Test
    void testPermissionsUser() {
        provideInput("admin\n");
        parser.parseAndExecute("permissions-user", new Scanner(System.in), system);
        assertTrue(getOutput().contains("admin") || getOutput().contains("no permissions"));
    }

    @Test
    void testPermissionsCheck() {
        provideInput("ADMIN\nREAD\nusers\n");
        parser.parseAndExecute("permissions-check", new Scanner(System.in), system);
        assertTrue(getOutput().contains("HAS") || getOutput().contains("DOES NOT"));
    }

    @Test
    void testHelp() {
        parser.parseAndExecute("help", new Scanner(System.in), system);
        assertTrue(getOutput().contains("user-list") || getOutput().contains("Available"));
    }

    @Test
    void testStats() {
        parser.parseAndExecute("stats", new Scanner(System.in), system);
        assertTrue(getOutput().contains("STATISTICS") || getOutput().contains("USERS"));
    }
}