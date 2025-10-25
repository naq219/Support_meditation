package naq.sm4.core.storage;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import naq.sm4.data.MeditationConfig;
import naq.sm4.data.MeditationStage;

public class ConfigRepository {

    private static final String TAG = "ConfigRepo";
    private static final Gson GSON_INSTANCE = new GsonBuilder().setPrettyPrinting().create();
    private static final Type CONFIG_LIST_TYPE = new TypeToken<List<MeditationConfig>>() {
    }.getType();

    @NonNull
    public List<MeditationConfig> loadConfigs(@NonNull Context context) {
        try {
            File configFile = StorageHelper.getConfigFile(context);
            if (!configFile.exists()) {
                return new ArrayList<>();
            }
            String json = StorageHelper.readTextFile(configFile);
            List<MeditationConfig> parsed = GSON_INSTANCE.fromJson(json, CONFIG_LIST_TYPE);
            if (parsed == null) {
                return new ArrayList<>();
            }
            return sanitizeConfigs(parsed);
        } catch (IOException | JsonSyntaxException e) {
            Log.e(TAG, "Failed to load configs", e);
            return new ArrayList<>();
        }
    }

    public boolean saveConfigs(@NonNull Context context, @NonNull List<MeditationConfig> configs) {
        try {
            File configFile = StorageHelper.getConfigFile(context);
            String serialized = GSON_INSTANCE.toJson(configs, CONFIG_LIST_TYPE);
            StorageHelper.writeTextFile(configFile, serialized);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Failed to save configs", e);
            return false;
        }
    }

    @NonNull
    private List<MeditationConfig> sanitizeConfigs(@Nullable List<MeditationConfig> configs) {
        if (configs == null) {
            return new ArrayList<>();
        }
        List<MeditationConfig> sanitized = new ArrayList<>();
        for (MeditationConfig config : configs) {
            if (config == null) {
                continue;
            }
            String name = config.getName() == null ? "" : config.getName();
            int totalMinutes = clampTotalMinutes(config.getTotalMinutes());
            List<MeditationStage> stages = config.getStages() == null
                    ? Collections.emptyList()
                    : sanitizeStages(config.getStages());
            sanitized.add(new MeditationConfig(name, totalMinutes, stages));
        }
        return sanitized;
    }

    @NonNull
    private List<MeditationStage> sanitizeStages(@NonNull List<MeditationStage> stages) {
        List<MeditationStage> sanitized = new ArrayList<>();
        for (MeditationStage stage : stages) {
            if (stage == null) {
                continue;
            }
            String name = stage.getName() == null ? "" : stage.getName();
            int minutes = clampStageMinutes(stage.getMinutes());
            int repeat = clampRepeatMinutes(stage.getRepeatMinutes());
            List<String> sounds = stage.getSounds() == null ? Collections.emptyList() : stage.getSounds();
            sanitized.add(new MeditationStage(name, minutes, repeat, new ArrayList<>(sounds)));
        }
        return sanitized;
    }

    private int clampTotalMinutes(int totalMinutes) {
        if (totalMinutes < 1) {
            return 1;
        }
        return Math.min(totalMinutes, 180);
    }

    private int clampStageMinutes(int minutes) {
        if (minutes < 1) {
            return 1;
        }
        return Math.min(minutes, 180);
    }

    private int clampRepeatMinutes(int repeat) {
        if (repeat < 0) {
            return 0;
        }
        return Math.min(repeat, 60);
    }
}
