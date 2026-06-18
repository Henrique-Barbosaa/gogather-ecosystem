package com.role.net.gogather.dto.user;

import jakarta.validation.constraints.NotBlank;

public record RegisterPixKeyRequest(
    @NotBlank(message = "Pix key is missing.") String pixKey,
    @NotBlank(message = "Merchant name is missing.") String merchantName,
    @NotBlank(message = "Merchant city is missing.") String merchantCity
) {
}
