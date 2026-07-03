package com.role.net.roomiesapp.dto.notice;

import jakarta.validation.constraints.NotBlank;

public record CreateNoticeRequest(
    @NotBlank(message = "O título é obrigatório.") String title,
    @NotBlank(message = "O conteúdo é obrigatório.") String content
) {}
