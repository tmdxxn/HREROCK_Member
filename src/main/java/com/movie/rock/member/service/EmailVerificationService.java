package com.movie.rock.member.service;

import com.movie.rock.member.data.EmailVerificationEntity;
import com.movie.rock.member.data.EmailVerificationRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
public class EmailVerificationService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final JavaMailSender mailSender;

    @Autowired
    public EmailVerificationService(EmailVerificationRepository emailVerificationRepository, JavaMailSender mailSender) {
        this.emailVerificationRepository = emailVerificationRepository;
        this.mailSender = mailSender;
    }

    // 회원가입용 이메일 인증 코드 생성 및 발송
    @Transactional
    public void sendSignUpVerificationEmail(String email) {

        // 기존 인증메일 삭제
        emailVerificationRepository.deleteByEmail(email);

        String verificationCode = generateVerificationCode();
        EmailVerificationEntity verification = EmailVerificationEntity.builder()
                .email(email)
                .verificationCode(verificationCode)
                .expiryDate(LocalDateTime.now().plusMinutes(10))
                .verified(false)
                .token(null)
                .build();

        emailVerificationRepository.save(verification);
        sendSignUpEmailVerificationMessage(email, verificationCode);
    }

    // 6자리 영문 + 숫자 난수 코드 생성
    private String generateVerificationCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    // 회원가입 인증용 메시지 발송
    private void sendSignUpEmailVerificationMessage(String email, String verificationCode) {
        try {
            String subject = "Rock 회원가입 이메일 인증";
            String message = "<p>안녕하세요,</p>" +
                    "<p>회원가입을 완료하기 위한 인증 코드입니다:</p>" +
                    "<h2>" + verificationCode + "</h2>" +
                    "<p>이 코드는 10분 동안 유효합니다.</p>" +
                    "<p>감사합니다</p>";

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            try {
                helper.setFrom("sjk030221@gmail.com", "MovieRock@gmail.com");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("Failed to set email sender", e);
            }

            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(message, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email verification message", e);
        }
    }

    // 회원가입 이메일 인증 확인
    @Transactional
    public boolean verifySignUpEmail(String email, String verificationCode) {
        EmailVerificationEntity verification = emailVerificationRepository.findByEmailAndVerificationCode(email, verificationCode)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 이메일 또는 인증 코드입니다."));

        if (verification.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("인증 코드가 만료되었습니다.");
        }

        verification.setVerified(true);
        emailVerificationRepository.save(verification);
        return true;
    }

    // 회원가입 이메일 인증 여부확인
    @Transactional(readOnly = true)
    public boolean isEmailVerifiedForSignUp(String email) {
        return emailVerificationRepository.findByEmailAndVerified(email, true).isPresent();
    }
}