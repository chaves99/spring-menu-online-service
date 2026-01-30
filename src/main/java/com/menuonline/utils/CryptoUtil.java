package com.menuonline.utils;

import java.nio.charset.StandardCharsets;

import at.favre.lib.crypto.bcrypt.BCrypt;
import at.favre.lib.crypto.bcrypt.BCrypt.Result;
import at.favre.lib.crypto.bcrypt.BCrypt.Version;

public final class CryptoUtil {

    private static final Version version = Version.VERSION_2A;

    private CryptoUtil() {
    }

    public static String encrypt(String value) {
        return BCrypt.with(version).hashToString(6, value.toCharArray());
    }

    public static boolean validate(String rawPassword, String hashPassword) {
        Result verify = BCrypt
            .verifyer(version)
            .verify(rawPassword.getBytes(StandardCharsets.UTF_8),
                    hashPassword.getBytes(StandardCharsets.UTF_8));
        return verify.verified;
    }

}
