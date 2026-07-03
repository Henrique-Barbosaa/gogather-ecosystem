package com.role.net.roomiesapp.entity;

import gogather.framework.billing.pix.PixRecipient;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_pix_info")
public class PixInfo extends BaseEntity implements PixRecipient {

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "pix_key", nullable = false, length = 150)
    private String pixKey;

    @Column(name = "merchant_name", nullable = false, length = 100)
    private String merchantName;

    @Column(name = "merchant_city", nullable = false, length = 100)
    private String merchantCity;
}
