package src.sorters;
import models.User;
import java.util.Comparator;

public class UserSorters {

    private UserSorters() {
        // Приватный конструктор чтобы нельзя было создать экземпляр
    }

    public static Comparator<User> byUsername() {
        return Comparator.comparing(User::username);
    }

    public static Comparator<User> byFullName() {
        return Comparator.comparing(User::fullName);
    }

    public static Comparator<User> byEmail() {
        return Comparator.comparing(User::email);
    }
}