import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TemporaryAssignment extends AbstractRoleAssignment {
    private String expiresAt;
    private boolean autoRenew;

    public TemporaryAssignment(User user, Role role, AssignmentMetadata metadata, String expiresAt, boolean autoRenew) {
        super(user, role, metadata);
        this.expiresAt = expiresAt;
        this.autoRenew = autoRenew;
    }

    @Override
    public boolean isActive(){
        return !isExpired();
    }
    public boolean isExpired() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiration = LocalDateTime.parse(expiresAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return now.isAfter(expiration);
    }

    @Override
    public String assignmentType() {
        return "TEMPORARY";
    }

    public void extend(String newExpiresAt) {
        this.expiresAt = newExpiresAt;
    }

    public String getTimeRemaining(){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiration = LocalDateTime.parse(expiresAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        if (now.isAfter(expiration)) {
            return "Completed!";
        }
        Duration duration = Duration.between(now, expiration);

        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;

        return String.format("%d д %d ч %d мин %d сек", days, hours, minutes, seconds);
    }

    @Override
    public String summary(){
        String type = assignmentType();
        String status;
        if (isActive()){
            status = "ACTIVE";
        }else{
            status = "INACTIVE";
        }
        return String.format("[%s] %s assigned to %s by %s at %s\nReason: %s\nStatus: %s\nTime remaining: %s", type, role().name(), user().username(), metadata().assignedBy(), metadata().assignedAt(), metadata().reason(), status, getTimeRemaining());
    }
}