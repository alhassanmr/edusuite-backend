package com.hasfatempire.sms.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.notifications.email.from:no-reply@edusuite.app}")
    private String fromAddress;

    @Value("${app.notifications.email.enabled:false}")
    private boolean enabled;

    /**
     * Send an email. In dev mode (email.enabled=false) it logs instead.
     */
    public String send(String to, String subject, String body) throws Exception {
        if (!enabled || mailSender == null) {
            log.info("[DEV EMAIL] To: {} | Subject: {} | Body: {}", to, subject, body);
            return "logged (dev mode)";
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
        return "sent";
    }
}
