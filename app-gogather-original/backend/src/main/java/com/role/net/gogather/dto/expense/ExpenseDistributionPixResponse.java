package com.role.net.gogather.dto.expense;

import java.util.UUID;

import gogather.framework.group.jpa.domain.GroupMember;
import com.role.net.gogather.entity.User;

public record ExpenseDistributionPixResponse(
    UUID receiverMemberExternalId,
    String receiverMerchantName,
    String pixCopyAndPaste
) {
    public static ExpenseDistributionPixResponse from(GroupMember groupMember, String pixcnp) {
        User user = (User) groupMember.getUser();
        return new ExpenseDistributionPixResponse(
            groupMember.getExternalId(),
            user.getPixInfo().getMerchantName(),
            pixcnp
        );
    }
}
