package naq.sm4;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import naq.sm4.core.storage.SoundFileRepository;

/**
 * Custom Application class for initializing app-wide components.
 */
public class SupportMeditationApp extends Application {
    private static final String TAG = "SupportMeditationApp";
    private static SupportMeditationApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        
        // Initialize default sounds in a background thread to avoid ANR
        new Thread(this::initializeDefaultSounds).start();
    }

    /**
     * Initializes default sounds if they don't already exist in the working directory.
     */
    private void initializeDefaultSounds() {
        try {
            SoundFileRepository soundRepo = new SoundFileRepository(this);
            soundRepo.initializeDefaultSounds();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing default sounds", e);
        }
    }

    /**
     * @return the application instance
     */
    @NonNull
    public static SupportMeditationApp getInstance() {
        return instance;
    }
}
