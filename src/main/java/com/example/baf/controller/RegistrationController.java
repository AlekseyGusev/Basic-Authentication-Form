package com.example.baf.controller;

import com.example.baf.model.User;
import com.example.baf.model.VerificationToken;
import com.example.baf.registration.OnRegistrationCompleteEvent;
import com.example.baf.service.IUserService;
import com.example.baf.validation.EmailExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDateTime;

@Controller
public class RegistrationController {

    private final IUserService userService;
    private final ApplicationEventPublisher eventPublisher;
    private final Environment env;
    private final JavaMailSender mailSender;

    @Autowired
    public RegistrationController(IUserService userService, ApplicationEventPublisher eventPublisher, Environment env, JavaMailSender mailSender) {
        this.userService = userService;
        this.eventPublisher = eventPublisher;
        this.env = env;
        this.mailSender = mailSender;
    }

// REGISTRATION

    @RequestMapping("/signup")
    public ModelAndView registrationForm() {
        return new ModelAndView("registrationPage", "user", new User());
    }

    @RequestMapping("/user/register")
    public ModelAndView registerUser(@Valid User user, BindingResult result, HttpServletRequest request, RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return new ModelAndView("registrationPage", "user", user);
        }

        try {
            user.setEnabled(true);
            User registeredNewUser = userService.registerNewUser(user);
            String appUrl = "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();

            eventPublisher.publishEvent(new OnRegistrationCompleteEvent(appUrl, registeredNewUser));

            redirectAttributes.addFlashAttribute("message", "Verification code has been send");

        } catch (EmailExistsException e) {
            result.addError(new FieldError("user", "email", e.getMessage()));
            return new ModelAndView("registrationPage", "user", user);
        }

        return new ModelAndView("redirect:/login");
    }

    @RequestMapping("/registrationConfirm")
    public ModelAndView confirmRegistration(@RequestParam("token") String token, RedirectAttributes redirectAttributes) {

        VerificationToken verificationToken = userService.getVerificationToken(token);

        if (verificationToken == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid account confirmation token");
            return new ModelAndView("redirect:/login");
        }

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Your registration token has expired. Please register again");
            return new ModelAndView("redirect:/login");
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userService.saveRegisteredUser(user);
        redirectAttributes.addFlashAttribute("message", "Your account verified successfully");

        return new ModelAndView("redirect:/login");
    }
}
