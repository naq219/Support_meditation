package naq.sm4.ui.stage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.core.content.ContextCompat;

import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import naq.sm4.R;
import naq.sm4.data.MeditationStage;
import naq.sm4.data.MockData;
import naq.sm4.databinding.FragmentStageEditorBinding;

public class StageEditorFragment extends Fragment {

    public static final String ARG_SELECTED_SOUNDS = "selected_sounds";
    public static final String RESULT_KEY = "select_sounds_result";

    private FragmentStageEditorBinding binding;
    private final List<String> availableSounds = new ArrayList<>(MockData.getSoundFiles());
    private final Set<String> selectedSounds = new HashSet<>();

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

        MeditationStage templateStage = MockData.getTemplateStages().get(0);
        binding.stageNameInput.setText(templateStage.getName());
        binding.stageMinutesInput.setText(String.valueOf(templateStage.getMinutes()));
        binding.repeatMinutesInput.setText(String.valueOf(templateStage.getRepeatMinutes()));

        selectedSounds.addAll(templateStage.getSounds());
        renderSelectedChips();

        getParentFragmentManager().setFragmentResultListener(RESULT_KEY, getViewLifecycleOwner(), (requestKey, bundle) -> {
            ArrayList<String> updatedSounds = bundle.getStringArrayList(ARG_SELECTED_SOUNDS);
            if (updatedSounds != null) {
                selectedSounds.clear();
                selectedSounds.addAll(updatedSounds);
                renderSelectedChips();
            }
        });

        binding.selectSoundButton.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putStringArrayList(ARG_SELECTED_SOUNDS, new ArrayList<>(selectedSounds));
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_stageEditorFragment_to_selectSoundDialogFragment, args);
        });

        binding.cancelStageButton.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        binding.saveStageButton.setOnClickListener(v -> {
            // Demo-only: just navigate back for now
            NavHostFragment.findNavController(this).popBackStack();
        });
    }

    private void renderSelectedChips() {
        if (binding == null) {
            return;
        }

        binding.selectedSoundsChipGroup.removeAllViews();
        binding.selectedSoundLabel.setText(getString(R.string.label_selected_sounds));
        if (selectedSounds.isEmpty()) {
            Chip chip = createChip(getString(R.string.dialog_select_sound_empty));
            chip.setEnabled(false);
            binding.selectedSoundsChipGroup.addView(chip);
            return;
        }

        for (String sound : selectedSounds) {
            Chip chip = createChip(sound);
            binding.selectedSoundsChipGroup.addView(chip);
        }

        binding.selectedSoundLabel.setText(getString(R.string.label_selected_sound_count, selectedSounds.size()));
    }

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
