package lt.creditco.cupa.remote;

import io.micrometer.common.util.StringUtils;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
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
        log.info("placeTransaction: {}", request);

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
                "Received error status: {} for orderId: {}, error: {}",
                response.getStatusCode(),
                request.getOrderId(),
                getErrorMessage(response.getBody())
            );
        }

        return response.getBody();
    }

    public GatewayResponse<PaymentReply> queryTransaction(String orderId, GatewayConfig config) {
        log.info("queryTransaction: {}", orderId);

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
                "Received error status: {} for orderId: {}, error: {}",
                response.getStatusCode(),
                orderId,
                getErrorMessage(response.getBody())
            );
        }

        return response.getBody();
    }

    public GatewayResponse<ClientDetails> getClientDetails(String clientId, GatewayConfig config) {
        log.info("getClientDetails: {}", clientId);

        String url = UriComponentsBuilder.fromUriString(config.getBaseUrl())
            .path("/merchants/{merchantMid}/clients/{clientId}")
            .buildAndExpand(config.getMerchantMid(), clientId)
            .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("x-api-key", config.getApiKey());

        log.debug("Request to URL: {}", url);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ParameterizedTypeReference<GatewayResponse<ClientDetails>> responseType = new ParameterizedTypeReference<>() {};
        ResponseEntity<GatewayResponse<ClientDetails>> response = restTemplate.exchange(url, HttpMethod.GET, entity, responseType);

        if (response.getStatusCode().isError()) {
            log.error(
                "Received error status: {} for clientId: {}, error: {}",
                response.getStatusCode(),
                clientId,
                getErrorMessage(response.getBody())
            );
        }

        return response.getBody();
    }

    public GatewayResponse<List<ClientDetails>> getClientList(String nextClientId, GatewayConfig config) {
        log.info("getClientList: nextClientId:{}", nextClientId);

        String url = null;
        if (StringUtils.isNotBlank(nextClientId)) {
            url = UriComponentsBuilder.fromUriString(config.getBaseUrl())
                .path("/merchants/{merchantMid}/clients")
                .queryParam("next", nextClientId)
                .buildAndExpand(config.getMerchantMid())
                .toUriString();
        } else {
            url = UriComponentsBuilder.fromUriString(config.getBaseUrl())
                .path("/merchants/{merchantMid}/clients")
                .buildAndExpand(config.getMerchantMid())
                .toUriString();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("x-api-key", config.getApiKey());

        log.debug("Request to URL: {}", url);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ParameterizedTypeReference<GatewayResponse<List<ClientDetails>>> responseType = new ParameterizedTypeReference<>() {};
        ResponseEntity<GatewayResponse<List<ClientDetails>>> response = restTemplate.exchange(url, HttpMethod.GET, entity, responseType);

        if (response.getStatusCode().isError()) {
            log.error(
                "Received error status: {} for nextClientId: {}, error: {}",
                response.getStatusCode(),
                nextClientId,
                getErrorMessage(response.getBody())
            );
        }

        return response.getBody();
    }

    private String getErrorMessage(GatewayResponse<?> responseBody) {
        return Optional.ofNullable(responseBody)
            .map(GatewayResponse::getResponse)
            .map(message ->
                String.format("message: %s, details: %s, reason: %s", message.getMessage(), message.getDetail(), message.getReason())
            )
            .orElse("Unknown error");
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
