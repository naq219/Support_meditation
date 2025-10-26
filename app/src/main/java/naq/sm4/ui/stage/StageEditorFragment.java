package naq.sm4.ui.stage;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.chip.Chip;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import naq.sm4.R;
import naq.sm4.data.MeditationStage;
import naq.sm4.databinding.FragmentStageEditorBinding;
import naq.sm4.ui.sound.SelectSoundDialogFragment;
import naq.sm4.ui.sound.SoundLibraryViewModel;

/**
 * Fragment that allows the user to create or edit a meditation stage, including
 * configuring duration, repeat interval, and associated sound assets.
 */
public class StageEditorFragment extends DialogFragment {

    public static final String ARG_STAGE_NAME = "arg_stage_name";
    public static final String ARG_STAGE_MINUTES = "arg_stage_minutes";
    public static final String ARG_STAGE_REPEAT = "arg_stage_repeat";
    public static final String ARG_STAGE_SOUNDS = "arg_stage_sounds";
    public static final String ARG_STAGE_INDEX = "arg_stage_index";

    public static final String RESULT_KEY_STAGE = "stage_editor_result";
    public static final String RESULT_STAGE_NAME = "result_stage_name";
    public static final String RESULT_STAGE_MINUTES = "result_stage_minutes";
    public static final String RESULT_STAGE_REPEAT = "result_stage_repeat";
    public static final String RESULT_STAGE_SOUNDS = "result_stage_sounds";
    public static final String RESULT_STAGE_INDEX = "result_stage_index";

    private static final int MIN_MINUTES = 1;
    private static final int MAX_MINUTES = 180;
    private static final int MAX_REPEAT = 60;

    private FragmentStageEditorBinding binding;
    private final Set<String> selectedSounds = new LinkedHashSet<>();
    private int editingIndex = -1;
    private SoundLibraryViewModel soundLibraryViewModel;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Context context = requireContext();
        binding = FragmentStageEditorBinding.inflate(LayoutInflater.from(context));

        soundLibraryViewModel = new ViewModelProvider(requireActivity()).get(SoundLibraryViewModel.class);
        soundLibraryViewModel.refreshSounds();

        initialiseFromArguments(getArguments());
        observeSoundLibrary();
        registerSoundSelectionListener();
        configureButtons();
        renderSelectedChips();

