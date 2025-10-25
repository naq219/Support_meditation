package naq.sm4.ui.stage;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.chip.Chip;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import naq.sm4.R;
import naq.sm4.core.storage.StorageHelper;
import naq.sm4.data.MeditationStage;
import naq.sm4.databinding.FragmentStageEditorBinding;
import naq.sm4.ui.sound.SelectSoundDialogFragment;

/**
 * Fragment that allows the user to create or edit a meditation stage, including
 * configuring duration, repeat interval, and associated sound assets.
 */
public class StageEditorFragment extends Fragment {

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentStageEditorBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        String stageName = args != null ? args.getString(ARG_STAGE_NAME, "") : "";
        int minutes = args != null ? args.getInt(ARG_STAGE_MINUTES, 5) : 5;
        int repeat = args != null ? args.getInt(ARG_STAGE_REPEAT, 0) : 0;
        ArrayList<String> soundsArg = args != null ? args.getStringArrayList(ARG_STAGE_SOUNDS) : null;
        editingIndex = args != null ? args.getInt(ARG_STAGE_INDEX, -1) : -1;

        binding.stageNameInput.setText(stageName);
        binding.stageMinutesInput.setText(String.valueOf(minutes));
        binding.repeatMinutesInput.setText(String.valueOf(repeat));

        if (soundsArg != null && !soundsArg.isEmpty()) {
            selectedSounds.addAll(soundsArg);
        } else {
            selectedSounds.addAll(loadFallbackSounds());
        }
        renderSelectedChips();

        // Set up the sound selection button to navigate to the sound selection dialog.
        binding.selectSoundButton.setOnClickListener(v -> {
            // Create a bundle to pass the currently selected sounds to the sound selection dialog.
            Bundle args1 = new Bundle();
            args1.putStringArrayList(SelectSoundDialogFragment.ARG_SELECTED, new ArrayList<>(selectedSounds));
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_stageEditorFragment_to_selectSoundDialogFragment, args1);
        });

        // Set up the cancel button to navigate back to the previous fragment.
        binding.cancelStageButton.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        // Set up the save button to validate and save the stage.
        binding.saveStageButton.setOnClickListener(v -> onSaveStage());

        // Set up a fragment result listener to receive the updated sound selection from the sound selection dialog.
        getParentFragmentManager().setFragmentResultListener(SelectSoundDialogFragment.RESULT_KEY, getViewLifecycleOwner(), (requestKey, bundle) -> {
            // Get the updated sound selection from the bundle.
            ArrayList<String> updatedSounds = bundle.getStringArrayList(SelectSoundDialogFragment.RESULT_SELECTED);
            if (updatedSounds != null) {
                selectedSounds.clear();
                selectedSounds.addAll(updatedSounds);
                renderSelectedChips();
            }
        });
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
     * Provides a minimal list of available sounds so the user is not presented with an empty
     * selection when no stage-specific sounds were provided.
     */
    private List<String> loadFallbackSounds() {
        try {
            List<String> files = StorageHelper.listAudioFileNamesSorted();
            if (!files.isEmpty()) {
                return new ArrayList<>(files.subList(0, Math.min(files.size(), 1)));
            }
        } catch (IOException ignored) {
        }
        return new ArrayList<>();
    }

    /**
     * Reflects the currently selected sound list as chips within the UI for quick visual feedback.
     */
    private void renderSelectedChips() {
        if (binding == null) {
            return;
        }

        binding.selectedSoundsChipGroup.removeAllViews();
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
}
