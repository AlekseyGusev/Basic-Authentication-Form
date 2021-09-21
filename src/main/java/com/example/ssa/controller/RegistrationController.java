package com.example.ssa.controller;

import com.example.ssa.model.PasswordResetToken;
import com.example.ssa.model.User;
import com.example.ssa.model.VerificationToken;
import com.example.ssa.registration.OnRegistrationCompleteEvent;
import com.example.ssa.service.IUserService;
import com.example.ssa.validation.EmailExistsException;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.websocket.server.PathParam;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

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

    // PASSWORD RESET

    @PostMapping("/user/resetPassword")
    public ModelAndView resetPassword(@RequestParam("email") String userEmail, HttpServletRequest request, RedirectAttributes redirectAttributes) {

        if (userEmail.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Email cannot be empty");
        } else {

            User user = userService.findUserByEmail(userEmail);

            if (user != null) {
                String token = UUID.randomUUID().toString();
                userService.createPasswordResetTokenForUser(user, token);
                String appUrl = "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
                SimpleMailMessage email = constructResetToken(appUrl, user, token);
                mailSender.send(email);
                redirectAttributes.addFlashAttribute("message", "You should receive an Password Reset Email shortly");
            } else {
                redirectAttributes.addFlashAttribute("error", String.format("User with email: %s not found", userEmail));
            }
        }

        return new ModelAndView("redirect:/login");
    }

    @GetMapping("/user/changePassword")
    public ModelAndView displayChangePasswordPage(@PathParam("id") long id,
                                                  @PathParam("token") String token,
                                                  RedirectAttributes redirectAttributes) {

        PasswordResetToken passwordResetToken = userService.getPasswordResetToken(token);

        if (passwordResetToken == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid password reset token");
            return new ModelAndView("redirect:/login");
        }

        if (passwordResetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Your password reset token has expired");
            return new ModelAndView("redirect:/login");
        }

        User user = passwordResetToken.getUser();

        if (user.getId() != id) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid password reset token");
            return new ModelAndView("redirect:/login");
        }

        ModelAndView view = new ModelAndView("resetPassword");
        view.addObject("token", token);
        return view;
    }

    @PostMapping("/user/savePassword")
    public ModelAndView savePassword(@RequestParam("password") String password,
                                     @RequestParam("passwordConfirmation") String passwordConfirmation,
                                     @RequestParam("token") String token,
                                     RedirectAttributes redirectAttributes) {

        if (!Objects.equals(password, passwordConfirmation)) {
            return new ModelAndView("resetPassword", ImmutableMap.of("errorMessage", "Passwords do not match"));
        }

        PasswordResetToken passwordResetToken = userService.getPasswordResetToken(token);

        if (passwordResetToken == null) {
            redirectAttributes.addFlashAttribute("message", "Invalid token");
        } else {
            User user = passwordResetToken.getUser();
            if (user == null) {
                redirectAttributes.addFlashAttribute("message", "Unknown user");
            } else {
                userService.changeUserPassword(user, password);
                redirectAttributes.addFlashAttribute("message", "Password reset successfully");
            }
        }
        return new ModelAndView("redirect:/login");
    }

    private SimpleMailMessage constructResetToken(String contextPath, User user, String token) {

        String url = contextPath + "/user/changePassword?id=" + user.getId() + "&token=" + token;

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(user.getEmail());
        email.setSubject("Reset Password");
        email.setText("Please open the following URL to reset your password: \r\n" + url);
        email.setFrom(env.getProperty("support.email"));

        return email;
    }
}
