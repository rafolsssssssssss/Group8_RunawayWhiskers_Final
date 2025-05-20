import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class ShopPanel extends JPanel {
    private int playerId;
    private int playerCoins;
    private JLabel coinLabel;

    private JPanel catsGrid;
    private JPanel mapsGrid;

    private Set<Integer> ownedCatSkinIds = new HashSet<>();
    private Set<Integer> ownedMapIds = new HashSet<>();

    private final String DB_URL = "jdbc:mysql://localhost:3306/game.db";
    private final String DB_USER = "root";
    private final String DB_PASS = "";

    public ShopPanel(int playerId) {
        this.playerId = playerId;
        setLayout(new BorderLayout());
        setBackground(new Color(30, 30, 30));

        // Top panel
        coinLabel = new JLabel("Coins: Loading...");
        coinLabel.setFont(new Font("Rubik", Font.PLAIN, 22));
        coinLabel.setForeground(Color.WHITE);
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(new Color(20, 20, 20));
        topPanel.add(coinLabel);
        add(topPanel, BorderLayout.NORTH);

        // Center panel (scrollable content)
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(new Color(30, 30, 30));

        // Cat Skins Section
        JLabel catsTitle = new JLabel("Cat Skins");
        catsTitle.setForeground(Color.WHITE);
        catsTitle.setFont(new Font("Rubik", Font.BOLD, 20));
        catsTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(catsTitle);
        centerPanel.add(Box.createVerticalStrut(5));

        catsGrid = new JPanel(new GridLayout(0, 3, 15, 15));
        catsGrid.setBackground(new Color(30, 30, 30));
        catsGrid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        centerPanel.add(catsGrid);
        centerPanel.add(Box.createVerticalStrut(20));

        // Maps Section
        JLabel mapsTitle = new JLabel("Maps");
        mapsTitle.setForeground(Color.WHITE);
        mapsTitle.setFont(new Font("Rubik", Font.BOLD, 20));
        mapsTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(mapsTitle);
        centerPanel.add(Box.createVerticalStrut(5));

        mapsGrid = new JPanel(new GridLayout(0, 3, 15, 15));
        mapsGrid.setBackground(new Color(30, 30, 30));
        mapsGrid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        centerPanel.add(mapsGrid);

        // Add scrollable centerPanel
        JScrollPane scrollPane = new JScrollPane(centerPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);

        loadPlayerData();
    }

    private void loadPlayerData() {
        ownedCatSkinIds.clear();
        ownedMapIds.clear();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT IFNULL((SELECT SUM(coin) FROM playerdata WHERE player_id = ?),0) + " +
                "IFNULL((SELECT SUM(coins_collected) FROM gamesessions WHERE player_id = ?),0) AS total_coins"
            );
            ps.setInt(1, playerId);
            ps.setInt(2, playerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                playerCoins = rs.getInt("total_coins");
            } else {
                playerCoins = 0;
            }
            coinLabel.setText("Coins: " + playerCoins);

            // Owned cats
            ps = conn.prepareStatement("SELECT catskin_id FROM unlockedcats WHERE player_id = ?");
            ps.setInt(1, playerId);
            rs = ps.executeQuery();
            while (rs.next()) {
                ownedCatSkinIds.add(rs.getInt("catskin_id"));
            }

            // Owned maps
            ps = conn.prepareStatement("SELECT map_id FROM unlockedmaps WHERE player_id = ?");
            ps.setInt(1, playerId);
            rs = ps.executeQuery();
            while (rs.next()) {
                ownedMapIds.add(rs.getInt("map_id"));
            }

            populateCats(conn);
            populateMaps(conn);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB Error: " + e.getMessage());
        }
    }

    private void populateCats(Connection conn) throws SQLException {
        catsGrid.removeAll();
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM catskins ORDER BY catskin_id");
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            int id = rs.getInt("catskin_id");
            String name = rs.getString("cat_skin");
            int price = rs.getInt("cat_skin_price");
            String imagePath = rs.getString("image_path");

            ImageIcon icon = createScaledIcon(imagePath, 140, 140);
            JPanel itemPanel = createItemPanel(id, name, price, icon, "catskin");
            catsGrid.add(itemPanel);
        }
        catsGrid.revalidate();
        catsGrid.repaint();
    }

    private void populateMaps(Connection conn) throws SQLException {
        mapsGrid.removeAll();
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM maps ORDER BY map_id");
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            int id = rs.getInt("map_id");
            String name = rs.getString("map_name");
            int price = rs.getInt("map_price");
            String imagePath = rs.getString("image_path");

            ImageIcon icon = createScaledIcon(imagePath, 140, 140);
            JPanel itemPanel = createItemPanel(id, name, price, icon, "map");
            mapsGrid.add(itemPanel);
        }
        mapsGrid.revalidate();
        mapsGrid.repaint();
    }

    private JPanel createItemPanel(int id, String name, int price, ImageIcon icon, String type) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(160, 220));
        panel.setBackground(new Color(40, 40, 40));
        panel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));

        JLabel imageLabel = new JLabel(icon, JLabel.CENTER);
        panel.add(imageLabel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(40, 40, 40));
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel(name);
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("Rubik", Font.BOLD, 16));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel priceLabel = new JLabel(price + " coins");
        priceLabel.setForeground(Color.LIGHT_GRAY);
        priceLabel.setFont(new Font("Rubik", Font.PLAIN, 14));
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton button;
        boolean owned = (type.equals("catskin") && ownedCatSkinIds.contains(id)) ||
                        (type.equals("map") && ownedMapIds.contains(id));
        if (owned) {
            button = new JButton("Use");
            button.setBackground(new Color(0, 153, 102));
            button.setForeground(Color.WHITE);
            button.addActionListener(e -> useItem(type, id));
        } else {
            button = new JButton("Buy");
            button.setBackground(new Color(102, 0, 153));
            button.setForeground(Color.WHITE);
            button.addActionListener(e -> buyItem(type, id, price, name));
        }
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        bottomPanel.add(Box.createVerticalStrut(6));
        bottomPanel.add(nameLabel);
        bottomPanel.add(priceLabel);
        bottomPanel.add(Box.createVerticalStrut(8));
        bottomPanel.add(button);
        bottomPanel.add(Box.createVerticalStrut(6));

        panel.add(bottomPanel, BorderLayout.SOUTH);
        return panel;
    }

    private ImageIcon createScaledIcon(String path, int width, int height) {
        ImageIcon icon = new ImageIcon(path);
        Image img = icon.getImage();
        return new ImageIcon(img.getScaledInstance(width, height, Image.SCALE_SMOOTH));
    }

    private void buyItem(String type, int id, int price, String name) {
        if (playerCoins < price) {
            JOptionPane.showMessageDialog(this, "You do not have enough coins.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            conn.setAutoCommit(false);

            PreparedStatement ps = conn.prepareStatement(
                "SELECT playerdata_id FROM playerdata WHERE player_id = ? ORDER BY played_at DESC LIMIT 1"
            );
            ps.setInt(1, playerId);
            ResultSet rs = ps.executeQuery();
            int dataId = -1;
            if (rs.next()) {
                dataId = rs.getInt("playerdata_id");
            } else {
                JOptionPane.showMessageDialog(this, "Player data not found!");
                return;
            }

            ps = conn.prepareStatement("UPDATE playerdata SET coin = coin - ? WHERE playerdata_id = ?");
            ps.setInt(1, price);
            ps.setInt(2, dataId);
            ps.executeUpdate();

            if (type.equals("catskin")) {
                ps = conn.prepareStatement("INSERT IGNORE INTO unlockedcats (player_id, catskin_id) VALUES (?, ?)");
                ps.setInt(1, playerId);
                ps.setInt(2, id);
                ps.executeUpdate();
                ownedCatSkinIds.add(id);
            } else {
                ps = conn.prepareStatement("INSERT IGNORE INTO unlockedmaps (player_id, map_id) VALUES (?, ?)");
                ps.setInt(1, playerId);
                ps.setInt(2, id);
                ps.executeUpdate();
                ownedMapIds.add(id);
            }

            conn.commit();
            playerCoins -= price;
            coinLabel.setText("Coins: " + playerCoins);
            JOptionPane.showMessageDialog(this, "Successfully purchased: " + name);
            loadPlayerData();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Purchase failed: " + e.getMessage());
        }
    }

    private void useItem(String type, int id) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT playerdata_id FROM playerdata WHERE player_id = ? ORDER BY played_at DESC LIMIT 1"
            );
            ps.setInt(1, playerId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Player data not found!");
                return;
            }

            int dataId = rs.getInt("playerdata_id");
            String column = type.equals("catskin") ? "selected_catskin" : "selected_map";
            ps = conn.prepareStatement("UPDATE playerdata SET " + column + " = ? WHERE playerdata_id = ?");
            ps.setInt(1, id);
            ps.setInt(2, dataId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Selected " + type + " applied!");

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to use item: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Shop");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(640, 750);
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);

            ShopPanel shopPanel = new ShopPanel(1); // Replace with actual playerId
            frame.add(shopPanel);
            frame.setVisible(true);
        });
    }
}
