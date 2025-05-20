import javax.swing.*;
import java.awt.*;

public class AuthFrame extends JFrame {

    private final DatabaseManager dbManager = new DatabaseManager();

    public AuthFrame() {
        setTitle("Runaway Whiskers - Login / Sign Up");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        showLoginPanel();
    }

    public void showLoginPanel() {
        LoginPanel loginPanel = new LoginPanel(this, dbManager);
        setContentPane(loginPanel);
        revalidate();
        repaint();
    }

    protected void showSignUpPanel() {
        SignUpPanel signUpPanel = new SignUpPanel(this, dbManager);
        setContentPane(signUpPanel);
        revalidate();
        repaint();
    }

    protected void proceedToGame() {
        dispose(); // Close the auth window
        SwingUtilities.invokeLater(() -> {
            GameLauncher game = new GameLauncher();
            game.setVisible(true);
        });
    }
}
