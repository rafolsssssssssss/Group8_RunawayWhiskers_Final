import javax.swing.*;

public class LeaderboardFrame extends JFrame {
    public LeaderboardFrame() {
        setTitle("Leaderboard");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        add(new LeaderboardPanel());
        setVisible(true);
    }
}
