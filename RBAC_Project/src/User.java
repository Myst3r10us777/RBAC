public record User(String username, String fullName, String email){
    static String regex = "^[a-zA-Z0-9_]+$";

    public static User create(String username, String fullName, String email){
        if (username.trim().isEmpty() || fullName.trim().isEmpty() || email.trim().isEmpty())
            throw new IllegalArgumentException("Все поля должны быть не пустыми!");

        if (!(username.matches(regex) && username.length() > 3 &&  username.length() < 20))
            throw new IllegalArgumentException("username должен состоять только из латинских букв, цифр и _, также находиться в диапазоне [3,20]");

        if (!email.matches(".+@.+\\..+"))
            throw new IllegalArgumentException("Email должен содержать @ и . после неё");

        return new User(username, fullName, email);
    }

    public String format(){
        String info = username + " " + fullName + " " + email;
        return info;
    }

}