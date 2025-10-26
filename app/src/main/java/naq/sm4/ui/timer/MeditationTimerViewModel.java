package naq.sm4.ui.timer;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.os.Build;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import naq.sm4.R;
import naq.sm4.core.storage.StorageHelper;
import naq.sm4.data.MeditationConfig;
import naq.sm4.data.MeditationStage;
import naq.sm4.data.SettingsState;
import naq.sm4.ui.settings.SettingsManager;

/**
 * ViewModel maintaining the state of a meditation session, including stage progression,
 * countdown timers, audio playback, and pause/resume handling.
 */
public class MeditationTimerViewModel extends AndroidViewModel {

    /**
     * High level lifecycle states for the session timer, used to drive UI controls and behavior.
     */
    public enum TimerState {
        IDLE,
        RUNNING,
        PAUSED,
        COMPLETED,
        STOPPED
    }

    private static final long TICK_INTERVAL_MS = 1000L;

    private final MutableLiveData<String> countdownText = new MutableLiveData<>("00:00");
    private final MutableLiveData<String> stageTitle = new MutableLiveData<>("");
    private final MutableLiveData<String> stageCounter = new MutableLiveData<>("");
    private final MutableLiveData<String> nextStageTitle = new MutableLiveData<>("");
    private final MutableLiveData<String> sessionSummary = new MutableLiveData<>("");
    private final MutableLiveData<String> sessionTotal = new MutableLiveData<>("");
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);
    private final MutableLiveData<TimerState> stateLiveData = new MutableLiveData<>(TimerState.IDLE);

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable tickRunnable = this::handleTick;

    private MeditationConfig activeConfig;
    private List<MeditationStage> stages = Collections.emptyList();
    private int currentStageIndex = 0;
    private int secondsRemaining = 0;
    private int sessionSecondsElapsed = 0;
    private boolean soundEnabled = true;
    private boolean vibrationEnabled = true;
    private int vibrationStrengthPercent = 0;
    private int screenBrightnessPercent = 100;

    private final MutableLiveData<Integer> screenBrightnessLiveData = new MutableLiveData<>(100);

    private final TimerSoundPlayer soundPlayer = new TimerSoundPlayer();
    private PowerManager.WakeLock wakeLock;
    private int repeatIntervalSeconds = -1;
    private int repeatCountdown = -1;
    private final Random random = new Random();

    public MeditationTimerViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * @return formatted remaining time for the current stage.
     */
    public LiveData<String> getCountdownText() {
        return countdownText;
    }

    /**
     * @return name of the current stage prefixed via resources.
     */
    public LiveData<String> getStageTitle() {
        return stageTitle;
    }

    /**
     * @return human-readable counter (e.g. "1/3") of the active stage.
     */
    public LiveData<String> getStageCounter() {
        return stageCounter;
    }

    /**
     * @return label describing the next stage, or empty when none remains.
     */
    public LiveData<String> getNextStageTitle() {
        return nextStageTitle;
    }

    /**
     * @return summary text displayed when the session finishes or is stopped.
     */
    public LiveData<String> getSessionSummary() {
        return sessionSummary;
    }

    /**
     * @return formatted total elapsed time for the completed session.
     */
    public LiveData<String> getSessionTotal() {
        return sessionTotal;
    }

    /**
     * @return transient error message emitted when sounds fail to load or similar issues occur.
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * @return observable timer state allowing the UI to toggle controls.
     */
    public LiveData<TimerState> getState() {
        return stateLiveData;
    }

    /**
     * @return brightness preference to apply while the timer is visible (0-100).
     */
    public LiveData<Integer> getScreenBrightnessPercent() {
        return screenBrightnessLiveData;
    }

    /**
     * @return {@code true} when audio cues are currently allowed.
     */
    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    /**
     * @return {@code true} when vibration cues are currently allowed.
     */
    public boolean isVibrationEnabled() {
        return vibrationEnabled;
    }

    /**
     * Prepares the timer with the supplied {@link MeditationConfig} and optionally starts it
     * immediately.
     */
    @MainThread
    public void initialise(@NonNull MeditationConfig config, boolean startImmediately) {
        activeConfig = config;
        stages = new ArrayList<>(config.getStages());
        currentStageIndex = 0;
        sessionSecondsElapsed = 0;
        sessionSummary.setValue("");
        sessionTotal.setValue("");
        applySettingsDefaults();
        if (stages.isEmpty()) {
            countdownText.setValue("00:00");
            stageTitle.setValue(getApplication().getString(R.string.label_stage_placeholder));
            stageCounter.setValue("0/0");
            nextStageTitle.setValue("");
            stateLiveData.setValue(TimerState.COMPLETED);
            return;
        }
        loadStage(currentStageIndex);
        if (startImmediately) {
            startTimer();
        } else {
            stateLiveData.setValue(TimerState.IDLE);
        }
    }

    /**
     * Starts the countdown if not already running and at least one stage is available.
     */
    @MainThread
    public void startTimer() {
        if (stateLiveData.getValue() == TimerState.RUNNING || stages.isEmpty()) {
            return;
        }
        stateLiveData.setValue(TimerState.RUNNING);
        acquireWakeLock();
        handler.removeCallbacks(tickRunnable);
        handler.postDelayed(tickRunnable, TICK_INTERVAL_MS);
        playStageCue();
    }

    /**
     * Pauses the timer, allowing the user to resume later without losing progress.
     */
    @MainThread
    public void pauseTimer() {
        if (stateLiveData.getValue() != TimerState.RUNNING) {
            return;
        }
        stateLiveData.setValue(TimerState.PAUSED);
        handler.removeCallbacks(tickRunnable);
        releaseWakeLock();
    }

    /**
     * Resumes a previously paused timer.
     */
    @MainThread
    public void resumeTimer() {
        if (stateLiveData.getValue() != TimerState.PAUSED) {
            return;
        }
        stateLiveData.setValue(TimerState.RUNNING);
        acquireWakeLock();
        handler.postDelayed(tickRunnable, TICK_INTERVAL_MS);
    }

    /**
     * Stops the session and emits a summary message. When invoked by the user the timer screen
     * should return to the previous UI immediately.
     */
    @MainThread
    public void stopTimer(boolean userInitiated) {
        handler.removeCallbacks(tickRunnable);
        releaseWakeLock();
        stateLiveData.setValue(TimerState.STOPPED);
        int completedStages = Math.min(currentStageIndex, stages.size());
        String summary = getApplication().getString(R.string.timer_session_stopped, completedStages, stages.size());
        sessionSummary.setValue(summary);
        sessionTotal.setValue(formatDuration(sessionSecondsElapsed));
        playSilently();
    }

    /**
     * Enables or disables sound playback for subsequent cues.
     */
    @MainThread
    public void updateSoundEnabled(boolean enabled) {
        soundEnabled = enabled;
        if (!enabled) {
            playSilently();
        }
    }

    /**
     * Enables or disables vibration feedback for subsequent cues.
     */
    @MainThread
    public void updateVibrationEnabled(boolean enabled) {
        vibrationEnabled = enabled && vibrationStrengthPercent > 0;
    }

    /**
     * Loads a stage by index, resetting timers and exposing metadata to observers.
     */
    private void loadStage(int index) {
        MeditationStage stage = stages.get(index);
        secondsRemaining = stage.getMinutes() * 60;
        stageTitle.setValue(stage.getName());
        stageCounter.setValue(getApplication().getString(R.string.timer_stage_counter, index + 1, stages.size()));
        countdownText.setValue(formatDuration(secondsRemaining));

        repeatIntervalSeconds = stage.getRepeatMinutes() > 0 ? stage.getRepeatMinutes() * 60 : -1;
        repeatCountdown = repeatIntervalSeconds;

        if (index + 1 < stages.size()) {
            nextStageTitle.setValue(getApplication().getString(R.string.timer_next_stage, stages.get(index + 1).getName()));
        } else {
            nextStageTitle.setValue("");
        }
    }

    /**
     * Called every second to drive countdown progression and stage transitions.
     */
    private void handleTick() {
        if (stateLiveData.getValue() != TimerState.RUNNING) {
            return;
        }
        secondsRemaining--;
        sessionSecondsElapsed++;
        countdownText.setValue(formatDuration(Math.max(0, secondsRemaining)));
        if (repeatIntervalSeconds > 0) {
            repeatCountdown--;
            if (repeatCountdown <= 0 && secondsRemaining > 0) {
                playStageCue();
                repeatCountdown = repeatIntervalSeconds;
            }
        }
        if (secondsRemaining <= 0) {
            advanceStage();
        } else {
            handler.postDelayed(tickRunnable, TICK_INTERVAL_MS);
        }
    }

    /**
     * Moves to the next stage or completes the session when none remain.
     */
    private void advanceStage() {
        currentStageIndex++;
        if (currentStageIndex >= stages.size()) {
            completeSession();
            return;
        }
        loadStage(currentStageIndex);
        playStageCue();
        handler.postDelayed(tickRunnable, TICK_INTERVAL_MS);
    }

    /**
     * Finalises the session, emits summary text, and cleans up resources.
     */
    private void completeSession() {
        handler.removeCallbacks(tickRunnable);
        stateLiveData.setValue(TimerState.COMPLETED);
        releaseWakeLock();
        sessionSummary.setValue(getApplication().getString(R.string.timer_completed_summary, stages.size(), stages.size()));
        sessionTotal.setValue(formatDuration(sessionSecondsElapsed));
        playSilently();
    }

    /**
     * Triggers the cue for the current stage, combining vibration feedback and audio playback.
     */
    private void playStageCue() {
        vibrateCue();
        if (!soundEnabled || stages.isEmpty()) {
            return;
        }
        MeditationStage stage = stages.get(currentStageIndex);
        List<String> sounds = stage.getSounds();
        if (sounds.isEmpty()) {
            return;
        }
        String sound = selectRandomSound(sounds);
        try {
            List<String> available = StorageHelper.listAudioFileNamesSorted();
            if (!available.contains(sound)) {
                errorMessage.setValue(getApplication().getString(R.string.timer_missing_sound, sound));
                return;
            }
            soundPlayer.play(getApplication(), sound);
        } catch (IOException e) {
            errorMessage.setValue(getApplication().getString(R.string.timer_missing_sound, sound));
        }
    }

    /**
     * Stops any active sound playback.
     */
    private void playSilently() {
        soundPlayer.stop();
    }

    /**
     * Triggers a vibration feedback using the configured strength when enabled.
     */
    private void vibrateCue() {
        if (!vibrationEnabled || vibrationStrengthPercent <= 0) {
            return;
        }
        Vibrator vibrator = (Vibrator) getApplication().getSystemService(Application.VIBRATOR_SERVICE);
        if (vibrator == null || !vibrator.hasVibrator()) {
            return;
        }
        int durationMs = Math.max(40, 80 + vibrationStrengthPercent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int amplitude = Math.max(1, Math.round((vibrationStrengthPercent / 100f) * 255f));
            vibrator.vibrate(VibrationEffect.createOneShot(durationMs, amplitude));
        } else {
            vibrator.vibrate(durationMs);
        }
    }

    /**
     * Ensures the screen stays awake/dimmed appropriately during active sessions.
     */
    private void acquireWakeLock() {
        if (wakeLock == null) {
            PowerManager powerManager = (PowerManager) getApplication().getSystemService(Application.POWER_SERVICE);
            if (powerManager != null) {
                wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "SM4:Timer");
            }
        }
        if (wakeLock != null && !wakeLock.isHeld()) {
            wakeLock.acquire();
        }
    }

    /**
     * Releases the wake lock once the session pauses or stops.
     */
    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    /**
     * Formats seconds into a mm:ss string for display.
     */
    private String formatDuration(int seconds) {
        int minutes = seconds / 60;
        int remainder = seconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, remainder);
    }

    /**
     * Reloads persisted settings so the timer honours the latest user preferences.
     */
    private void applySettingsDefaults() {
        SettingsState state = SettingsManager.getInstance().getSettings(getApplication());
        soundEnabled = state.isSoundEnabled();
        vibrationStrengthPercent = state.getVibrationStrengthPercent();
        vibrationEnabled = vibrationStrengthPercent > 0;
        screenBrightnessPercent = state.getScreenBrightnessPercent();
        screenBrightnessLiveData.setValue(screenBrightnessPercent);
    }

    /**
     * Picks a random sound from the provided list to add variation between repeats.
     */
    private String selectRandomSound(@NonNull List<String> sounds) {
        if (sounds.size() == 1) {
            return sounds.get(0);
        }
        return sounds.get(random.nextInt(sounds.size()));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        handler.removeCallbacks(tickRunnable);
        releaseWakeLock();
        soundPlayer.stop();
    }
}
