import javax.swing.*;

public class TimeFrame extends JFrame {
    public TimeFrame() {
        setTitle("Endless Cat Racing");
        setSize(600, 800); //setSize(800, 600); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        TimeRushPanel timePanel = new TimeRushPanel(this);
        add(timePanel);
        timePanel.startGameLoop();
        
    }
}
