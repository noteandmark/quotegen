package com.andmark.quotegen.dto;

import com.andmark.quotegen.domain.enums.UserRole;
import lombok.*;

@RequiredArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class UserDTO {
    private Long usertgId;
    private String username;
    private String password;
    private UserRole role;
}
