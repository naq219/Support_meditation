package naq.sm4.ui.config;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import naq.sm4.R;
import naq.sm4.data.MeditationStage;
import naq.sm4.data.MockData;
import naq.sm4.databinding.FragmentConfigEditorBinding;

public class ConfigEditorFragment extends Fragment implements StageListAdapter.StageCardListener {

    private FragmentConfigEditorBinding binding;
    private StageListAdapter stageListAdapter;
    private final List<MeditationStage> mockStages = new ArrayList<>(MockData.getTemplateStages());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentConfigEditorBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        stageListAdapter = new StageListAdapter(mockStages, this);
        binding.stageRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.stageRecyclerView.setAdapter(stageListAdapter);

        binding.addStageButton.setOnClickListener(v -> NavHostFragment.findNavController(this)
                .navigate(R.id.action_configEditorFragment_to_stageEditorFragment));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onEditStage(@NonNull MeditationStage stage) {
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_configEditorFragment_to_stageEditorFragment);
    }

    @Override
    public void onDeleteStage(@NonNull MeditationStage stage) {
        // demo only, no deletion logic yet
    }
}
