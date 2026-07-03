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
                    .merchantName(request.merchantName())
                    .merchantCity(request.merchantCity())
                    .user(user)
                    .build()
            );
        } else {
            user.getPixInfo().setPixKey(request.pixKey());
            user.getPixInfo().setMerchantName(request.merchantName());
            user.getPixInfo().setMerchantCity(request.merchantCity());
        }

        userRepository.save(user);
    }
}
