package allfiles;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AssignmentManager implements Repository<RoleAssignment> {
    Map<String, RoleAssignment> assignments = new ConcurrentHashMap<>();
    UserManager userManager;
    RoleManager roleManager;
    Object object = new Object();

    public AssignmentManager(UserManager userManager, RoleManager roleManager) {
        this.userManager = userManager;
        this.roleManager = roleManager;
    }

    public List<RoleAssignment> findByUser(User user) {
        List<RoleAssignment> result = new ArrayList<>();
        synchronized (object) {
            for (RoleAssignment assignment : assignments.values()) {
                if (assignment.user().equals(user)) {
                    result.add(assignment);
                }
            }
        }
        return result;
    }

    public List<RoleAssignment> findByRole(Role role) {
        List<RoleAssignment> result = new ArrayList<>();
        synchronized (object) {
            for (RoleAssignment assignment : assignments.values()) {
                if (assignment.role().equals(role)) {
                    result.add(assignment);
                }
            }
        }
        return result;
    }

    public List<RoleAssignment> findByFilter(AssignmentFilter filter) {
        List<RoleAssignment> result = new ArrayList<>();
        synchronized (object) {
            for (RoleAssignment assignment : assignments.values()) {
                if (filter.test(assignment)) {
                    result.add(assignment);
                }
            }
        }
        return result;
    }

    public List<RoleAssignment> findByFilterParallel(AssignmentFilter filter) {
        return assignments.values().parallelStream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findByFilterParallel(AssignmentFilter filter, Comparator<RoleAssignment> sorter) {
        return assignments.values().parallelStream()
                .filter(filter::test)
                .sorted(sorter)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findAll(AssignmentFilter filter, Comparator<RoleAssignment> sorter) {
        List<RoleAssignment> result = new ArrayList<>();
        synchronized (object) {
            for (RoleAssignment assignment : assignments.values()) {
                if (filter.test(assignment)) {
                    result.add(assignment);
                }
            }
            Collections.sort(result, sorter);
        }
        return result;
    }

    public List<RoleAssignment> getActiveAssignments() {
        List<RoleAssignment> result = new ArrayList<>();
        synchronized (object) {
            for (RoleAssignment assignment : assignments.values()) {
                if (assignment.isActive()) {
                    result.add(assignment);
                }
            }
        }
        return result;
    }

    public List<RoleAssignment> getExpiredAssignments() {
        List<RoleAssignment> result = new ArrayList<>();
        synchronized (object) {
            for (RoleAssignment assignment : assignments.values()) {
                if (!assignment.isActive()) {
                    result.add(assignment);
                }
            }
        }
        return result;
    }

    public boolean userHasRole(User user, Role role) {
        synchronized (object) {
            for (RoleAssignment assignment : assignments.values()) {
                if (assignment.user().equals(user) &&
                        assignment.role().equals(role) &&
                        assignment.isActive()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean userHasPermission(User user, String permissionName, String resource) {
        synchronized (object) {
            Set<Permission> permissions = getUserPermissions(user);
            for (Permission permission : permissions) {
                if (permission.name().equals(permissionName) &&
                        permission.resource().equals(resource)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Set<Permission> getUserPermissions(User user) {
        Set<Permission> permissions = new HashSet<>();
        synchronized (object) {
            for (RoleAssignment assignment : assignments.values()) {
                if (assignment.user().equals(user) && assignment.isActive()) {
                    permissions.addAll(assignment.role().getPermissions());
                }
            }
        }
        return permissions;
    }

    public void revokeAssignment(String assignmentId) {
        RoleAssignment assignment = assignments.get(assignmentId);
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment with id '" + assignmentId + "' not found!");
        }
        synchronized (object) {
            if (assignment instanceof PermanentAssignment) {
                ((PermanentAssignment) assignment).revoke();
            }
        }
    }

    public void extendTemporaryAssignment(String assignmentId, String newExpirationDate) {
        RoleAssignment assignment = assignments.get(assignmentId);
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment with id '" + assignmentId + "' not found!");
        }
        synchronized (object) {
            if (assignment instanceof TemporaryAssignment) {
                ((TemporaryAssignment) assignment).extend(newExpirationDate);
            }
        }
    }

    @Override
    public void add(RoleAssignment assignment) {
        if (assignment == null) throw new IllegalArgumentException("Assignment cannot be null");

        User user = assignment.user();
        if (!userManager.exists(user.username())) {
            throw new IllegalArgumentException("java.User '" + user.username() + "' does not exist!");
        }

        Role role = assignment.role();
        if (!roleManager.exists(role.name())) {
            throw new IllegalArgumentException("java.Role '" + role.name() + "' does not exist!");
        }
        synchronized (object) {
            for (RoleAssignment a : assignments.values()) {
                if (a.user().equals(user) &&
                        a.role().equals(role) &&
                        a.isActive()) {
                    throw new IllegalStateException("java.User '" + user.username() +
                            "' already has active assignment for role '" + role.name() + "'!");
                }
            }

            assignments.put(assignment.assignmentId(), assignment);
        }
    }

    @Override
    public boolean remove(RoleAssignment assignment) {
        if (assignment == null) return false;
        synchronized (object) {
            return assignments.remove(assignment.assignmentId()) != null;
        }
    }

    @Override
    public Optional<RoleAssignment> findById(String id) {
        return Optional.ofNullable(assignments.get(id));
    }

    @Override
    public List<RoleAssignment> findAll() {
        List<RoleAssignment> result = new ArrayList<>();
        synchronized (object) {
            for (RoleAssignment assignment : assignments.values()) {
                result.add(assignment);
            }
        }
        return result;
    }

    @Override
    public int count() {
        return assignments.size();
    }

    @Override
    public void clear() {
        assignments.clear();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AssignmentManager that = (AssignmentManager) obj;
        return Objects.equals(assignments, that.assignments);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(assignments);
    }
}