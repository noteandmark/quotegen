package com.andmark.quotegen.domain;

import com.andmark.quotegen.domain.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usertg_id", unique = true)
    private Long usertgId;

    @NotBlank(message = "The name must not be blank")
    @Column(name = "username", nullable = false, unique = true)
    @Size(min = 3, max = 20, message = "Name must be between 3 and 20 characters")
    private String username;

    @Setter
    @NotBlank(message = "Password must not be blank")
    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @NotBlank(message = "The nickname must not be blank")
    @Column(name = "nickname", nullable = false)
    @Size(min = 3, max = 20, message = "Nickname must be between 3 and 20 characters")
    private String nickname;

}

