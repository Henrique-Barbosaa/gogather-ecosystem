package gogather.framework.billing.pix;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PixCodeGeneratorTest {

    private static class MockPixRecipient implements PixRecipient {
        private final String key;
        private final String name;
        private final String city;

        public MockPixRecipient(String key, String name, String city) {
            this.key = key;
            this.name = name;
            this.city = city;
        }

        @Override public String getPixKey() { return key; }
        @Override public String getMerchantName() { return name; }
        @Override public String getMerchantCity() { return city; }
    }

    @Test
    public void testGeneratePixCodeWithValidRecipient() {
        PixCodeGenerator generator = new PixCodeGenerator();
        PixRecipient recipient = new MockPixRecipient("12345678900", "João da Silva", "São Paulo");
        
        String pixCode = generator.generatePixCode(recipient, 15050L); // 150.50 BRL

        assertNotNull(pixCode);
        assertTrue(pixCode.startsWith("000201"));
        assertTrue(pixCode.contains("br.gov.bcb.pix"));
        assertTrue(pixCode.contains("5303986")); // BRL Currency
        assertTrue(pixCode.contains("5406150.50")); // Amount
        assertTrue(pixCode.contains("5802BR")); // Country BR
        assertTrue(pixCode.contains("JOAO DA")); // Name uppercase without accent (first 2 names)
        assertTrue(pixCode.contains("SAO PAULO")); // City upper without accent
        assertTrue(pixCode.contains("6304")); // CRC tag
    }

    @Test
    public void testGerarPixCopiaEColaAlias() {
        PixCodeGenerator generator = new PixCodeGenerator();
        PixRecipient recipient = new MockPixRecipient("email@teste.com", "Maria Souza", "Rio de Janeiro");
        
        String pixCode = generator.gerarPixCopiaECola(recipient, 5000L); // 50.00 BRL

        assertNotNull(pixCode);
        assertTrue(pixCode.contains("540550.00"));
        assertTrue(pixCode.contains("6304"));
    }

    @Test
    public void testGeneratePixCodeWithoutAmount() {
        PixCodeGenerator generator = new PixCodeGenerator();
        PixRecipient recipient = new MockPixRecipient("12345678900", "Carlos", "Curitiba");
        
        String pixCode = generator.generatePixCode(recipient, null); // Free amount

        assertNotNull(pixCode);
        assertFalse(pixCode.contains("540")); // No amount tag
        assertTrue(pixCode.contains("6304"));
    }

    @Test
    public void testNullRecipientThrowsException() {
        PixCodeGenerator generator = new PixCodeGenerator();
        assertThrows(IllegalArgumentException.class, () -> generator.generatePixCode((PixRecipient) null, 100L));
    }

    @Test
    public void testEmptyPixKeyThrowsException() {
        PixCodeGenerator generator = new PixCodeGenerator();
        PixRecipient recipient = new MockPixRecipient("", "Name", "City");
        assertThrows(IllegalArgumentException.class, () -> generator.generatePixCode(recipient, 100L));
    }

    @Test
    public void testCrc16Calculation() {
        String crc = PixCodeGenerator.calcularCRC16("00020126330014br.gov.bcb.pix01111234567890052040000530398654041.005802BR5904TEST6004TEST62070503***6304");
        assertEquals(4, crc.length());
        // Verify hexadecimal characters
        assertTrue(crc.matches("^[0-9A-F]{4}$"));
    }

    @Test
    public void testPhoneKeyFormatting() {
        assertEquals("+5511999999999", PixCodeGenerator.formatPixKey("+5511999999999"));
        assertEquals("+5511999999999", PixCodeGenerator.formatPixKey("5511999999999"));
        assertEquals("+5511999999999", PixCodeGenerator.formatPixKey("(11) 99999-9999"));
        assertEquals("+5511999999999", PixCodeGenerator.formatPixKey("11999999999"));
        assertEquals("+551133334444", PixCodeGenerator.formatPixKey("1133334444")); // Landline
        assertEquals("12345678900", PixCodeGenerator.formatPixKey("123.456.789-00")); // CPF (should not be treated as phone)
    }
}
