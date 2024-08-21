package com.movie.rock.member.service;

import com.movie.rock.member.data.MemberEntity;
import com.movie.rock.member.data.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// 김승준 - 회원
@Service
public class CustomUserDetailsService implements UserDetailsService {

    // 회원 정보를 조회하기 위해
    @Autowired
    private MemberRepository memberRepository;

    // 시큐리티에서 사용자 정보를 로드하기 위해
    @Override
    public UserDetails loadUserByUsername(String memId) throws UsernameNotFoundException {
        
        // memId로 회원정보 DB에서 조회
        MemberEntity memberEntity = memberRepository.findByMemId(memId)
                .orElseThrow(() -> new UsernameNotFoundException(memId + " 라는 아이디를 찾을수 없습니다."));
        
        // 조회된 정보를 반환
        return new CustomUserDetails(memberEntity);
    }
}
