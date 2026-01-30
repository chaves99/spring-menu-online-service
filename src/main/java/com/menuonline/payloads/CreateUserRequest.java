package com.menuonline.payloads;

public record CreateUserRequest(
        String email,
        String password,
        String establishmentName,
        String code,
        String line,
        String city) {
}
