import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public record AssignmentMetadata(String assignedBy, String assignedAt, String reason) {
    static public AssignmentMetadata now(String assignedBy, String reason){
        String time = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return new AssignmentMetadata(assignedBy, time, reason);
    }

    public String format(){
        return assignedBy + " set role in: " + assignedAt + ". Reason: " + reason;
    }

}