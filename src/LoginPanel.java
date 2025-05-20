import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class LoginPanel extends JPanel {
    private final AuthFrame parentFrame;
    private final DatabaseManager dbManager;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private Image backgroundImage;

    public LoginPanel(AuthFrame frame, DatabaseManager dbManager) {
        this.parentFrame = frame;
        this.dbManager = dbManager;

        // Load background.png from resources
        URL bgURL = getClass().getResource("/assets/1.png");
        if (bgURL != null) {
            backgroundImage = new ImageIcon(bgURL).getImage();
        } else {
            System.out.println("⚠️ background.png not found!");
        }

        setOpaque(false); // allow background to show
        setupUI();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw the background image
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    private void setupUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);  // Add some spacing

        // Username Label + Field
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Username:"), gbc);
        usernameField = new JTextField(15);
        gbc.gridx = 1;
        add(usernameField, gbc);

        // Password Label + Field
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Password:"), gbc);
        passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        add(passwordField, gbc);

        // Login Button
        JButton loginBtn = new JButton("Login");
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        loginBtn.addActionListener(e -> login());
        add(loginBtn, gbc);

        // Sign Up Button
        JButton signUpBtn = new JButton("Sign Up");
        gbc.gridy = 3;
        signUpBtn.addActionListener(e -> parentFrame.showSignUpPanel());
        add(signUpBtn, gbc);

        // Continue as Guest Button
        JButton guestBtn = new JButton("Continue as Guest");
        gbc.gridy = 4;
        guestBtn.addActionListener(e -> continueAsGuest());
        add(guestBtn, gbc);
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (dbManager.isValidLogin(username, password)) {
            // Get email from DB for the username
            String email = dbManager.getEmailByUsername(username);

            // Get playerId from DB using username
            int playerIdFromDB = dbManager.getPlayerIdByUsername(username);
            if (playerIdFromDB <= 0) {
                JOptionPane.showMessageDialog(this, "Could not find player ID. Please try again.", "Login Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Set session info
            SessionManager.setPlayerId(playerIdFromDB);
            SessionManager.setUsername(username);
            SessionManager.setLoggedInUsername(username);

            // Set PlayerData info (if you use this elsewhere)
            PlayerData.setUserInfo(username, email, password);

            // Proceed to next frame or main game UI
            parentFrame.proceedToGame();

        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
        }
    }

    private void continueAsGuest() {
        PlayerData.setUserInfo("Guest", "guest@example.com", "");
        // For guest, clear session or set guest session info
        SessionManager.clearSession();
        SessionManager.setUsername("Guest");
        SessionManager.setLoggedInUsername("Guest");
        SessionManager.setPlayerId(0);

        parentFrame.proceedToGame();
    }
}
