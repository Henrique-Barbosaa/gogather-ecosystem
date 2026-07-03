package com.role.net.tripmaker.dto.document;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record CreateDocumentRequest(
    @NotBlank(message = "O título é obrigatório.") String title,
    @NotBlank(message = "A URL é obrigatória.") @URL(message = "Deve ser uma URL válida.") String url,
    String category
) {}
