import javax.swing.*;

public class App {
public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
        AuthFrame authFrame = new AuthFrame();
        authFrame.setVisible(true);
    }); }
}

