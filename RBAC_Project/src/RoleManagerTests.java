import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class RoleManagerTests {

    private RoleManager roleManager;
    private Role adminRole;
    private Role userRole;
    private Permission readPerm;
    private Permission writePerm;

    @BeforeEach
    void setUp() {
        roleManager = new RoleManager();

        readPerm = new Permission("READ", "users", "Read blablabla");
        writePerm = new Permission("WRITE", "users", "Write blablabla");

        Set<Permission> adminPerms = new HashSet<>();
        adminPerms.add(readPerm);
        adminPerms.add(writePerm);

        Set<Permission> userPerms = new HashSet<>();
        userPerms.add(readPerm);

        adminRole = new Role("ADMIN", "Administrator", adminPerms);
        userRole = new Role("USER", "Regular User", userPerms);
    }

    @Test
    void testFindByName() {
        roleManager.add(adminRole);
        Optional<Role> result = roleManager.findByName("ADMIN");
        assertTrue(result.isPresent());
    }

    @Test
    void testFindByFilter() {
        roleManager.add(adminRole);
        roleManager.add(userRole);

        RoleFilter filter = role -> role.name().equals("ADMIN");
        List<Role> result = roleManager.findByFilter(filter);

        assertEquals(1, result.size());
    }

    @Test
    void testFindAllWithFilterAndSorter() {
        roleManager.add(userRole);
        roleManager.add(adminRole);

        RoleFilter allFilter = role -> true;
        Comparator<Role> byName = (r1, r2) -> r1.name().compareTo(r2.name());

        List<Role> result = roleManager.findAll(allFilter, byName);

        assertEquals(2, result.size());
    }

    @Test
    void testExists() {
        roleManager.add(adminRole);
        assertTrue(roleManager.exists("ADMIN"));
    }

    @Test
    void testAddPermissionToRole() {
        roleManager.add(adminRole);
        Permission newPerm = new Permission("DELETE", "users", "Delete blablabla");

        roleManager.addPermissionToRole("ADMIN", newPerm);

        Optional<Role> role = roleManager.findByName("ADMIN");
        assertTrue(role.isPresent());
    }

    @Test
    void testRemovePermissionFromRole() {
        roleManager.add(adminRole);

        roleManager.removePermissionFromRole("ADMIN", writePerm);

        Optional<Role> role = roleManager.findByName("ADMIN");
        assertTrue(role.isPresent());
    }

    @Test
    void testFindRolesWithPermission() {
        roleManager.add(adminRole);
        roleManager.add(userRole);

        List<Role> result = roleManager.findRolesWithPermission("READ", "users");

        assertEquals(2, result.size());
    }

    @Test
    void testAdd() {
        roleManager.add(adminRole);

        assertTrue(roleManager.exists("ADMIN"));
        assertEquals(1, roleManager.count());
    }

    @Test
    void testRemove() {
        roleManager.add(adminRole);

        boolean removed = roleManager.remove(adminRole);

        assertTrue(removed);
        assertFalse(roleManager.exists("ADMIN"));
    }

    @Test
    void testFindById() {
        roleManager.add(adminRole);

        Optional<Role> result = roleManager.findById(adminRole.id);

        assertTrue(result.isPresent());
    }

    @Test
    void testFindAll() {
        roleManager.add(adminRole);
        roleManager.add(userRole);

        List<Role> result = roleManager.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void testCount() {
        roleManager.add(adminRole);

        assertEquals(1, roleManager.count());
    }

    @Test
    void testClear() {
        roleManager.add(adminRole);
        roleManager.add(userRole);

        roleManager.clear();

        assertEquals(0, roleManager.count());
        assertFalse(roleManager.exists("ADMIN"));
    }

    @Test
    void testEquals() {
        RoleManager manager1 = new RoleManager();
        RoleManager manager2 = new RoleManager();

        manager1.add(adminRole);
        manager2.add(adminRole);

        assertTrue(manager1.equals(manager2));
    }

    @Test
    void testHashCode() {
        RoleManager manager1 = new RoleManager();
        RoleManager manager2 = new RoleManager();

        manager1.add(adminRole);
        manager2.add(adminRole);

        assertEquals(manager1.hashCode(), manager2.hashCode());
    }
}