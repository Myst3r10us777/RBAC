package allfiles;

import java.util.List;

public class FormatUtils {

    public static String formatTable(String[] headers, List<String[]> rows) {
        if (headers == null || rows == null) return "";

        int cols = headers.length;
        int[] colWidths = new int[cols];

        for (int i = 0; i < cols; i++) {
            colWidths[i] = headers[i].length();
        }

        for (String[] row : rows) {
            for (int i = 0; i < cols && i < row.length; i++) {
                int len = row[i] != null ? row[i].length() : 0;
                colWidths[i] = Math.max(colWidths[i], len);
            }
        }

        StringBuilder sb = new StringBuilder();

        String line = "+";
        for (int i = 0; i < cols; i++) {
            line += "-".repeat(colWidths[i] + 2) + "+";
        }
        sb.append(line).append("\n");

        sb.append("|");
        for (int i = 0; i < cols; i++) {
            sb.append(" ").append(padRight(headers[i], colWidths[i])).append(" |");
        }
        sb.append("\n");

        sb.append(line).append("\n");

        for (String[] row : rows) {
            sb.append("|");
            for (int i = 0; i < cols; i++) {
                String val = (i < row.length && row[i] != null) ? row[i] : "";
                sb.append(" ").append(padRight(val, colWidths[i])).append(" |");
            }
            sb.append("\n");
        }

        sb.append(line).append("\n");
        return sb.toString();
    }

    public static String formatBox(String text) {
        if (text == null) return "";
        int width = text.length() + 4;
        String border = "+" + "-".repeat(width) + "+";
        return border + "\n" + "|  " + text + "  |\n" + border;
    }

    public static String formatHeader(String text) {
        if (text == null) return "";
        int width = text.length() + 10;
        String line = "=".repeat(width);
        return "\n" + line + "\n" + "    " + text + "    \n" + line;
    }

    public static String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    public static String padRight(String text, int length) {
        if (text == null) text = "";
        if (text.length() >= length) return text;
        return text + " ".repeat(length - text.length());
    }

    public static String padLeft(String text, int length) {
        if (text == null) text = "";
        if (text.length() >= length) return text;
        return " ".repeat(length - text.length()) + text;
    }
}