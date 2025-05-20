import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlayerData {
    private static String username;
    private static String email;
    private static String password;
    private static final String URL = "jdbc:mysql://localhost:3306/game.db"; // Confirm DB name here
    private static final String USER = "root";
    private static final String PASS = "";

    public static int getLoggedInPlayerId() {
    return getCurrentPlayerId();
}
    // Set user info (call on login/signup success)
    public static void setUserInfo(String user, String mail, String pass) {
        username = user;
        email = mail;
        password = pass;
        System.out.println("User info set: " + username + " | " + email);
    }

    public static boolean isLoggedIn() {
        return username != null && !username.isEmpty()
            && email != null && !email.isEmpty()
            && password != null && !password.isEmpty();
    }

    private static int getPlayerId(Connection conn) throws SQLException {
        String query = "SELECT player_id FROM players WHERE username = ? AND email = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, username);
            ps.setString(2, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("player_id");
                } else {
                    return -1;
                }
            }
        }
    }

public static int getCurrentPlayerId() {
    if (!isLoggedIn()) return -1;  // no player logged in
    
    try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
        return getPlayerId(conn);    // call your existing private method
    } catch (SQLException e) {
        e.printStackTrace();
        return -1;                   // error or no player found
    }
}

    // Save game data into playerdata table
    public static void saveGameData(long score, long coinsCollected) {
        if (!isLoggedIn()) {
            System.out.println("No logged-in player — data won't be saved.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            int playerId = getPlayerId(conn);
            if (playerId == -1) {
                System.out.println("⚠️ Player not found in database.");
                return;
            }

            String insertDataQuery = "INSERT INTO playerdata (player_id, score, coin, played_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertDataQuery)) {
                insertStmt.setInt(1, playerId);
                insertStmt.setLong(2, score);
                insertStmt.setLong(3, coinsCollected);
                insertStmt.executeUpdate();
                System.out.println("✅ Game data saved for player_id: " + playerId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Save coins collected in gamesession table
    public static void saveGameSession(String gameMode, long coinsCollected) {
        if (!isLoggedIn()) {
            System.out.println("No logged-in player — session won't be saved.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            int playerId = getPlayerId(conn);
            if (playerId == -1) {
                System.out.println("⚠️ Player not found in database.");
                return;
            }

            String insertSessionQuery = "INSERT INTO gamesessions (player_id, game_mode, coins_collected, timestamp) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
            try (PreparedStatement ps = conn.prepareStatement(insertSessionQuery)) {
                ps.setInt(1, playerId);
                ps.setString(2, gameMode);
                ps.setLong(3, coinsCollected);
                ps.executeUpdate();
                System.out.println("✅ Game session saved for player_id: " + playerId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Get total coins across playerdata and gamesession tables
    public static int getTotalCoins() {
        if (!isLoggedIn()) {
            System.out.println("No logged-in player — cannot fetch coins.");
            return 0;
        }

        int totalCoins = 0;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            int playerId = getPlayerId(conn);
            if (playerId == -1) {
                System.out.println("⚠️ Player not found in database.");
                return 0;
            }

            String coinSumQuery =
                "SELECT " +
                "  (SELECT COALESCE(SUM(coin), 0) FROM playerdata WHERE player_id = ?) + " +
                "  (SELECT COALESCE(SUM(coins_collected), 0) FROM gamesessions WHERE player_id = ?) AS total_coins";

            try (PreparedStatement coinStmt = conn.prepareStatement(coinSumQuery)) {
                coinStmt.setInt(1, playerId);
                coinStmt.setInt(2, playerId);
                try (ResultSet coinRs = coinStmt.executeQuery()) {
                    if (coinRs.next()) {
                        totalCoins = coinRs.getInt("total_coins");
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return totalCoins;
    }

    // Unlock a cat skin for player
    public static void unlockCat(int catSkinId) {
        if (!isLoggedIn()) {
            System.out.println("No logged-in player — cat unlock not saved.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            int playerId = getPlayerId(conn);
            if (playerId == -1) {
                System.out.println("Player not found.");
                return;
            }

            // Check if already unlocked
            String checkQuery = "SELECT * FROM unlockedcats WHERE player_id = ? AND catskin_id = ?";
            try (PreparedStatement psCheck = conn.prepareStatement(checkQuery)) {
                psCheck.setInt(1, playerId);
                psCheck.setInt(2, catSkinId);
                try (ResultSet rsCheck = psCheck.executeQuery()) {
                    if (rsCheck.next()) {
                        System.out.println("Cat skin already unlocked for this player.");
                        return;
                    }
                }
            }

            // Insert unlock record
            String insertQuery = "INSERT INTO unlockedcats (player_id, catskin_id) VALUES (?, ?)";
            try (PreparedStatement psInsert = conn.prepareStatement(insertQuery)) {
                psInsert.setInt(1, playerId);
                psInsert.setInt(2, catSkinId);
                psInsert.executeUpdate();
                System.out.println("Cat skin unlocked: catskin_id=" + catSkinId + " for player_id=" + playerId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Unlock a map for player
    public static void unlockMap(int mapId) {
        if (!isLoggedIn()) {
            System.out.println("No logged-in player — map unlock not saved.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            int playerId = getPlayerId(conn);
            if (playerId == -1) {
                System.out.println("Player not found.");
                return;
            }

            // Check if already unlocked
            String checkQuery = "SELECT * FROM unlockedmaps WHERE player_id = ? AND map_id = ?";
            try (PreparedStatement psCheck = conn.prepareStatement(checkQuery)) {
                psCheck.setInt(1, playerId);
                psCheck.setInt(2, mapId);
                try (ResultSet rsCheck = psCheck.executeQuery()) {
                    if (rsCheck.next()) {
                        System.out.println("Map already unlocked for this player.");
                        return;
                    }
                }
            }

            // Insert unlock record
            String insertQuery = "INSERT INTO unlockedmaps (player_id, map_id) VALUES (?, ?)";
            try (PreparedStatement psInsert = conn.prepareStatement(insertQuery)) {
                psInsert.setInt(1, playerId);
                psInsert.setInt(2, mapId);
                psInsert.executeUpdate();
                System.out.println("Map unlocked: map_id=" + mapId + " for player_id=" + playerId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Fetch list of unlocked cat skin IDs for player
    public static List<Integer> getUnlockedCats() {
        List<Integer> catSkinIds = new ArrayList<>();
        if (!isLoggedIn()) {
            System.out.println("No logged-in player — cannot fetch unlocked cats.");
            return catSkinIds;
        }

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            int playerId = getPlayerId(conn);
            if (playerId == -1) {
                System.out.println("Player not found.");
                return catSkinIds;
            }

            String query = "SELECT catskin_id FROM unlockedcats WHERE player_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, playerId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        catSkinIds.add(rs.getInt("catskin_id"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return catSkinIds;
    }

    // Fetch list of unlocked map IDs for player
    public static List<Integer> getUnlockedMaps() {
        List<Integer> mapIds = new ArrayList<>();
        if (!isLoggedIn()) {
            System.out.println("No logged-in player — cannot fetch unlocked maps.");
            return mapIds;
        }

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            int playerId = getPlayerId(conn);
            if (playerId == -1) {
                System.out.println("Player not found.");
                return mapIds;
            }

            String query = "SELECT map_id FROM unlockedmaps WHERE player_id = ?";
try (PreparedStatement ps = conn.prepareStatement(query)) {
ps.setInt(1, playerId);
try (ResultSet rs = ps.executeQuery()) {
while (rs.next()) {
mapIds.add(rs.getInt("map_id"));
}
}
}
} catch (SQLException e) {
e.printStackTrace();
}
    return mapIds;
}
}