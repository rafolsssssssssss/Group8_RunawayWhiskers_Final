import javax.sound.sampled.*;

public class AudioManager {
    private static AudioManager instance;
    private Clip clip;
    private boolean isMuted = false;

    // Private constructor for singleton
    private AudioManager(String filePath) {
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(new java.io.File(filePath));
            clip = AudioSystem.getClip();
            clip.open(audioIn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Singleton getInstance method (lazy init)
    public static AudioManager getInstance() {
        if (instance == null) {
            // Set your BGM path here
            String BGM_PATH = "C:\\Users\\Emman\\Desktop\\2ND YEAR\\2ND SEM\\CC 221\\SOUND\\BGM\\bgm.wav";
            instance = new AudioManager(BGM_PATH);
        }
        return instance;
    }

    public boolean isPlaying() {
        return clip != null && clip.isRunning();
    }

    public void play() {
        if (clip != null && clip.isRunning()) {
            return; // already playing
        }
        if (!isMuted && clip != null) {
            clip.setFramePosition(0);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    public void mute() {
        isMuted = true;
        stop();
    }

    public void unmute() {
        isMuted = false;
    }
}
