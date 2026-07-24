package com.hasfatempire.sms.notification;

public interface SmsProvider {
    /**
     * Send an SMS. Returns provider response string.
     * @throws Exception on failure
     */
    String send(String phoneNumber, String message) throws Exception;
}
