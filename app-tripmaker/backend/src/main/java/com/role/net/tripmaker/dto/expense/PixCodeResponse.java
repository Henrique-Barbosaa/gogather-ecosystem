package com.role.net.tripmaker.dto.expense;

public record PixCodeResponse(
    String pixCopyAndPaste,
    String pixKey,
    String merchantName,
    String merchantCity,
    Long amountInCents
) {}
