import javax.swing.*;

public class GameFrame extends JFrame {
    public GameFrame(int playerId) {
        setTitle("Endless Cat Racing");
        setSize(600, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        GamePanel gamePanel = new GamePanel(this, playerId);
        add(gamePanel);
        gamePanel.startGameLoop();
    }
}

