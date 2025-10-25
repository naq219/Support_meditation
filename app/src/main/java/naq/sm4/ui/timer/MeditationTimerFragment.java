package naq.sm4.ui.timer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;

import naq.sm4.R;
import naq.sm4.data.MeditationConfig;
import naq.sm4.databinding.FragmentMeditationTimerBinding;
import naq.sm4.ui.home.HomeViewModel;

/**
 * Fragment responsible for binding the timer UI to {@link MeditationTimerViewModel} state.
 */
public class MeditationTimerFragment extends Fragment {

    private FragmentMeditationTimerBinding binding;
    private MeditationTimerViewModel timerViewModel;
    private HomeViewModel homeViewModel;
    private Float originalBrightness;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMeditationTimerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        timerViewModel = new ViewModelProvider(requireActivity()).get(MeditationTimerViewModel.class);
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        storeOriginalBrightness();
        observeViewModel();
        setupControls();
        initialiseTimerIfNeeded();
    }

    private void observeViewModel() {
        timerViewModel.getCountdownText().observe(getViewLifecycleOwner(), binding.countdownText::setText);
        timerViewModel.getStageTitle().observe(getViewLifecycleOwner(), title -> binding.currentStageText.setText(getString(R.string.timer_stage_label, title)));
        timerViewModel.getStageCounter().observe(getViewLifecycleOwner(), binding.stageCounterText::setText);
        timerViewModel.getNextStageTitle().observe(getViewLifecycleOwner(), next -> {
            binding.nextStageText.setVisibility(next == null || next.isEmpty() ? View.GONE : View.VISIBLE);
            binding.nextStageText.setText(next);
        });
        timerViewModel.getSessionSummary().observe(getViewLifecycleOwner(), summary -> {
            binding.sessionSummary.setVisibility(summary == null || summary.isEmpty() ? View.GONE : View.VISIBLE);
            if (summary != null) {
                binding.sessionSummary.setText(summary);
            }
        });
        timerViewModel.getSessionTotal().observe(getViewLifecycleOwner(), total -> {
            binding.sessionTotal.setVisibility(total == null || total.isEmpty() ? View.GONE : View.VISIBLE);
            if (total != null) {
                binding.sessionTotal.setText(getString(R.string.timer_total_elapsed, total));
            }
        });
        timerViewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && binding != null) {
                Snackbar.make(binding.timerRoot, message, Snackbar.LENGTH_LONG).show();
            }
        });
        timerViewModel.getState().observe(getViewLifecycleOwner(), state -> {
            if (binding == null) {
                return;
            }
            boolean running = state == MeditationTimerViewModel.TimerState.RUNNING;
            boolean paused = state == MeditationTimerViewModel.TimerState.PAUSED;
            binding.pauseButton.setVisibility(running ? View.VISIBLE : View.GONE);
            binding.resumeButton.setVisibility(paused ? View.VISIBLE : View.GONE);
            binding.stopButton.setEnabled(state != MeditationTimerViewModel.TimerState.COMPLETED && state != MeditationTimerViewModel.TimerState.STOPPED);
        });
        timerViewModel.getScreenDimPercent().observe(getViewLifecycleOwner(), this::applyScreenDim);
    }

    private void setupControls() {
        binding.pauseButton.setOnClickListener(v -> timerViewModel.pauseTimer());
        binding.resumeButton.setOnClickListener(v -> timerViewModel.resumeTimer());
        binding.stopButton.setOnClickListener(v -> {
            timerViewModel.stopTimer(true);
            NavHostFragment.findNavController(this).navigateUp();
        });
        binding.soundToggle.setOnCheckedChangeListener((buttonView, isChecked) -> timerViewModel.updateSoundEnabled(isChecked));
        binding.vibrationToggle.setOnCheckedChangeListener((buttonView, isChecked) -> timerViewModel.updateVibrationEnabled(isChecked));
        timerViewModel.updateSoundEnabled(binding.soundToggle.isChecked());
        timerViewModel.updateVibrationEnabled(binding.vibrationToggle.isChecked());
    }

    private void initialiseTimerIfNeeded() {
        MeditationTimerViewModel.TimerState state = timerViewModel.getState().getValue();
        if (state == MeditationTimerViewModel.TimerState.RUNNING || state == MeditationTimerViewModel.TimerState.PAUSED) {
            return;
        }
        MeditationConfig session = homeViewModel.consumeActiveSession();
        if (session == null) {
            if (homeViewModel.getConfigs().getValue() != null && !homeViewModel.getConfigs().getValue().isEmpty()) {
                session = homeViewModel.getConfigs().getValue().get(0);
            }
        }
        if (session != null) {
            timerViewModel.initialise(session, true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        restoreBrightness();
        binding = null;
    }

    private void storeOriginalBrightness() {
        Window window = requireActivity().getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        originalBrightness = params.screenBrightness;
    }

    private void applyScreenDim(int percent) {
        Window window = requireActivity().getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        if (percent <= 0) {
            params.screenBrightness = originalBrightness != null ? originalBrightness : WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        } else {
            float dimLevel = 1f - (Math.min(100, Math.max(0, percent)) / 100f);
            params.screenBrightness = Math.max(0.05f, dimLevel);
        }
        window.setAttributes(params);
    }

    private void restoreBrightness() {
        Window window = requireActivity().getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.screenBrightness = originalBrightness != null ? originalBrightness : WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        window.setAttributes(params);
    }
}
