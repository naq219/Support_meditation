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

        originalConfig = homeViewModel.getPendingEdit();
        if (originalConfig != null) {
            binding.configNameInput.setText(originalConfig.getName());
            stages.clear();
            stages.addAll(new ArrayList<>(originalConfig.getStages()));
        } else {
            stages.clear();
            stages.addAll(homeViewModel.buildDefaultStages());
        }
        stageListAdapter.notifyDataSetChanged();
        updateTotalMinutes();

        getParentFragmentManager().setFragmentResultListener(StageEditorFragment.RESULT_KEY, getViewLifecycleOwner(), (requestKey, bundleResult) -> {
            ArrayList<String> sounds = bundleResult.getStringArrayList(StageEditorFragment.ARG_SELECTED_SOUNDS);
            if (sounds != null && !sounds.isEmpty()) {
                // future: integrate stage updates
            }
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
    public void onEditStage(@NonNull MeditationStage stage) {
        openStageEditor(stage);
    }

    @Override
    public void onDeleteStage(@NonNull MeditationStage stage) {
        stages.remove(stage);
        stageListAdapter.notifyDataSetChanged();
        updateTotalMinutes();
    }

    private void openStageEditor(@Nullable MeditationStage stage) {
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_configEditorFragment_to_stageEditorFragment);
    }

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
