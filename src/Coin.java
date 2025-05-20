import java.awt.*;
import javax.swing.*;

public class Coin {
    private int x, y;
    private Image coinImage;

    public Coin(int x, int y) {
        this.x = x;
        this.y = y;
        loadImage();
    }

    private void loadImage() {
        try {
            coinImage = new ImageIcon(getClass().getResource("/assets/coin.png")).getImage();
        } catch (Exception e) {
            System.out.println("Failed to load coin image.");
            coinImage = null;
        }
    }

    public void move(double speedMultiplier) {
        y += 5 * speedMultiplier;
    }

    public void draw(Graphics g) {
        if (coinImage != null) {
            g.drawImage(coinImage, x, y, 30, 30, null);
        } else {
            g.setColor(Color.YELLOW);
            g.fillOval(x, y, 30, 30);
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 30, 30);
    }

    public void playCoinSound() {
        if (SettingsStorage.isSfxOn()) {
            SoundPlayer.getInstance().playSound("C:\\Users\\Emman\\Desktop\\2ND YEAR\\2ND SEM\\CC 221\\SOUND\\BGM\\coin.wav", false);
        }
    }
}
