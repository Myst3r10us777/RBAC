import java.util.Objects;
import java.util.UUID;

abstract class AbstractRoleAssignment implements RoleAssignment {
    private String assignmentId;
    private User user;
    private Role role;
    private AssignmentMetadata metadata;

    public AbstractRoleAssignment(User user, Role role, AssignmentMetadata metadata){
        this.assignmentId = UUID.randomUUID().toString();
        this.user = user;
        this.role = role;
        this.metadata = metadata;
    }

    @Override
    public String assignmentId(){
        return assignmentId;
    }
    @Override
    public User user(){
        return user;
    }

    @Override
    public Role role(){
        return role;
    }

    @Override
    public AssignmentMetadata metadata(){
        return metadata;
    }
    public abstract boolean isActive();

    public abstract String assignmentType();

    @Override
    public boolean equals(Object obj){
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;

        AbstractRoleAssignment abrole = (AbstractRoleAssignment) obj;
        return Objects.equals(assignmentId, abrole.assignmentId);
    }

    @Override
    public int hashCode(){
        return Objects.hash(assignmentId);
    }


    public String summary(){
        String type = assignmentType();
        String status;
        if (isActive()){
            status = "ACTIVE";
        }else{
            status = "INACTIVE";
        }
        return String.format("[%s] %s assigned to %s by %s at %s\nReason: %s\nStatus: %s", type, role.name(), user.username(), metadata.assignedBy(), metadata.assignedAt(), metadata.reason(), status);
    }
}
