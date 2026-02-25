import java.util.*;

public class Role {
    private String id;
    private String name;
    private String description;
    private final Set<Permission> permissions;
    public Role(String name, String description, Set<Permission> permissions){
        this.id = generateId();
        this.name = name;
        this.description=description;
        this.permissions = new HashSet<>(permissions);
    }

    private String generateId(){
        return "role_" + UUID.randomUUID();
    }

    public void addPermission(Permission permission){
        permissions.add(permission);
    }

    public void removePermission(Permission permission) {
        permissions.remove(permission);
    }

    public boolean hasPermission(Permission permission){
        return permissions.contains(permission);
    }

    public boolean hasPermission(String permissionName, String resource){
        for (Permission per : permissions){
            if ((per.name().equals(permissionName)) && (per.resource().equals(resource)))
                return true;
        }
        return false;
    }

    public Set<Permission> getPermissions(){
        return Collections.unmodifiableSet(new HashSet<>(permissions));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;

        Role role = (Role) obj;
        return Objects.equals(id, role.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString(){
        String str = "Role: " + name + " [ID: " + id + "]\n" +
                "Description: " + description + '\n' +
                "Permissions " + '(' + permissions.size() + "):";
        for(Permission per : permissions){
            str += ("\t - " + per.format()) + '\n';
        }
        return str;
    }

    public String format(){
        String str = "Role: " + name + " [ID: " + id + "]\n" +
                "Description: " + description + '\n' +
                "Permissions " + '(' + permissions.size() + "):" + '\n';
        for(Permission per : permissions){
            str += ("\t - " + per.format()) + '\n';
        }
        return str;
    }
}
