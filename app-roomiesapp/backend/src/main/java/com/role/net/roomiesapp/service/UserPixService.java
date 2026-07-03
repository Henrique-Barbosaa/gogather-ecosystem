package com.role.net.roomiesapp.service;

import com.role.net.roomiesapp.dto.pix.PixInfoResponse;
import com.role.net.roomiesapp.dto.pix.RegisterPixKeyRequest;
import com.role.net.roomiesapp.entity.PixInfo;
import com.role.net.roomiesapp.entity.User;
import com.role.net.roomiesapp.exception.ResourceNotFoundException;
import com.role.net.roomiesapp.repository.PixInfoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserPixService {

    private final PixInfoRepository pixInfoRepository;

    public UserPixService(PixInfoRepository pixInfoRepository) {
        this.pixInfoRepository = pixInfoRepository;
    }

    @Transactional(readOnly = true)
    public PixInfoResponse getPixInfo(User user) {
        PixInfo pixInfo = pixInfoRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Chave Pix não cadastrada para o usuário."));
        return PixInfoResponse.fromEntity(pixInfo);
    }

    @Transactional
    public PixInfoResponse registerOrUpdatePix(User user, RegisterPixKeyRequest request) {
        PixInfo pixInfo = pixInfoRepository.findByUser(user).orElseGet(() -> {
            PixInfo newPix = new PixInfo();
            newPix.setUser(user);
            return newPix;
        });

        pixInfo.setPixKey(request.pixKey());
        pixInfo.setMerchantName(request.merchantName());
        pixInfo.setMerchantCity(request.merchantCity());

        pixInfo = pixInfoRepository.save(pixInfo);
        return PixInfoResponse.fromEntity(pixInfo);
    }

    @Transactional(readOnly = true)
    public void validateUserHasPixKey(User user) {
        if (user == null) return;
        if (!pixInfoRepository.findByUser(user).isPresent()) {
            throw new IllegalStateException("O usuário contribuidor/credor '" + user.getDisplayName() + "' (username: " + user.getUsername() + ") não possui uma chave Pix cadastrada. É obrigatório cadastrar uma chave Pix antes de adicioná-lo como credor de uma conta.");
        }
    }
}
