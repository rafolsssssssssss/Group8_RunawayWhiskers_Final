import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;

public class DatabaseManager {

    private final String url = "jdbc:mysql://localhost:3306/game.db"; // change your DB URL
    private final String user = "root";
    private final String password = "";

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    // Create a new user with hashed password
    public boolean createUser(String username, String email, String password) {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        String sql = "INSERT INTO players (username, email, password) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, hashedPassword);

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Check if a username exists in the database
    public boolean userExists(String username) {
        String sql = "SELECT COUNT(*) FROM players WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Check if an email exists in the database
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM players WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Validate login by comparing plain password with hashed password in DB
    public boolean isValidLogin(String username, String plainPassword) {
        String query = "SELECT password FROM players WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password");
                return BCrypt.checkpw(plainPassword, storedHash);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    

    // Retrieve email by username
    public String getEmailByUsername(String username) {
        String email = null;
        String query = "SELECT email FROM players WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                email = rs.getString("email");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return email;
    }

    // Retrieve player ID by username
    public int getPlayerIdByUsername(String username) {
        int playerId = -1;
        String query = "SELECT player_id FROM players WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                playerId = rs.getInt("player_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return playerId;
    }

    // Save player stats (score, coins) to database
    public boolean savePlayerStats(String username, int score, int coins) {
        String findPlayerIdQuery = "SELECT player_id FROM players WHERE username = ?";
        String insertStatsQuery = "INSERT INTO playerdata (player_id, score, coin, timestamp) VALUES (?, ?, ?, NOW())";

        try (Connection conn = getConnection();
             PreparedStatement findStmt = conn.prepareStatement(findPlayerIdQuery)) {

            findStmt.setString(1, username);
            ResultSet rs = findStmt.executeQuery();

            if (rs.next()) {
                int playerId = rs.getInt("player_id");

                try (PreparedStatement insertStmt = conn.prepareStatement(insertStatsQuery)) {
                    insertStmt.setInt(1, playerId);
                    insertStmt.setInt(2, score);
                    insertStmt.setInt(3, coins);

                    int rows = insertStmt.executeUpdate();
                    return rows > 0;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // Get total coins collected by a player
    public int getTotalCoinsForPlayer(String username) {
        String query = "SELECT SUM(pd.coin) AS total_coins " +
                       "FROM playerdata pd " +
                       "JOIN players p ON pd.player_id = p.player_id " +
                       "WHERE p.username = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("total_coins");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
