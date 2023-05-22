package br.com.edu.services.user;

import br.com.edu.services.notification.Notification;

public class UserService {

    private final Notification notification;

    public UserService(final Notification notification) {
        this.notification = notification;
    }

    public void registerUser(final String name, final String email) {
        this.notification.send(email, String.format("Hello %s, welcome to the container injection example!", name));
    }

}
