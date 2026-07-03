package gogather.framework.billing.dto;

/**
 * Representa o status do ciclo de vida financeiro de uma distribuição de dívida no rateio.
 */
public enum DebtStatus {
    /**
     * Dívida calculada e pendente de pagamento pelo devedor.
     */
    PENDING,

    /**
     * O devedor informou que realizou o pagamento (ex: via Pix) e aguarda confirmação do credor.
     */
    AWAITING_CONFIRMATION,

    /**
     * O credor confirmou o recebimento ou a dívida foi devidamente quitada.
     */
    PAID,

    /**
     * A dívida foi cancelada ou perdoada no rateio.
     */
    CANCELLED
}
