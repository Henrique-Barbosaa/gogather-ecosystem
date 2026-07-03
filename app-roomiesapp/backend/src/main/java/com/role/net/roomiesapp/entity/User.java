package com.role.net.roomiesapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import gogather.framework.group.jpa.domain.BaseUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseUser implements UserDetails {

    @NotNull(message = "User username cannot be null!")
    @Column(nullable = false, unique = true)
    private String username;

    @NotNull(message = "User password cannot be null!")
    @Column(nullable = false)
    private String password;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "birthdate")
    private LocalDate birthDate;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.active;
    }
}
