package com.role.net.tripmaker.dto.expense;

import com.role.net.tripmaker.entity.TripExpenseContribution;

public record ContributionResponse(
    Long userId,
    String username,
    String displayName,
    Long amountInCents
) {
    public static ContributionResponse from(TripExpenseContribution c) {
        return new ContributionResponse(
            c.getPayer().getId(),
            c.getPayer().getUsername(),
            c.getPayer().getDisplayName(),
            c.getAmountInCents()
        );
    }
}
