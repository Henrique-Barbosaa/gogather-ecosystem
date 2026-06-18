package com.role.net.gogather.service;

import org.springframework.stereotype.Service;

import com.role.net.gogather.utils.PixUtils;

@Service
public class PixService {

    public String gerarPixCopiaECola(String chave, String receiverName, String city, Long valueInCents) {
        StringBuilder pix = new StringBuilder();

        // 00: Payload Format Indicator
        pix.append("000201");

        // 26: Merchant Account Information (Específico do Pix)
        String gui = "0014br.gov.bcb.pix";
        String formattedKey = "01" + String.format("%02d", chave.length()) + chave;
        String merchantAccount = gui + formattedKey;
        pix.append("26").append(String.format("%02d", merchantAccount.length())).append(merchantAccount);

        // 52: Merchant Category Code (0000 para geral)
        pix.append("52040000");

        // 53: Transaction Currency (986 para Real)
        pix.append("5303986");

        String formattedValue = PixUtils.formatCents(valueInCents);

        // 54: Transaction Amount (Opcional)
        if (!formattedValue.isBlank()) {
            pix.append("54").append(String.format("%02d", formattedValue.length())).append(formattedValue);
        }

        // 58: Country Code
        pix.append("5802BR");

        String formattedReceiverName = PixUtils.formatName(receiverName);

        // 59: Merchant Name
        pix.append("59").append(String.format("%02d", formattedReceiverName.length())).append(formattedReceiverName);

        String formattedCityName = PixUtils.formatText(city, 15);

        // 60: Merchant City
        pix.append("60").append(String.format("%02d", formattedCityName.length())).append(formattedCityName);

        // 62: Additional Data Field (TXID - use *** para gerar no app do pagador)
        String txid = "0503***";
        pix.append("62").append(String.format("%02d", txid.length())).append(txid);

        // 63: CRC16 (A tag é '63', o tamanho é '04', e o valor vem depois)
        pix.append("6304");

        String crc = PixUtils.calcularCRC16(pix.toString());
        return pix.append(crc).toString();
    }

}
