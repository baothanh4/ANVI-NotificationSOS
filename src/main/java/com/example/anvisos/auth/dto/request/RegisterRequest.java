package com.example.anvisos.auth.dto.request;

import com.example.anvisos.model.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    @NotBlank
    private String fullName;

    @NotBlank
    private String phone;

    @Email
    private String email;

    @NotBlank
    private String password;

    private UserRole role;

    private String bloodType;
    private Integer birthYear;
}
