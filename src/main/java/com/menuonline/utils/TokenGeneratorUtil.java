package com.menuonline.utils;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class TokenGeneratorUtil {

    public static final int DEFAULT_PASSWORD_RECOVERY_TOKEN_SIZE = 7;

    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";

    public static String generate(int length) {
        if (length <= 0) {
            return "";
        }

        StringBuilder password = new StringBuilder(length);
        Random random = new SecureRandom();

        List<String> charCategories = new ArrayList<>(4);
            charCategories.add(LOWER);
            charCategories.add(UPPER);
            charCategories.add(DIGITS);

        for (int i = 0; i < length; i++) {
            String charCategory = charCategories.get(random.nextInt(charCategories.size()));
            int position = random.nextInt(charCategory.length());
            password.append(charCategory.charAt(position));
        }
        return new String(password);
    }
}
