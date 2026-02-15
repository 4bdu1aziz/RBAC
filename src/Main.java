package src;

import models.User;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Тестирование User Record ===\n");

        testValidUser();

        testInvalidUsername();
    }

    private static void testValidUser() {
        System.out.println("--- Тест 1: Создание валидного пользователя ---");
        try {
            User user1 = User.validate("abdulaziz_123", "Abdulaziz_123", "abdulaziz@gmail.com");
            System.out.println("Успешно создан: " + user1.format());

            User user2 = User.validate("egor123", "Egor Avdeev", "egor.avdeev@mail.ru");
            System.out.println("Успешно создан: " + user2.format());

            User user3 = User.validate("Lev_o_n", "Levon 123", "Levon@gmail.com");
            System.out.println("Успешно создан: " + user3.format());

        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testInvalidUsername() {
        System.out.println("--- Тест 2: Невалидные username ---");

        try {
            User.validate("Ab", "Abdul 123", "abdulaziz@gmail.com");
            System.out.println("Должно было упасть: слишком короткий username");
        } catch (IllegalArgumentException e) {
            System.out.println("Слишком короткий username: " + e.getMessage());
        }

        try {
            User.validate("thisusernamelong123123123123", "Egor Avdeev", "egor@mail.ru");
            System.out.println("Должно было упасть: слишком длинный username");
        } catch (IllegalArgumentException e) {
            System.out.println("Слишком длинный username: " + e.getMessage());
        }

        try {
            User.validate("levo-n", "levon 123", "Levon@gmail.com");
            System.out.println("Должно было упасть: дефис недопустим");
        } catch (IllegalArgumentException e) {
            System.out.println("Недопустимый символ (дефис): " + e.getMessage());
        }

        System.out.println();
    }
}