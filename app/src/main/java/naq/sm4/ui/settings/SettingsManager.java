package naq.sm4.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import naq.sm4.data.SettingsState;

/**
 * Centralised manager that persists {@link SettingsState} using {@link SharedPreferences} and
 * exposes helpers to read or update individual values.
 */
public final class SettingsManager {

    private static final String PREFS_NAME = "naq.sm4.settings";
    private static final String KEY_SOUND = "sound_enabled";
    private static final String KEY_VIBRATION_STRENGTH = "vibration_strength_percent";
    private static final String KEY_SCREEN_BRIGHTNESS = "screen_brightness_percent";

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
    public synchronized SettingsState updateVibrationStrength(@NonNull Context context, int percent) {
        SettingsState current = getSettings(context);
        cachedState = current.withVibrationStrength(percent);
        persist(context.getApplicationContext(), cachedState);
        return cachedState;
    }

    @NonNull
    public synchronized SettingsState updateScreenBrightness(@NonNull Context context, int percent) {
        int clamped = Math.max(0, Math.min(100, percent));
        SettingsState current = getSettings(context);
        cachedState = current.withScreenBrightnessPercent(clamped);
        persist(context.getApplicationContext(), cachedState);
        return cachedState;
    }

    @NonNull
    private SettingsState load(@NonNull Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean soundEnabled = prefs.getBoolean(KEY_SOUND, true);
        int vibrationStrength = prefs.getInt(KEY_VIBRATION_STRENGTH, 50);
        int screenBrightness = prefs.getInt(KEY_SCREEN_BRIGHTNESS, 30);
        return new SettingsState(vibrationStrength, soundEnabled, screenBrightness);
    }

    private void persist(@NonNull Context context, @NonNull SettingsState state) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean(KEY_SOUND, state.isSoundEnabled())
                .putInt(KEY_VIBRATION_STRENGTH, state.getVibrationStrengthPercent())
                .putInt(KEY_SCREEN_BRIGHTNESS, state.getScreenBrightnessPercent())
                .apply();
    }
}
