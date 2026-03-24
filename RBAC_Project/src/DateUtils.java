import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String getCurrentDate() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(DATETIME_FORMATTER);
    }

    public static boolean isBefore(String date1, String date2) {
        if (date1 == null || date2 == null) return false;
        return date1.compareTo(date2) < 0;
    }

    public static boolean isAfter(String date1, String date2) {
        if (date1 == null || date2 == null) return false;
        return date1.compareTo(date2) > 0;
    }

    public static String addDays(String date, int days) {
        if (date == null) return null;
        LocalDate ld = LocalDate.parse(date, DATE_FORMATTER);
        return ld.plusDays(days).format(DATE_FORMATTER);
    }

    public static String formatRelativeTime(String date) {
        if (date == null) return "unknown";
        LocalDate target = LocalDate.parse(date, DATE_FORMATTER);
        LocalDate now = LocalDate.now();

        long days = ChronoUnit.DAYS.between(now, target);

        if (days < 0) {
            long absDays = -days;
            return absDays + (absDays == 1 ? " day ago" : " days ago");
        } else if (days > 0) {
            return "in " + days + (days == 1 ? " day" : " days");
        } else {
            return "today";
        }
    }
}