public class RoleFilters {
    public static RoleFilter byName(String name){
        return role -> role.name().equals(name);
    }

    public static RoleFilter byNameContains(String substring){
        return role -> role.name().contains(substring);
    }

    public static RoleFilter hasPermission(Permission permission){
        return role -> role.hasPermission(permission);
    }

    public static RoleFilter hasPermission(String permissionName, String resource){
        return role -> role.hasPermission(permissionName, resource);
    }

    public static RoleFilter hasAtLeastNPermissions(int n){
        return role -> {
            int per = role.getPermissions().size();
            return per >= n;
        };
    }
}
