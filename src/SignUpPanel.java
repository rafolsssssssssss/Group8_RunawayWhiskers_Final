import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class SignUpPanel extends JPanel {
    private Image backgroundImage;

    public SignUpPanel(AuthFrame authFrame, DatabaseManager dbManager) {
        // Load background.gif from resources
        URL bgURL = getClass().getResource("/assets/3.png");
        if (bgURL != null) {
            backgroundImage = new ImageIcon(bgURL).getImage();
        } else {
            System.out.println("⚠️ background.gif not found!");
        }

        setOpaque(false); // Let background show through
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JTextField usernameField = new JTextField(15);
        JTextField emailField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        JPasswordField confirmPasswordField = new JPasswordField(15);

        int row = 0;

        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = ++row;
        add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = ++row;
        add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = ++row;
        add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        add(confirmPasswordField, gbc);

        gbc.gridx = 0; gbc.gridy = ++row; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        JButton signUpButton = new JButton("Sign Up");
        add(signUpButton, gbc);

        gbc.gridy++;
        JButton backToLoginButton = new JButton("Already have an account? Login");
        backToLoginButton.setBorderPainted(false);
        backToLoginButton.setContentAreaFilled(false);
        backToLoginButton.setForeground(Color.BLUE);
        add(backToLoginButton, gbc);

        signUpButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.");
                return;
            }

            if (!isValidEmail(email)) {
                JOptionPane.showMessageDialog(this, "Invalid email format.");
                return;
            }

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.");
                return;
            }

            if (dbManager.userExists(username)) {
                JOptionPane.showMessageDialog(this, "Username already exists.");
                return;
            }

            if (dbManager.emailExists(email)) {
                JOptionPane.showMessageDialog(this, "Email already registered.");
                return;
            }

            if (dbManager.createUser(username, email, password)) {
                JOptionPane.showMessageDialog(this, "Account created! Please login.");
                authFrame.showLoginPanel();
            } else {
                JOptionPane.showMessageDialog(this, "Account creation failed.");
            }
        });

        backToLoginButton.addActionListener(e -> authFrame.showLoginPanel());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw the background image
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}
