package naq.sm4.ui.sound;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import naq.sm4.data.MockData;

class SoundLibraryViewModel extends ViewModel {

    private final MutableLiveData<List<String>> soundsLiveData = new MutableLiveData<>();

    SoundLibraryViewModel() {
        soundsLiveData.setValue(new ArrayList<>(MockData.getSoundFiles()));
    }

    LiveData<List<String>> getSounds() {
        return soundsLiveData;
    }

    void addSound(String rawName) {
        String sanitized = sanitizeName(rawName);
        if (sanitized.isEmpty()) {
            sanitized = generateDefaultName();
        }
        List<String> current = new ArrayList<>(ensureList());
        sanitized = ensureUniqueName(sanitized, current);
        current.add(sanitized);
        Collections.sort(current, String.CASE_INSENSITIVE_ORDER);
        soundsLiveData.setValue(current);
    }

    void removeSounds(List<String> toRemove) {
        if (toRemove == null || toRemove.isEmpty()) {
            return;
        }
        List<String> current = new ArrayList<>(ensureList());
        current.removeAll(toRemove);
        soundsLiveData.setValue(current);
    }

    private List<String> ensureList() {
        List<String> current = soundsLiveData.getValue();
        return current == null ? new ArrayList<>() : new ArrayList<>(current);
    }

    private String generateDefaultName() {
        List<String> current = ensureList();
        int index = current.size() + 1;
        String candidate;
        do {
            candidate = "sound_" + index + ".mp3";
            index++;
        } while (current.contains(candidate));
        return candidate;
    }

    private String ensureUniqueName(String name, List<String> current) {
        if (!current.contains(name)) {
            return name;
        }
        int index = 1;
        String base = name;
        int dotIndex = name.lastIndexOf('.');
        String extension = "";
        if (dotIndex > 0) {
            base = name.substring(0, dotIndex);
            extension = name.substring(dotIndex);
        }
        String candidate;
        do {
            candidate = base + "_" + index + extension;
            index++;
        } while (current.contains(candidate));
        return candidate;
    }

    private String sanitizeName(String name) {
        if (name == null) {
            return "";
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        return trimmed.replaceAll("[^a-zA-Z0-9_.-]", "_");
    }
}
