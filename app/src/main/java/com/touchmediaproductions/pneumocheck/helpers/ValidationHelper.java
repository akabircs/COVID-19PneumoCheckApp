package com.touchmediaproductions.pneumocheck.helpers;

public class ValidationHelper {

    /**
     * Returns true if valid email address is provided
     *
     * @param email
     * @return
     */
    public static boolean isEmailValid(String email) {
        boolean isUsernameNotEmpty = !email.isEmpty();
        return isUsernameNotEmpty && email.matches(".*@.*") && !email.contains(" ");
    }

}
