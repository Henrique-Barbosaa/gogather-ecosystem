package com.role.net.tripmaker.dto.friend;

import jakarta.validation.constraints.NotBlank;

public record FriendRequestDto(
    @NotBlank(message = "E-mail ou nome de usuário do amigo é obrigatório!")
    String email
) {}
