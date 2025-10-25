package naq.sm4.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import naq.sm4.data.SettingsState;
import naq.sm4.data.SettingsState.VibrationLevel;

/**
 * Centralised manager that persists {@link SettingsState} using {@link SharedPreferences} and
 * exposes helpers to read or update individual values.
 */
public final class SettingsManager {

    private static final String PREFS_NAME = "naq.sm4.settings";
    private static final String KEY_SOUND = "sound_enabled";
    private static final String KEY_VIBRATION = "vibration_level";
    private static final String KEY_SCREEN_DIM = "screen_dim_percent";

    private static SettingsManager instance;
    private SettingsState cachedState;

    private SettingsManager() {
    }

    public static synchronized SettingsManager getInstance() {
        if (instance == null) {
            instance = new SettingsManager();
        }
        return instance;
    }

    @NonNull
    public synchronized SettingsState getSettings(@NonNull Context context) {
        if (cachedState == null) {
            cachedState = load(context.getApplicationContext());
        }
        return cachedState;
    }

    @NonNull
    public synchronized SettingsState updateSoundEnabled(@NonNull Context context, boolean enabled) {
        SettingsState current = getSettings(context);
        cachedState = current.withSoundEnabled(enabled);
        persist(context.getApplicationContext(), cachedState);
        return cachedState;
    }

    @NonNull
    public synchronized SettingsState updateVibrationLevel(@NonNull Context context, @NonNull VibrationLevel level) {
        SettingsState current = getSettings(context);
        cachedState = current.withVibration(level);
        persist(context.getApplicationContext(), cachedState);
        return cachedState;
    }

    @NonNull
    public synchronized SettingsState updateScreenDim(@NonNull Context context, int percent) {
        int clamped = Math.max(0, Math.min(100, percent));
        SettingsState current = getSettings(context);
        cachedState = current.withScreenDimPercent(clamped);
        persist(context.getApplicationContext(), cachedState);
        return cachedState;
    }

    @NonNull
    private SettingsState load(@NonNull Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean soundEnabled = prefs.getBoolean(KEY_SOUND, true);
        String vibrationName = prefs.getString(KEY_VIBRATION, VibrationLevel.LOW.name());
        int screenDimPercent = prefs.getInt(KEY_SCREEN_DIM, 30);
        VibrationLevel vibrationLevel;
        try {
            vibrationLevel = VibrationLevel.valueOf(vibrationName);
        } catch (IllegalArgumentException e) {
            vibrationLevel = VibrationLevel.LOW;
        }
        return new SettingsState(vibrationLevel, soundEnabled, screenDimPercent);
    }

    private void persist(@NonNull Context context, @NonNull SettingsState state) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean(KEY_SOUND, state.isSoundEnabled())
                .putString(KEY_VIBRATION, state.getVibrationLevel().name())
                .putInt(KEY_SCREEN_DIM, state.getScreenDimPercent())
                .apply();
    }
}
