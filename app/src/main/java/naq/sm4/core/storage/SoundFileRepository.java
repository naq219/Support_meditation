package naq.sm4.core.storage;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class SoundFileRepository {

    private static final String TAG = "SoundRepo";

    public List<String> loadSoundFiles() {
        try {
            return StorageHelper.listAudioFileNamesSorted();
        } catch (IOException e) {
            Log.e(TAG, "Failed to list audio files", e);
            return Collections.emptyList();
        }
    }

    public String importSound(@NonNull Context context, @NonNull Uri sourceUri, @NonNull String desiredName) {
        try {
            return StorageHelper.copyToWorkingDirectory(context, sourceUri, desiredName);
        } catch (IOException e) {
            Log.e(TAG, "Failed to copy sound file", e);
            return "";
        }
    }

    public boolean deleteSound(@NonNull String fileName) {
        try {
            return StorageHelper.deleteSound(fileName);
        } catch (IOException e) {
            Log.e(TAG, "Failed to delete sound" + fileName, e);
            return false;
        }
    }

    public boolean isSupported(@NonNull String fileName) {
        return StorageHelper.isSupportedAudioFile(fileName);
    }
}
