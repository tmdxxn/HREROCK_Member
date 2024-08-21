package com.movie.rock.member.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberListDTO {
    private Long memNum;  // 회원 번호 (Entity에 있다고 가정)
    private String memId;
    private String memEmail;
    private String memTel;
    private String memGender;
    private LocalDate memBirth;
    private String memName;
    private RoleEnum memRole;

    // MemberEntity를 MemberListDTO로 변환하는 생성자
    public MemberListDTO(MemberEntity entity) {
        this.memNum = entity.getMemNum();
        this.memId = entity.getMemId();
        this.memEmail = entity.getMemEmail();
        this.memTel = entity.getMemTel();
        this.memGender = entity.getMemGender();
        this.memBirth = entity.getMemBirth();
        this.memName = entity.getMemName();
        this.memRole = entity.getMemRole();
    }
}