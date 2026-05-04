package com.example.anvisos.auth.dto.response;

import com.example.anvisos.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegisterResponse {
    private Long id;
    private String fullName;
    private String phone;
    private String email;
    private UserRole role;
}
