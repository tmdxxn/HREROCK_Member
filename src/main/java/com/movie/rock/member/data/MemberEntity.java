package com.movie.rock.member.data;

//import com.movie.rock.board.data.BoardEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// 김승준 - 회원
@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mem_num")
    private Long memNum;

    @Column(name = "mem_id",nullable = false, unique = true)
    private String memId;

    @Column(name = "mem_password", nullable = false)
    private String memPassword;

    @Column(name = "mem_email", nullable = false, unique = true)
    private String memEmail;

    @Column(name = "mem_tel", nullable = false)
    private String memTel;

    @Column(name = "mem_gender", nullable = false)
    private String memGender;

    @Column(name = "mem_birth", nullable = false)
    private LocalDate memBirth;

    @Column(name = "mem_name", nullable = false)
    private String memName;

    @Enumerated(EnumType.STRING)
    @Column(name = "mem_role", nullable = false)
    private RoleEnum memRole;

    // 1, 2, 3, 4, 5 프로필 사진
    @Column(name = "mem_profile")
    private String memProfile;

    //DB 및 연관관계 설정
//    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    public List<BoardEntity> boards = new ArrayList<>();


    @Builder
    public MemberEntity(String memId, String memPassword, String memEmail, String memTel, String memGender, LocalDate memBirth, String memName, RoleEnum memRole, String memProfile) {
        this.memId = memId;
        this.memPassword = memPassword;
        this.memEmail = memEmail;
        this.memTel = memTel;
        this.memGender = memGender;
        this.memBirth = memBirth;
        this.memName = memName;
        this.memRole = memRole;
        this.memProfile = memProfile;
    }

    // 비밀번호 변경을 위한 메서드
    public void updatePassword(String newPassword) {
        this.memPassword = newPassword;
    }

    // 이메일 변경을 위한 메서드
    public void updateEmail(String newEmail) {
        this.memEmail = newEmail;
    }

    // 전화번호 변경을 위한 메서드
    public void updateTel(String newTel) {
        this.memTel = newTel;
    }

    // 프로필 사진 변경을 위한 메서드
    public void updateProfile(String newProfile) {
        this.memProfile = newProfile;
    }

}

