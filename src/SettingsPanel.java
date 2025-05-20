
import javax.swing.*;
import java.awt.*;

public class SettingsPanel extends JPanel {
    private JRadioButton soundOnButton;
    private JRadioButton soundOffButton;
    private JRadioButton sfxOnButton;
    private JRadioButton sfxOffButton;

    private AudioManager bgmManager;
    private SoundPlayer sfxPlayer;

    public SettingsPanel() {
        bgmManager = AudioManager.getInstance();
        sfxPlayer = SoundPlayer.getInstance();

        initComponents();

        // Set radio buttons from saved settings
        soundOnButton.setSelected(SettingsStorage.isSoundOn());
        soundOffButton.setSelected(!SettingsStorage.isSoundOn());
        sfxOnButton.setSelected(SettingsStorage.isSfxOn());
        sfxOffButton.setSelected(!SettingsStorage.isSfxOn());

        // Play BGM only if sound is ON and not already playing
        if (SettingsStorage.isSoundOn() && !bgmManager.isPlaying()) {
            bgmManager.play();
        }
    }

    private void initComponents() {
        Font rubikFont = new Font("Rubik", Font.PLAIN, 18);
        Color bgColor = new Color(102, 0, 153);
        setLayout(new BorderLayout(10, 10));
        setBackground(bgColor);

        JPanel soundPanel = new JPanel(new GridBagLayout());
        soundPanel.setBackground(bgColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Sound Label and Buttons
        JLabel soundLabel = new JLabel("Sound:");
        soundLabel.setFont(rubikFont);
        soundLabel.setForeground(Color.WHITE);

        soundOnButton = new JRadioButton("On");
        soundOffButton = new JRadioButton("Off");
        configureRadioButton(soundOnButton, rubikFont);
        configureRadioButton(soundOffButton, rubikFont);

        ButtonGroup soundGroup = new ButtonGroup();
        soundGroup.add(soundOnButton);
        soundGroup.add(soundOffButton);

        JPanel soundButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        soundButtonsPanel.setOpaque(false);
        soundButtonsPanel.add(soundOnButton);
        soundButtonsPanel.add(soundOffButton);

        gbc.gridx = 0; gbc.gridy = 0;
        soundPanel.add(soundLabel, gbc);
        gbc.gridx = 1;
        soundPanel.add(soundButtonsPanel, gbc);

        // SFX Label and Buttons
        JLabel sfxLabel = new JLabel("SFX:");
        sfxLabel.setFont(rubikFont);
        sfxLabel.setForeground(Color.WHITE);

        sfxOnButton = new JRadioButton("On");
        sfxOffButton = new JRadioButton("Off");
        configureRadioButton(sfxOnButton, rubikFont);
        configureRadioButton(sfxOffButton, rubikFont);

        ButtonGroup sfxGroup = new ButtonGroup();
        sfxGroup.add(sfxOnButton);
        sfxGroup.add(sfxOffButton);

        JPanel sfxButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        sfxButtonsPanel.setOpaque(false);
        sfxButtonsPanel.add(sfxOnButton);
        sfxButtonsPanel.add(sfxOffButton);

        gbc.gridx = 0; gbc.gridy = 1;
        soundPanel.add(sfxLabel, gbc);
        gbc.gridx = 1;
        soundPanel.add(sfxButtonsPanel, gbc);

        // Buttons Panel
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setBackground(bgColor);
        JButton saveButton = new JButton("Save");
        saveButton.setFont(rubikFont);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(rubikFont);

        buttonsPanel.add(saveButton);
        buttonsPanel.add(cancelButton);

        // Save Button ActionListener
        saveButton.addActionListener(e -> {
            // Read current radio button selections and save to SettingsStorage
            boolean soundOn = soundOnButton.isSelected();
            boolean sfxOn = sfxOnButton.isSelected();

            SettingsStorage.setSoundOn(soundOn);
            SettingsStorage.setSfxOn(sfxOn);

            // Handle BGM play/stop based on new settings
            if (soundOn) {
                if (!bgmManager.isPlaying()) {
                    bgmManager.play();
                }
                bgmManager.unmute();
            } else {
                bgmManager.mute();
                bgmManager.stop();
            }

            // Handle SFX player stop if disabled
            if (!sfxOn) {
                sfxPlayer.stop();
            }

            JOptionPane.showMessageDialog(this, "Settings saved!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        // Cancel Button ActionListener
        cancelButton.addActionListener(e -> {
            // Revert radio buttons to saved settings without saving
            soundOnButton.setSelected(SettingsStorage.isSoundOn());
            soundOffButton.setSelected(!SettingsStorage.isSoundOn());
            sfxOnButton.setSelected(SettingsStorage.isSfxOn());
            sfxOffButton.setSelected(!SettingsStorage.isSfxOn());

            JOptionPane.showMessageDialog(this, "Changes canceled.", "Notice", JOptionPane.INFORMATION_MESSAGE);
        });

        add(soundPanel, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);
    }

    private void configureRadioButton(JRadioButton button, Font font) {
        button.setFont(font);
        button.setForeground(Color.WHITE);
        button.setOpaque(false);
    }
}