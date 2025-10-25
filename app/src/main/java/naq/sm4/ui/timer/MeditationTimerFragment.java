package naq.sm4.ui.timer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import naq.sm4.databinding.FragmentMeditationTimerBinding;

public class MeditationTimerFragment extends Fragment {

    private FragmentMeditationTimerBinding binding;

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
        binding.stopButton.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        binding.pauseButton.setOnClickListener(v -> {
            binding.pauseButton.setVisibility(View.GONE);
            binding.resumeButton.setVisibility(View.VISIBLE);
        });
        binding.resumeButton.setOnClickListener(v -> {
            binding.pauseButton.setVisibility(View.VISIBLE);
            binding.resumeButton.setVisibility(View.GONE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
