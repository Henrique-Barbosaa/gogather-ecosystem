package com.role.net.gogather.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@SequenceGenerator(name = "id_generator", sequenceName = "seq_refresh_token", allocationSize = 1)
@Table(name = "refresh_tokens")
public class RefreshToken extends BaseEntity {

    @NotEmpty(message = "RefreshToken token cannot be null!")
    @Column(nullable = false, unique = true)
    private String token;

    @NotNull(message = "RefreshToken expireDate cannot be null!")
    @Column(nullable = false)
    private Instant expireDate;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}
