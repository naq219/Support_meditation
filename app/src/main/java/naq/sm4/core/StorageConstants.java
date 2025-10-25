package naq.sm4.core;

public final class StorageConstants {

    private StorageConstants() {
    }

    public static final String WORKING_DIRECTORY = "/sdcard/Music/SupportMeditation/";
    public static final String DEFAULT_CONFIG_FILE = "configs.json";
    public static final String CONFIG_FILE_EXTENSION = ".json";

    public static final String[] SUPPORTED_AUDIO_EXTENSIONS = {
            ".mp3", ".wav", ".ogg", ".m4a"
    };
}
