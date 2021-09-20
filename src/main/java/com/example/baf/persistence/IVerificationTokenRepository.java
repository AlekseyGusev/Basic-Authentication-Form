package com.example.baf.persistence;

import com.example.baf.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IVerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    VerificationToken findByToken(String token);
}
