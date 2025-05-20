import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.*;
import java.net.URL;

public class HardCorePanel extends JPanel implements ActionListener, KeyListener {
    private JFrame parentFrame;
    private Timer gameTimer;
    private int score = 0;
    private int coinsCollected = 0;
    private double speedMultiplier = 1.2;
    private int obstacleCount = 3;
    private final int MIN_OBSTACLE_SPACING = 60;

    private PlayerCat player;
    private ArrayList<ObstacleDog> obstacles;
    private ArrayList<Coin> coins;
    private Random random;

    private boolean isPaused = false;
    private JButton pauseButton;
    private JPopupMenu pauseMenu;
    private Image playerSkinImage;
    private Image backgroundImage;

    public HardCorePanel(JFrame frame) {
        this.parentFrame = frame;
        setFocusable(true);
        addKeyListener(this);
        setLayout(null);
        setPreferredSize(new Dimension(600, 800));

        // Load the background image (PNG)
        backgroundImage = new ImageIcon(getClass().getResource("/assets/hardcore.png")).getImage();

        player = new PlayerCat(300, 700);
        obstacles = new ArrayList<>();
        coins = new ArrayList<>();
        random = new Random();

        gameTimer = new Timer(25, this);

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
    }

    private void playGameOverSound() {
        try {
            URL soundUrl = getClass().getClassLoader().getResource("assets/gameoverhardcore.wav");
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

    public void startGameLoop() {
        gameTimer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isPaused) return;

        score += 1;

        if (score % 300 == 0) {
            speedMultiplier += 0.025;
            obstacleCount += 3;
        }

        player.update();
        updateObstacles();
        updateCoins();

        repaint();
    }

    private void updateObstacles() {
        while (obstacles.size() < obstacleCount) {
            ObstacleDog newDog;
            boolean hasSpace;

            int attempts = 0;
            do {
                hasSpace = true;
                newDog = new ObstacleDog(random.nextInt(550), -50);
                for (ObstacleDog existing : obstacles) {
                    if (Math.abs(existing.getX() - newDog.getX()) < MIN_OBSTACLE_SPACING &&
                        Math.abs(existing.getY() - newDog.getY()) < 50) {
                        hasSpace = false;
                        break;
                    }
                }
                attempts++;
            } while (!hasSpace && attempts < 10);

            if (hasSpace) obstacles.add(newDog);
        }

        for (int i = 0; i < obstacles.size(); i++) {
            ObstacleDog dog = obstacles.get(i);
            dog.move(speedMultiplier);
            if (player.collidesWith(dog)) {
                gameOver();
                return;
            }
            if (dog.getY() > 800) {
                obstacles.remove(i--);
            }
        }
    }

    private void updateCoins() {
        if (random.nextDouble() < 0.01) {
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
            } else if (coin.getY() > 800) {
                coins.remove(i);
                i--;
            }
        }
    }

    private void gameOver() {
        playGameOverSound();  // <<-- Play the WAV here

        gameTimer.stop();

        if (PlayerData.isLoggedIn()) {
            PlayerData.saveGameSession("Hardcore", coinsCollected);
        }

        int option = JOptionPane.showOptionDialog(this,
                "HARD CORE MODE OVER!\nCoins: " + coinsCollected + "\nRetry?",
                "HardCore Over",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE,
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
                launcher.refreshCoinLabel();
            });
        }
    }

    private void resetGame() {
        score = 0;
        coinsCollected = 0;
        speedMultiplier = 1.2;
        obstacleCount = 3;

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
        JFrame frame = new JFrame("Settings");
        frame.setContentPane(new SettingsPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
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
        // Draw the PNG background
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
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
