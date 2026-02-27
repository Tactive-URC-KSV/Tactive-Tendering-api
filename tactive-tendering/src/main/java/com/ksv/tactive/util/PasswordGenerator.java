package com.ksv.tactive.util;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PasswordGenerator {

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGIT = "0123456789";
    private static final String SPECIAL = "@#$%&*!?";
    private static final String ALL = UPPER + LOWER + DIGIT + SPECIAL;
    private static final SecureRandom random = new SecureRandom();

    public static String generatePassword() {
        int length = 10;
        List<Character> passwordChars = new ArrayList<>();
        passwordChars.add(UPPER.charAt(random.nextInt(UPPER.length())));
        passwordChars.add(LOWER.charAt(random.nextInt(LOWER.length())));
        passwordChars.add(DIGIT.charAt(random.nextInt(DIGIT.length())));
        passwordChars.add(SPECIAL.charAt(random.nextInt(SPECIAL.length())));
        for (int i = 4; i < length; i++) {
            passwordChars.add(ALL.charAt(random.nextInt(ALL.length())));
        }
        Collections.shuffle(passwordChars, random);
        StringBuilder password = new StringBuilder();
        for (Character ch : passwordChars) {
            password.append(ch);
        }
        return password.toString();
    }
}
