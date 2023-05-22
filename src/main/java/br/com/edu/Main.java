package br.com.edu;

import br.com.edu.container.Container;
import br.com.edu.container.Inject;
import br.com.edu.services.notification.Notification;
import br.com.edu.services.notification.email.EmailService;
import br.com.edu.services.notification.sms.SmsService;
import br.com.edu.services.user.UserService;

public class Main {

    private static final Container container = new Container();

    @Inject
    private UserService userService;

    public static void main(final String ... args) {
        container.register(Notification.class, EmailService.class);
        container.register(Notification.class, SmsService.class);
        container.register(UserService.class);
        container.register(Main.class);

        final Main main = container.resolve(Main.class);
        main.run();
    }

    public void run() {
        this.userService.registerUser("John Doe", "john.doe@gmail.com");
    }

}

