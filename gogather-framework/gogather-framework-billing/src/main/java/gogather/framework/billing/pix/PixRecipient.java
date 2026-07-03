package gogather.framework.billing.pix;

/**
 * Interface de base fornecida pelo framework para representar o beneficiário de uma cobrança Pix.
 * As aplicações consumidoras devem implementar esta interface em suas entidades ou DTOs
 * para garantir que possuem todas as informações necessárias para a geração do Pix Copia e Cola (BR Code).
 */
public interface PixRecipient {
    String getPixKey();
    String getMerchantName();
    String getMerchantCity();
}
