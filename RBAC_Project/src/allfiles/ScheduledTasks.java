package allfiles;

import java.util.concurrent.*;
import java.util.List;

public class ScheduledTasks {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final AssignmentManager assignmentManager;
    private final AuditLog auditLog;

    public ScheduledTasks(AssignmentManager assignmentManager, AuditLog auditLog) {
        this.assignmentManager = assignmentManager;
        this.auditLog = auditLog;
    }

    public void start(int intervalSeconds) {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                expireTemporaryAssignments();
                logStatistics();
            } catch (Exception e) {
                System.err.println("Scheduled task error: " + e.getMessage());
            }
        }, 0, intervalSeconds, TimeUnit.SECONDS);
    }

    private void expireTemporaryAssignments() {
        List<RoleAssignment> all = assignmentManager.findAll();
        int expired = 0;

        for (RoleAssignment ra : all) {
            if (ra instanceof TemporaryAssignment) {
                TemporaryAssignment temp = (TemporaryAssignment) ra;
                if (temp.isActive()) {
                    expired++;
                }
            }
        }

        if (expired > 0) {
            auditLog.log("SCHEDULED_EXPIRE", "system", "temporary_assignments",
                    "Expired and revoked: " + expired);
        }
    }

    private void logStatistics() {
        int total = assignmentManager.count();
        int active = assignmentManager.getActiveAssignments().size();
        int expired = assignmentManager.getExpiredAssignments().size();

        String stats = String.format("Total: %d, Active: %d, Expired: %d", total, active, expired);
        auditLog.log("SCHEDULED_STATS", "system", "assignments", stats);
    }

    public void shutdown() {
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }
}