import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.*;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private JFrame parentFrame;
    private Timer gameTimer;
    private int score = 0;
    private int coinsCollected = 0;
    private double speedMultiplier = 1.0;
    private int obstacleCount = 1;

    private PlayerCat player;
    private ArrayList<ObstacleDog> obstacles;
    private ArrayList<Coin> coins;
    private Random random;

    private boolean isPaused = false;
    private JButton pauseButton;
    private JPopupMenu pauseMenu;
    private final int playerId;
    private Image playerSkinImage;
    private Image backgroundMapImage;


    public GamePanel(JFrame frame, int playerId) {
        this.parentFrame = frame;
        this.playerId = PlayerData.getLoggedInPlayerId();
        // Check if the player is logged in
        if (!PlayerData.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, "You must be logged in to play the game.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            frame.dispose();
            SwingUtilities.invokeLater(() -> {
                GameLauncher launcher = new GameLauncher();
                launcher.setVisible(true);
            });
            return;
        }

        setFocusable(true);
        addKeyListener(this);
        setLayout(null);
        setPreferredSize(new Dimension(600, 800));

        loadSelectedMap(); // Load background map based on player selection

        player = new PlayerCat(300, 700);
        obstacles = new ArrayList<>();
        coins = new ArrayList<>();
        random = new Random();

        gameTimer = new Timer(30, this);

        pauseButton = new JButton("Pause");
        pauseButton.setBounds(500, 10, 80, 30);
        pauseButton.setFocusable(false);
        pauseButton.addActionListener(e -> showPauseMenu());
        add(pauseButton);

        setupPauseMenu();
    }
    
    private void loadMapImage(String imagePath) {
    try {
        File imageFile = new File(imagePath);
        if (imageFile.exists()) {
            backgroundMapImage = new ImageIcon(imagePath).getImage();
        } else {
            System.out.println("Map image not found: " + imagePath);
            backgroundMapImage = new ImageIcon(getClass().getResource("/assets/1(1).png")).getImage(); // fallback
        }
    } catch (Exception e) {
        System.out.println("Error loading map image: " + imagePath);
        e.printStackTrace();
        backgroundMapImage = new ImageIcon(getClass().getResource("/assets/1(1).png")).getImage(); // fallback
    }
}

