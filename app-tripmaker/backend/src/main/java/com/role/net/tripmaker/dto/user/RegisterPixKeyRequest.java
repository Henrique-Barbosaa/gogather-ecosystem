package com.role.net.tripmaker.dto.user;

import jakarta.validation.constraints.NotBlank;

public record RegisterPixKeyRequest(
    @NotBlank(message = "Chave Pix é obrigatória.") String pixKey,
    @NotBlank(message = "Nome do beneficiário é obrigatório.") String merchantName,
    @NotBlank(message = "Cidade do beneficiário é obrigatória.") String merchantCity
) {}
