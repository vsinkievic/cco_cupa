package lt.creditco.cupa.remote;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for verifying webhook signatures from the payment gateway.
 * Based on the gateway documentation and signature calculation algorithm.
 */
public class SignatureVerifier {

    private static final Logger LOG = LoggerFactory.getLogger(SignatureVerifier.class);

    /**
     * Verify the webhook signature.
     *
     * The signature is calculated as:
     * MD5(success + clientID + orderID.toLowerCase() + MD5(merchantKey) + amount + currency + merchantID)
     *
     * @param paymentReply the payment reply containing all the parameters
     * @param merchantKey the merchant key for signature verification
     * @return true if signature is valid, false otherwise
     */
    public static boolean verifyWebhookSignature(PaymentReply paymentReply, String merchantKey) {
        if (paymentReply == null || merchantKey == null || paymentReply.getSignature() == null) {
            LOG.warn("Cannot verify signature: missing required parameters");
            return false;
        }

        try {
            String calculatedSignature = calculateWebhookSignature(paymentReply, merchantKey);
            boolean isValid = calculatedSignature.equals(paymentReply.getSignature());

            if (!isValid) {
                LOG.warn("Signature verification failed. Expected: {}, Received: {}", calculatedSignature, paymentReply.getSignature());
            } else {
                LOG.debug("Signature verification successful");
            }

            return isValid;
        } catch (Exception e) {
            LOG.error("Error during signature verification", e);
            return false;
        }
    }

    /**
     * Calculate the webhook signature for verification.
     *
     * @param paymentReply the payment reply containing all the parameters
     * @param merchantKey the merchant key
     * @return the calculated signature
     */
    private static String calculateWebhookSignature(PaymentReply paymentReply, String merchantKey) {
        // Create the clear text string according to the gateway documentation
        StringBuilder clearText = new StringBuilder();

        // success
        if (paymentReply.getSuccess() != null) {
            clearText.append(paymentReply.getSuccess());
        }

        // clientID
        if (paymentReply.getClientId() != null) {
            clearText.append(paymentReply.getClientId());
        }

        // orderID (lowercase)
        if (paymentReply.getOrderId() != null) {
            clearText.append(paymentReply.getOrderId().toLowerCase());
        }

        // MD5(merchantKey)
        clearText.append(md5(merchantKey));

        // amount
        if (paymentReply.getAmount() != null) {
            clearText.append(paymentReply.getAmount());
        }

        // currency
        if (paymentReply.getCurrency() != null) {
            clearText.append(paymentReply.getCurrency());
        }

        // merchantID
        if (paymentReply.getMerchantId() != null) {
            clearText.append(paymentReply.getMerchantId());
        }

        // Calculate final MD5
        return md5(clearText.toString());
    }

    /**
     * Calculate MD5 hash of the input string.
     *
     * @param input the input string
     * @return the MD5 hash as a hexadecimal string
     */
    private static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }
}
