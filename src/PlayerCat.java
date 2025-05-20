import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.sql.*;

public class PlayerCat {
    private int x, y;
    private int dx = 0;
    private Image catImage;

    private final int playerId;

    public PlayerCat(int x, int y) {
        this.x = x;
        this.y = y;
        this.playerId = PlayerData.getLoggedInPlayerId(); // Now properly gets the int ID
        loadSelectedCatSkin(); // Loads image dynamically from DB
    }

    private void loadImage(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                catImage = new ImageIcon(imagePath).getImage();
            } else {
                System.out.println("Image not found: " + imagePath);
                catImage = new ImageIcon(getClass().getResource("/assets/ginger.png")).getImage(); // fallback
            }
        } catch (Exception e) {
            System.out.println("Error loading image: " + imagePath);
            e.printStackTrace();
            catImage = new ImageIcon(getClass().getResource("/assets/ginger.png")).getImage(); // fallback
        }
    }

    private void loadSelectedCatSkin() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/game.db", "root", "");
             PreparedStatement ps1 = conn.prepareStatement("SELECT selected_catskin FROM playerdata WHERE player_id = ? ORDER BY played_at DESC LIMIT 1");
             PreparedStatement ps2 = conn.prepareStatement("SELECT image_path FROM catskins WHERE catskin_id = ?")) {

            ps1.setInt(1, playerId);
            ResultSet rs1 = ps1.executeQuery();

            if (rs1.next()) {
                int catskinId = rs1.getInt("selected_catskin");

                ps2.setInt(1, catskinId);
                ResultSet rs2 = ps2.executeQuery();

                if (rs2.next()) {
                    String imagePath = rs2.getString("image_path");
                    loadImage(imagePath);
                    System.out.println("Loaded cat skin: " + imagePath);
                } else {
                    System.out.println("No image found for catskin ID: " + catskinId);
                    loadImage("assets/ginger.png");
                }
            } else {
                System.out.println("No playerdata found for player ID: " + playerId);
                loadImage("assets/ginger.png");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            loadImage("assets/ginger.png");
        }
    }

    public void update() {
        x += dx;
        if (x < 0) x = 0;
        if (x > 550) x = 550;
    }

    public boolean collects(PlusTen plusTen) {
        return getBounds().intersects(plusTen.getBounds());
    }

    public void draw(Graphics g) {
        if (catImage != null) {
            g.drawImage(catImage, x, y, 50, 50, null);
        } else {
            g.setColor(Color.ORANGE);
            g.fillRect(x, y, 50, 50);
        }
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) dx = -5;
        if (key == KeyEvent.VK_RIGHT) dx = 5;
    }

    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT) dx = 0;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 50, 50);
    }

    public boolean collidesWith(ObstacleDog dog) {
        return getBounds().intersects(dog.getBounds());
    }

    public boolean collects(Coin coin) {
        return getBounds().intersects(coin.getBounds());
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return 50; }
    public int getHeight() { return 50; }
}
