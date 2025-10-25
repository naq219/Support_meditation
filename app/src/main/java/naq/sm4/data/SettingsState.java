package naq.sm4.data;

import androidx.annotation.NonNull;

public class SettingsState {

    public enum VibrationLevel {
        OFF,
        LOW,
        HIGH
    }

    private final VibrationLevel vibrationLevel;
    private final boolean soundEnabled;
    private final int screenDimPercent;

    public SettingsState(@NonNull VibrationLevel vibrationLevel, boolean soundEnabled, int screenDimPercent) {
        this.vibrationLevel = vibrationLevel;
        this.soundEnabled = soundEnabled;
        this.screenDimPercent = screenDimPercent;
    }

    @NonNull
    public VibrationLevel getVibrationLevel() {
        return vibrationLevel;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public int getScreenDimPercent() {
        return screenDimPercent;
    }

    public SettingsState withVibration(@NonNull VibrationLevel level) {
        return new SettingsState(level, soundEnabled, screenDimPercent);
    }

    public SettingsState withSoundEnabled(boolean enabled) {
        return new SettingsState(vibrationLevel, enabled, screenDimPercent);
    }

    public SettingsState withScreenDimPercent(int percent) {
        return new SettingsState(vibrationLevel, soundEnabled, percent);
    }
}
