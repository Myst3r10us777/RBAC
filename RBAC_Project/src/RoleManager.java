import java.util.*;

public class RoleManager implements Repository<Role> {
    Map<String, Role> rolesById = new HashMap<>();
    Map<String, Role> rolesByName = new HashMap<>();

    public Optional<Role> findByName(String name) {
        return Optional.ofNullable(rolesByName.get(name));
    }

    public List<Role> findByFilter(RoleFilter filter) {
        List<Role> result = new ArrayList<>();
        for (Role role : rolesById.values()) {
            if (filter.test(role)) {
                result.add(role);
            }
        }
        return result;
    }

    public List<Role> findAll(RoleFilter filter, Comparator<Role> sorter) {
        List<Role> result = new ArrayList<>();
        for (Role role : rolesById.values()) {
            if (filter.test(role)) {
                result.add(role);
            }
        }
        Collections.sort(result, sorter);
        return result;
    }

    public boolean exists(String name) {
        return rolesByName.containsKey(name);
    }

    public void addPermissionToRole(String roleName, Permission permission) {
        Role role = rolesByName.get(roleName);
        if (role == null) {
            throw new IllegalArgumentException("Role with name '" + roleName + "' not found!");
        }
        role.addPermission(permission);
    }

    public void removePermissionFromRole(String roleName, Permission permission) {
        Role role = rolesByName.get(roleName);
        if (role == null) {
            throw new IllegalArgumentException("Role with name '" + roleName + "' not found!");
        }
        role.removePermission(permission);
    }

    public List<Role> findRolesWithPermission(String permissionName, String resource) {
        List<Role> result = new ArrayList<>();
        for (Role role : rolesById.values()) {
            if (role.hasPermission(permissionName, resource)) {
                result.add(role);
            }
        }
        return result;
    }

    @Override
    public void add(Role role) {
        if (role == null) throw new IllegalArgumentException("Role cannot be null");
        if (role.name() == null || role.name().trim().isEmpty()) {
            throw new IllegalArgumentException("Role name cannot be empty");
        }
        if (role.id == null || role.id.trim().isEmpty()) {
            throw new IllegalArgumentException("Role id cannot be empty");
        }
        if (rolesById.containsKey(role.id)) {
            throw new IllegalArgumentException("Role with id '" + role.id + "' already exists!");
        }
        if (rolesByName.containsKey(role.name())) {
            throw new IllegalArgumentException("Role with name '" + role.name() + "' already exists!");
        }
        rolesById.put(role.id, role);
        rolesByName.put(role.name(), role);
    }

    @Override
    public boolean remove(Role role) {
        if (role == null) return false;
        rolesById.remove(role.id);
        rolesByName.remove(role.name());
        return true;
    }

    @Override
    public Optional<Role> findById(String id) {
        return Optional.ofNullable(rolesById.get(id));
    }

    @Override
    public List<Role> findAll() {
        List<Role> roleList = new ArrayList<>();
        for (Role role : rolesById.values()) {
            roleList.add(role);
        }
        return roleList;
    }

    @Override
    public int count() {
        return rolesById.size();
    }

    @Override
    public void clear() {
        rolesById.clear();
        rolesByName.clear();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RoleManager that = (RoleManager) obj;
        return Objects.equals(rolesById, that.rolesById);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(rolesById);
    }
}