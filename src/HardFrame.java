import javax.swing.*;

public class HardFrame extends JFrame {
    public HardFrame() {
        setTitle("Endless Cat Racing");
        setSize(600, 800); //setSize(800, 600); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        HardCorePanel hardPanel = new HardCorePanel(this);
        add(hardPanel);
        hardPanel.startGameLoop();
        
    }
}
