package com.menuonline.utils;

import java.time.LocalDateTime;

import com.menuonline.entity.TokenAccess;

public final class TokenAccessUtil {

    private TokenAccessUtil(){}

    public static boolean validate(TokenAccess token) {
        return token.getExpirationDate().isAfter(LocalDateTime.now());
    }
}
