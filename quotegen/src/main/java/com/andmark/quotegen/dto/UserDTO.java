package com.andmark.quotegen.dto;

import com.andmark.quotegen.domain.enums.UserRole;
import jakarta.validation.constraints.Size;
import lombok.*;

@RequiredArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class UserDTO {
    private Long id;
    private Long usertgId;
    private String username;
    @Size(min = 3, max = 20, message = "Password must be between 3 and 20 characters")
    private String password;
    private UserRole role;
    private String nickname;
}
