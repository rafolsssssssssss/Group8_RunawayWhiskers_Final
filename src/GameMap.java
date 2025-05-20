import javax.swing.*;
import java.awt.*;

public class GameMap extends JPanel {
private Image backgroundImage;

public GameMap(String imagePath) {
    // Load the background image from resources
    ImageIcon icon = new ImageIcon(imagePath);
    backgroundImage = icon.getImage();

    // Set panel size or preferred size based on image
    this.setPreferredSize(new Dimension(600, 800)); // Your game panel size
}

@Override
protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    // Draw background
    if (backgroundImage != null) {
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
    }
}
}
