package com.hasfatempire.sms.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Dev/default SMS provider — logs messages to console instead of sending.
 * Active when app.notifications.sms.provider=log or not set.
 */
@Component
@ConditionalOnProperty(name = "app.notifications.sms.provider", havingValue = "log", matchIfMissing = true)
public class LogSmsProvider implements SmsProvider {

    private static final Logger log = LoggerFactory.getLogger(LogSmsProvider.class);

    @Override
    public String send(String phoneNumber, String message) {
        log.info("[DEV SMS] To: {} | Message: {}", phoneNumber, message);
        return "logged (dev mode)";
    }
}
