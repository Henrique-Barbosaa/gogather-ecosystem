package gogather.framework.billing.pix;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Componente do framework responsável pela geração de strings do Pix Copia e Cola (BR Code)
 * segundo as especificações EMVCo e do Banco Central do Brasil (BCB), incluindo cálculo de CRC16.
 */
public class PixCodeGenerator {

    /**
     * Gera a string Pix Copia e Cola a partir de um PixRecipient e valor em centavos.
     *
     * @param recipient O beneficiário contendo chave Pix, nome e cidade.
     * @param valueInCents O valor da cobrança em centavos (ou nulo/zero para valor livre).
     * @return String formatada no padrão EMVCo BR Code pronta para pagamento via Pix Copia e Cola ou QR Code.
     */
    public String generatePixCode(PixRecipient recipient, Long valueInCents) {
        if (recipient == null) {
            throw new IllegalArgumentException("PixRecipient não pode ser nulo.");
        }
        return generatePixCode(recipient.getPixKey(), recipient.getMerchantName(), recipient.getMerchantCity(), valueInCents);
    }

    /**
     * Alias em português para generatePixCode.
     */
    public String gerarPixCopiaECola(PixRecipient recipient, Long valueInCents) {
        return generatePixCode(recipient, valueInCents);
    }

    /**
     * Gera a string Pix Copia e Cola a partir dos dados informados diretamente.
     *
     * @param pixKey Chave Pix do beneficiário.
     * @param merchantName Nome do beneficiário.
     * @param merchantCity Cidade do beneficiário.
     * @param valueInCents Valor em centavos.
     * @return String formatada no padrão EMVCo BR Code.
     */
    public String generatePixCode(String pixKey, String merchantName, String merchantCity, Long valueInCents) {
        if (pixKey == null || pixKey.isBlank()) {
            throw new IllegalArgumentException("Chave Pix não pode ser nula ou vazia.");
        }
        if (merchantName == null || merchantName.isBlank()) {
            throw new IllegalArgumentException("Nome do beneficiário não pode ser nulo ou vazio.");
        }
        if (merchantCity == null || merchantCity.isBlank()) {
            throw new IllegalArgumentException("Cidade do beneficiário não pode ser nula ou vazia.");
        }

        StringBuilder pix = new StringBuilder();

        // 00: Payload Format Indicator
        pix.append("000201");

        // 26: Merchant Account Information (Específico do Pix)
        String gui = "0014br.gov.bcb.pix";
        String cleanKey = formatPixKey(pixKey);
        String formattedKey = "01" + String.format("%02d", cleanKey.length()) + cleanKey;
        String merchantAccount = gui + formattedKey;
        pix.append("26").append(String.format("%02d", merchantAccount.length())).append(merchantAccount);

        // 52: Merchant Category Code (0000 para geral)
        pix.append("52040000");

        // 53: Transaction Currency (986 para Real)
        pix.append("5303986");

        String formattedValue = formatCents(valueInCents);

        // 54: Transaction Amount (Opcional)
        if (!formattedValue.isBlank()) {
            pix.append("54").append(String.format("%02d", formattedValue.length())).append(formattedValue);
        }

        // 58: Country Code
        pix.append("5802BR");

        String formattedReceiverName = formatName(merchantName);

        // 59: Merchant Name
        pix.append("59").append(String.format("%02d", formattedReceiverName.length())).append(formattedReceiverName);

        String formattedCityName = formatText(merchantCity, 15);

        // 60: Merchant City
        pix.append("60").append(String.format("%02d", formattedCityName.length())).append(formattedCityName);

        // 62: Additional Data Field (TXID - use *** para gerar no app do pagador)
        String txid = "0503***";
        pix.append("62").append(String.format("%02d", txid.length())).append(txid);

        // 63: CRC16 (A tag é '63', o tamanho é '04', e o valor vem depois)
        pix.append("6304");

        String crc = calcularCRC16(pix.toString());
        return pix.append(crc).toString();
    }

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

    public static String formatPixKey(String key) {
        if (key == null || key.isBlank()) return "";
        key = key.trim();
        if (key.contains("@")) {
            return key.toLowerCase();
        }

        boolean isPhone = false;
        if (key.startsWith("+") || key.contains("(") || key.contains(")") || key.contains(" ") || key.contains("-")) {
            // Se contém caracteres típicos de telefone (exceto pontos de CPF/CNPJ)
            String digitsOnly = key.replaceAll("[^0-9]", "");
            if (digitsOnly.length() >= 10 && digitsOnly.length() <= 13) {
                // CPFs normalmente não usam parênteses, espaço ou têm esse tamanho de dígitos sem pontos
                if (!key.contains(".")) {
                    isPhone = true;
                }
            }
        }

        if (!isPhone) {
            String digitsOnly = key.replaceAll("[^0-9]", "");
            if (digitsOnly.length() == 10) {
                isPhone = true;
            } else if (digitsOnly.length() == 11) {
                try {
                    int ddd = Integer.parseInt(digitsOnly.substring(0, 2));
                    char thirdDigit = digitsOnly.charAt(2);
                    if (ddd >= 11 && ddd <= 99 && thirdDigit == '9') {
                        isPhone = true;
                    }
                } catch (Exception ignored) {}
            } else if ((digitsOnly.length() == 12 || digitsOnly.length() == 13) && digitsOnly.startsWith("55")) {
                isPhone = true;
            }
        }

        if (isPhone) {
            String digits = key.replaceAll("[^0-9]", "");
            if (digits.startsWith("55") && (digits.length() == 12 || digits.length() == 13)) {
                return "+" + digits;
            }
            return "+55" + digits;
        }

        // Caso padrão para CPF, CNPJ ou chave aleatória (EVP)
        return key.replaceAll("[\\s\\.\\-\\(\\)\\/]", "");
    }

    public static String formatName(String name) {
        if (name == null || name.isBlank()) {
            return "BENEFICIARIO";
        }
        String normalName = Normalizer.normalize(name, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String semAcentos = pattern.matcher(normalName).replaceAll("");
        
        String cleanName = semAcentos.replaceAll("[^a-zA-Z0-9 ]", "").toUpperCase().trim();
        
        String[] words = cleanName.split("\\s+");
        if (words.length >= 2) {
            cleanName = words[0] + " " + words[1];
        } else if (words.length == 1) {
            cleanName = words[0];
        } else {
            cleanName = "BENEFICIARIO";
        }

        if (cleanName.length() > 25) {
            return cleanName.substring(0, 25).trim();
        }
        return cleanName;
    }

    public static String formatText(String text, int maxLength) {
        if (text == null || text.isBlank()) return "BRASILIA";

        String nfdNormalizedString = Normalizer.normalize(text, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String semAcentos = pattern.matcher(nfdNormalizedString).replaceAll("");
        String apenasLetrasENumeros = semAcentos.replaceAll("[^a-zA-Z0-9 ]", "");

        String resultado = apenasLetrasENumeros.toUpperCase().trim();

        if (resultado.isEmpty()) {
            resultado = "BRASILIA";
        }

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
