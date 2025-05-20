import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.*;  // Added for sound playback

public class TimeRushPanel extends JPanel implements ActionListener, KeyListener {
    private Timer gameTimer;
    private JFrame parentFrame;
    private int coinsCollected = 0;
    private double speedMultiplier = 1.0;
    private int obstacleCount = 1;

    private int timeLeft = 30; // Countdown time
    private long lastSecondCheck = System.currentTimeMillis();
    private int lastSpeedIncreaseTime = 30; // For speed increase control

    private PlayerCat player;
    private ArrayList<ObstacleDog> obstacles;
    private ArrayList<Coin> coins;
    private ArrayList<PlusTen> powerUps;

    private Random random;
    private boolean isPaused = false;
    private JButton pauseButton;
    private JPopupMenu pauseMenu;
    private Image playerSkinImage;
    private ImageIcon background; // GIF background

    // To track reason for game over
    private enum GameOverReason { TIME_UP, HIT_OBSTACLE }
    private GameOverReason gameOverReason;

    public TimeRushPanel(JFrame frame) {
        this.parentFrame = frame;
        setFocusable(true);
        addKeyListener(this);
        setLayout(null);
        setPreferredSize(new Dimension(600, 800));

        player = new PlayerCat(300, 700);
        obstacles = new ArrayList<>();
        coins = new ArrayList<>();
        powerUps = new ArrayList<>();
        random = new Random();

        gameTimer = new Timer(30, this); // Game update speed

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
        // Make sure "background.gif" is in your classpath or resource folder
        URL bgUrl = getClass().getResource("assets/timerush.gif");
        if (bgUrl != null) {
            background = new ImageIcon(bgUrl);
        } else {
            System.err.println("Background GIF not found!");
            background = null;
        }
    }

    public void startGameLoop() {
        resetGame(); // ensure game state is fresh on start
        gameTimer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isPaused) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSecondCheck >= 1000) {
            timeLeft--;
            lastSecondCheck = currentTime;
            if (timeLeft <= 0) {
                gameOverReason = GameOverReason.TIME_UP;
                gameOver();
                return;
            }

            // Increase speed only once per 10 seconds
            if (timeLeft % 10 == 0 && timeLeft != lastSpeedIncreaseTime) {
                speedMultiplier += 0.03;
                obstacleCount++;
                lastSpeedIncreaseTime = timeLeft;
            }
        }

        player.update();
        updateObstacles();
        updateCoins();
        updatePowerUps();

        repaint();
    }

    private void updateObstacles() {
        // Spawn new obstacles only if current obstacles are less than obstacleCount
        // AND there is no obstacle currently near the spawn position (-50 y)
        if (obstacles.size() < obstacleCount) {
            boolean canSpawn = true;
            for (ObstacleDog dog : obstacles) {
                if (dog.getY() < 0) { // obstacle still near spawn area
                    canSpawn = false;
                    break;
                }
            }
            if (canSpawn) {
                obstacles.add(new ObstacleDog(random.nextInt(550), -50));
            }
        }

        for (int i = 0; i < obstacles.size(); i++) {
            ObstacleDog dog = obstacles.get(i);
            dog.move(speedMultiplier);
            if (player.collidesWith(dog)) {
                gameOverReason = GameOverReason.HIT_OBSTACLE;
                gameOver();
                return;
            }
            if (dog.getY() > 800) {
                obstacles.remove(i--);
            }
        }
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
            PlusTen p = powerUps.get(i);
            p.move(speedMultiplier);
            if (player.collects(p)) {
                timeLeft += 10;
                p.playPlusTenSound();   // Play sound here
                powerUps.remove(i--);
            } else if (p.getY() > 800) {
                powerUps.remove(i--);
            }
        }
    }

    private void gameOver() {
        playGameOverSound();  // <-- PLAY WAV SOUND AT GAME OVER

        gameTimer.stop();
        String message;
        if (gameOverReason == GameOverReason.TIME_UP) {
            message = "Time's Up!";
        } else {
            message = "You hit an obstacle!";
        }
        if (PlayerData.isLoggedIn()) {
            PlayerData.saveGameSession("Time Rush", coinsCollected);
        }
        int option = JOptionPane.showOptionDialog(this,
                message + "\nCoins Collected: " + coinsCollected + "\nRetry?",
                "Game Over",
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
            }); // Or return to main menu
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

    private void resetGame() {
        coinsCollected = 0;
        speedMultiplier = 1.0;
        obstacleCount = 1;
        timeLeft = 30;
        lastSpeedIncreaseTime = 30;

        player = new PlayerCat(300, 700);
        obstacles.clear();
        coins.clear();
        powerUps.clear();

        isPaused = false;
        lastSecondCheck = System.currentTimeMillis();
        gameTimer.start();
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
        JFrame frame = new JFrame("Settings");
        frame.setContentPane(new SettingsPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
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

        // Draw the GIF background if available
        if (background != null) {
            // Draw it stretched to fit the panel size (optional)
            background.paintIcon(this, g, 0, 0);
        } else {
            // Fallback background if GIF not loaded
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
        for (PlusTen p : powerUps) p.draw(g);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Rubik", Font.BOLD, 20));
        g.drawString("Time Left: " + timeLeft + "s", 20, 30);
        g.drawString("Coins: " + coinsCollected, 20, 60);
    }

    @Override public void keyPressed(KeyEvent e) { if (!isPaused) player.keyPressed(e); }
    @Override public void keyReleased(KeyEvent e) { if (!isPaused) player.keyReleased(e); }
    @Override public void keyTyped(KeyEvent e) {}
}
