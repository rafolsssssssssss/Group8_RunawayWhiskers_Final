import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.net.URL;
import java.awt.Image;

public class GameLauncher extends JFrame {
    private JLabel coinLabel;
    private Image backgroundImage;
    private int currentPlayerId = 1; 
    private int playerId;// Or set it dynamically after login


    public GameLauncher() {
        // Load background.gif
        URL bgURL = getClass().getResource("/assets/background.gif");
        if (bgURL != null) {
            backgroundImage = new ImageIcon(bgURL).getImage();
        } else {
            System.out.println("⚠️ Background image not found!");
        }

        setTitle("Runaway Whiskers");
        setSize(600, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        

        // Custom panel with background
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        backgroundPanel.setLayout(new BorderLayout());
        setContentPane(backgroundPanel);

        // ===== Top Panel (Coin and Count) =====
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        topPanel.setOpaque(false);

        // Coin image
        ImageIcon coinIcon = null;
        URL coinURL = getClass().getResource("/assets/coin.png");
        JLabel coinIconLabel;
        if (coinURL != null) {
            ImageIcon originalIcon = new ImageIcon(coinURL);
            Image scaled = originalIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
            coinIcon = new ImageIcon(scaled);
            coinIconLabel = new JLabel(coinIcon);
        } else {
            System.out.println("⚠️ Coin image not found!");
            coinIconLabel = new JLabel("💰");
        }

        coinLabel = new JLabel("0");
        coinLabel.setFont(new Font("Rubik", Font.BOLD, 20));

        if (PlayerData.isLoggedIn()) {
            int totalCoins = PlayerData.getTotalCoins();
            coinLabel.setText(String.valueOf(totalCoins));
        } else {
            System.out.println("User not logged in — cannot display coins.");
        }

        JPanel coinPanel = new JPanel();
        coinPanel.setOpaque(false);
        coinPanel.add(coinIconLabel);
        coinPanel.add(coinLabel);

        topPanel.add(coinPanel);
        backgroundPanel.add(topPanel, BorderLayout.NORTH);

        // ===== Main Panel (Title and Buttons) =====
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel titleLabel = new JLabel("Runaway Whiskers");
        titleLabel.setFont(new Font("Rubik", Font.BOLD, 32));
        gbc.gridy = 0;
        mainPanel.add(titleLabel, gbc);

        gbc.gridy++;
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        Dimension buttonSize = new Dimension(220, 45);

        buttonPanel.add(createMenuButton("Play", e -> startGame(), buttonSize));
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(createMenuButton("Game Modes", e -> openGameModes(), buttonSize));
        buttonPanel.add(Box.createVerticalStrut(10));buttonPanel.add(createMenuButton("Shop", e -> openShop(currentPlayerId), buttonSize));
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(createMenuButton("Leaderboards", e -> openLeaderboard(), buttonSize));
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(createMenuButton("Settings", e -> openSettings(), buttonSize));
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(createMenuButton("Exit", e -> System.exit(0), buttonSize));

        mainPanel.add(buttonPanel, gbc);
        backgroundPanel.add(mainPanel, BorderLayout.CENTER);
    }

private JButton createMenuButton(String text, ActionListener action, Dimension size) {
    JButton button = new JButton(text);
    button.setFont(new Font("Rubik", Font.BOLD, 18));
    button.setAlignmentX(Component.CENTER_ALIGNMENT);
    button.setMaximumSize(size);
    button.setPreferredSize(size);

    button.addActionListener(e -> {
        if (SettingsStorage.isSfxOn()) {
            SoundPlayer.getInstance().playSound("C:\\Users\\Emman\\Desktop\\2ND YEAR\\2ND SEM\\CC 221\\SOUND\\BGM\\click.wav", false);
        }
        action.actionPerformed(e);
    });

    return button;
}

    public void refreshCoinLabel() {
        if (PlayerData.isLoggedIn()) {
            int totalCoins = PlayerData.getTotalCoins();
            coinLabel.setText(String.valueOf(totalCoins));
        }
    }

private void startGame() {
    dispose(); // Close the current window
    SwingUtilities.invokeLater(() -> {
        GameFrame gameFrame = new GameFrame(playerId);
        gameFrame.setVisible(true);
    });
}
    private void openGameModes() {
        JDialog modeDialog = new JDialog(this, "Select Game Mode", true);
        modeDialog.setSize(350, 400);
        modeDialog.setLocationRelativeTo(this);
        modeDialog.setLayout(new GridBagLayout());
        modeDialog.getContentPane().setBackground(new Color(102, 0, 153));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(15, 0, 15, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font buttonFont = new Font("Rubik", Font.PLAIN, 18);
        Dimension buttonSize = new Dimension(240, 50);

        // Gold Rush Button
        gbc.gridy = 0;
        JButton goldRushButton = new JButton("Gold Rush");
        goldRushButton.setFont(buttonFont);
        goldRushButton.setPreferredSize(buttonSize);
        goldRushButton.addActionListener(e -> {
            modeDialog.dispose();
            dispose();
            SwingUtilities.invokeLater(() -> {
                GoldFrame goldFrame = new GoldFrame();
                goldFrame.setVisible(true);
            });
        });
        modeDialog.add(goldRushButton, gbc);

        // Time Rush Button
        gbc.gridy++;
        JButton timeRushButton = new JButton("Time Rush");
        timeRushButton.setFont(buttonFont);
        timeRushButton.setPreferredSize(buttonSize);
        timeRushButton.addActionListener(e -> {
            modeDialog.dispose();
            dispose();
            SwingUtilities.invokeLater(() -> {
                TimeFrame timeFrame = new TimeFrame();
                timeFrame.setVisible(true);
            });
        });
        modeDialog.add(timeRushButton, gbc);

        // Hard Core Button
        gbc.gridy++;
        JButton hardCoreButton = new JButton("Hard Core");
        hardCoreButton.setFont(buttonFont);
        hardCoreButton.setPreferredSize(buttonSize);
        hardCoreButton.addActionListener(e -> {
            modeDialog.dispose();
            dispose();
            SwingUtilities.invokeLater(() -> {
                HardFrame hardFrame = new HardFrame();
                hardFrame.setVisible(true);
            });
        });
        modeDialog.add(hardCoreButton, gbc);

        modeDialog.setVisible(true);
    }

private void openShop(int playerId) {
    JFrame shopFrame = new JFrame("Game Shop");
    shopFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Just closes the shop window
    shopFrame.setSize(700, 1000);
    shopFrame.setResizable(false);
    
    ShopPanel shopPanel = new ShopPanel(playerId);
    shopFrame.add(shopPanel);
    
    shopFrame.setLocationRelativeTo(null); // Center on screen
    shopFrame.setVisible(true);
}



    private void openLeaderboard() {
        new LeaderboardFrame();
    }

    private void openSettings() {
JFrame frame = new JFrame("Settings");
frame.setContentPane(new SettingsPanel());
frame.pack();
frame.setLocationRelativeTo(null);
frame.setVisible(true);

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AuthFrame authFrame = new AuthFrame();
            authFrame.setVisible(true);
        });
    }
}