        Dialog dialog = new Dialog(context, R.style.Theme_SM4_DialogFragment);
        dialog.setContentView(binding.getRoot());
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnShowListener((DialogInterface.OnShowListener) d -> {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            }
        });
        return dialog;
    }

    /**
     * Validates user input, constructs the resulting {@link MeditationStage}, and returns it to
     * the caller via fragment result when all constraints are satisfied.
     */
    private void onSaveStage() {
        if (binding == null) {
            return;
        }
        String nameInput = binding.stageNameInput.getText() == null ? "" : binding.stageNameInput.getText().toString().trim();
        String finalName = nameInput.isEmpty() ? getString(R.string.label_stage_placeholder) : nameInput;

        Integer minutes = parseInteger(binding.stageMinutesInput.getText());
        Integer repeat = parseInteger(binding.repeatMinutesInput.getText());

        boolean valid = true;
        if (minutes == null || minutes < MIN_MINUTES || minutes > MAX_MINUTES) {
            binding.stageMinutesInputLayout.setError(getString(R.string.error_stage_minutes, MIN_MINUTES, MAX_MINUTES));
            valid = false;
        } else {
            binding.stageMinutesInputLayout.setError(null);
        }

        if (repeat == null || repeat < 0 || repeat > MAX_REPEAT) {
            binding.repeatMinutesInputLayout.setError(getString(R.string.error_stage_repeat, MAX_REPEAT));
            valid = false;
        } else {
            binding.repeatMinutesInputLayout.setError(null);
        }

        if (!valid) {
            return;
        }

        ArrayList<String> sounds = new ArrayList<>(selectedSounds);

        Bundle result = new Bundle();
        result.putString(RESULT_STAGE_NAME, finalName);
        result.putInt(RESULT_STAGE_MINUTES, minutes);
        result.putInt(RESULT_STAGE_REPEAT, repeat);
        result.putStringArrayList(RESULT_STAGE_SOUNDS, sounds);
        result.putInt(RESULT_STAGE_INDEX, editingIndex);
        getParentFragmentManager().setFragmentResult(RESULT_KEY_STAGE, result);
        NavHostFragment.findNavController(this).popBackStack();
    }

    /**
     * Safely parses an integer value from the provided character sequence.
     *
     * @param text raw textual input from the UI field.
     * @return parsed integer or {@code null} if parsing fails.
     */
    private Integer parseInteger(@Nullable CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            return null;
        }
        try {
            return Integer.parseInt(text.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Reflects the currently selected sound list as chips within the UI for quick visual feedback.
     */
    private void renderSelectedChips() {
        if (binding == null) {
            return;
        }

        binding.selectedSoundsChipGroup.removeAllViews();
        if (binding.clearSelectedSoundsButton != null) {
            binding.clearSelectedSoundsButton.setEnabled(!selectedSounds.isEmpty());
        }
        if (selectedSounds.isEmpty()) {
            Chip chip = createChip(getString(R.string.dialog_select_sound_empty));
            chip.setEnabled(false);
            binding.selectedSoundsChipGroup.addView(chip);
            binding.selectedSoundLabel.setText(getString(R.string.label_selected_sounds));
            return;
        }

        for (String sound : selectedSounds) {
            Chip chip = createChip(sound);
            binding.selectedSoundsChipGroup.addView(chip);
        }

        binding.selectedSoundLabel.setText(getString(R.string.label_selected_sound_count, selectedSounds.size()));
    }

    /**
     * Creates a display-only chip carrying the provided text label.
     */
    private Chip createChip(String text) {
        Chip chip = new Chip(requireContext());
        chip.setText(text);
        chip.setChipBackgroundColorResource(R.color.surface_secondary);
        chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        chip.setCloseIconVisible(false);
        chip.setChipStrokeWidth(0f);
        chip.setEllipsize(android.text.TextUtils.TruncateAt.END);
        return chip;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initialiseFromArguments(@Nullable Bundle args) {
        selectedSounds.clear();
        editingIndex = -1;

        if (args == null) {
            binding.stageNameInput.setText("");
            binding.stageMinutesInput.setText("");
            binding.repeatMinutesInput.setText("");
            return;
        }

        editingIndex = args.getInt(ARG_STAGE_INDEX, -1);
        binding.stageNameInput.setText(args.getString(ARG_STAGE_NAME, ""));

        if (editingIndex >= 0) {
            int minutes = args.getInt(ARG_STAGE_MINUTES, MIN_MINUTES);
            int repeat = args.getInt(ARG_STAGE_REPEAT, 0);
            binding.stageMinutesInput.setText(String.valueOf(minutes));
            binding.repeatMinutesInput.setText(String.valueOf(repeat));

            ArrayList<String> soundsArg = args.getStringArrayList(ARG_STAGE_SOUNDS);
            if (soundsArg != null && !soundsArg.isEmpty()) {
                selectedSounds.addAll(soundsArg);
            }
        } else {
            binding.stageMinutesInput.setText("");
            binding.repeatMinutesInput.setText("");
        }
    }

    private void configureButtons() {
        binding.selectSoundButton.setOnClickListener(v -> {
            Bundle dialogArgs = new Bundle();
            dialogArgs.putStringArrayList(SelectSoundDialogFragment.ARG_SELECTED, new ArrayList<>(selectedSounds));
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_stageEditorFragment_to_selectSoundDialogFragment, dialogArgs);
        });

        binding.clearSelectedSoundsButton.setOnClickListener(v -> {
            selectedSounds.clear();
            renderSelectedChips();
        });

        binding.cancelStageButton.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        binding.saveStageButton.setOnClickListener(v -> onSaveStage());
    }

    private void observeSoundLibrary() {
        if (soundLibraryViewModel == null) {
            return;
        }
        soundLibraryViewModel.getSounds().observe(this, sounds -> {
            if (selectedSounds.isEmpty()) {
                return;
            }
            selectedSounds.retainAll(sounds);
            renderSelectedChips();
        });
    }

    private void registerSoundSelectionListener() {
        getParentFragmentManager().setFragmentResultListener(SelectSoundDialogFragment.RESULT_KEY, this, (requestKey, bundle) -> {
            ArrayList<String> updatedSounds = bundle.getStringArrayList(SelectSoundDialogFragment.RESULT_SELECTED);
            if (updatedSounds != null) {
                selectedSounds.clear();
                selectedSounds.addAll(updatedSounds);
                renderSelectedChips();
            }
        });
    }
}
