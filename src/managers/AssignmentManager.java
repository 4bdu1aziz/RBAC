package src.managers;
import models.User;
import models.Role;
import models.Permission;
import models.RoleAssignment;
import models.PermanentAssignment;
import models.TemporaryAssignment;
import src.filters.AssignmentFilter;
import src.repositories.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class AssignmentManager implements Repository<RoleAssignment> {
    private final Map<String, RoleAssignment> assignmentsById;
    private final UserManager userManager;
    private final RoleManager roleManager;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AssignmentManager(UserManager userManager, RoleManager roleManager) {
        this.assignmentsById = new HashMap<>();
        this.userManager = userManager;
        this.roleManager = roleManager;
    }

    @Override
    public void add(RoleAssignment assignment) {
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment cannot be null");
        }

        // Проверяем существование пользователя
        Optional<User> userOpt = userManager.findByUsername(assignment.user().username());
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User '" + assignment.user().username() + "' does not exist");
        }

        // Проверяем существование роли
        Optional<Role> roleOpt = roleManager.findById(assignment.role().getId());
        if (roleOpt.isEmpty()) {
            throw new IllegalArgumentException("Role with id '" + assignment.role().getId() + "' does not exist");
        }

        // Проверяем на дублирование активного назначения
        boolean hasActiveAssignment = assignmentsById.values().stream()
                .filter(a -> a.user().username().equals(assignment.user().username()))
                .filter(a -> a.role().getId().equals(assignment.role().getId()))
                .anyMatch(RoleAssignment::isActive);

        if (hasActiveAssignment) {
            throw new IllegalStateException("User already has active assignment for this role");
        }

        assignmentsById.put(assignment.assignmentId(), assignment);
    }

    @Override
    public boolean remove(RoleAssignment assignment) {
        if (assignment == null) {
            return false;
        }
        return assignmentsById.remove(assignment.assignmentId()) != null;
    }

    @Override
    public Optional<RoleAssignment> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(assignmentsById.get(id));
    }

    @Override
    public List<RoleAssignment> findAll() {
        return new ArrayList<>(assignmentsById.values());
    }

    @Override
    public int count() {
        return assignmentsById.size();
    }

    @Override
    public void clear() {
        assignmentsById.clear();
    }

    public List<RoleAssignment> findByUser(User user) {
        if (user == null) {
            return Collections.emptyList();
        }
        return assignmentsById.values().stream()
                .filter(a -> a.user().username().equals(user.username()))
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findByRole(Role role) {
        if (role == null) {
            return Collections.emptyList();
        }
        return assignmentsById.values().stream()
                .filter(a -> a.role().getId().equals(role.getId()))
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findByFilter(AssignmentFilter filter) {
        if (filter == null) {
            return findAll();
        }
        return assignmentsById.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findAll(AssignmentFilter filter, Comparator<RoleAssignment> sorter) {
        List<RoleAssignment> result = findByFilter(filter);
        if (sorter != null) {
            result.sort(sorter);
        }
        return result;
    }

    public List<RoleAssignment> getActiveAssignments() {
        return assignmentsById.values().stream()
                .filter(RoleAssignment::isActive)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> getExpiredAssignments() {
        return assignmentsById.values().stream()
                .filter(a -> !a.isActive())
                .collect(Collectors.toList());
    }

    public boolean userHasRole(User user, Role role) {
        if (user == null || role == null) {
            return false;
        }
        return assignmentsById.values().stream()
                .filter(a -> a.user().username().equals(user.username()))
                .filter(a -> a.role().getId().equals(role.getId()))
                .anyMatch(RoleAssignment::isActive);
    }

    public boolean userHasPermission(User user, String permissionName, String resource) {
        if (user == null || permissionName == null || resource == null) {
            return false;
        }
        return assignmentsById.values().stream()
                .filter(a -> a.user().username().equals(user.username()))
                .filter(RoleAssignment::isActive)
                .map(RoleAssignment::role)
                .anyMatch(role -> role.hasPermission(permissionName, resource));
    }

    public Set<Permission> getUserPermissions(User user) {
        if (user == null) {
            return Collections.emptySet();
        }
        return assignmentsById.values().stream()
                .filter(a -> a.user().username().equals(user.username()))
                .filter(RoleAssignment::isActive)
                .map(RoleAssignment::role)
                .flatMap(role -> role.getPermissions().stream())
                .collect(Collectors.toSet());
    }

    public void revokeAssignment(String assignmentId) {
        Optional<RoleAssignment> opt = findById(assignmentId);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Assignment with id '" + assignmentId + "' not found");
        }

        RoleAssignment assignment = opt.get();
        if (assignment instanceof PermanentAssignment permanent) {
            permanent.revoke();
        } else if (assignment instanceof TemporaryAssignment temp) {
            // Для временных назначений - устанавливаем дату истечения в прошлое
            LocalDateTime past = LocalDateTime.now().minusDays(1);
            String expiredDate = past.format(FORMATTER);
            temp.extend(expiredDate);
        }
    }

    public void extendTemporaryAssignment(String assignmentId, String newExpirationDate) {
        Optional<RoleAssignment> opt = findById(assignmentId);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Assignment with id '" + assignmentId + "' not found");
        }

        RoleAssignment assignment = opt.get();
        if (!(assignment instanceof TemporaryAssignment temp)) {
            throw new IllegalArgumentException("Assignment is not temporary");
        }

        temp.extend(newExpirationDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssignmentManager that = (AssignmentManager) o;
        return Objects.equals(assignmentsById, that.assignmentsById);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assignmentsById);
    }
}