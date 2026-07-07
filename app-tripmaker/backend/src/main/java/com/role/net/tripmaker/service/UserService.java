package com.role.net.tripmaker.service;

import com.role.net.tripmaker.dto.user.RegisterPixKeyRequest;
import com.role.net.tripmaker.entity.PixInfo;
import com.role.net.tripmaker.entity.User;
import com.role.net.tripmaker.exception.ResourceNotFoundException;
import com.role.net.tripmaker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public void registerPix(Long loggedUserId, RegisterPixKeyRequest request) {
        User user = userRepository.findById(loggedUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (user.getPixInfo() == null) {
            user.setPixInfo(
                PixInfo.builder()
                    .pixKey(request.pixKey())
                    .pixType(request.pixType() != null ? request.pixType() : "CPF")
                    .merchantName(request.merchantName())
                    .merchantCity(request.merchantCity())
                    .user(user)
                    .build()
            );
        } else {
            user.getPixInfo().setPixKey(request.pixKey());
            if (request.pixType() != null && !request.pixType().isBlank()) {
                user.getPixInfo().setPixType(request.pixType());
            }
            user.getPixInfo().setMerchantName(request.merchantName());
            user.getPixInfo().setMerchantCity(request.merchantCity());
        }

        userRepository.save(user);
    }

    @Transactional
    public User updateProfile(Long loggedUserId, com.role.net.tripmaker.dto.user.UpdateProfileRequest request) {
        User user = userRepository.findById(loggedUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name());
        }
        if (request.email() != null && !request.email().isBlank()) {
            user.setEmail(request.email());
        }
        if (request.bio() != null) {
            user.setBio(request.bio());
        }
        return userRepository.save(user);
    }
}
