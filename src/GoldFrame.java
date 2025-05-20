import javax.swing.*;

public class GoldFrame extends JFrame {
    public GoldFrame() {
        setTitle("Endless Cat Racing");
        setSize(600, 800); //setSize(800, 600); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        GoldRushPanel goldPanel = new GoldRushPanel(this);
        add(goldPanel);
        goldPanel.startGameLoop();
        
    }
}
