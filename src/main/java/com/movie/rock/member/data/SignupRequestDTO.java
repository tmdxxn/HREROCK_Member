package com.movie.rock.member.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// 김승준 - 회원
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequestDTO {
    private String memId;

    private String memPassword;

    private String memPasswordCheck;

    private String memName;

    private String memEmail;

    private String memTel;

    private String memGender;

    private LocalDate memBirth;

    private RoleEnum memRole;

    private String memProfile;



}
