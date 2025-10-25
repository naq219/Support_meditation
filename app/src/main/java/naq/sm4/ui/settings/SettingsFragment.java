package naq.sm4.ui.settings;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;

import naq.sm4.R;
import naq.sm4.data.SettingsState;
import naq.sm4.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SettingsViewModel viewModel;
    private boolean updatingUi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        setupListeners();

        viewModel.getSettings().observe(getViewLifecycleOwner(), this::renderSettings);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupListeners() {
        binding.vibrationSlider.addOnChangeListener((slider, value, fromUser) -> {
            int percent = Math.round(value);
            binding.vibrationValueText.setText(getString(R.string.settings_vibration_value, percent));
            if (updatingUi || !fromUser) {
                return;
            }
            viewModel.updateVibrationStrength(percent);
        });

        binding.vibrationSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                if (updatingUi) {
                    return;
                }
                int percent = Math.round(slider.getValue());
                previewVibration(percent);
                showSavedMessage();
            }
        });

        binding.soundToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (updatingUi) {
                return;
            }
            viewModel.updateSoundEnabled(isChecked);
            showSavedMessage();
        });

        binding.screenBrightnessSlider.addOnChangeListener((slider, value, fromUser) -> {
            int percent = Math.round(value);
            binding.screenBrightnessValueText.setText(getString(R.string.settings_screen_brightness_value, percent));
            if (updatingUi || !fromUser) {
                return;
            }
            viewModel.updateScreenBrightness(percent);
        });

        binding.screenBrightnessSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                if (!updatingUi) {
                    showSavedMessage();
                }
            }
        });
    }

    private void renderSettings(SettingsState state) {
        if (binding == null) {
            return;
        }
        updatingUi = true;
        int vibrationPercent = state.getVibrationStrengthPercent();
        if (binding.vibrationSlider.getValue() != vibrationPercent) {
            binding.vibrationSlider.setValue(vibrationPercent);
        }
        binding.vibrationValueText.setText(getString(R.string.settings_vibration_value, vibrationPercent));
        binding.soundToggle.setChecked(state.isSoundEnabled());
        int brightnessPercent = state.getScreenBrightnessPercent();
        if (binding.screenBrightnessSlider.getValue() != brightnessPercent) {
            binding.screenBrightnessSlider.setValue(brightnessPercent);
        }
        binding.screenBrightnessValueText.setText(getString(R.string.settings_screen_brightness_value, brightnessPercent));
        updatingUi = false;
    }

    private void showSavedMessage() {
        if (binding == null) {
            return;
        }
        Snackbar.make(binding.getRoot(), R.string.settings_saved_message, Snackbar.LENGTH_SHORT).show();
    }

    private void previewVibration(int percent) {
        if (percent <= 0 || binding == null) {
            return;
        }
        Vibrator vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null || !vibrator.hasVibrator()) {
            return;
        }
        int durationMs = Math.max(40, 80 + percent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int amplitude = Math.max(1, Math.round((percent / 100f) * 255f));
            vibrator.vibrate(VibrationEffect.createOneShot(durationMs, amplitude));
        } else {
            vibrator.vibrate(durationMs);
        }
    }
}
