package com.andmark.quotebot.dto;

import com.andmark.quotebot.domain.enums.UserRole;
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
    private String nickname;
}
