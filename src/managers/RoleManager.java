package src.managers;
import models.Role;
import models.Permission;
import src.filters.RoleFilter;
import src.repositories.Repository;

import java.util.*;
import java.util.stream.Collectors;

public class RoleManager implements Repository<Role> {
    private final Map<String, Role> rolesById;
    private final Map<String, Role> rolesByName;
    private final AssignmentManager assignmentManager;

    public RoleManager(AssignmentManager assignmentManager) {
        this.rolesById = new HashMap<>();
        this.rolesByName = new HashMap<>();
        this.assignmentManager = assignmentManager;
    }

    @Override
    public void add(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        String roleId = role.getId();
        String roleName = role.getName();

        if (rolesById.containsKey(roleId)) {
            throw new IllegalArgumentException("Role with id '" + roleId + "' already exists");
        }

        if (rolesByName.containsKey(roleName)) {
            throw new IllegalArgumentException("Role with name '" + roleName + "' already exists");
        }

        rolesById.put(roleId, role);
        rolesByName.put(roleName, role);
    }

    @Override
    public boolean remove(Role role) {
        if (role == null) {
            return false;
        }

        if (assignmentManager.isRoleAssigned(role.getId())) {
            throw new IllegalStateException("Cannot delete role '" + role.getName() + "' because it is assigned to users");
        }

        Role removed = rolesById.remove(role.getId());
        if (removed != null) {
            rolesByName.remove(role.getName());
            return true;
        }
        return false;
    }

    @Override
    public Optional<Role> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(rolesById.get(id));
    }

    @Override
    public List<Role> findAll() {
        return new ArrayList<>(rolesById.values());
    }

    @Override
    public int count() {
        return rolesById.size();
    }

    @Override
    public void clear() {
        rolesById.clear();
        rolesByName.clear();
    }

    public Optional<Role> findByName(String name) {
        if (name == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(rolesByName.get(name));
    }

    public List<Role> findByFilter(RoleFilter filter) {
        if (filter == null) {
            return findAll();
        }
        return rolesById.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<Role> findAll(RoleFilter filter, Comparator<Role> sorter) {
        List<Role> result = findByFilter(filter);
        if (sorter != null) {
            result.sort(sorter);
        }
        return result;
    }

    public boolean exists(String name) {
        if (name == null) {
            return false;
        }
        return rolesByName.containsKey(name);
    }

    public void addPermissionToRole(String roleName, Permission permission) {
        if (roleName == null) {
            throw new IllegalArgumentException("Role name cannot be null");
        }
        if (permission == null) {
            throw new IllegalArgumentException("Permission cannot be null");
        }

        Role role = rolesByName.get(roleName);
        if (role == null) {
            throw new IllegalArgumentException("Role with name '" + roleName + "' not found");
        }

        role.addPermission(permission);
    }

    public void removePermissionFromRole(String roleName, Permission permission) {
        if (roleName == null) {
            throw new IllegalArgumentException("Role name cannot be null");
        }
        if (permission == null) {
            throw new IllegalArgumentException("Permission cannot be null");
        }

        Role role = rolesByName.get(roleName);
        if (role == null) {
            throw new IllegalArgumentException("Role with name '" + roleName + "' not found");
        }

        role.removePermission(permission);
    }

    public List<Role> findRolesWithPermission(String permissionName, String resource) {
        if (permissionName == null || resource == null) {
            return Collections.emptyList();
        }

        return rolesById.values().stream()
                .filter(role -> role.hasPermission(permissionName, resource))
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleManager that = (RoleManager) o;
        return Objects.equals(rolesById, that.rolesById);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rolesById);
    }
}