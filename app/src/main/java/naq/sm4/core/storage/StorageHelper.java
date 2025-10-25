package naq.sm4.core.storage;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import naq.sm4.core.StorageConstants;

public final class StorageHelper {

    private static final String CONFIG_DIRECTORY_NAME = "configs";

    private StorageHelper() {
    }

    @NonNull
    public static File ensureWorkingDirectory() throws IOException {
        File musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        File workingDir = new File(musicDir, "SupportMeditation");
        if (!workingDir.exists() && !workingDir.mkdirs()) {
            throw new IOException("Unable to create working directory: " + workingDir.getAbsolutePath());
        }
        return workingDir;
    }

    @NonNull
    public static File ensureConfigDirectory(@NonNull Context context) throws IOException {
        File configDir = new File(context.getFilesDir(), CONFIG_DIRECTORY_NAME);
        if (!configDir.exists() && !configDir.mkdirs()) {
            throw new IOException("Unable to create config directory: " + configDir.getAbsolutePath());
        }
        return configDir;
    }

    @NonNull
    public static File getConfigFile(@NonNull Context context) throws IOException {
        File dir = ensureConfigDirectory(context);
        return new File(dir, StorageConstants.DEFAULT_CONFIG_FILE);
    }

    @NonNull
    public static List<File> listAudioFiles() throws IOException {
        File workingDir = ensureWorkingDirectory();
        if (!workingDir.exists()) {
            return Collections.emptyList();
        }
        File[] files = workingDir.listFiles(pathname -> pathname.isFile() && isSupportedAudioFile(pathname.getName()));
        if (files == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(Arrays.asList(files));
    }

    public static boolean isSupportedAudioFile(@Nullable String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return false;
        }
        String lower = fileName.toLowerCase();
        for (String ext : StorageConstants.SUPPORTED_AUDIO_EXTENSIONS) {
            if (lower.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    @NonNull
    public static String sanitizeFileName(@Nullable String name) {
        if (name == null) {
            return "";
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        return trimmed.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    @NonNull
    public static String resolveCollision(@NonNull File directory, @NonNull String desiredName) {
        String base = desiredName;
        String extension = "";
        int dotIndex = desiredName.lastIndexOf('.');
        if (dotIndex >= 0) {
            base = desiredName.substring(0, dotIndex);
            extension = desiredName.substring(dotIndex);
        }
        String candidate = desiredName;
        int index = 1;
        while (new File(directory, candidate).exists()) {
            candidate = base + "_" + index + extension;
            index++;
        }
        return candidate;
    }

    @NonNull
    public static String copyToWorkingDirectory(@NonNull Context context,
                                                @NonNull Uri sourceUri,
                                                @NonNull String desiredName) throws IOException {
        File workingDir = ensureWorkingDirectory();
        String sanitized = sanitizeFileName(desiredName);
        if (sanitized.isEmpty()) {
            sanitized = "sound";
        }
        if (!isSupportedAudioFile(sanitized)) {
            sanitized += ".mp3";
        }
        String finalName = resolveCollision(workingDir, sanitized);
        File target = new File(workingDir, finalName);

        ContentResolver resolver = context.getContentResolver();
        try (InputStream inputStream = resolver.openInputStream(sourceUri)) {
            if (inputStream == null) {
                throw new IOException("Unable to open source uri: " + sourceUri);
            }
            writeStreamToFile(target, inputStream);
        }
        return finalName;
    }

    public static boolean deleteSound(@NonNull String fileName) throws IOException {
        File workingDir = ensureWorkingDirectory();
        File target = new File(workingDir, fileName);
        return target.exists() && target.delete();
    }

    @NonNull
    public static String readTextFile(@NonNull File file) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
        }
        return builder.toString();
    }

    public static void writeTextFile(@NonNull File file, @NonNull String content) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
            outputStream.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }

    public static void writeStreamToFile(@NonNull File target, @NonNull InputStream inputStream) throws IOException {
        try (OutputStream outputStream = new FileOutputStream(target, false)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
        }
    }

    @NonNull
    public static List<String> listAudioFileNamesSorted() throws IOException {
        List<File> files = listAudioFiles();
        List<String> names = new ArrayList<>();
        for (File file : files) {
            names.add(file.getName());
        }
        Collections.sort(names, String::compareToIgnoreCase);
        return names;
    }
}
