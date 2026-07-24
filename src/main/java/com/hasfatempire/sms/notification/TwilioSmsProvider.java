package com.hasfatempire.sms.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

/**
 * Twilio SMS provider — for international schools.
 * Enable with: app.notifications.sms.provider=twilio
 */
@Component
@ConditionalOnProperty(name = "app.notifications.sms.provider", havingValue = "twilio")
public class TwilioSmsProvider implements SmsProvider {

    @Value("${app.notifications.sms.twilio.account-sid}")
    private String accountSid;

    @Value("${app.notifications.sms.twilio.auth-token}")
    private String authToken;

    @Value("${app.notifications.sms.twilio.from-number}")
    private String fromNumber;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String send(String phoneNumber, String message) throws Exception {
        String url = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String auth = Base64.getEncoder().encodeToString((accountSid + ":" + authToken).getBytes());
        headers.set("Authorization", "Basic " + auth);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("To", phoneNumber);
        body.add("From", fromNumber);
        body.add("Body", message);

        ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new Exception("Twilio SMS failed: " + response.getBody());
        }
        return response.getBody();
    }
}
