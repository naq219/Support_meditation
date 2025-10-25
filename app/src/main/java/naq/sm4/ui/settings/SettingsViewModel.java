package naq.sm4.ui.settings;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import naq.sm4.data.SettingsState;

public class SettingsViewModel extends AndroidViewModel {

    private final MutableLiveData<SettingsState> settingsLiveData = new MutableLiveData<>();
    private final SettingsManager settingsManager;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        settingsManager = SettingsManager.getInstance();
        settingsLiveData.setValue(settingsManager.getSettings(application));
    }

    public LiveData<SettingsState> getSettings() {
        return settingsLiveData;
    }

    public void updateVibrationStrength(int percent) {
        SettingsState updated = settingsManager.updateVibrationStrength(getApplication(), percent);
        settingsLiveData.setValue(updated);
    }

    public void updateSoundEnabled(boolean enabled) {
        SettingsState updated = settingsManager.updateSoundEnabled(getApplication(), enabled);
        settingsLiveData.setValue(updated);
    }

    public void updateScreenBrightness(int percent) {
        SettingsState updated = settingsManager.updateScreenBrightness(getApplication(), percent);
        settingsLiveData.setValue(updated);
    }
}
