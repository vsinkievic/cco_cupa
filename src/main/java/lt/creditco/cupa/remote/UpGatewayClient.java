package lt.creditco.cupa.remote;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Slf4j
public class UpGatewayClient {

    private final RestTemplate restTemplate;
    private static final String SIGNATURE_VERSION = "1.0";

    public UpGatewayClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public GatewayResponse<PaymentReply> placeTransaction(PaymentRequest request, GatewayConfig config) {
        request.setAmount(request.getAmount().stripTrailingZeros());

        String signature = calculateSignature(request, config);
        request.setSignature(signature);
        request.setSignatureVersion(SIGNATURE_VERSION);

        String url = UriComponentsBuilder.fromUriString(config.getBaseUrl())
            .path("/merchants/{merchantMid}/transactions/")
            .buildAndExpand(config.getMerchantMid())
            .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("x-api-key", config.getApiKey());

        HttpEntity<PaymentRequest> entity = new HttpEntity<>(request, headers);

        log.debug("Request to URL: {}", url);
        log.debug("Request Headers: {}", headers);

        ParameterizedTypeReference<GatewayResponse<PaymentReply>> responseType = new ParameterizedTypeReference<>() {};

        ResponseEntity<GatewayResponse<PaymentReply>> response = restTemplate.exchange(url, HttpMethod.POST, entity, responseType);

        if (response.getStatusCode().isError()) {
            log.error(
                "Received error status: {} for orderId: {}, message: {}, details: {}, reason: {}",
                response.getStatusCode(),
                request.getOrderId(),
                response.getBody().getResponse().getMessage(),
                response.getBody().getResponse().getDetail(),
                response.getBody().getResponse().getReason()
            );
            // You can throw a custom exception here or handle the error as needed
        }

        return response.getBody();
    }

    public GatewayResponse<PaymentReply> queryTransaction(String orderId, GatewayConfig config) {
        String url = UriComponentsBuilder.fromUriString(config.getBaseUrl())
            .path("/merchants/{merchantMid}/transactions/{orderId}")
            .buildAndExpand(config.getMerchantMid(), orderId)
            .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("x-api-key", config.getApiKey());

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        log.debug("Request to URL: {}", url);

        ParameterizedTypeReference<GatewayResponse<PaymentReply>> responseType = new ParameterizedTypeReference<>() {};
        ResponseEntity<GatewayResponse<PaymentReply>> response = restTemplate.exchange(url, HttpMethod.GET, entity, responseType);

        if (response.getStatusCode().isError()) {
            log.error(
                "Received error status: {} for orderId: {}, message: {}, details: {}, reason: {}",
                response.getStatusCode(),
                orderId,
                response.getBody().getResponse().getMessage(),
                response.getBody().getResponse().getDetail(),
                response.getBody().getResponse().getReason()
            );
            // You can throw a custom exception here or handle the error as needed
        }

        return response.getBody();
    }

    private String calculateSignature(PaymentRequest request, GatewayConfig config) {
        String merchantKeyMd5 = md5(config.getMerchantKey());

        StringBuilder clearText = new StringBuilder();
        if (request.getClientId() != null) {
            clearText.append(request.getClientId());
        }
        clearText.append(request.getOrderId().toLowerCase());
        clearText.append(merchantKeyMd5);
        clearText.append(request.getAmount());
        clearText.append(request.getCurrency());
        if (request.getReplyUrl() != null) {
            clearText.append(request.getReplyUrl());
        }
        if (request.getBackofficeUrl() != null) {
            clearText.append(request.getBackofficeUrl());
        }

        return md5(clearText.toString());
    }

    private String md5(String input) {
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
            throw new RuntimeException(e);
        }
    }
}
