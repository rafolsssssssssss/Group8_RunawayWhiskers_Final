import java.awt.*;
import javax.swing.*;
import java.util.Random;

public class ObstacleDog {
    private int x, y;
    private Image dogImage;

    public int getY() {
        return this.y;
    }

    public ObstacleDog(int x, int y) {
        this.x = x;
        this.y = y;
        loadRandomImage();
    }

    private void loadRandomImage() {
        Random rand = new Random();
        int choice = rand.nextInt(3) + 1; // 1 to 3

        String imagePath = "/assets/dog" + choice + ".png";
        try {
            dogImage = new ImageIcon(getClass().getResource(imagePath)).getImage();
        } catch (Exception e) {
            System.out.println("Failed to load dog image: " + imagePath);
            dogImage = null;
        }
    }

    public void move(double speedMultiplier) {
        y += 5 * speedMultiplier;
    }

    public void draw(Graphics g) {
        if (dogImage != null) {
            g.drawImage(dogImage, x, y, 50, 50, null);
        } else {
            g.setColor(Color.RED);
            g.fillRect(x, y, 50, 50); // fallback
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 50, 50);
    }

    public int getX() {
        return x;
    }

}


