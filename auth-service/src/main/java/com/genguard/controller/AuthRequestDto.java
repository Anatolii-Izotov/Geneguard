package com.genguard.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class AuthRequestDto {
    @Getter
    @Setter
    private String username;
    @Getter
    @Setter
    private String password;
}
