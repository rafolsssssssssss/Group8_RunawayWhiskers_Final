import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class LeaderboardPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;

    public LeaderboardPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JLabel title = new JLabel("Leaderboard", SwingConstants.CENTER);
        title.setFont(new Font("Rubik", Font.BOLD, 22));
        title.setForeground(new Color(102, 0, 153));
        add(title, BorderLayout.NORTH);

        model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[]{"Rank", "Username", "Score"});

        table = new JTable(model);
        table.setFont(new Font("Rubik", Font.PLAIN, 18));
        table.setRowHeight(30);

        add(new JScrollPane(table), BorderLayout.CENTER);

        loadLeaderboardData();
    }

    private void loadLeaderboardData() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/game.db", "root", "")) {
            String query = "SELECT p.username, pd.score " +
                           "FROM playerdata pd " +
                           "JOIN players p ON pd.player_id = p.player_id " +
                           "ORDER BY pd.score DESC LIMIT 10";

            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            int rank = 1;
            while (rs.next()) {
                String username = rs.getString("username");
                int score = rs.getInt("score");
                model.addRow(new Object[]{rank++, username, score});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load leaderboard data.", "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
