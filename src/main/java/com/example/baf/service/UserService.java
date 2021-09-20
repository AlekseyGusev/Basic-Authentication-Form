package com.example.baf.service;

import com.example.baf.model.PasswordResetToken;
import com.example.baf.model.User;
import com.example.baf.model.VerificationToken;
import com.example.baf.persistence.IPasswordResetTokenRepository;
import com.example.baf.persistence.IUserRepository;
import com.example.baf.persistence.IVerificationTokenRepository;
import com.example.baf.validation.EmailExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class UserService implements IUserService {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final IVerificationTokenRepository verificationTokenRepository;
    private final IPasswordResetTokenRepository resetTokenRepository;

    @Autowired
    public UserService(IUserRepository userRepository, PasswordEncoder passwordEncoder, IVerificationTokenRepository verificationTokenRepository, IPasswordResetTokenRepository resetTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.verificationTokenRepository = verificationTokenRepository;
        this.resetTokenRepository = resetTokenRepository;
    }

    @Override
    public Iterable<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User registerNewUser(User user) throws EmailExistsException {

        if (emailExist(user.getEmail())) {
            throw new EmailExistsException("There is an account with that email address: " + user.getEmail());
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public void updateExistingUser(User user) throws EmailExistsException {

        Long id = user.getId();
        String email = user.getEmail();
        User emailOwner = userRepository.findByEmail(email);

        if (emailOwner != null && !id.equals(emailOwner.getId())) {
            throw new EmailExistsException("Email not available.");
        }

        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        userRepository.save(user);
    }

    @Override
    public void delete(Long id) {
        userRepository.findById(id).ifPresent(userRepository::delete);
    }

    @Override
    public void createVerificationTokenForUser(User user, String token) {
        VerificationToken newToken = new VerificationToken(token, user);
        verificationTokenRepository.save(newToken);
    }

    @Override
    public VerificationToken getVerificationToken(String token) {
        return verificationTokenRepository.findByToken(token);
    }

    @Override
    public void saveRegisteredUser(User user) {
        userRepository.save(user);
    }

    @Override
    public void createPasswordResetTokenForUser(User user, String token) {
        PasswordResetToken newToken = new PasswordResetToken(token, user);
        resetTokenRepository.save(newToken);
    }

    @Override
    public PasswordResetToken getPasswordResetToken(String token) {
        return resetTokenRepository.findByToken(token);
    }

    @Override
    public void changeUserPassword(User user, String password) {
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }

    private boolean emailExist(String email) {
        User user = userRepository.findByEmail(email);
        return user != null;
    }
}
