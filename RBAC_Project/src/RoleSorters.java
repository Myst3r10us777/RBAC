import java.util.Comparator;

public class RoleSorters {
    public static Comparator<Role> byName(){
        return (role1, role2) -> role1.name().compareTo(role2.name());
    }

    public static Comparator<Role> byPermissionCount(){
        return (role1, role2) -> {
            int r1 = role1.getPermissions().size();
            int r2 = role2.getPermissions().size();
            return Integer.compare(r1, r2);
        };
    }
}
