package com.role.net.gogather.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class PixUtils {
    public static String calcularCRC16(String payload) {
        int crc = 0xFFFF;
        int polynomial = 0x1021;

        for (byte b : payload.getBytes()) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) crc ^= polynomial;
            }
        }
        return String.format("%04X", crc & 0xFFFF).toUpperCase();
    }

    public static String formatName(String name) {
        String normalName = java.text.Normalizer.normalize(name, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String cleanName = normalName.replaceAll("[^a-zA-Z ]", "");

        if (cleanName.length() > 25) {
            return cleanName.substring(0, 25).trim();
        }
        return cleanName.trim();
    }

    public static String formatText(String text, int maxLength) {
        if (text == null) return "";

        String nfdNormalizedString = Normalizer.normalize(text, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String semAcentos = pattern.matcher(nfdNormalizedString).replaceAll("");
        String apenasLetrasENumeros = semAcentos.replaceAll("[^a-zA-Z0-9 ]", "");

        String resultado = apenasLetrasENumeros.toUpperCase().trim();

        if (resultado.length() > maxLength) {
            return resultado.substring(0, maxLength).trim();
        }

        return resultado;
    }

    public static String formatCents(Long cents) {
        if (cents == null || cents <= 0) {
            return "";
        }

        BigDecimal valorEmReais = BigDecimal.valueOf(cents)
                                            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat df = new DecimalFormat("0.00", symbols);

        return df.format(valorEmReais);
    }
}
