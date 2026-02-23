package src.sorters;
import models.Role;
import java.util.Comparator;

public class RoleSorters {

    private RoleSorters() {
        // Приватный конструктор чтобы нельзя было создать экземпляр
    }

    public static Comparator<Role> byName() {
        return Comparator.comparing(Role::getName);
    }

    public static Comparator<Role> byPermissionCount() {
        return Comparator.comparingInt(role -> role.getPermissions().size());
    }
}