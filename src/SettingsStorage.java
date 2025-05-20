import java.io.*;
import java.util.Properties;

public class SettingsStorage {
    private static final String FILE_PATH = "settings.properties";
    private static final Properties properties = new Properties();

    static {
        loadSettings();
    }
    

    public static boolean isSoundOn() {
        return Boolean.parseBoolean(properties.getProperty("soundOn", "true"));
    }

    public static boolean isSfxOn() {
        return Boolean.parseBoolean(properties.getProperty("sfxOn", "true"));
    }

    public static void setSoundOn(boolean soundOn) {
        properties.setProperty("soundOn", String.valueOf(soundOn));
        saveSettings();
    }

    public static void setSfxOn(boolean sfxOn) {
        properties.setProperty("sfxOn", String.valueOf(sfxOn));
        saveSettings();
    }

    private static void loadSettings() {
        try (InputStream input = new FileInputStream(FILE_PATH)) {
            properties.load(input);
        } catch (IOException e) {
            // Default settings will be used
        }
    }

    private static void saveSettings() {
        try (OutputStream output = new FileOutputStream(FILE_PATH)) {
            properties.store(output, "Game Sound Settings");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
