package com.movie.rock.member.data;

import lombok.Data;

@Data
public class FindPasswordRequestDTO {
    private String memId;

    private String memEmail;
}
