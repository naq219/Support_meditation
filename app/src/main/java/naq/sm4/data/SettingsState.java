package naq.sm4.data;

/**
 * Immutable snapshot of the persisted user settings that influence timer behaviour and UI chrome.
 * Instances are created and replaced wholesale whenever the user changes a setting.
 */
public class SettingsState {

    private final int vibrationStrengthPercent;
    private final boolean soundEnabled;
    private final int screenBrightnessPercent;

    /**
     * Constructs a new settings snapshot clamping numeric values to the valid range.
     *
     * @param vibrationStrengthPercent vibration amplitude in percent (0 - 100)
     * @param soundEnabled             {@code true} if stage cues should play audio
     * @param screenBrightnessPercent  screen brightness in percent (0 - 100)
     */
    public SettingsState(int vibrationStrengthPercent, boolean soundEnabled, int screenBrightnessPercent) {
        this.vibrationStrengthPercent = clampPercent(vibrationStrengthPercent);
        this.soundEnabled = soundEnabled;
        this.screenBrightnessPercent = clampPercent(screenBrightnessPercent);
    }

    /**
     * @return vibration intensity the user selected.
     */
    public int getVibrationStrengthPercent() {
        return vibrationStrengthPercent;
    }

    /**
     * @return {@code true} when audio cues are enabled.
     */
    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    /**
     * @return brightness level to apply while the timer is visible.
     */
    public int getScreenBrightnessPercent() {
        return screenBrightnessPercent;
    }

    /**
     * @return a copy with updated vibration strength.
     */
    public SettingsState withVibrationStrength(int percent) {
        return new SettingsState(percent, soundEnabled, screenBrightnessPercent);
    }

    /**
     * @return a copy with updated sound toggle.
     */
    public SettingsState withSoundEnabled(boolean enabled) {
        return new SettingsState(vibrationStrengthPercent, enabled, screenBrightnessPercent);
    }

    /**
     * @return a copy with updated brightness level.
     */
    public SettingsState withScreenBrightnessPercent(int percent) {
        return new SettingsState(vibrationStrengthPercent, soundEnabled, percent);
    }

    private int clampPercent(int value) {
        if (value < 0) {
            return 0;
        }
        if (value > 100) {
            return 100;
        }
        return value;
    }
}
