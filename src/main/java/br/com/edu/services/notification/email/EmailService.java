package br.com.edu.services.notification.email;

import br.com.edu.services.notification.Notification;

public class EmailService implements Notification {

    @Override
    public void send(final String recipient, final String message) {
        System.out.printf("Send email to '%s' with message: %s%n", recipient, message);
    }

}
