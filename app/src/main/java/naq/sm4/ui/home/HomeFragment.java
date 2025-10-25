package naq.sm4.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import naq.sm4.R;
import naq.sm4.data.MeditationConfig;
import naq.sm4.data.MockData;
import naq.sm4.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment implements HomeConfigAdapter.ConfigCardListener {

    private FragmentHomeBinding binding;
    private HomeConfigAdapter configAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<MeditationConfig> configs = MockData.getMeditationConfigs();
        configAdapter = new HomeConfigAdapter(configs, this);

        binding.configRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.configRecyclerView.setAdapter(configAdapter);

        binding.addConfigFab.setOnClickListener(v -> NavHostFragment.findNavController(this)
                .navigate(R.id.action_homeFragment_to_configEditorFragment));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onStartClicked(@NonNull MeditationConfig config) {
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_homeFragment_to_meditationTimerFragment);
    }

    @Override
    public void onEditClicked(@NonNull MeditationConfig config) {
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_homeFragment_to_configEditorFragment);
    }
}
