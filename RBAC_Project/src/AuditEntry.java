import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record AuditEntry(
        String timestamp,
        String action,
        String performer,
        String target,
        String details
) {
    public static AuditEntry now(String action, String performer, String target, String details) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return new AuditEntry(timestamp, action, performer, target, details);
    }

    public String format() {
        return timestamp + " | " + action + " | " + performer + " | " + target + " | " + details + " |";
    }
}
