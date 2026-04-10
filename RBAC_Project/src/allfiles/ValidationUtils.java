package allfiles;

import java.util.Locale;

public class ValidationUtils {
    public ValidationUtils(){

    }
    static String regex = "^[a-zA-Z0-9_]+$";
    public static boolean isValidUsername(String username){
        if (username.trim().isEmpty() || username == null) {
            return false;
        } else if (!(username.matches(regex) && username.length() > 3 &&  username.length() < 20)) {
            return false;
        }
        return true;
    }

    public static boolean isValidEmail(String email){
        if (email == null || email.trim().isEmpty()){
            return false;
        } else if (!email.matches(".+@.+\\..+")){
            return false;
        }
        return true;
    }

    public static boolean isValidDate(String date){
        if (date == null || date.trim().isEmpty()){
            return false;
        } else if (!date.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?$")){
            return false;
        }
        return true;
    }

    public static String normalizeString(String input){
        if (input == null)
            return null;
        String in = input.trim();
        in = in.toLowerCase(Locale.ROOT);
        return in.replaceAll("\\s+", " ");
    }

    public static void requireNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName);
        }
    }
}