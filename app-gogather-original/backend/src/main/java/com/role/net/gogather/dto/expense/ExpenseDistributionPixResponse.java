package com.role.net.gogather.dto.expense;

import java.util.UUID;

import com.role.net.gogather.entity.GroupMember;

public record ExpenseDistributionPixResponse(
    UUID receiverMemberExternalId,
    String receiverMerchantName,
    String pixCopyAndPaste
) {
    public static ExpenseDistributionPixResponse from(GroupMember groupMember, String pixcnp) {
        return new ExpenseDistributionPixResponse(
            groupMember.getExternalId(),
            groupMember.getUser().getPixInfo().getMerchantName(),
            pixcnp
        );
    }
}
