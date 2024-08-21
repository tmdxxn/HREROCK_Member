package com.movie.rock.member.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 김승준 - 회원
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateMemberDTO {
    private String memNewPassword;

    private String memNewPasswordCheck;

    private String memNewEmail;

    private String memNewTel;

    private String memNewProfile;


}
