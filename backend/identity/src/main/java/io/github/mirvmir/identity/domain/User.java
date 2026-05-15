package io.github.mirvmir.identity.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "profile_completed", nullable = false)
    private boolean profileCompleted;

    protected User() {
    };

    public User(Role role, String passwordHash, String email, boolean profileCompleted) {
        this.role = role;
        this.passwordHash = passwordHash;
        this.email = email;
        this.profileCompleted = profileCompleted;
    };

    public int hashCode() {
        return Long.hashCode(id);
    }

    public static User createNewCustomer(String passwordHash, String email) {
        return new User(Role.USER, passwordHash, email, false);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (null == o || getClass() != o.getClass()) {
            return false;
        }

        User other = (User) o;
        return id != null && id.equals(other.id);
    }

    public void setAdmin() {
        this.role = Role.ADMIN;
    }

    public void confirmEmailChange(String newEmail) {
        this.email = newEmail;
    }

    public void assignId(Long id) {
        this.id = id;
    }

    public void markProfileCompleted() {
        this.profileCompleted = true;
    }
}
