package com.example.baf.persistence;

import com.example.baf.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IPasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    PasswordResetToken findByToken(String token);
}
