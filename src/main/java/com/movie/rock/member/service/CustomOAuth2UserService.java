package com.movie.rock.member.service;

import com.movie.rock.config.JwtUtil;
import com.movie.rock.member.data.MemberEntity;
import com.movie.rock.member.data.MemberRepository;
import com.movie.rock.member.data.RoleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // OAuth2 제공자로서 받은 정보에서 이메일 추출
        String email = oAuth2User.getAttribute("email");
        Optional<MemberEntity> existingMember = memberRepository.findByMemEmail(email);

        // 해당 이메일로 등록된 회원정보가 DB에 없으면 생성
        if (!existingMember.isPresent()) {
            MemberEntity newMember = MemberEntity.builder()
                    .memId(oAuth2User.getAttribute("sub")) // 제공된 기본 ID
                    .memEmail(email)
                    .memName(oAuth2User.getAttribute("name"))
                    .memPassword("구글사용자") // 비밀번호가 필요없음
                    .memGender("구글사용자") // 기본값
                    .memTel("구글사용자") // 기본값
                    .memBirth(LocalDate.now()) // 기본값
                    .memRole(RoleEnum.USER) // 권한 = 회원
                    .memProfile("/static/media/Profile_1.e607f6bcdd36d2f31fae912e7a1cb894.svg")
                    .build();
            memberRepository.save(newMember);
        }

        return oAuth2User;
    }

    // JWT 토큰 생성
    public String generateToken(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        Optional<MemberEntity> member = memberRepository.findByMemEmail(email);
        if (member.isPresent()) {
            return jwtUtil.generateAccessToken(member.get().getMemId());
        }
        return null;
    }
}
