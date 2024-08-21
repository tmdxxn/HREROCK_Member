package com.movie.rock.member.data;

import lombok.Data;

import java.time.LocalDate;

// 김승준 - 회원
@Data
public class MemberInfoDTO {

    private String memId;

    private String memEmail;

    private String memName;

    private String memGender;

    private String memBirth;

    private String memTel;

    private RoleEnum memRole;

    private Long memNum;

    private String memProfile;
}