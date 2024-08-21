package com.movie.rock.member.service;

import com.movie.rock.member.data.MemberEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

// 김승준 - 회원
public record CustomUserDetails(MemberEntity memberEntity) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + memberEntity.getMemRole().name()));
    }

    @Override
    public String getPassword() {
        return memberEntity.getMemPassword();
    }

    @Override
    public String getUsername() {
        return memberEntity.getMemId();
    }

    public Long getMemNum() {
        return memberEntity.getMemNum();
    }

    public MemberEntity getMember() { return memberEntity; }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


}
