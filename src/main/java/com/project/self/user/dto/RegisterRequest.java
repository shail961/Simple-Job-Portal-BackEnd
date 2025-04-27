package com.project.self.user.dto;

import com.project.self.user.enums.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class  RegisterRequest {
    private String username;
    private String email;
    private String password;
    private Role role;
}