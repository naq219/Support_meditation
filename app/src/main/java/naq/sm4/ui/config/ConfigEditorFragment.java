package naq.sm4.ui.config;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import naq.sm4.R;
import naq.sm4.data.MeditationConfig;
import naq.sm4.data.MeditationStage;
import naq.sm4.databinding.FragmentConfigEditorBinding;
import naq.sm4.ui.home.HomeViewModel;
import naq.sm4.ui.stage.StageEditorFragment;

public class ConfigEditorFragment extends Fragment implements StageListAdapter.StageCardListener {

    private FragmentConfigEditorBinding binding;
    private StageListAdapter stageListAdapter;
    private final List<MeditationStage> stages = new ArrayList<>();
    private HomeViewModel homeViewModel;
    private MeditationConfig originalConfig;
    private boolean initialised;
    private String configNameCache = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentConfigEditorBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        stageListAdapter = new StageListAdapter(stages, this);
        binding.stageRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.stageRecyclerView.setAdapter(stageListAdapter);

        binding.addStageButton.setOnClickListener(v -> openStageEditor(null));
        binding.openSoundLibraryButton.setOnClickListener(v -> NavHostFragment.findNavController(this)
                .navigate(R.id.action_homeFragment_to_soundLibraryFragment));
        binding.saveButton.setOnClickListener(v -> saveConfig());

        binding.configNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.configNameInputLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
                configNameCache = s == null ? "" : s.toString();
            }
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (homeViewModel != null) {
                    homeViewModel.setPendingEdit(null);
                }
                setEnabled(false);
                NavHostFragment.findNavController(ConfigEditorFragment.this).popBackStack();
            }
        });

        if (!initialised) {
            originalConfig = homeViewModel.getPendingEdit();
            stages.clear();
            if (originalConfig != null) {
                configNameCache = originalConfig.getName();
                stages.addAll(new ArrayList<>(originalConfig.getStages()));
            }
            initialised = true;
        }
        binding.configNameInput.setText(configNameCache);
        stageListAdapter.notifyDataSetChanged();
        updateTotalMinutes();

        getParentFragmentManager().setFragmentResultListener(StageEditorFragment.RESULT_KEY_STAGE, getViewLifecycleOwner(), (requestKey, bundleResult) -> {
            String stageName = bundleResult.getString(StageEditorFragment.RESULT_STAGE_NAME, getString(R.string.label_stage_placeholder));
            int minutes = bundleResult.getInt(StageEditorFragment.RESULT_STAGE_MINUTES, 5);
            int repeat = bundleResult.getInt(StageEditorFragment.RESULT_STAGE_REPEAT, 0);
            ArrayList<String> sounds = bundleResult.getStringArrayList(StageEditorFragment.RESULT_STAGE_SOUNDS);
            int index = bundleResult.getInt(StageEditorFragment.RESULT_STAGE_INDEX, -1);

            MeditationStage updatedStage = new MeditationStage(stageName, minutes, repeat, sounds == null ? new ArrayList<>() : new ArrayList<>(sounds));
            if (index >= 0 && index < stages.size()) {
                stages.set(index, updatedStage);
            } else {
                stages.add(updatedStage);
            }
            stageListAdapter.notifyDataSetChanged();
            updateTotalMinutes();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (homeViewModel != null) {
            homeViewModel.setPendingEdit(null);
        }
        binding = null;
    }

    @Override
    public void onEditStage(int position, @NonNull MeditationStage stage) {
        openStageEditor(stage, position);
    }

    @Override
    public void onDeleteStage(int position, @NonNull MeditationStage stage) {
        if (position >= 0 && position < stages.size()) {
            stages.remove(position);
        } else {
            stages.remove(stage);
        }
        stageListAdapter.notifyDataSetChanged();
        updateTotalMinutes();
    }

    /**
     * Opens the stage editor to create a new stage without pre-filled data.
     */
    private void openStageEditor(@Nullable MeditationStage stage) {
        openStageEditor(stage, -1);
    }

    /**
     * Launches the stage editor with the provided stage details so the user can edit them.
     *
     * @param stage existing stage or {@code null} when creating a new one.
     * @param index index of the stage in the current list, or {@code -1} when adding.
     */
    private void openStageEditor(@Nullable MeditationStage stage, int index) {
        Bundle args = new Bundle();
        if (stage != null) {
            args.putString(StageEditorFragment.ARG_STAGE_NAME, stage.getName());
            args.putInt(StageEditorFragment.ARG_STAGE_MINUTES, stage.getMinutes());
            args.putInt(StageEditorFragment.ARG_STAGE_REPEAT, stage.getRepeatMinutes());
            args.putStringArrayList(StageEditorFragment.ARG_STAGE_SOUNDS, new ArrayList<>(stage.getSounds()));
            args.putInt(StageEditorFragment.ARG_STAGE_INDEX, index);
        }
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_configEditorFragment_to_stageEditorFragment, args);
    }

    /**
     * Recomputes the total duration for the config and updates UI warnings when exceeding limits.
     */
    private void updateTotalMinutes() {
        int total = 0;
        for (MeditationStage stage : stages) {
            total += stage.getMinutes();
        }
        binding.totalMinutesInput.setText(String.valueOf(total));
        binding.timeWarningText.setVisibility(total > 180 ? View.VISIBLE : View.GONE);
        if (total > 180) {
            binding.timeWarningText.setText(getString(R.string.warning_stage_total_exceed, 180));
        }
    }

    /**
     * Validates the current config data, persists through {@link HomeViewModel}, and closes editor.
     */
    private void saveConfig() {
        String name = binding.configNameInput.getText() == null ? "" : binding.configNameInput.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            binding.configNameInputLayout.setError(getString(R.string.dialog_add_config_hint));
            return;
        }
        if (stages.isEmpty()) {
            binding.timeWarningText.setVisibility(View.VISIBLE);
            binding.timeWarningText.setText(R.string.label_stage_placeholder);
            return;
        }
        List<MeditationStage> copy = new ArrayList<>(stages);
        int total = 0;
        for (MeditationStage stage : copy) {
            total += stage.getMinutes();
        }
        MeditationConfig updated = new MeditationConfig(name, total, new ArrayList<>(copy));
        homeViewModel.saveConfig(updated, originalConfig);
        homeViewModel.setPendingEdit(null);
        NavHostFragment.findNavController(this).popBackStack();
    }
}
