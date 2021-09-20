package com.example.baf.service;

import com.example.baf.model.PasswordResetToken;
import com.example.baf.model.User;
import com.example.baf.model.VerificationToken;
import com.example.baf.validation.EmailExistsException;

public interface IUserService {

    User findUserByEmail(String email);
    User registerNewUser(User user) throws EmailExistsException;
    void updateExistingUser(User user) throws EmailExistsException;
    Iterable<User> findAll();
    void delete(Long id);

    void createVerificationTokenForUser(User user, String token);
    VerificationToken getVerificationToken(String token);
    void saveRegisteredUser(User user);

    void createPasswordResetTokenForUser(User user, String token);
    PasswordResetToken getPasswordResetToken(String token);
    void changeUserPassword(User user, String password);

}
