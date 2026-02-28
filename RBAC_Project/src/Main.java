import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
public class Main{
    private static void validTest(){
        System.out.println("Тестирование валидации:");
        try {
            User user0 = User.create("", "", "");
            System.out.println("Тест 1: Успешно создал пустого пользователя!");
        } catch (IllegalArgumentException e){
            System.out.println("Тест 1: Пустой пользователь не создался, ошибка: " + e.getMessage());
        }

        try {
            User user1 = User.create("Мирон", "Мирон", "qeqwe@gmail.com");
            System.out.println("Тест 2: Успешно создал пользователя с русским username!");
        } catch (IllegalArgumentException e){
            System.out.println("Тест 2: Пользовотель с русским username!, ошибка: " + e.getMessage());
        }

        try {
            User user2 = User.create("AG", "Мирон", "qeqwe@gmail.com");
            System.out.println("Тест 3: Успешно создал пользователя в неверном диапазоне username!");
        } catch (IllegalArgumentException e){
            System.out.println("Тест 3: Пользовотель в неверном диапазоне username!, ошибка: " + e.getMessage());
        }

        try {
            User user3 = User.create("Miron", "Мирон", "qeqwe@gmail/com");
            System.out.println("Тест 4: Успешно создал пользователя с неверным форматом email!");
        } catch (IllegalArgumentException e){
            System.out.println("Тест 4: Пользовотель с неверным форматом email!, ошибка: " + e.getMessage());
        }

        try {
            User user4 = User.create("Miron", "Мирон", "qeqwe@gmail.com");
            System.out.println("Тест 5: Успешно создал пользователя c верными всеми полями!");
        } catch (IllegalArgumentException e){
            System.out.println("Тест 5: Не создал пользовотель со всеми верными полями!, ошибка:" + e.getMessage());
        }
    }
    public static void main(String[] args){
        validTest();
        User user = User.create("Vasya", "Vasuy1", "vasya@gmail.com");
        Permission per = new Permission("Read", "users", "Читаем пользователя");
        System.out.println(per.format());
        Set<Permission> permissions = new HashSet<>();
        permissions.add(per);

        Role admin = new Role("Admin", "can everything", permissions);
        System.out.println(admin.format());

        AssignmentMetadata a = AssignmentMetadata.now("admin", "tak hochy");
        System.out.println(a.format());

        LocalDateTime date = LocalDateTime.of(2027, 12, 31, 23, 59);
        String expiresAt = date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        PermanentAssignment permanent = new PermanentAssignment(user, admin, a);
        System.out.println(permanent.summary());

        TemporaryAssignment temp = new TemporaryAssignment(user, admin, a, expiresAt, false);
        System.out.println(temp.summary());
        System.out.println("===========================================\n 2 DZ:\n");
        UserFilter name = UserFilters.byUsername("Vasya");
        UserFilter domen = UserFilters.byEmailDomain("@gmal.com");
        boolean testFil = domen.test(user);
        System.out.println(testFil);
    }
}