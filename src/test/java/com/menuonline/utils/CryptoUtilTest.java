package com.menuonline.utils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.GeneralSecurityException;

import org.junit.jupiter.api.Test;

public class CryptoUtilTest {

    @Test
    public void encrypt() throws GeneralSecurityException {
        String encrypt = CryptoUtil.encrypt("value");
        System.out.println();
        System.out.println();
        System.out.println(encrypt);
        System.out.println();
        System.out.println();
        assertNotNull(encrypt);
    }

    @Test
    public void validate() throws GeneralSecurityException {
        boolean valid = CryptoUtil.validate("value", "$2a$06$dq22RgB7oUp9yjtvUq0xpuDjnNLiibIGKx.PgGrEamdNNZeQxkVQq");
        assertTrue(valid);

        valid = CryptoUtil.validate("value", "$2a$06$Qunob7kKM/m9nVFsIG1fGOqqoebjCZtQ5iiNNYZRrhhvTYPgqLL4q");
        assertTrue(valid);

        valid = CryptoUtil.validate("value", "$2a$06$9ulQlfiq9XmnR2rcg4jp7OG5OunGOs8THFAm6sfB3dRF7gRqd2y2.");
        assertTrue(valid);

        valid = CryptoUtil.validate("value", "$2a$06$jtGBj/kA/pqnh8GkQR9fz.FVzaJ01sPDXSnEvO7o2crwMIzEYEvI6");
        assertTrue(valid);
    }

}
