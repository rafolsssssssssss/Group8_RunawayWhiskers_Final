public final class SessionManager {
    private static String currentUsername;
    private static String loggedInUsername;
    private static int playerId;
    private static int currentPlayerId = -1;

    public static void setCurrentPlayerId(int id) {
        currentPlayerId = id;
    }

    public static int getCurrentPlayerId() {
        return currentPlayerId;
    }

    private SessionManager() {}

    public static void setPlayerId(int id) { playerId = id; }
    public static int getPlayerId() { return playerId; }

    public static void setLoggedInUsername(String username) { loggedInUsername = username; }
    public static String getLoggedInUsername() { return loggedInUsername; }

    public static void setUsername(String username) { currentUsername = username; }
    public static String getUsername() { return currentUsername; }

    public static void clearSession() {
        currentUsername = null;
        loggedInUsername = null;
        playerId = 0;
    }
}
