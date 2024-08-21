package com.movie.rock.member.service;

import com.movie.rock.member.data.*;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// 김승준 - 회원
@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmailVerificationService emailVerificationService;

    // 회원 가입 ------------------------------------------------------------------
    
    // 회원가입 이메일 인증 발송
    public void sendSignUpVerificationEmail(String memEmail) throws Exception {
        if (memberRepository.findByMemEmail(memEmail).isPresent()) {
            throw new Exception("이메일이 이미 사용중입니다.");
        }
        emailVerificationService.sendSignUpVerificationEmail(memEmail);
    }

    // 회원가입 이메일 인증 확인
    public boolean verifySignUpEmail(String email, String verificationCode) {
        return emailVerificationService.verifySignUpEmail(email, verificationCode);
    }

    // 새 회원 등록
    @Transactional
    public void registerNewMember(SignupRequestDTO signupRequestDTO) throws Exception {
        if (memberRepository.findByMemId(signupRequestDTO.getMemId()).isPresent()) {
            throw new Exception("아이디가 이미 사용중입니다.");
        }

        if (!emailVerificationService.isEmailVerifiedForSignUp(signupRequestDTO.getMemEmail())) {
            throw new Exception("이메일 인증이 완료되지 않았습니다.");
        }

        String encodedPassword = passwordEncoder.encode(signupRequestDTO.getMemPassword());

        MemberEntity newMember = MemberEntity
                .builder()
                .memId(signupRequestDTO.getMemId())
                .memPassword(encodedPassword)
                .memName(signupRequestDTO.getMemName())
                .memEmail(signupRequestDTO.getMemEmail())
                .memTel(signupRequestDTO.getMemTel())
                .memGender(signupRequestDTO.getMemGender())
                .memBirth(signupRequestDTO.getMemBirth())
                .memRole(RoleEnum.USER)
                .memProfile("1")
                .build();

        memberRepository.save(newMember);
    }

    // 중복 체크 ------------------------------------------------------------------
    
    // 아이디 중복 확인
    public boolean isUsernameExists(String memId) {
        return memberRepository.findByMemId(memId).isPresent();
    }

    // 이메일 중복 확인
    public boolean isEmailExists(String memEmail) {
        return memberRepository.findByMemEmail(memEmail).isPresent();
    }

    // 회원 탈퇴 ------------------------------------------------------------------
    
    // 회원탈퇴
    @Transactional
    public void delete(String memId) throws Exception {
        Optional<MemberEntity> member = memberRepository.findByMemId(memId);
        if(member.isPresent()) {
            memberRepository.delete(member.get());
        } else {
            throw new Exception("회원 정보를 찾을 수 없습니다");
        }
    }

    // 회원 단체 탈퇴
    @Transactional
    public void deleteMultipleMembers(List<String> memIds) throws Exception {
        for (String memId : memIds) {
            Optional<MemberEntity> member = memberRepository.findByMemId(memId);
            if (member.isPresent()) {
                memberRepository.delete(member.get());
            } else {
                throw new Exception("회원 정보를 찾을 수 없습니다");
            }
        }
    }

    // 정보 수정 ------------------------------------------------------------------
    
    // 회원 정보 수정
    @Transactional
    public void updateMember(String memId, UpdateMemberDTO updateDto) {
        MemberEntity member = memberRepository.findByMemId(memId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        // 비밀번호 변경
        if (updateDto.getMemNewPassword() != null && !updateDto.getMemNewPassword().isEmpty()) {
            if (!updateDto.getMemNewPassword().equals(updateDto.getMemNewPasswordCheck())) {
                throw new IllegalArgumentException("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
            }
            member.updatePassword(passwordEncoder.encode(updateDto.getMemNewPassword()));
        }

        // 이메일 변경
        if (updateDto.getMemNewEmail() != null && !updateDto.getMemNewEmail().isEmpty()) {
            member.updateEmail(updateDto.getMemNewEmail());
        }

        // 전화번호 변경
        if (updateDto.getMemNewTel() != null && !updateDto.getMemNewTel().isEmpty()) {
            member.updateTel(updateDto.getMemNewTel());
        }

        // 프로필 사진 변경
        if (updateDto.getMemNewProfile() != null && !updateDto.getMemNewProfile().isEmpty()) {
            member.updateProfile(updateDto.getMemNewProfile());
        }

        memberRepository.save(member);
    }

    // 아이디 찾기 -----------------------------------------------------------------

    // 아이디 찾기 로직
    public Optional<MemberEntity> findMemId(String memEmail, String memName) {
        return memberRepository.findByMemEmailAndMemName(memEmail, memName);
    }

    // 비밀번호 찾기 ----------------------------------------------------------------

    // 비밀번호 재설정 토큰 생성
    @Transactional
    public void createPasswordResetTokenForMember(MemberEntity member, String token) {


        // 기존 토큰 삭제
        passwordResetTokenRepository.deleteAllByMember(member);

        // 새 토큰 생성
        PasswordResetTokenEntity myToken = PasswordResetTokenEntity.builder()
                .token(token)
                .member(member)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();

        // 토큰 저장
        try {
            passwordResetTokenRepository.saveAndFlush(myToken);

        } catch (DataIntegrityViolationException e) {

            throw new RuntimeException("Failed to create password reset token. Please try again.", e);
        }
    }

    // 비밀번호 재설정
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetTokenEntity resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token has expired");
        }

        MemberEntity member = resetToken.getMember();
        member.updatePassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);

        passwordResetTokenRepository.delete(resetToken);

    }

    // 비밀번호 재설정 이메일 발송
    public void sendPasswordResetEmail(String memEmail, String token) {
        try {
            String subject = "Rock 비밀번호 재설정";
            String confirmationUrl = "http://localhost:3000/user/ChangePassword?token=" + token;
            String message = "<p>안녕하세요,</p>" +
                    "<p>비밀번호를 재설정하려면 아래 링크를 클릭하세요:</p>" +
                    "<a href=\"" + confirmationUrl + "\">비밀번호 재설정</a>" +
                    "<p>감사합니다</p>";

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setTo(memEmail);
            helper.setSubject(subject);
            helper.setText(message, true); // true indicates HTML

            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    // 비밀번호 재설정 토큰으로 회원 찾기
    public Optional<MemberEntity> getMemberByPasswordResetToken(String token) {
        return passwordResetTokenRepository.findByToken(token)
                .map(PasswordResetTokenEntity::getMember);
    }

    // 회원 검증 -----------------------------------------------------------------

    // 아이디로 회원 찾기
    public MemberEntity findByMemId(String memId) {
        return memberRepository.findByMemId(memId).orElse(null);
    }

    // 아이디와 이메일로 회원 찾기
    public MemberEntity findMemberByUsernameAndEmail(String memId, String memEmail) {
        return memberRepository.findByMemIdAndMemEmail(memId, memEmail)
                .orElse(null);
    }

    // 이메일로 회원 찾기
    public Optional<MemberEntity> findByEmail(String memEmail) {
        return memberRepository.findByMemEmail(memEmail);
    }

    // 권한 확인
    public boolean isAdminMember(String memId) {
        Optional<MemberEntity> member = memberRepository.findByMemId(memId);
        return member.map(m -> m.getMemRole() == RoleEnum.ADMIN).orElse(false);
    }

    // 관리자) 회원 전체 조회 ----------------------------------------------

    // 회원 전체 보기
    public List<MemberEntity> getAllMembers() {
        return memberRepository.findAll(Sort.by(Sort.Direction.ASC, "memNum"));
    }

    // 페이징 처리
    public Page<MemberEntity> getAllMembersPageable(Pageable pageable) {
        return memberRepository.findAll(pageable);
    }

}
