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
        Permission per = new Permission("Read", "users", "Читаем пользователя");
        System.out.println(per.format());
    }
}