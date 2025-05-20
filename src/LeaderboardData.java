import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardData {
    public static List<PlayerScore> getTopScores() {
        List<PlayerScore> scores = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/game.db", "root", "")) {
            String query = "SELECT p.username, pd.score " +
                           "FROM playerdata pd " +
                           "JOIN players p ON pd.player_id = p.player_id " +
                           "ORDER BY pd.score DESC LIMIT 10";

            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String username = rs.getString("username");
                int score = rs.getInt("score");
                scores.add(new PlayerScore(username, score));
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return scores;
    }
}
