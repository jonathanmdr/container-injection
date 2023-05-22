package br.com.edu.services.notification;

public interface Notification {

    void send(final String recipient, final String message);

}
