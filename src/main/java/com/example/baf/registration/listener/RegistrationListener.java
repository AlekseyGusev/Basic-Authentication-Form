package com.example.baf.registration.listener;

import com.example.baf.model.User;
import com.example.baf.registration.OnRegistrationCompleteEvent;
import com.example.baf.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Component
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {

    private final IUserService userService;
    private final JavaMailSender mailSender;
    private final Environment env;

    @Autowired
    public RegistrationListener(IUserService userService, JavaMailSender mailSender, Environment env) {
        this.userService = userService;
        this.mailSender = mailSender;
        this.env = env;
    }

    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        this.confirmRegistration(event);
    }

    private void confirmRegistration(OnRegistrationCompleteEvent event) {

        User user = event.getUser();
        String token = UUID.randomUUID().toString();

        userService.createVerificationTokenForUser(user, token);

        SimpleMailMessage email = constructEmailMessage(event, user, token);
        mailSender.send(email);
    }

    private SimpleMailMessage constructEmailMessage(OnRegistrationCompleteEvent event, User user, String token) {

        String recipientEmail = user.getEmail();
        String subject = "Registration confirmation";
        String confirmationUrl = event.getAppUrl() + "/registrationConfirm?token=" + token;

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(recipientEmail);
        email.setSubject(subject);
        email.setText("Please open the following URL to verify your account: \r\n" + confirmationUrl);
        email.setFrom(Objects.requireNonNull(env.getProperty("support.email")));

        return email;
    }
}
