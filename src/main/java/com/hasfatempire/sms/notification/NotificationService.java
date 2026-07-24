package com.hasfatempire.sms.notification;

import com.hasfatempire.sms.model.NotificationLog;
import com.hasfatempire.sms.model.School;
import com.hasfatempire.sms.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final SmsProvider smsProvider;
    private final EmailService emailService;
    private final NotificationLogRepository logRepository;

    /**
     * Send SMS asynchronously with logging. Never throws — failures are logged.
     */
    @Async
    public void sendSms(School school, String phoneNumber, String message) {
        if (phoneNumber == null || phoneNumber.isBlank()) return;
        NotificationLog entry = NotificationLog.builder()
                .school(school)
                .channel(NotificationLog.Channel.SMS)
                .recipient(phoneNumber)
                .message(message)
                .status(NotificationLog.Status.PENDING)
                .build();
        try {
            String response = smsProvider.send(phoneNumber, message);
            entry.setStatus(NotificationLog.Status.SENT);
            entry.setProviderResponse(response);
        } catch (Exception e) {
            log.error("SMS failed to {}: {}", phoneNumber, e.getMessage());
            entry.setStatus(NotificationLog.Status.FAILED);
            entry.setProviderResponse(e.getMessage());
        }
        logRepository.save(entry);
    }

    /**
     * Send email asynchronously with logging. Never throws.
     */
    @Async
    public void sendEmail(School school, String to, String subject, String body) {
        if (to == null || to.isBlank()) return;
        NotificationLog entry = NotificationLog.builder()
                .school(school)
                .channel(NotificationLog.Channel.EMAIL)
                .recipient(to)
                .subject(subject)
                .message(body)
                .status(NotificationLog.Status.PENDING)
                .build();
        try {
            String response = emailService.send(to, subject, body);
            entry.setStatus(NotificationLog.Status.SENT);
            entry.setProviderResponse(response);
        } catch (Exception e) {
            log.error("Email failed to {}: {}", to, e.getMessage());
            entry.setStatus(NotificationLog.Status.FAILED);
            entry.setProviderResponse(e.getMessage());
        }
        logRepository.save(entry);
    }

    /**
     * Send both SMS and email to a recipient (best-effort each).
     */
    public void notifyBoth(School school, String phone, String email, String subject, String message) {
        sendSms(school, phone, message);
        sendEmail(school, email, subject, message);
    }
}
