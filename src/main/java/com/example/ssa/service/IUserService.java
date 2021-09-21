package com.example.ssa.service;

import com.example.ssa.model.PasswordResetToken;
import com.example.ssa.model.User;
import com.example.ssa.model.VerificationToken;
import com.example.ssa.validation.EmailExistsException;

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
