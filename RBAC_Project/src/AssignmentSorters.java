import java.util.Comparator;

public class AssignmentSorters {
    public static Comparator<RoleAssignment> byUsername(){
        return (roleass1, roleass2) -> roleass1.user().username().compareTo(roleass2.user().username());
    }

    public static Comparator<RoleAssignment> byRoleName(){
        return (roleass1, roleass2) -> roleass1.role().name().compareTo(roleass2.role().name());
    }

    public static Comparator<RoleAssignment> byAssignmentDate(){
        return (roleass1, roleass2) -> roleass1.metadata().assignedAt().compareTo(roleass2.metadata().assignedAt());
    }
}
