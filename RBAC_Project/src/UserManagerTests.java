import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class UserManagerTests {

    private UserManager userManager;
    private User john;
    private User alice;

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
        john = User.create("john", "John Doe", "john@mail.com");
        alice = User.create("alice", "Alice Smith", "alice@gmail.com");
    }

    @AfterEach
    void tearDown() {
        userManager.clear();
    }

    @Test
    void testFindByUsername() {
        userManager.add(john);

        Optional<User> result = userManager.findByUsername("john");

        assertTrue(result.isPresent());
        assertEquals("john", result.get().username());
    }

    @Test
    void testFindByEmail() {
        userManager.add(john);
        Optional<User> result = userManager.findByEmail("john@mail.com");
        assertTrue(result.isPresent());
    }

    @Test
    void testFindByFilter() {
        userManager.add(john);
        userManager.add(alice);
        UserFilter filter = user -> user.email().endsWith("@gmail.com");
        List<User> result = userManager.findByFilter(filter);
        assertEquals(1, result.size());
    }

    @Test
    void testFindAllByFilterAndResorce() {
        userManager.add(alice);
        userManager.add(john);
        UserFilter allFilter = user -> true;
        Comparator<User> byName = (u1, u2) -> u1.username().compareTo(u2.username());
        List<User> result = userManager.findAll(allFilter, byName);
        assertEquals(2, result.size());
    }

    @Test
    void testExists() {
        userManager.add(john);
        assertTrue(userManager.exists("john"));
    }

    @Test
    void testUpdate() {
        userManager.add(john);
        userManager.update("john", "John Updated", "john.new@mail.com");
        Optional<User> updated = userManager.findByUsername("john");
        assertTrue(updated.isPresent());
    }

    @Test
    void testAdd() {
        userManager.add(john);
        assertTrue(userManager.exists("john"));
    }

    @Test
    void testRemove() {
        userManager.add(john);
        boolean removed = userManager.remove(john);
        assertTrue(removed);
    }

    @Test
    void testFindById() {
        userManager.add(john);
        Optional<User> result = userManager.findById("john");
        assertTrue(result.isPresent());
    }

    @Test
    void testFindAll() {
        userManager.add(john);
        userManager.add(alice);
        List<User> result = userManager.findAll();
        assertEquals(2, result.size());
    }

    @Test
    void testCount() {
        userManager.add(john);
        assertEquals(1, userManager.count());
    }

    @Test
    void testClear() {
        userManager.add(john);
        userManager.clear();
        assertEquals(0, userManager.count());
    }

    @Test
    void testEquals() {
        UserManager manager1 = new UserManager();
        UserManager manager2 = new UserManager();
        manager1.add(john);
        manager2.add(john);
        assertTrue(manager1.equals(manager2));
    }

    @Test
    void testHashCode() {
        UserManager manager1 = new UserManager();
        UserManager manager2 = new UserManager();
        manager1.add(john);
        manager2.add(john);
        assertEquals(manager1.hashCode(), manager2.hashCode());
    }

}