
package com.you.springsecurity.user.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "tbl_users")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // Many Users belong to One Role
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "role_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_users_role")
    )
    private Role role;

    @Column(nullable = false)
    private boolean enable = true;

    @Column(nullable = false)
    private boolean accountNonlocked = true;

    @Column(nullable = false)
    private boolean expiresAt = true;

    @Column(name = "create_at", updatable = false)
    private Instant createAt;

    public Users() {
    }

    public Users(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public void addRole(Role role) {
        this.role = role;
    }

    @PrePersist
    protected void onCreate() {
        if (createAt == null) {
            createAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isAccountNonlocked() {
        return accountNonlocked;
    }

    public void setAccountNonlocked(boolean accountNonlocked) {
        this.accountNonlocked = accountNonlocked;
    }

    public boolean isExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(boolean expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getCreateAt() {
        return createAt;
    }
}
