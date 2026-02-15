package src;

import models.User;
import models.Permission;
import models.Role;
import models.AssignmentMetadata;
import models.PermanentAssignment;
import models.TemporaryAssignment;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== ТЕСТИРОВАНИЕ RBAC СИСТЕМЫ ===\n");

        testUser();
        testPermission();
        testRole();
        testAssignmentMetadata();
        testPermanentAssignment();
        testTemporaryAssignment();
    }

    private static void testUser() {
        System.out.println("--- ТЕСТ 1.1: User Record ---");

        try {
            User user1 = User.validate("john_doe", "John Doe", "john@example.com");
            System.out.println("Успех: " + user1.format());

            User user2 = User.validate("jane_smith", "Jane Smith", "jane@gmail.com");
            System.out.println("Успех: " + user2.format());
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }

        try {
            User user3 = User.validate("john", "John Doe", "john@example.com");
            System.out.println("Успех: " + user3.format());
        } catch (Exception e) {
            System.out.println("Ошибка (короткий username): " + e.getMessage());
        }
        System.out.println();
    }

    private static void testPermission() {
        System.out.println("--- ТЕСТ 1.2: Permission Record ---");

        try {
            Permission p1 = new Permission("READ", "users", "Can read users");
            System.out.println("Успех: " + p1.format());

            Permission p2 = new Permission("WRITE", "reports", "Can write reports");
            System.out.println("Успех: " + p2.format());
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }

        try {
            Permission p3 = new Permission(" delete ", "USERS", "  Can delete users  ");
            System.out.println("Нормализация: " + p3.format());
            System.out.println("  name='" + p3.name() + "', resource='" + p3.resource() + "'");
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testRole() {
        System.out.println("--- ТЕСТ 1.3: Role Class ---");

        try {
            Permission readUsers = new Permission("READ", "users", "Can read users");
            Permission writeUsers = new Permission("WRITE", "users", "Can write users");

            Role adminRole = new Role("Administrator", "Full system access");
            adminRole.addPermission(readUsers);
            adminRole.addPermission(writeUsers);

            System.out.println("Роль создана: " + adminRole.getName());
            System.out.println("  ID: " + adminRole.getId());
            System.out.println("  Permissions: " + adminRole.getPermissions().size());

            System.out.println("  Has READ on users: " + adminRole.hasPermission("READ", "users"));
            System.out.println("  Has WRITE on reports: " + adminRole.hasPermission("WRITE", "reports"));

        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testAssignmentMetadata() {
        System.out.println("--- ТЕСТ 1.4: AssignmentMetadata Record ---");

        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Initial setup");
        System.out.println("С причиной:");
        System.out.println("  " + meta1.format());

        AssignmentMetadata meta2 = AssignmentMetadata.now("manager", null);
        System.out.println("Без причины:");
        System.out.println("  " + meta2.format());
        System.out.println();
    }

    private static void testPermanentAssignment() {
        System.out.println("--- ТЕСТ 1.7: PermanentAssignment ---");

        try {
            User user = User.validate("alice", "Alice Wonder", "alice@example.com");

            Permission readUsers = new Permission("READ", "users", "Can read users");
            Role viewerRole = new Role("Viewer", "Can view content");
            viewerRole.addPermission(readUsers);

            AssignmentMetadata metadata = AssignmentMetadata.now("admin", "Permanent access");

            PermanentAssignment assignment = new PermanentAssignment(user, viewerRole, metadata);
            System.out.println("Создано постоянное назначение:");
            System.out.println("  Type: " + assignment.assignmentType());
            System.out.println("  Active: " + assignment.isActive());

            assignment.revoke();
            System.out.println("  After revoke - Active: " + assignment.isActive());
            System.out.println("  Revoked: " + assignment.isRevoked());

        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testTemporaryAssignment() {
        System.out.println("--- ТЕСТ 1.8: TemporaryAssignment ---");

        try {
            User user = User.validate("bob", "Bob Wilson", "bob@example.com");

            Permission writeReports = new Permission("WRITE", "reports", "Can write reports");
            Role editorRole = new Role("Editor", "Can edit content");
            editorRole.addPermission(writeReports);

            AssignmentMetadata metadata = AssignmentMetadata.now("manager", "Temporary access");

            LocalDateTime future = LocalDateTime.now().plusDays(30);
            String expiresAt = future.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            TemporaryAssignment assignment = new TemporaryAssignment(user, editorRole, metadata, expiresAt, true);
            System.out.println("Создано временное назначение:");
            System.out.println("  Type: " + assignment.assignmentType());
            System.out.println("  Expires: " + assignment.getExpiresAt());
            System.out.println("  Active: " + assignment.isActive());
            System.out.println("  Time remaining: " + assignment.getTimeRemaining());

            LocalDateTime later = LocalDateTime.now().plusDays(60);
            String newExpiresAt = later.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            assignment.extend(newExpiresAt);
            System.out.println("  After extension - Expires: " + assignment.getExpiresAt());

        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
        System.out.println();
    }
}