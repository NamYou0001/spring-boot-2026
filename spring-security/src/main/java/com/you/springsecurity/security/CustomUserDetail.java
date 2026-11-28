package com.you.springsecurity.security;

import com.you.springsecurity.user.model.Permission;
import com.you.springsecurity.user.model.Users;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class CustomUserDetail
        implements UserDetails, CredentialsContainer {

    private final Long id;
    private final String username;
    private String password;

    private final String role;
    private final List<String> permissions;

    private final boolean enabled;
    private final boolean accountNonLocked;
    private final boolean accountNonExpired;
    private final boolean credentialsNonExpired;

    public CustomUserDetail(Users user) {

        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();

        this.role = "ROLE_" + user.getRole().getName();

        this.permissions = user.getRole()
                .getPermissions()
                .stream()
                .map(Permission::getCode)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        this.enabled = user.isEnable();
        this.accountNonLocked = user.isAccountNonlocked();
        this.accountNonExpired = user.isExpiresAt();
        this.credentialsNonExpired = true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role));
        for (String permission : permissions) {
            authorities.add(new SimpleGrantedAuthority(permission));
        }
        return authorities;
    }

    public Long getId() {
        return id;
    }

    public List<String> getRoles() {
        return List.of(role);
    }

    public List<String> getPermissions() {
        return permissions;
    }

    @Override
    public @Nullable String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void eraseCredentials() {
        this.password = null;
    }
}