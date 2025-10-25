package naq.sm4.ui.home;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import naq.sm4.data.MeditationConfig;
import naq.sm4.data.MeditationStage;
import naq.sm4.data.MockData;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<List<MeditationConfig>> configsLiveData = new MutableLiveData<>();

    public HomeViewModel() {
        try {
            List<MeditationConfig> configs = MockData.getMeditationConfigs();
            if (configs == null) {
                configs = new ArrayList<>();
                Log.e("HomeViewModel", "MockData.getMeditationConfigs() returned null");
            }
            configsLiveData.setValue(new ArrayList<>(configs));
        } catch (Exception e) {
            Log.e("HomeViewModel", "Error initializing ViewModel", e);
            configsLiveData.setValue(new ArrayList<>());
        }
    }

    public LiveData<List<MeditationConfig>> getConfigs() {
        return configsLiveData;
    }

    public void addConfig(@NonNull String name) {
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            trimmed = "Buổi thiền mới";
        }
        List<MeditationStage> templateStages = MockData.getTemplateStages();
        if (templateStages == null) {
            templateStages = new ArrayList<>();
            Log.e("HomeViewModel", "MockData.getTemplateStages() returned null");
        }
        int totalMinutes = calculateTotal(templateStages);
        MeditationConfig newConfig = new MeditationConfig(trimmed, totalMinutes, templateStages);
        List<MeditationConfig> current = new ArrayList<>(ensureList());
        current.add(0, newConfig);
        configsLiveData.setValue(current);
    }

    public void deleteConfig(@NonNull MeditationConfig config) {
        List<MeditationConfig> current = new ArrayList<>(ensureList());
        current.remove(config);
        configsLiveData.setValue(current);
    }

    private List<MeditationConfig> ensureList() {
        List<MeditationConfig> current = configsLiveData.getValue();
        return current == null ? new ArrayList<>() : current;
    }

    private int calculateTotal(List<MeditationStage> stages) {
        int total = 0;
        for (MeditationStage stage : stages) {
            total += stage.getMinutes();
        }
        return total;
    }
}