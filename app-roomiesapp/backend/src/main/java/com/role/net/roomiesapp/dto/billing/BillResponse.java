package com.role.net.roomiesapp.dto.billing;

import com.role.net.roomiesapp.dto.user.UserResponse;
import com.role.net.roomiesapp.entity.BillType;
import com.role.net.roomiesapp.entity.HouseBill;
import com.role.net.roomiesapp.entity.RecurrenceInterval;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record BillResponse(
    UUID externalId,
    UUID groupId,
    String title,
    String description,
    Long totalCents,
    BillType billType,
    RecurrenceInterval recurrenceInterval,
    Integer customIntervalDays,
    LocalDate dueDate,
    UserResponse contributor,
    List<UserResponse> participants
) {
    public static BillResponse fromEntity(HouseBill bill) {
        if (bill == null) return null;
        return new BillResponse(
            bill.getExternalId(),
            bill.getGroup() != null ? bill.getGroup().getExternalId() : null,
            bill.getTitle(),
            bill.getDescription(),
            bill.getTotalCents(),
            bill.getBillType(),
            bill.getRecurrenceInterval(),
            bill.getCustomIntervalDays(),
            bill.getDueDate(),
            bill.getContributor() != null ? UserResponse.from(bill.getContributor()) : null,
            bill.getParticipants() != null ? bill.getParticipants().stream().map(UserResponse::from).collect(Collectors.toList()) : List.of()
        );
    }
}
