package com.role.net.roomiesapp.dto.chore;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record CreateChoreRequest(
    @NotBlank(message = "O título da tarefa é obrigatório") String title,
    String description,
    LocalDate dueDate
) {}
