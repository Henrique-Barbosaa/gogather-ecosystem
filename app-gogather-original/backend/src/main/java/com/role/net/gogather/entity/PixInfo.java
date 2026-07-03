package com.role.net.gogather.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import gogather.framework.billing.pix.PixRecipient;

@Table(name = "pix_info")
@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PixInfo extends BaseEntity implements PixRecipient {

    @NotBlank(message = "Pix info must have a pix key.")
    @Column(nullable = false, unique = true)
    private String pixKey;

    @NotBlank(message = "Pix info must have a merchant name.")
    @Column(nullable = false)
    private String merchantName;

    @NotBlank(message = "Pix info must have a merchant city.")
    @Column(nullable = false)
    private String merchantCity;

    @OneToOne(mappedBy = "pixInfo")
    private User user;

}
