package com.andmark.quotegen.dto;

import com.andmark.quotegen.domain.enums.UserRole;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@Setter
@ToString
public class UserDTO {
    private Long usertgId;
    private String username;
    private String password;
    private UserRole role;
}
