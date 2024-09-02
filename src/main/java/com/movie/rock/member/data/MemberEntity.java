package com.movie.rock.member.data;

//import com.movie.rock.board.data.BoardEntity;
import com.movie.rock.board.data.BoardEntity;
import com.movie.rock.chat.data.ChatRoomEntity;
import com.movie.rock.chat.data.MessageEntity;
import com.movie.rock.chat.data.SessionEntity;
import com.movie.rock.movie.data.entity.MovieFavorEntity;
import com.movie.rock.movie.data.entity.MovieReviewEntity;
import com.movie.rock.movie.data.entity.MovieWatchHistoryEntity;
import jakarta.mail.Message;
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

    @Column(name = "mem_profile", nullable = false)
    private String memProfile;

    @Enumerated(EnumType.STRING)
    @Column(name = "mem_role", nullable = false)
    private RoleEnum memRole;

    //DB 및 연관관계 설정
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<BoardEntity> boards = new ArrayList<>();

    @OneToMany(mappedBy = "sender")     //실시간 챗봇 메시지
    private List<MessageEntity> message;

    @OneToMany(mappedBy = "member" ,cascade = CascadeType.ALL,orphanRemoval = true)
    private List<ChatRoomEntity> member = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<SessionEntity> session;

    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<MovieFavorEntity> favorites;

    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<MovieReviewEntity> reviews;

    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<MovieWatchHistoryEntity> movieWatch;



    @Builder
    public MemberEntity(String memId, 
                        String memPassword, 
                        String memEmail, 
                        String memTel, 
                        String memGender, 
                        LocalDate memBirth, 
                        String memName, 
                        RoleEnum memRole, 
                        String memProfile, 
                        List<MessageEntity> message, 
                        List<ChatRoomEntity> member, 
                        List<SessionEntity> session, 
                        List<MovieFavorEntity> favorites,
                        List<MovieReviewEntity> reviews, 
                        List<MovieWatchHistoryEntity> movieWatch) {
        this.memId = memId;
        this.memPassword = memPassword;
        this.memEmail = memEmail;
        this.memTel = memTel;
        this.memGender = memGender;
        this.memBirth = memBirth;
        this.memName = memName;
        this.memRole = memRole;
        this.message = message;
        this.member = member;
        this.session =session;
        this.favorites = favorites;
        this.reviews = reviews;
        this.movieWatch = movieWatch;
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

