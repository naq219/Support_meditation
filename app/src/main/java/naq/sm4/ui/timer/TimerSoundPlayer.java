package naq.sm4.ui.timer;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;

import naq.sm4.core.storage.StorageHelper;

/**
 * Lightweight helper class encapsulating media playback for timer stage cues.
 * <p>
 * The class intentionally avoids exposing {@link android.media.MediaPlayer} to the rest of the
 * codebase to simplify lifecycle management and error handling.
 */
class TimerSoundPlayer {

    private MediaPlayer mediaPlayer;

    /**
     * Starts playback of the provided audio file from the working directory.
     *
     * @param context current context
     * @param fileName name of the file to play
     * @throws IOException when the file cannot be played
     */
    void play(@NonNull Context context, @NonNull String fileName) throws IOException {
        stop();
        File root = StorageHelper.ensureWorkingDirectory();
        File target = new File(root, fileName);
        if (!target.exists()) {
            throw new IOException("Missing file: " + fileName);
        }
        mediaPlayer = MediaPlayer.create(context, Uri.fromFile(target));
        if (mediaPlayer == null) {
            throw new IOException("Unable to create player for: " + fileName);
        }
        mediaPlayer.setOnCompletionListener(mp -> stop());
        mediaPlayer.start();
    }

    /**
     * Stops playback if in progress and releases the underlying media player resources.
     */
    void stop() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
