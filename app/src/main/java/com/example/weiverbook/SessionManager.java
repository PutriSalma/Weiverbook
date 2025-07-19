package com.example.weiverbook;

public class SessionManager {
    private static int loggedInUserId = -1;
    private static String loggedInUsername = null;

    public static void login(int userId, String username) {
        loggedInUserId = userId;
        loggedInUsername = username;
    }

    public static void logout() {
        loggedInUserId = -1;
        loggedInUsername = null;
    }

    public static int getLoggedInUserId() {
        return loggedInUserId;
    }

    public static String getLoggedInUsername() {
        return loggedInUsername;
    }

    public static boolean isLoggedIn() {
        return loggedInUserId != -1;
    }
}