package naq.sm4.core.storage;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Lightweight façade around {@link StorageHelper} that exposes sound library operations to
 * view-models without leaking low-level file-system details.
 */
public class SoundFileRepository {

    private static final String TAG = "SoundRepo";

    /**
     * @return sorted list of available audio file names in the working directory.
     */
    public List<String> loadSoundFiles() {
        try {
            return StorageHelper.listAudioFileNamesSorted();
        } catch (IOException e) {
            Log.e(TAG, "Failed to list audio files", e);
            return Collections.emptyList();
        }
    }

    /**
     * Imports a sound file from the given {@link Uri} into the working directory.
     */
    public String importSound(@NonNull Context context, @NonNull Uri sourceUri, @NonNull String desiredName) {
        try {
            return StorageHelper.copyToWorkingDirectory(context, sourceUri, desiredName);
        } catch (IOException e) {
            Log.e(TAG, "Failed to copy sound file", e);
            return "";
        }
    }

    /**
     * Removes the identified sound file if it exists.
     */
    public boolean deleteSound(@NonNull String fileName) {
        try {
            return StorageHelper.deleteSound(fileName);
        } catch (IOException e) {
            Log.e(TAG, "Failed to delete sound" + fileName, e);
            return false;
        }
    }

    /**
     * @return {@code true} when the file name has a supported audio extension.
     */
    public boolean isSupported(@NonNull String fileName) {
        return StorageHelper.isSupportedAudioFile(fileName);
    }
}
