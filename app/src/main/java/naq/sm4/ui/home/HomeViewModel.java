package naq.sm4.ui.home;

import android.app.Application;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import naq.sm4.R;
import naq.sm4.core.storage.ConfigRepository;
import naq.sm4.core.storage.StorageHelper;
import naq.sm4.data.MeditationConfig;
import naq.sm4.data.MeditationStage;

public class HomeViewModel extends AndroidViewModel {

    private static final String TAG = "HomeViewModel";

    private final MutableLiveData<List<MeditationConfig>> configsLiveData = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> messageLiveData = new MutableLiveData<>(null);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>(null);

    private final ConfigRepository configRepository = new ConfigRepository();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    private MeditationConfig pendingEdit;
    private MeditationConfig activeSession;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        loadConfigs();
    }

    public LiveData<List<MeditationConfig>> getConfigs() {
        return configsLiveData;
    }

    public LiveData<Boolean> isLoading() {
        return loadingLiveData;
    }

    public LiveData<String> getMessages() {
        return messageLiveData;
    }

    public LiveData<String> getErrors() {
        return errorLiveData;
    }

    public void clearMessage() {
        messageLiveData.setValue(null);
    }

    public void clearError() {
        errorLiveData.setValue(null);
    }

    @MainThread
    public void loadConfigs() {
        if (initialized.compareAndSet(false, true)) {
            loadingLiveData.setValue(true);
        }
        executor.execute(() -> {
            List<MeditationConfig> configs = configRepository.loadConfigs(getApplication());
            if (configs.isEmpty()) {
                MeditationConfig defaultConfig = buildDefaultConfig(getApplication().getString(R.string.label_config_name));
                configs.add(defaultConfig);
                configRepository.saveConfigs(getApplication(), configs);
            }
            configsLiveData.postValue(new ArrayList<>(configs));
            loadingLiveData.postValue(false);
        });
    }

    @MainThread
    public void addConfig(@NonNull String name) {
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            trimmed = getApplication().getString(R.string.label_config_name);
        }
        List<MeditationStage> defaultStages = createDefaultStages();
        MeditationConfig newConfig = new MeditationConfig(trimmed, calculateTotal(defaultStages), defaultStages);
        persistAndUpdate(list -> {
            list.add(0, newConfig);
            messageLiveData.postValue(getApplication().getString(R.string.home_config_added, newConfig.getName()));
        });
    }

    @MainThread
    public void deleteConfig(@NonNull MeditationConfig config) {
        persistAndUpdate(list -> {
            if (list.remove(config)) {
                messageLiveData.postValue(getApplication().getString(R.string.home_config_deleted, config.getName()));
            }
        });
    }

    public void setPendingEdit(@Nullable MeditationConfig config) {
        pendingEdit = config;
    }

    @Nullable
    public MeditationConfig getPendingEdit() {
        return pendingEdit;
    }

    /**
     * Marks the provided config as the next session to start when the timer screen opens.
     */
    public void setActiveSession(@NonNull MeditationConfig config) {
        activeSession = config;
    }

    /**
     * Returns and clears the active session so it is consumed exactly once by the timer.
     */
    @Nullable
    public MeditationConfig consumeActiveSession() {
        MeditationConfig session = activeSession;
        activeSession = null;
        return session;
    }

    @NonNull
    public List<MeditationStage> buildDefaultStages() {
        return new ArrayList<>(createDefaultStages());
    }

    @NonNull
    public MeditationConfig buildDefaultConfig(@NonNull String name) {
        List<MeditationStage> stages = createDefaultStages();
        return new MeditationConfig(name, calculateTotal(stages), stages);
    }

    public void saveConfig(@NonNull MeditationConfig updatedConfig, @Nullable MeditationConfig original) {
        persistAndUpdate(list -> {
            if (original != null) {
                int index = list.indexOf(original);
                if (index >= 0) {
                    list.set(index, updatedConfig);
                } else {
                    list.add(0, updatedConfig);
                }
            } else {
                list.add(0, updatedConfig);
            }
            messageLiveData.postValue(getApplication().getString(R.string.action_save));
        });
    }

    private void persistAndUpdate(@NonNull ListUpdateAction action) {
        loadingLiveData.setValue(true);
        executor.execute(() -> {
            List<MeditationConfig> current = new ArrayList<>(configsLiveData.getValue() == null
                    ? Collections.emptyList() : configsLiveData.getValue());
            action.update(current);
            boolean saved = configRepository.saveConfigs(getApplication(), current);
            if (saved) {
                configsLiveData.postValue(new ArrayList<>(current));
                loadingLiveData.postValue(false);
            } else {
                loadingLiveData.postValue(false);
                errorLiveData.postValue(getApplication().getString(R.string.error_save_config));
            }
        });
    }

    private List<MeditationStage> createDefaultStages() {
        List<String> sounds = new ArrayList<>();
        try {
            List<String> stored = StorageHelper.listAudioFileNamesSorted();
            if (!stored.isEmpty()) {
                sounds.add(stored.get(0));
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to read audio files", e);
        }
        if (sounds.isEmpty()) {
            sounds.add("bell_start.mp3");
        }
        List<MeditationStage> stages = new ArrayList<>();
        stages.add(new MeditationStage("Khởi động", 5, 0, sounds));
        stages.add(new MeditationStage("Thiền sâu", 15, 5, new ArrayList<>(sounds)));
        stages.add(new MeditationStage("Thư giãn", 10, 0, new ArrayList<>(sounds)));
        return stages;
    }

    private int calculateTotal(@NonNull List<MeditationStage> stages) {
        int total = 0;
        for (MeditationStage stage : stages) {
            total += stage.getMinutes();
        }
        return Math.max(1, total);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdownNow();
    }

    private interface ListUpdateAction {
        void update(@NonNull List<MeditationConfig> configs);
    }
}