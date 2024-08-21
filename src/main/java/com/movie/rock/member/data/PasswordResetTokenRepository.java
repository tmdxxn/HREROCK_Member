package com.movie.rock.member.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

// 김승준 - 회원
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, Long> {
    Optional<PasswordResetTokenEntity> findByToken(String token);

    void deleteByMember(MemberEntity member);

    @Modifying
    @Query("DELETE FROM PasswordResetTokenEntity p WHERE p.member = :member")
    void deleteAllByMember(@Param("member") MemberEntity member);
}
