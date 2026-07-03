package gogather.framework.billing.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DebtStatusTest {

    @Test
    public void testAllStatusValuesExist() {
        assertNotNull(DebtStatus.valueOf("PENDING"));
        assertNotNull(DebtStatus.valueOf("AWAITING_CONFIRMATION"));
        assertNotNull(DebtStatus.valueOf("PAID"));
        assertNotNull(DebtStatus.valueOf("CANCELLED"));
        assertEquals(4, DebtStatus.values().length);
    }
}
