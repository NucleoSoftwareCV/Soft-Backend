package com.hean.consigueventas.oonabe.auth.repository;

import com.hean.consigueventas.oonabe.auth.entity.PasswordResetToken;
import com.hean.consigueventas.oonabe.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    @Modifying
    int deleteByUser(User user);
}
