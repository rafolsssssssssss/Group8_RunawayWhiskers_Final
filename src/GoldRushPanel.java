import java.net.URL;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.*;

public class GoldRushPanel extends JPanel implements ActionListener, KeyListener {
    private JFrame parentFrame;
    private Timer gameTimer;
    private int coinsCollected = 0;
    private double speedMultiplier = 1.0;
    private int timeLeft = 30; // in seconds
    private long lastSecondTick;

    private PlayerCat player;
    private ArrayList<Coin> coins;
    private ArrayList<PlusTen> powerUps;
    private Random random;

    private boolean isPaused = false;
    private JButton pauseButton;
    private JPopupMenu pauseMenu;
    
    private Image playerSkinImage;
    // New: Animated GIF background icon
    private ImageIcon backgroundGif;

    public GoldRushPanel(JFrame frame) {
        this.parentFrame = frame;
        setFocusable(true);
        addKeyListener(this);
        setLayout(null);
        setPreferredSize(new Dimension(600, 800));

        player = new PlayerCat(300, 700);
        coins = new ArrayList<>();
        powerUps = new ArrayList<>();
        random = new Random();

        gameTimer = new Timer(30, this); // 30ms interval
        lastSecondTick = System.currentTimeMillis();

        pauseButton = new JButton("Pause");
        pauseButton.setBounds(500, 10, 80, 30);
        pauseButton.setFocusable(false);
        pauseButton.addActionListener(e -> showPauseMenu());
        add(pauseButton);

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

        // Load the GIF background
        URL bgUrl = getClass().getClassLoader().getResource("assets/goldrush.gif");
        if (bgUrl != null) {
            backgroundGif = new ImageIcon(bgUrl);
        } else {
            System.err.println("Error: Could not load background GIF from assets/goldrush_bg.gif");
        }
    }

    public void startGameLoop() {
        gameTimer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isPaused) return;

        long now = System.currentTimeMillis();
        if (now - lastSecondTick >= 1000) {
            timeLeft--;
            lastSecondTick = now;

            if (timeLeft <= 0) {
                gameOver();
                return;
            }
        }

        player.update();
        updateCoins();
        updatePowerUps();

        repaint();
    }

    private void updateCoins() {
        if (random.nextDouble() < 0.01) { // Reduced frequency
            coins.add(new Coin(random.nextInt(550), -50));
        }

        for (int i = 0; i < coins.size(); i++) {
            Coin coin = coins.get(i);
            coin.move(speedMultiplier);

            if (player.collects(coin)) {
                coinsCollected++;
                coin.playCoinSound();  // Play sound on coin collection
                coins.remove(i);
                break;  // Exit loop after collecting a coin to avoid issues
            } else if (coin.getY() > 800) {
                coins.remove(i);
                i--;  // Decrement index to account for removed element
            }
        }
    }

    private void updatePowerUps() {
        if (random.nextDouble() < 0.005) {
            powerUps.add(new PlusTen(random.nextInt(550), -50));
        }

        for (int i = 0; i < powerUps.size(); i++) {
            PlusTen plusTen = powerUps.get(i);
            plusTen.move(speedMultiplier);
            if (player.collects(plusTen)) {
                plusTen.playPlusTenSound();
                timeLeft += 10;
                powerUps.remove(i);
                break;
            }
            if (plusTen.getY() > 800) {
                powerUps.remove(i);
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
        playGameOverSound();  // <-- Play sound right when game over triggers

        gameTimer.stop();

        // ✅ Save coins to DB
        if (PlayerData.isLoggedIn()) {
            PlayerData.saveGameSession("Gold Rush", coinsCollected);
        }

        int option = JOptionPane.showOptionDialog(this,
                "Time's Up!\nCoins Collected: " + coinsCollected + "\nRetry?",
                "Gold Rush Over",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"Retry", "Back"},
                "Retry");

        if (option == JOptionPane.YES_OPTION) {
            resetGame();
        } else {
            parentFrame.dispose();  // closes the current window

            // Open the main menu window again
            SwingUtilities.invokeLater(() -> {
                GameLauncher launcher = new GameLauncher();
                launcher.setVisible(true);
                launcher.refreshCoinLabel();
            });
        }
    }

    private void resetGame() {
        coinsCollected = 0;
        timeLeft = 30;
        speedMultiplier = 1.0;

        player = new PlayerCat(300, 700);
        coins.clear();
        powerUps.clear();

        lastSecondTick = System.currentTimeMillis();
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
        lastSecondTick = System.currentTimeMillis(); // Prevent time skipping
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

        // When the settings window is closed, resume game state based on updated settings
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
        parentFrame.dispose();  // closes the current window
        // Open the main menu window again
        SwingUtilities.invokeLater(() -> {
            GameLauncher launcher = new GameLauncher();
            launcher.setVisible(true);
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw the animated GIF background if loaded
        if (backgroundGif != null) {
            backgroundGif.paintIcon(this, g, 0, 0);
        } else {
            // fallback background color if GIF missing
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
         if (playerSkinImage != null) {
            g.drawImage(playerSkinImage, player.getX(), player.getY(), player.getWidth(), player.getHeight(), this);
        } else {
            player.draw(g);
        }
        for (Coin coin : coins) coin.draw(g);
        for (PlusTen p : powerUps) p.draw(g);

        // Draw time and coins in black for contrast on white background
        g.setColor(Color.BLACK);
        g.setFont(new Font("Rubik", Font.BOLD, 20));
        g.drawString("Coins: " + coinsCollected, 20, 30);
        g.drawString("Time Left: " + timeLeft + "s", 20, 60);
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
