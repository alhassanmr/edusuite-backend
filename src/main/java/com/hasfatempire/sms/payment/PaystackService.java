package com.hasfatempire.sms.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hasfatempire.sms.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Paystack integration — supports Ghana (GHS + mobile money: MTN, Telecel,
 * AirtelTigo), Nigeria, and international cards.
 * Docs: https://paystack.com/docs/api
 *
 * Config: app.payments.paystack.secret-key (sk_test_... / sk_live_...)
 */
@Service
public class PaystackService {

    private static final String BASE_URL = "https://api.paystack.co";

    @Value("${app.payments.paystack.secret-key:}")
    private String secretKey;

    @Value("${app.payments.paystack.callback-url:http://localhost:5173/payment/callback}")
    private String callbackUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private HttpHeaders authHeaders() {
        if (secretKey == null || secretKey.isBlank()) {
            throw new BadRequestException("Paystack is not configured. Set PAYSTACK_SECRET_KEY.");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(secretKey);
        return headers;
    }

    /**
     * Initialize a transaction. Amount is in major units (e.g. GHS 150.00);
     * Paystack expects minor units (pesewas/kobo), so we multiply by 100.
     * Returns map with authorization_url, access_code, reference.
     */
    public Map<String, String> initialize(String email, BigDecimal amount, String currency, String reference) {
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("amount", amount.multiply(BigDecimal.valueOf(100)).toBigInteger().toString());
        body.put("currency", currency);
        body.put("reference", reference);
        body.put("callback_url", callbackUrl);
        // Enable GH mobile money + card channels
        body.put("channels", new String[]{"card", "mobile_money", "bank"});

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    BASE_URL + "/transaction/initialize",
                    new HttpEntity<>(body, authHeaders()), String.class);

            JsonNode json = objectMapper.readTree(response.getBody());
            if (!json.path("status").asBoolean()) {
                throw new BadRequestException("Paystack init failed: " + json.path("message").asText());
            }
            JsonNode data = json.path("data");
            Map<String, String> result = new HashMap<>();
            result.put("authorizationUrl", data.path("authorization_url").asText());
            result.put("accessCode", data.path("access_code").asText());
            result.put("reference", data.path("reference").asText());
            return result;
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Could not reach Paystack: " + e.getMessage());
        }
    }

    /**
     * Verify a transaction by reference. Returns the "data" node with
     * status ("success"/"failed"/"abandoned"), amount (minor units), channel, currency.
     */
    public JsonNode verify(String reference) {
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    BASE_URL + "/transaction/verify/" + reference,
                    HttpMethod.GET, new HttpEntity<>(authHeaders()), String.class);
            JsonNode json = objectMapper.readTree(response.getBody());
            if (!json.path("status").asBoolean()) {
                throw new BadRequestException("Paystack verify failed: " + json.path("message").asText());
            }
            return json.path("data");
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Could not verify with Paystack: " + e.getMessage());
        }
    }

    /**
     * List banks & mobile money providers for a country (for settlement setup).
     * currency GHS returns Ghanaian banks + MoMo (type=mobile_money).
     */
    public JsonNode listBanks(String currency) {
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    BASE_URL + "/bank?currency=" + currency,
                    HttpMethod.GET, new HttpEntity<>(authHeaders()), String.class);
            JsonNode json = objectMapper.readTree(response.getBody());
            if (!json.path("status").asBoolean()) {
                throw new BadRequestException("Could not fetch banks: " + json.path("message").asText());
            }
            return json.path("data");
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Could not reach Paystack: " + e.getMessage());
        }
    }

    /**
     * Create a subaccount for a school. percentageCharge is the share the
     * PLATFORM (main account) keeps on each split payment; the remainder is
     * settled directly to the school's bank/MoMo account by Paystack.
     * Returns the subaccount_code (ACCT_xxx).
     */
    public String createSubaccount(String businessName, String bankCode,
                                    String accountNumber, double platformPercentageCharge) {
        Map<String, Object> body = new HashMap<>();
        body.put("business_name", businessName);
        body.put("settlement_bank", bankCode);
        body.put("account_number", accountNumber);
        body.put("percentage_charge", platformPercentageCharge);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    BASE_URL + "/subaccount",
                    new HttpEntity<>(body, authHeaders()), String.class);
            JsonNode json = objectMapper.readTree(response.getBody());
            if (!json.path("status").asBoolean()) {
                throw new BadRequestException("Subaccount creation failed: " + json.path("message").asText());
            }
            return json.path("data").path("subaccount_code").asText();
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Could not reach Paystack: " + e.getMessage());
        }
    }

    /**
     * Initialize a SPLIT transaction: parent's payment is split automatically —
     * the school's share goes directly to their subaccount (their own bank/MoMo),
     * the platform commission stays on the main account. The school bears the
     * Paystack processing fee (deducted from their share).
     */
    public Map<String, String> initializeSplit(String email, BigDecimal amount, String currency,
                                                String reference, String subaccountCode) {
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("amount", amount.multiply(BigDecimal.valueOf(100)).toBigInteger().toString());
        body.put("currency", currency);
        body.put("reference", reference);
        body.put("callback_url", callbackUrl);
        body.put("channels", new String[]{"card", "mobile_money", "bank"});
        body.put("subaccount", subaccountCode);
        body.put("bearer", "subaccount"); // school bears Paystack's processing fee

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    BASE_URL + "/transaction/initialize",
                    new HttpEntity<>(body, authHeaders()), String.class);
            JsonNode json = objectMapper.readTree(response.getBody());
            if (!json.path("status").asBoolean()) {
                throw new BadRequestException("Paystack init failed: " + json.path("message").asText());
            }
            JsonNode data = json.path("data");
            Map<String, String> result = new HashMap<>();
            result.put("authorizationUrl", data.path("authorization_url").asText());
            result.put("accessCode", data.path("access_code").asText());
            result.put("reference", data.path("reference").asText());
            return result;
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Could not reach Paystack: " + e.getMessage());
        }
    }

    /**
     * Validate webhook signature (x-paystack-signature header):
     * HMAC-SHA512 of raw body using the secret key.
     */
    public boolean isValidWebhookSignature(String rawBody, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(secretKey.getBytes(), "HmacSHA512"));
            byte[] hash = mac.doFinal(rawBody.getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString().equals(signature);
        } catch (Exception e) {
            return false;
        }
    }
}
