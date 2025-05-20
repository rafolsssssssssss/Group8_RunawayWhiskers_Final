import java.awt.*;

public class PlusTen {
    private int x, y;
    private int size = 30;
    private final SoundPlayer sfxPlayer;

    public PlusTen(int x, int y) {
        this.x = x;
        this.y = y;
        this.sfxPlayer = SoundPlayer.getInstance(); // Singleton instance for sound
    }

    public void move(double speedMultiplier) {
        y += (int)(5 * speedMultiplier);
    }

    public void draw(Graphics g) {
        g.setColor(Color.ORANGE); // Different color than coin
        g.fillOval(x, y, size, size);

        g.setColor(Color.BLACK);
        g.setFont(new Font("Rubik", Font.BOLD, 12));
        g.drawString("+10", x + 4, y + 18); // Centered text
    }

    public int getY() {
        return y;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, size, size);
    }

    // Play sound effect for PlusTen collection
    public void playPlusTenSound() {
        if (SettingsStorage.isSfxOn()) {
            sfxPlayer.playSound("C:\\Users\\Emman\\Desktop\\2ND YEAR\\2ND SEM\\CC 221\\SOUND\\BGM\\plusten.wav", false);
        }
    }
}
