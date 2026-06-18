package com.role.net.gogather.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Setter
@Entity
@SequenceGenerator(
    name = "id_generator",
    sequenceName = "seq_user",
    allocationSize = 1
)
@Table(name = "users")
public class User extends BaseEntity implements UserDetails {

    @NotNull(message = "User username cannot be null!")
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = true)
    private String displayName;

    @NotNull(message = "User email cannot be null!")
    @Column(nullable = false, unique = true)
    private String email;

    @NotNull(message = "User password cannot be null!")
    @Column(nullable = false)
    private String password;

    @NotNull(message = "User birth date cannot be null!")
    @Column(name = "birthdate", nullable = false)
    private LocalDate birthDate;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "pix_info_id", referencedColumnName = "id")
    private PixInfo pixInfo;

    @OneToMany(
        mappedBy = "user",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private Set<GroupMember> groupMemberships = new HashSet<>();

    @OneToMany(
        mappedBy = "requester",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private Set<Friendship> friendRequestsSent = new HashSet<>();

    @OneToMany(
        mappedBy = "receiver",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private Set<Friendship> friendRequestsReceived = new HashSet<>();

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
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
