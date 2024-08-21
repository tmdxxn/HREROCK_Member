package com.movie.rock.member.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerificationEntity, Long> {

    Optional<EmailVerificationEntity> findByToken(String token);

    Optional<EmailVerificationEntity> findByEmailAndVerified(String email, boolean verified);

    Optional<EmailVerificationEntity> findByEmailAndToken(String email, String token);

    void deleteByEmail(String email);

    boolean existsByEmailAndVerified(String email, boolean verified);

    Optional<EmailVerificationEntity> findByEmailAndVerificationCode(String email, String verificationCode);

    Optional<EmailVerificationEntity> findByEmailAndVerificationCodeAndVerified(String email, String verificationCode, boolean verified);
}