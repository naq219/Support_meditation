package naq.sm4.ui.settings;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import naq.sm4.data.SettingsState;
import naq.sm4.data.SettingsState.VibrationLevel;

public class SettingsViewModel extends ViewModel {

    private final MutableLiveData<SettingsState> settingsLiveData = new MutableLiveData<>();

    public SettingsViewModel() {
        try {
            settingsLiveData.setValue(new SettingsState(VibrationLevel.LOW, true, 30));
        } catch (Exception e) {
            Log.e("SettingsVM", "Failed to initialize default settings", e);
            settingsLiveData.setValue(new SettingsState(VibrationLevel.OFF, true, 30));
        }
    }

    public LiveData<SettingsState> getSettings() {
        return settingsLiveData;
    }

    public void updateVibration(@NonNull VibrationLevel level) {
        SettingsState current = getCurrent();
        settingsLiveData.setValue(current.withVibration(level));
    }

    public void updateSoundEnabled(boolean enabled) {
        SettingsState current = getCurrent();
        settingsLiveData.setValue(current.withSoundEnabled(enabled));
    }

    public void updateScreenDim(int percent) {
        percent = Math.max(0, Math.min(100, percent));
        SettingsState current = getCurrent();
        settingsLiveData.setValue(current.withScreenDimPercent(percent));
    }

    private SettingsState getCurrent() {
        SettingsState current = settingsLiveData.getValue();
        if (current == null) {
            current = new SettingsState(VibrationLevel.OFF, true, 30);
            settingsLiveData.setValue(current);
        }
        return current;
    }
}
