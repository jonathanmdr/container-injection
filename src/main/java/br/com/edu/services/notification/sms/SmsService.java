package br.com.edu.services.notification.sms;

import br.com.edu.services.notification.Notification;

public class SmsService implements Notification {

    @Override
    public void send(final String recipient, final String message) {
        System.out.printf("Send sms to '%s' with message: %s%n", recipient, message);
    }

}
