import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AssignmentManagerTest {

    private AssignmentManager assignmentManager;
    private UserManager userManager;
    private RoleManager roleManager;
    private User testUser, testUser2;
    private Role testRole;
    private Permission testPermission;
    private AssignmentMetadata metadata;
    private PermanentAssignment permanentAssignment;
    private TemporaryAssignment temporaryAssignment;

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
        roleManager = new RoleManager();
        assignmentManager = new AssignmentManager(userManager, roleManager);

        testUser = User.create("testuser", "Test User", "test@mail.com");
        testUser2 = User.create("UserTest2", "user2", "user2@mail.com");
        userManager.add(testUser);
        userManager.add(testUser2);

        testPermission = new Permission("READ", "users", "read blablabla");
        Set<Permission> perms = new HashSet<>();
        perms.add(testPermission);

        testRole = new Role("TEST_ROLE", "Test Role", perms);
        roleManager.add(testRole);

        metadata = AssignmentMetadata.now("admin", "Test assignment");

        permanentAssignment = new PermanentAssignment(testUser, testRole, metadata);
        String expiresAt = LocalDateTime.now().plusDays(30)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        temporaryAssignment = new TemporaryAssignment(testUser2, testRole, metadata, expiresAt, false);
    }

    @Test
    void testFindByUser() {
        assignmentManager.add(permanentAssignment);

        List<RoleAssignment> result = assignmentManager.findByUser(testUser);

        assertEquals(1, result.size());
    }

    @Test
    void testFindByRole() {
        assignmentManager.add(permanentAssignment);

        List<RoleAssignment> result = assignmentManager.findByRole(testRole);

        assertEquals(1, result.size());
    }

    @Test
    void testFindByFilter() {
        assignmentManager.add(permanentAssignment);

        AssignmentFilter filter = a -> a.assignmentType().equals("PERMANENT");
        List<RoleAssignment> result = assignmentManager.findByFilter(filter);

        assertEquals(1, result.size());
    }

    @Test
    void testFindAllWithFilterAndSorter() {
        assignmentManager.add(permanentAssignment);
        assignmentManager.add(temporaryAssignment);

        AssignmentFilter allFilter = a -> true;
        Comparator<RoleAssignment> byType = (a1, a2) ->
                a1.assignmentType().compareTo(a2.assignmentType());

        List<RoleAssignment> result = assignmentManager.findAll(allFilter, byType);

        assertEquals(2, result.size());
    }

    @Test
    void testGetActiveAssignments() {
        assignmentManager.add(permanentAssignment);

        List<RoleAssignment> result = assignmentManager.getActiveAssignments();

        assertEquals(1, result.size());
    }

    @Test
    void testGetExpiredAssignments() {
        assignmentManager.add(permanentAssignment);

        List<RoleAssignment> result = assignmentManager.getExpiredAssignments();

        assertEquals(0, result.size());
    }

    @Test
    void testUserHasRole() {
        assignmentManager.add(permanentAssignment);

        boolean result = assignmentManager.userHasRole(testUser, testRole);

        assertTrue(result);
    }

    @Test
    void testUserHasPermission() {
        assignmentManager.add(permanentAssignment);

        boolean result = assignmentManager.userHasPermission(testUser, "READ", "users");

        assertTrue(result);
    }

    @Test
    void testGetUserPermissions() {
        assignmentManager.add(permanentAssignment);

        Set<Permission> result = assignmentManager.getUserPermissions(testUser);

        assertEquals(1, result.size());
    }

    @Test
    void testRevokeAssignment() {
        assignmentManager.add(permanentAssignment);

        assignmentManager.revokeAssignment(permanentAssignment.assignmentId());

        assertFalse(permanentAssignment.isActive());
    }

    @Test
    void testExtendTemporaryAssignment() {
        assignmentManager.add(temporaryAssignment);
        String newDate = LocalDateTime.now().plusDays(60)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        assignmentManager.extendTemporaryAssignment(temporaryAssignment.assignmentId(), newDate);

        assertTrue(true);
    }

    @Test
    void testAdd() {
        assignmentManager.add(permanentAssignment);

        Optional<RoleAssignment> found = assignmentManager.findById(permanentAssignment.assignmentId());
        assertTrue(found.isPresent());
    }

    @Test
    void testRemove() {
        assignmentManager.add(permanentAssignment);

        boolean removed = assignmentManager.remove(permanentAssignment);

        assertTrue(removed);
        assertEquals(0, assignmentManager.count());
    }

    @Test
    void testFindById() {
        assignmentManager.add(permanentAssignment);

        Optional<RoleAssignment> result = assignmentManager.findById(permanentAssignment.assignmentId());

        assertTrue(result.isPresent());
    }

    @Test
    void testFindAll() {
        assignmentManager.add(permanentAssignment);
        assignmentManager.add(temporaryAssignment);

        List<RoleAssignment> result = assignmentManager.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void testCount() {
        assignmentManager.add(permanentAssignment);

        assertEquals(1, assignmentManager.count());
    }

    @Test
    void testClear() {
        assignmentManager.add(permanentAssignment);
        assignmentManager.add(temporaryAssignment);

        assignmentManager.clear();

        assertEquals(0, assignmentManager.count());
    }

    @Test
    void testEquals() {
        AssignmentManager manager1 = new AssignmentManager(userManager, roleManager);
        AssignmentManager manager2 = new AssignmentManager(userManager, roleManager);

        manager1.add(permanentAssignment);
        manager2.add(permanentAssignment);

        assertTrue(manager1.equals(manager2));
    }

    @Test
    void testHashCode() {
        AssignmentManager manager1 = new AssignmentManager(userManager, roleManager);
        AssignmentManager manager2 = new AssignmentManager(userManager, roleManager);

        manager1.add(permanentAssignment);
        manager2.add(permanentAssignment);

        assertEquals(manager1.hashCode(), manager2.hashCode());
    }
}