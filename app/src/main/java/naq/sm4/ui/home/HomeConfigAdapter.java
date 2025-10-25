package naq.sm4.ui.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import naq.sm4.R;
import naq.sm4.data.MeditationConfig;
import naq.sm4.databinding.ItemConfigCardBinding;

public class HomeConfigAdapter extends RecyclerView.Adapter<HomeConfigAdapter.ConfigViewHolder> {

    public interface ConfigCardListener {
        void onStartClicked(@NonNull MeditationConfig config);

        void onEditClicked(@NonNull MeditationConfig config);
    }

    private final List<MeditationConfig> configs;
    private final ConfigCardListener listener;

    public HomeConfigAdapter(@NonNull List<MeditationConfig> configs, @NonNull ConfigCardListener listener) {
        this.configs = configs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConfigViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemConfigCardBinding binding = ItemConfigCardBinding.inflate(inflater, parent, false);
        return new ConfigViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ConfigViewHolder holder, int position) {
        MeditationConfig config = configs.get(position);
        ItemConfigCardBinding binding = holder.binding;
        binding.configNameText.setText(config.getName());

        String minutesText = binding.getRoot().getResources()
                .getString(R.string.label_minutes_suffix, config.getTotalMinutes());
        String stagesText = binding.getRoot().getResources()
                .getString(R.string.label_stage_count, config.getStageCount());
        binding.configSummaryText.setText(minutesText + " · " + stagesText);

        binding.startButton.setOnClickListener(v -> listener.onStartClicked(config));
        binding.editButton.setOnClickListener(v -> listener.onEditClicked(config));
        binding.getRoot().setOnClickListener(v -> listener.onStartClicked(config));
    }

    @Override
    public int getItemCount() {
        return configs.size();
    }

    static class ConfigViewHolder extends RecyclerView.ViewHolder {
        final ItemConfigCardBinding binding;

        ConfigViewHolder(@NonNull ItemConfigCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
