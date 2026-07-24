package com.hasfatempire.sms.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Arkesel SMS provider — popular in Ghana, supports MTN / Telecel / AirtelTigo.
 * Docs: https://developers.arkesel.com
 * Enable with: app.notifications.sms.provider=arkesel + app.notifications.sms.api-key
 */
@Component
@ConditionalOnProperty(name = "app.notifications.sms.provider", havingValue = "arkesel")
public class ArkeselSmsProvider implements SmsProvider {

    @Value("${app.notifications.sms.api-key}")
    private String apiKey;

    @Value("${app.notifications.sms.sender-id:EduSuite}")
    private String senderId;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String send(String phoneNumber, String message) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        Map<String, Object> body = Map.of(
                "sender", senderId,
                "message", message,
                "recipients", new String[]{phoneNumber}
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://sms.arkesel.com/api/v2/sms/send", request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new Exception("Arkesel SMS failed: " + response.getBody());
        }
        return response.getBody();
    }
}
