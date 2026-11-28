package com.you.springsecurity.common.seed;

import com.you.springsecurity.user.model.Permission;
import com.you.springsecurity.user.model.Role;
import com.you.springsecurity.user.model.Users;

import com.you.springsecurity.user.repository.PermissionRepository;
import com.you.springsecurity.user.repository.RoleRepository;
import com.you.springsecurity.user.repository.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Profile("dev")
public class DevelopmentUserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    public DevelopmentUserSeeder(UserRepository userRepository, RoleRepository roleRepository, PermissionRepository permissionRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {

        // 1. Create default permissions
        createDefaultPermissions();

        // 2. Create roles and assign permissions
        Role adminRole = createAdminRole();
        Role userRole = createUserRole();

        // 3. Create default users
        createUser("Admin", "Admin@gmail.com", "admin@123", adminRole);

        createUser("user", "user@gmail.com", "user@123", userRole);
    }

    // Create default permissions
    private void createDefaultPermissions() {

        List<Permission> permissions = List.of(

                new Permission("USER_READ", "View users" , ""),
                new Permission("USER_CREATE", "Create users" , ""),
                new Permission("USER_UPDATE", "Update users" , ""),
                new Permission("USER_DELETE", "Delete users" , ""),

                new Permission("ROLE_READ", "View roles" , ""),
                new Permission("ROLE_CREATE", "Create roles" , ""),
                new Permission("ROLE_UPDATE", "Update roles", ""),
                new Permission("ROLE_DELETE", "Delete roles", ""),

                new Permission("PERMISSION_READ", "View permissions" , ""),
                new Permission("PERMISSION_CREATE", "Create permissions" , ""),
                new Permission("PERMISSION_UPDATE", "Update permissions" , ""),
                new Permission("PERMISSION_DELETE", "Delete permissions", ""),

                new Permission("USER_ROLE_READ", "View user roles" , ""),
                new Permission("USER_ROLE_ASSIGN", "Assign roles to users", ""),

                new Permission("USER_PROFILE_READ", "View own profile" , ""));

        for (Permission permission : permissions) {

            if (!permissionRepository.existsByCode(permission.getCode())) {
                permissionRepository.save(permission);
            }
        }
    }

    // Create ADMIN role
    private Role createAdminRole() {

        Role role = roleRepository.findByNameIgnoreCase("ADMIN").orElseGet(() -> roleRepository.save(new Role("ADMIN", "System Administrator")));

        // ADMIN gets all permissions
        role.setPermissions(new HashSet<>(permissionRepository.findAll()));

        return roleRepository.save(role);
    }

    // Create USERS role
    private Role createUserRole() {

        Role role = roleRepository.findByNameIgnoreCase("USERS").orElseGet(() -> roleRepository.save(new Role("USERS", "Standard User")));

        Permission profilePermission = permissionRepository.findByCode("USER_PROFILE_READ").orElseThrow(() -> new IllegalStateException("USER_PROFILE_READ permission not found"));

        // USERS gets only profile permission
        role.setPermissions(new HashSet<>(Set.of(profilePermission)));

        return roleRepository.save(role);
    }

    // Create or update default user
    private void createUser(String username, String email, String rawPassword, Role role) {

        Users user = userRepository.findByUsernameWithRoleAndPermissions(username).orElseGet(() -> {

            Users newUser = new Users(username, email, passwordEncoder.encode(rawPassword));

            return newUser;
        });

        // User has only one role
        user.setRole(role);

        userRepository.save(user);
    }
}
