package com.role.net.roomiesapp.dto.billing;

import com.role.net.roomiesapp.entity.BillType;
import com.role.net.roomiesapp.entity.RecurrenceInterval;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateBillRequest(
    @NotBlank(message = "O título da conta é obrigatório") String title,
    String description,
    @NotNull(message = "O valor total é obrigatório") @Min(value = 1, message = "O valor deve ser positivo") Long totalCents,
    @NotNull(message = "O tipo da conta é obrigatório") BillType billType,
    RecurrenceInterval recurrenceInterval,
    Integer customIntervalDays,
    LocalDate dueDate,
    UUID contributorId,
    List<UUID> participantIds
) {}
