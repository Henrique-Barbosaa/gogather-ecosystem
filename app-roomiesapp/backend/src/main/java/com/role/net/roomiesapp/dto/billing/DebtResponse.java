package com.role.net.roomiesapp.dto.billing;

import com.role.net.roomiesapp.dto.user.UserResponse;
import com.role.net.roomiesapp.entity.HouseDebt;
import gogather.framework.billing.dto.DebtStatus;
import java.util.UUID;

public record DebtResponse(
    UUID externalId,
    UUID billId,
    UserResponse debtor,
    UserResponse creditor,
    Long amountInCents,
    DebtStatus status,
    String pixCopiaECola
) {
    public static DebtResponse fromEntity(HouseDebt debt) {
        if (debt == null) return null;
        return new DebtResponse(
            debt.getExternalId(),
            debt.getBill() != null ? debt.getBill().getExternalId() : null,
            debt.getDebtor() != null ? UserResponse.from(debt.getDebtor()) : null,
            debt.getCreditor() != null ? UserResponse.from(debt.getCreditor()) : null,
            debt.getAmountInCents(),
            debt.getStatus(),
            debt.getPixCodeCache()
        );
    }
}
