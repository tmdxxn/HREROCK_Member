package com.movie.rock.member.controller;

import com.movie.rock.config.JwtUtil;
import com.movie.rock.config.TokenRefreshRequest;
import com.movie.rock.member.data.*;
import com.movie.rock.member.service.CustomUserDetailsService;
import com.movie.rock.member.service.MemberService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

// 김승준 - 회원

@RestController
@RequestMapping("/auth") // 경로 지정
public class AuthController {


    private final CustomUserDetailsService userDetailsService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    public AuthController(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MemberService memberService;

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequestDTO authRequestDTO) throws Exception {
        try {
            // 사용자 인증
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequestDTO.getMemId(), authRequestDTO.getMemPassword()));
        } catch (AuthenticationException e) {
            throw new Exception("잘못된 자격 증명", e);
        }

        // JWT 토큰 생성
        final String accessToken = jwtUtil.generateAccessToken(authRequestDTO.getMemId());
        final String refreshToken = jwtUtil.generateRefreshToken(authRequestDTO.getMemPassword());

        return ResponseEntity.ok(new AuthResponseDTO(accessToken, refreshToken, "custom"));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        token = token.substring(7);

        return ResponseEntity.ok("로그아웃 되었습니다.");
    }

    // 회원가입 - 아이디 중복 체크
    @GetMapping("/check-id")
    public ResponseEntity<Boolean> checkUsername(@RequestParam(name = "memId") String memId) {
        return ResponseEntity.ok(memberService.isUsernameExists(memId));
    }

    // 회원가입 - 이메일 중복 확인 및 인증메일 발송
    @PostMapping("/check-and-send-verification")
    public ResponseEntity<?> checkAndSendVerification(@RequestParam(name = "memEmail") String memEmail) {
        try {
            boolean isEmailExists = memberService.isEmailExists(memEmail);
            if (isEmailExists) {
                return ResponseEntity.badRequest().body("이미 사용 중인 이메일입니다.");
            }

            memberService.sendSignUpVerificationEmail(memEmail);
            return ResponseEntity.ok("인증 이메일이 발송되었습니다. 이메일을 확인해주세요.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 회원가입 - 이메일 인증
    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam(name = "email") String email, @RequestParam(name = "verificationCode") String verificationCode) {
        boolean verified = memberService.verifySignUpEmail(email, verificationCode);
        if (verified) {
            return ResponseEntity.ok("이메일이 성공적으로 인증되었습니다.");
        } else {
            return ResponseEntity.badRequest().body("이메일 인증에 실패했습니다.");
        }
    }

    // 회원가입 - 회원정보 저장
    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody SignupRequestDTO signupRequestDTO) {
        try {
            // 아이디 중복 체크
            if (memberService.isUsernameExists(signupRequestDTO.getMemId())) {
                return ResponseEntity.badRequest().body("이미 존재하는 아이디입니다.");
            }

            // 이메일 중복 체크
            if (memberService.isEmailExists(signupRequestDTO.getMemEmail())) {
                return ResponseEntity.badRequest().body("이미 존재하는 이메일입니다.");
            }

            memberService.registerNewMember(signupRequestDTO);
            return ResponseEntity.ok("회원가입이 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("회원가입 중 오류 발생: " + e.getMessage());
        }
    }

    // 헤더 바디에 둘 memberinfo 정보
    @GetMapping("/memberinfo")
    public ResponseEntity<?> memberInfo(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증되지 않음");
        }

        String memId = authentication.getName();
        MemberEntity member = memberService.findByMemId(memId);

        if (member == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자를 찾지 못했습니다.");
        }

        // 필요한 정보만 포함하는 DTO 생성
        MemberInfoDTO memberInfo = new MemberInfoDTO();
        memberInfo.setMemNum(member.getMemNum()); //추가부분

        memberInfo.setMemEmail(member.getMemEmail());

        memberInfo.setMemName(member.getMemName());

        memberInfo.setMemGender(member.getMemGender());

        memberInfo.setMemRole(member.getMemRole());

        memberInfo.setMemTel(member.getMemTel());

        memberInfo.setMemId(member.getMemId());

        memberInfo.setMemProfile(member.getMemProfile());

        memberInfo.setMemBirth(member.getMemBirth().toString());

        return ResponseEntity.ok(memberInfo);
    }

    // 회원정보 수정
    @PutMapping("/update")
    public ResponseEntity<?> updateMember(@RequestBody UpdateMemberDTO updateDto, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증되지 않음");
        }

        String memId = authentication.getName();

        try {
            memberService.updateMember(memId, updateDto);
            return ResponseEntity.ok("회원 정보가 성공적으로 수정 되었습니다.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원 정보 수정 중 오류 발생: " + e.getMessage());
        }
    }

    // 회원 탈퇴
    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증되지 않음");
        }
        String memId = authentication.getName();
        try {
            memberService.delete(memId);
            return ResponseEntity.ok("탈퇴가 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원 탈퇴 중 오류가 발생하였습니다.");
        }
    }

    // 아이디 찾기
    @PostMapping("/find-id")
    public ResponseEntity<String> findId(@RequestBody FindIdRequestDTO requestDTO) {
        Optional<MemberEntity> member = memberService.findMemId(requestDTO.getMemEmail(), requestDTO.getMemName());
        if (member.isPresent()) {
            return ResponseEntity.ok(member.get().getMemId());
        } else {
            return ResponseEntity.status(404).body("회원정보를 찾지 못했습니다.");
        }
    }

    // 비밀번호 찾기
    @PostMapping("/find-password")
    public ResponseEntity<?> findPassword(@RequestBody FindPasswordRequestDTO request) {
        try {
            MemberEntity member = memberService.findMemberByUsernameAndEmail(request.getMemId(), request.getMemEmail());
            if (member == null) {
                return ResponseEntity.badRequest().body("사용자를 찾을수 없거나 이메일이 일치하지 않습니다.");
            }
            String token = UUID.randomUUID().toString();
            memberService.createPasswordResetTokenForMember(member, token);
            memberService.sendPasswordResetEmail(member.getMemEmail(), token);
            return ResponseEntity.ok().body("비밀번호 재설정 이메일 발송완료");
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("요청을 처리하는 동안 오류가 발생했습니다");
        }
    }

    // 비밀번호 찾기 - 비밀번호 재설정
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam(name = "token") String token, @RequestBody UpdatePasswordDTO updatePasswordDto) {
        try {
            memberService.resetPassword(token, updatePasswordDto.getMemNewPassword());
            return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("비밀번호를 재설정하는 동안 오류가 발생했습니다");
        }
    }

    // 토큰 리프래쉬
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody TokenRefreshRequest request) {
        try {
            String refreshToken = request.getRefreshToken();
            String memId = jwtUtil.extractMemberId(refreshToken);

            if (memId != null && jwtUtil.isTokenValid(refreshToken, memId)) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(memId);
                String newAccessToken = jwtUtil.generateAccessToken(userDetails.getUsername());
                String newRefreshToken = jwtUtil.generateRefreshToken(userDetails.getUsername());

                return ResponseEntity.ok(new AuthResponseDTO(newAccessToken, newRefreshToken, "custom"));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Refresh Token");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error refreshing token");
        }
    }

    // 비밀번호 인증
    @PostMapping("/verify-password")
    public ResponseEntity<?> verifyPassword(Authentication authentication, @RequestBody PasswordVerificationRequest request) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증되지 않음");
        }

        String memId = authentication.getName();
        MemberEntity member = memberService.findByMemId(memId);

        if (member == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자를 찾을 수 없습니다.");
        }

        if (passwordEncoder.matches(request.getMemPassword(), member.getMemPassword())) {
            return ResponseEntity.ok().body(true);
        } else {
            return ResponseEntity.ok().body(false);
        }
    }
}
