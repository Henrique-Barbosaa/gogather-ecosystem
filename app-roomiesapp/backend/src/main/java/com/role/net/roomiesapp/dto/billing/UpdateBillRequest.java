package com.role.net.roomiesapp.dto.billing;

import com.role.net.roomiesapp.entity.BillType;
import com.role.net.roomiesapp.entity.RecurrenceInterval;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record UpdateBillRequest(
    String title,
    String description,
    Long totalCents,
    BillType billType,
    RecurrenceInterval recurrenceInterval,
    Integer customIntervalDays,
    LocalDate dueDate,
    UUID contributorId,
    List<UUID> participantIds
) {}
