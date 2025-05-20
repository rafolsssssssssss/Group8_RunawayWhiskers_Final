import java.sql.*;

public class DatabaseUtil {
public static int getLatestPlayerCoin(int playerId) {
int coin = 0;
try {
Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/game.db", "root", "");
PreparedStatement stmt = conn.prepareStatement("SELECT coin FROM PlayerData WHERE player_id = ? ORDER BY played_at DESC LIMIT 1");
stmt.setInt(1, playerId);
ResultSet rs = stmt.executeQuery();
if (rs.next()) {
coin = rs.getInt("coin");
}
rs.close();
stmt.close();
conn.close();
} catch (Exception e) {
e.printStackTrace();
}
return coin;
}
}