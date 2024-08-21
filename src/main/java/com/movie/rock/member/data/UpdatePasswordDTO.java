package com.movie.rock.member.data;

import lombok.Data;

@Data
public class UpdatePasswordDTO {
    private String memNewPassword;
    private String memNewPasswordCheck;
}