private void loadSelectedMap() {
    try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/game.db", "root", "");
         PreparedStatement ps1 = conn.prepareStatement("SELECT selected_map FROM playerdata WHERE player_id = ? ORDER BY played_at DESC LIMIT 1");
         PreparedStatement ps2 = conn.prepareStatement("SELECT image_path FROM maps WHERE map_id = ?")) {

        ps1.setInt(1, playerId);
        ResultSet rs1 = ps1.executeQuery();

        if (rs1.next()) {
            int mapId = rs1.getInt("selected_map");

            ps2.setInt(1, mapId);
            ResultSet rs2 = ps2.executeQuery();

            if (rs2.next()) {
                String imagePath = rs2.getString("image_path");
                loadMapImage(imagePath);
                System.out.println("Loaded map: " + imagePath);
            } else {
                System.out.println("No image found for map ID: " + mapId);
                loadMapImage("assets/1(1).png");
            }
        } else {
            System.out.println("No playerdata found for player ID: " + playerId);
            loadMapImage("assets/1(1).png");
        }

    } catch (SQLException e) {
        e.printStackTrace();
        loadMapImage("assets/1(1).png");
    }
}


    private void setupPauseMenu() {
        pauseMenu = new JPopupMenu();
        JMenuItem resumeItem = new JMenuItem("Resume");
        JMenuItem optionsItem = new JMenuItem("Settings");
        JMenuItem backItem = new JMenuItem("Back");

        resumeItem.addActionListener(e -> resumeGame());
        optionsItem.addActionListener(e -> showOptions());
        backItem.addActionListener(e -> exitToMain());

        pauseMenu.add(resumeItem);
        pauseMenu.add(optionsItem);
        pauseMenu.add(backItem);
    }

    public void setPlayerSkin(Image skinImage) {
        this.playerSkinImage = skinImage;
        repaint();
    }

    public void setBackgroundMap(Image backgroundImage) {
        this.backgroundMapImage = backgroundImage;
        repaint();
    }

    public void startGameLoop() {
        gameTimer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isPaused) return;

        score += 1;

        if (score % 300 == 0) {
            speedMultiplier += 0.03;
            obstacleCount++;
        }

        player.update();
        updateObstacles();
        updateCoins();

        repaint();
    }

    private void updateObstacles() {
        if (obstacles.size() < obstacleCount) {
            obstacles.add(new ObstacleDog(random.nextInt(550), -50));
        }

        for (int i = 0; i < obstacles.size(); i++) {
            ObstacleDog dog = obstacles.get(i);
            dog.move(speedMultiplier);
            if (player.collidesWith(dog)) {
                gameOver();
                return;
            }
            if (dog.getY() > 800) {
                obstacles.remove(i);
                i--;
            }
        }
    }

    private void updateCoins() {
        if (random.nextDouble() < 0.02) {
            coins.add(new Coin(random.nextInt(550), -50));
        }

        for (int i = 0; i < coins.size(); i++) {
            Coin coin = coins.get(i);
            coin.move(speedMultiplier);
            if (player.collects(coin)) {
                coinsCollected++;
                coin.playCoinSound();
                coins.remove(i);
                break;
            }
            if (coin.getY() > 800) {
                coins.remove(i);
                i--;
            }
        }
    }

    private void playGameOverSound() {
        try {
            URL soundUrl = getClass().getClassLoader().getResource("assets/gameover.wav");
            if (soundUrl == null) {
                System.err.println("Game over sound file not found!");
                return;
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundUrl);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void gameOver() {
        playGameOverSound();
        gameTimer.stop();
        PlayerData.saveGameData(score, coinsCollected);

        int option = JOptionPane.showOptionDialog(this,
                "Game Over!\nScore: " + score + "\nCoins Collected: " + coinsCollected + "\nRetry?",
                "Game Over",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"Retry", "Back"},
                "Retry");

        if (option == JOptionPane.YES_OPTION) {
            resetGame();
        } else {
            parentFrame.dispose();
            SwingUtilities.invokeLater(() -> {
                GameLauncher launcher = new GameLauncher();
                launcher.setVisible(true);
            });
        }
    }

    private void resetGame() {
        score = 0;
        coinsCollected = 0;
        speedMultiplier = 1.0;
        obstacleCount = 1;

        player = new PlayerCat(300, 700);
        obstacles.clear();
        coins.clear();

        gameTimer.start();
        isPaused = false;
    }

    private void showPauseMenu() {
        isPaused = true;
        gameTimer.stop();
        pauseMenu.show(pauseButton, 0, pauseButton.getHeight());
    }

    private void resumeGame() {
        isPaused = false;
        gameTimer.start();
    }

    private void showOptions() {
        JFrame settingsFrame = new JFrame("Settings");
        settingsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        SettingsPanel settingsPanel = new SettingsPanel();

        settingsFrame.setContentPane(settingsPanel);
        settingsFrame.pack();
        settingsFrame.setLocationRelativeTo(null);
        settingsFrame.setVisible(true);

        settingsFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                AudioManager bgmManager = AudioManager.getInstance();
                SoundPlayer sfxPlayer = SoundPlayer.getInstance();

                if (SettingsStorage.isSoundOn()) {
                    if (!bgmManager.isPlaying()) {
                        bgmManager.play();
                    }
                    bgmManager.unmute();
                } else {
                    bgmManager.mute();
                    bgmManager.stop();
                }

                if (!SettingsStorage.isSfxOn()) {
                    sfxPlayer.stop();
                }

                resumeGame();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                windowClosed(e);
            }
        });
    }

    private void exitToMain() {
        parentFrame.dispose();
        SwingUtilities.invokeLater(() -> {
            GameLauncher launcher = new GameLauncher();
            launcher.setVisible(true);
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundMapImage != null) {
            g.drawImage(backgroundMapImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        if (playerSkinImage != null) {
            g.drawImage(playerSkinImage, player.getX(), player.getY(), player.getWidth(), player.getHeight(), this);
        } else {
            player.draw(g);
        }

        for (ObstacleDog dog : obstacles) dog.draw(g);
        for (Coin coin : coins) coin.draw(g);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Rubik", Font.BOLD, 20));
        g.drawString("Score: " + score, 20, 30);
        g.drawString("Coins: " + coinsCollected, 20, 60);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!isPaused) player.keyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (!isPaused) player.keyReleased(e);
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}
