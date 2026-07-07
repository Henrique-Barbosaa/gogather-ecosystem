package com.role.net.tripmaker.dto.user;

import jakarta.validation.constraints.NotBlank;

public record RegisterPixKeyRequest(
    @NotBlank(message = "Chave Pix é obrigatória.") String pixKey,
    String pixType,
    String merchantName,
    String merchantCity
) {
    @Override
    public String merchantName() {
        return merchantName != null && !merchantName.isBlank() ? merchantName : "Beneficiario";
    }

    @Override
    public String merchantCity() {
        return merchantCity != null && !merchantCity.isBlank() ? merchantCity : "Cidade";
    }
}
