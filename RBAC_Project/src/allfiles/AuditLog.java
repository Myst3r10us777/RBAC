package allfiles;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class AuditLog {

    public List<AuditEntry> entries = new ArrayList<>();
    private BlockingQueue<AuditEntry> queue = new LinkedBlockingQueue<>();
    private Thread worker;

    public AuditLog() {
        worker = new Thread(() -> {
            while (true) {
                try {
                    AuditEntry entry = queue.take();
                    entries.add(entry);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        worker.setDaemon(true);
        worker.start();
    }

    public void log(String action, String performer, String target, String details) {
        queue.offer(AuditEntry.now(action, performer, target, details));
    }

    public List<AuditEntry> getAll() {
        return new ArrayList<>(entries);
    }

    public List<AuditEntry> getByPerformer(String performer) {
        return entries.stream()
                .filter(entry -> entry.performer().equals(performer))
                .collect(Collectors.toList());
    }

    public List<AuditEntry> getByAction(String action) {
        return entries.stream()
                .filter(entry -> entry.action().equals(action))
                .collect(Collectors.toList());
    }

    public void printLog() {
        if (entries.isEmpty()) {
            System.out.println("Entries is empty!");
            return;
        }

        System.out.println("LOGS:");
        System.out.println("Date   |  Action   |  Performer   |   Target  |   details   |");
        System.out.println("-".repeat(100));
        for (AuditEntry entry : entries) {
            System.out.println(entry.format());
        }
        System.out.println("-".repeat(100));
    }

    public void saveToFile(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (AuditEntry entry : entries) {
                writer.println(entry.format());
            }
            System.out.println("Audit log saved to: " + filename);
        } catch (IOException e) {
            System.out.println("Error saving audit log: " + e.getMessage());
        }
    }

}
