package naq.sm4.ui.sound;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import naq.sm4.R;
import naq.sm4.core.storage.ConfigRepository;
import naq.sm4.core.storage.SoundFileRepository;
import naq.sm4.core.storage.StorageHelper;
import naq.sm4.data.MeditationConfig;
import naq.sm4.data.MeditationStage;

public class SoundLibraryViewModel extends AndroidViewModel {

    private final MutableLiveData<List<String>> soundsLiveData = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>(null);
    private final MutableLiveData<String> messageLiveData = new MutableLiveData<>(null);
    private final MutableLiveData<DeletePreview> deletePreviewLiveData = new MutableLiveData<>();

    private final SoundFileRepository repository = new SoundFileRepository();
    private final ConfigRepository configRepository = new ConfigRepository();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private DeletePreview lastPreview;

    public SoundLibraryViewModel(@NonNull Application application) {
        super(application);
        refreshSounds();
    }

    public LiveData<List<String>> getSounds() {
        return soundsLiveData;
    }

    public LiveData<Boolean> isLoading() {
        return loadingLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorLiveData;
    }

    public LiveData<String> getMessages() {
        return messageLiveData;
    }

    public LiveData<DeletePreview> getDeletePreview() {
        return deletePreviewLiveData;
    }

    @MainThread
    public void refreshSounds() {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);
        messageLiveData.setValue(null);
        executor.execute(() -> {
            List<String> files = repository.loadSoundFiles();
            postResults(files, null);
        });
    }

    @MainThread
    public void importSound(@NonNull Uri sourceUri, @Nullable String desiredName) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);
        messageLiveData.setValue(null);
        executor.execute(() -> {
            String sanitized = StorageHelper.sanitizeFileName(desiredName);
            if (sanitized.isEmpty()) {
                sanitized = "sound";
            }
            String result = repository.importSound(getApplication(), sourceUri, sanitized);
            if (result.isEmpty()) {
                String error = getApplication().getString(R.string.library_import_error);
                postResults(null, error);
                return;
            }
            List<String> updated = repository.loadSoundFiles();
            postResults(updated, null);
            messageLiveData.postValue(getApplication().getString(R.string.library_added_message, result));
        });
    }

    @MainThread
    public void prepareDelete(@NonNull List<String> files) {
        if (files.isEmpty()) {
            return;
        }
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);
        executor.execute(() -> {
            List<String> inUse = findFilesInUse(files);
            DeletePreview preview = new DeletePreview(new ArrayList<>(files), inUse);
            lastPreview = preview;
            deletePreviewLiveData.postValue(preview);
            loadingLiveData.postValue(false);
        });
    }

    @MainThread
    public void executeDelete(boolean ignoreUsage) {
        DeletePreview preview = lastPreview;
        if (preview == null || preview.files.isEmpty()) {
            return;
        }
        if (!ignoreUsage && !preview.inUse.isEmpty()) {
            deletePreviewLiveData.postValue(preview);
            return;
        }
        loadingLiveData.setValue(true);
        executor.execute(() -> {
            boolean success = true;
            for (String file : preview.files) {
                if (!repository.deleteSound(file)) {
                    success = false;
                }
            }
            List<String> updated = repository.loadSoundFiles();
            if (success) {
                postResults(updated, null);
                messageLiveData.postValue(getApplication().getString(R.string.library_deleted_message, preview.files.size()));
            } else {
                String error = getApplication().getString(R.string.library_delete_partial_error);
                postResults(updated, error);
            }
            lastPreview = null;
        });
    }

    public void clearDeletePreview() {
        deletePreviewLiveData.postValue(null);
    }

    @MainThread
    public void clearMessage() {
        messageLiveData.setValue(null);
    }

    @MainThread
    public void clearError() {
        errorLiveData.setValue(null);
    }

    private void postResults(@Nullable List<String> files, @Nullable String error) {
        List<String> list = files;
        if (list == null) {
            list = repository.loadSoundFiles();
        }
        List<String> copy = new ArrayList<>(list);
        Collections.sort(copy, String::compareToIgnoreCase);
        soundsLiveData.postValue(copy);
        loadingLiveData.postValue(false);
        errorLiveData.postValue(error);
    }

    private List<String> findFilesInUse(@NonNull List<String> files) {
        if (files.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> targets = new HashSet<>(files);
        Set<String> inUse = new LinkedHashSet<>();
        List<MeditationConfig> configs = configRepository.loadConfigs(getApplication());
        for (MeditationConfig config : configs) {
            List<MeditationStage> stages = config.getStages();
            for (MeditationStage stage : stages) {
                for (String sound : stage.getSounds()) {
                    if (targets.contains(sound)) {
                        inUse.add(sound);
                    }
                }
            }
        }
        return new ArrayList<>(inUse);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdownNow();
    }

    public static class DeletePreview {
        private final List<String> files;
        private final List<String> inUse;

        DeletePreview(@NonNull List<String> files, @NonNull List<String> inUse) {
            this.files = files;
            this.inUse = inUse;
        }

        @NonNull
        public List<String> getFiles() {
            return files;
        }

        @NonNull
        public List<String> getInUse() {
            return inUse;
        }
    }
}
