package com.role.net.roomiesapp.dto.pix;

import jakarta.validation.constraints.NotBlank;

public record RegisterPixKeyRequest(
    @NotBlank(message = "A chave Pix é obrigatória") String pixKey,
    @NotBlank(message = "O nome do beneficiário é obrigatório") String merchantName,
    @NotBlank(message = "A cidade do beneficiário é obrigatória") String merchantCity
) {}
