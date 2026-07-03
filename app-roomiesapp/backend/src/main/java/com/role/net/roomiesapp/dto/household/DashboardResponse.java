package com.role.net.roomiesapp.dto.household;

import com.role.net.roomiesapp.dto.chore.ChoreResponse;
import com.role.net.roomiesapp.dto.shopping.ShoppingItemResponse;
import java.util.List;

public record DashboardResponse(
    Long groupId,
    String groupName,
    Long myTotalDebtCents,
    Long totalOwedToMeCents,
    List<ChoreResponse> pendingChoresAssignedToMe,
    List<ShoppingItemResponse> pendingShoppingItems
) {}
