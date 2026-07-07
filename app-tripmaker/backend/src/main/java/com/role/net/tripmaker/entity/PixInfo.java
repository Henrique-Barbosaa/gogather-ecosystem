package com.role.net.tripmaker.entity;

import gogather.framework.billing.pix.PixRecipient;
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

@Entity
@Table(name = "pix_info")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PixInfo extends BaseEntity implements PixRecipient {

    @NotBlank(message = "Chave Pix é obrigatória.")
    @Column(nullable = false, unique = true)
    private String pixKey;

    @Column(name = "pix_type")
    private String pixType;

    @NotBlank(message = "Nome do beneficiário é obrigatório.")
    @Column(nullable = false)
    private String merchantName;

    @NotBlank(message = "Cidade do beneficiário é obrigatória.")
    @Column(nullable = false)
    private String merchantCity;

    @OneToOne(mappedBy = "pixInfo")
    private User user;

    @Override
    public String getPixKey() {
        return this.pixKey;
    }

    @Override
    public String getMerchantName() {
        return this.merchantName;
    }

    @Override
    public String getMerchantCity() {
        return this.merchantCity;
    }
}
