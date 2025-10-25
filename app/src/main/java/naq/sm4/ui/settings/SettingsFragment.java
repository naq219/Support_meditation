package naq.sm4.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;

import naq.sm4.R;
import naq.sm4.data.SettingsState;
import naq.sm4.data.SettingsState.VibrationLevel;
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
        binding.vibrationRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (updatingUi) {
                return;
            }
            VibrationLevel level = mapCheckedIdToLevel(checkedId);
            viewModel.updateVibration(level);
            showSavedMessage();
        });

        binding.soundToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (updatingUi) {
                return;
            }
            viewModel.updateSoundEnabled(isChecked);
            showSavedMessage();
        });

        binding.screenDimSlider.addOnChangeListener((slider, value, fromUser) -> {
            int percent = Math.round(value);
            binding.screenDimValueText.setText(getString(R.string.settings_screen_dim_value, percent));
            if (updatingUi || !fromUser) {
                return;
            }
            viewModel.updateScreenDim(percent);
        });

        binding.screenDimSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
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
        selectRadioForLevel(state.getVibrationLevel());
        binding.soundToggle.setChecked(state.isSoundEnabled());
        float sliderValue = state.getScreenDimPercent();
        if (binding.screenDimSlider.getValue() != sliderValue) {
            binding.screenDimSlider.setValue(sliderValue);
        }
        binding.screenDimValueText.setText(getString(R.string.settings_screen_dim_value, state.getScreenDimPercent()));
        updatingUi = false;
    }

    private void selectRadioForLevel(@NonNull VibrationLevel level) {
        int id;
        switch (level) {
            case LOW:
                id = R.id.vibrationLow;
                break;
            case HIGH:
                id = R.id.vibrationHigh;
                break;
            case OFF:
            default:
                id = R.id.vibrationOff;
                break;
        }
        if (binding.vibrationRadioGroup.getCheckedRadioButtonId() != id) {
            binding.vibrationRadioGroup.check(id);
        }
    }

    private VibrationLevel mapCheckedIdToLevel(int checkedId) {
        if (checkedId == R.id.vibrationLow) {
            return VibrationLevel.LOW;
        } else if (checkedId == R.id.vibrationHigh) {
            return VibrationLevel.HIGH;
        }
        return VibrationLevel.OFF;
    }

    private void showSavedMessage() {
        if (binding == null) {
            return;
        }
        Snackbar.make(binding.getRoot(), R.string.settings_saved_message, Snackbar.LENGTH_SHORT).show();
    }
}
