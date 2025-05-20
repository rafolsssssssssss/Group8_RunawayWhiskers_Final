import javax.sound.sampled.*;
import java.io.File;

public class SoundPlayer {
    private static SoundPlayer instance;
    private Clip clip;
    private String currentFile;

    private SoundPlayer() {}

    public static synchronized SoundPlayer getInstance() {
        if (instance == null) {
            instance = new SoundPlayer();
        }
        return instance;
    }

public synchronized void playSound(String filePath, boolean loop) {
    try {
        if (clip != null && clip.isRunning()) {
            if (filePath.equals(currentFile) && loop) {
                return;
            }
            clip.stop();
            clip.close();
        }

        File audioFile = new File(filePath);
        if (!audioFile.exists()) {
            System.err.println("Audio file not found: " + filePath);
            return;
        }

        AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);

        clip = AudioSystem.getClip();
        clip.open(audioStream);
        currentFile = filePath;

        if (loop) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        } else {
            clip.start();
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
        }
    } catch (Exception e) {
        System.err.println("Error playing sound: " + filePath);
        e.printStackTrace();
    }
}

    public synchronized void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
            currentFile = null;
        }
    }

    public boolean isPlaying() {
        return clip != null && clip.isRunning();
    }
}
