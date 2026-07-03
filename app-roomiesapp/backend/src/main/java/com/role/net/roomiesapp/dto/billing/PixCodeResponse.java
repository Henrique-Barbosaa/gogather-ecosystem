package com.role.net.roomiesapp.dto.billing;

import java.util.UUID;

public record PixCodeResponse(
    UUID debtId,
    String pixCopiaECola,
    String recipientName,
    String recipientCity,
    Long amountInCents
) {}
