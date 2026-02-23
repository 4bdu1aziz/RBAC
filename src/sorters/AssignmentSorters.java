package src.sorters;
import models.RoleAssignment;
import java.util.Comparator;

public class AssignmentSorters {

    private AssignmentSorters() {
        // Приватный конструктор чтобы нельзя было создать экземпляр
    }

    public static Comparator<RoleAssignment> byUsername() {
        return Comparator.comparing(assignment -> assignment.user().username());
    }

    public static Comparator<RoleAssignment> byRoleName() {
        return Comparator.comparing(assignment -> assignment.role().getName());
    }

    public static Comparator<RoleAssignment> byAssignmentDate() {
        return Comparator.comparing(assignment -> assignment.metadata().assignedAt());
    }
}