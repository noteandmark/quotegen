package com.andmark.quotegen.domain;

import com.andmark.quotegen.domain.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "Name may not be empty")
    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Setter
    @NotBlank(message = "Name may not be empty")
    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

}

