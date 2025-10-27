package naq.sm4.core.storage;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Lightweight façade around {@link StorageHelper} that exposes sound library operations to
 * view-models without leaking low-level file-system details.
 */
public class SoundFileRepository {

    private static final String TAG = "SoundRepo";
    private static final String[] DEFAULT_SOUNDS = {
        "biet_ro_toan_than.wav",
        "buong_long_2_tay.wav",
        "dung_tin_suy_nghi_minh.wav",
        "fadein_tieng_mua_va_cau_thien.wav",
        "khong_co_gi_la_ta.wav",
        "suy_nghi_chac_gi_dung.wav",
        "than_nay_khong_con_xuat_hien.wav",
        "than_nay_ngu_si.wav",
        "than_nay_roi_se_muc_nat.wav",
        "the_gian_la_huu_ao.wav"
    };
    
    private final Context context;
    
    public SoundFileRepository(Context context) {
        this.context = context.getApplicationContext();
    }

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
    
    /**
     * Initializes default sounds by copying them from raw resources to the working directory
     * if the working directory is empty.
     */
    public void initializeDefaultSounds() {
        try {
            // Check if working directory is empty
            List<String> existingSounds = loadSoundFiles();
            if (!existingSounds.isEmpty()) {
                return; // Already initialized
            }
            
            Log.i(TAG, "Initializing default sounds...");
            
            // Copy each default sound from raw to working directory
            for (String soundName : DEFAULT_SOUNDS) {
                String resourceName = soundName.substring(0, soundName.lastIndexOf('.'));
                int resId = context.getResources().getIdentifier(
                    resourceName, "raw", context.getPackageName());
                
                if (resId != 0) {
                    try (InputStream in = context.getResources().openRawResource(resId)) {
                        File workingDir = StorageHelper.ensureWorkingDirectory();
                        File targetFile = new File(workingDir, soundName);
                        StorageHelper.writeStreamToFile(targetFile, in);
                        Log.d(TAG, "Copied default sound: " + soundName);
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to copy sound: " + soundName, e);
                    }
                } else {
                    Log.w(TAG, "Sound resource not found: " + resourceName);
                }
            }
            
            Log.i(TAG, "Default sounds initialization completed");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize default sounds", e);
        }
    }
}
