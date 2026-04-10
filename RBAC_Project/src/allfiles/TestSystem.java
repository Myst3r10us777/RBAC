package allfiles;

import java.util.*;
import java.util.concurrent.*;

public class TestSystem {

    public static void run(RBACSystem system, int threads, int opsPerThread) {
        System.out.println("Load test: " + threads + " threads, " + opsPerThread + " ops each\n");

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Future<Integer>> results = new ArrayList<>();

        for (int t = 0; t < threads; t++) {
            final int threadId = t;
            results.add(executor.submit(() -> worker(system, threadId, opsPerThread)));
        }

        int totalSuccess = 0;
        int totalErrors = 0;
        for (Future<Integer> f : results) {
            try {
                totalSuccess += f.get();
            } catch (Exception e) {
                totalErrors++;
            }
        }


        System.out.println("\n=== RESULTS ===");
        System.out.println("Success: " + totalSuccess);
        System.out.println("Errors: " + totalErrors);
        System.out.println("Users: " + system.getUserManager().count());
        System.out.println("Roles: " + system.getRoleManager().count());
        System.out.println("Assignments: " + system.getAssignmentManager().count());

        System.out.println(system.generateStatistics());
        executor.shutdown();
    }

    private static int worker(RBACSystem system, int threadId, int ops) {
        UserManager um = system.getUserManager();
        RoleManager rm = system.getRoleManager();
        AssignmentManager am = system.getAssignmentManager();
        Random rnd = new Random();
        int success = 0;

        for (int i = 0; i < ops; i++) {
            try {
                String name = "t" + "_" + i;

                switch (rnd.nextInt(5)) {
                    case 0:
                        um.add(User.create(name, "Test", name + "@test.com"));
                        success++;
                        break;
                    case 1:
                        rm.add(new Role("role_" + name, "test", new HashSet<>()));
                        success++;
                        break;
                    case 2:
                        List<User> users = um.findAll();
                        List<Role> roles = rm.findAll();
                        if (!users.isEmpty() && !roles.isEmpty()) {
                            am.add(new PermanentAssignment(
                                    users.get(rnd.nextInt(users.size())),
                                    roles.get(rnd.nextInt(roles.size())),
                                    AssignmentMetadata.now("test", "load")
                            ));
                            success++;
                        }
                        break;
                    case 3:
                        um.findByFilter(u -> u.username().contains("user"));
                        success++;
                        break;
                    case 4:
                        if (!um.findAll().isEmpty()) {
                            User u = um.findAll().get(0);
                            um.update(u.username(), "Updated", u.email());
                            success++;
                        }
                        break;
                }
            } catch (Exception e) {
            }
        }
        return success;
    }
}