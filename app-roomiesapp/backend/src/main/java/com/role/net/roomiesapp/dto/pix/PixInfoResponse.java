package com.role.net.roomiesapp.dto.pix;

import com.role.net.roomiesapp.entity.PixInfo;
import java.util.UUID;

public record PixInfoResponse(
    UUID externalId,
    String pixKey,
    String merchantName,
    String merchantCity
) {
    public static PixInfoResponse fromEntity(PixInfo pixInfo) {
        if (pixInfo == null) return null;
        return new PixInfoResponse(
            pixInfo.getExternalId(),
            pixInfo.getPixKey(),
            pixInfo.getMerchantName(),
            pixInfo.getMerchantCity()
        );
    }
}
