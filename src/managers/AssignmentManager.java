package src.managers;
import models.User;
import models.Role;
import java.util.*;

public class AssignmentManager {
    private final Map<String, Set<String>> userRoles; // username -> set of role ids

    public AssignmentManager() {
        this.userRoles = new HashMap<>();
    }

    public void assignRoleToUser(String username, String roleId) {
        userRoles.computeIfAbsent(username, k -> new HashSet<>()).add(roleId);
    }

    public void removeRoleFromUser(String username, String roleId) {
        Set<String> roles = userRoles.get(username);
        if (roles != null) {
            roles.remove(roleId);
            if (roles.isEmpty()) {
                userRoles.remove(username);
            }
        }
    }

    public boolean isRoleAssigned(String roleId) {
        return userRoles.values().stream()
                .anyMatch(roles -> roles.contains(roleId));
    }

    public List<String> getUsersWithRole(String roleId) {
        return userRoles.entrySet().stream()
                .filter(entry -> entry.getValue().contains(roleId))
                .map(Map.Entry::getKey)
                .toList();
    }

    public Set<String> getUserRoles(String username) {
        return userRoles.getOrDefault(username, new HashSet<>());
    }

    public void clear() {
        userRoles.clear();
    }
}