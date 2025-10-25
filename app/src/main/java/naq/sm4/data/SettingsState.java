package naq.sm4.data;

public class SettingsState {

    private final int vibrationStrengthPercent;
    private final boolean soundEnabled;
    private final int screenBrightnessPercent;

    public SettingsState(int vibrationStrengthPercent, boolean soundEnabled, int screenBrightnessPercent) {
        this.vibrationStrengthPercent = clampPercent(vibrationStrengthPercent);
        this.soundEnabled = soundEnabled;
        this.screenBrightnessPercent = clampPercent(screenBrightnessPercent);
    }

    public int getVibrationStrengthPercent() {
        return vibrationStrengthPercent;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public int getScreenBrightnessPercent() {
        return screenBrightnessPercent;
    }

    public SettingsState withVibrationStrength(int percent) {
        return new SettingsState(percent, soundEnabled, screenBrightnessPercent);
    }

    public SettingsState withSoundEnabled(boolean enabled) {
        return new SettingsState(vibrationStrengthPercent, enabled, screenBrightnessPercent);
    }

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
