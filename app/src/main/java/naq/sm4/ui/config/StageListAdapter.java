package naq.sm4.ui.config;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import naq.sm4.data.MeditationStage;
import naq.sm4.databinding.ItemStageCardBinding;

class StageListAdapter extends RecyclerView.Adapter<StageListAdapter.StageViewHolder> {

    interface StageCardListener {
        void onEditStage(int position, @NonNull MeditationStage stage);

        void onDeleteStage(int position, @NonNull MeditationStage stage);
    }

    private final List<MeditationStage> stages;
    private final StageCardListener listener;

    StageListAdapter(@NonNull List<MeditationStage> stages, @NonNull StageCardListener listener) {
        this.stages = stages;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemStageCardBinding binding = ItemStageCardBinding.inflate(inflater, parent, false);
        return new StageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull StageViewHolder holder, int position) {
        MeditationStage stage = stages.get(position);
        ItemStageCardBinding binding = holder.binding;
        String displayName = stage.getName() == null || stage.getName().isEmpty()
                ? binding.getRoot().getResources().getString(naq.sm4.R.string.label_stage_placeholder)
                : stage.getName();
        binding.stageNameText.setText(displayName);

        String timeInfo = binding.getRoot().getResources()
                .getString(naq.sm4.R.string.label_minutes_suffix, stage.getMinutes());
        String repeatInfo = stage.hasRepeat()
                ? stage.getRepeatDescription()
                : binding.getRoot().getResources().getString(naq.sm4.R.string.label_stage_repeat_single);
        binding.stageDurationText.setText(timeInfo + " · " + repeatInfo);

        binding.stageSoundsText.setText(binding.getRoot().getResources()
                .getString(naq.sm4.R.string.label_sound_count, stage.getSounds().size()));

        binding.editStageButton.setOnClickListener(v -> listener.onEditStage(position, stage));
        binding.deleteStageButton.setOnClickListener(v -> listener.onDeleteStage(position, stage));
        binding.getRoot().setOnClickListener(v -> listener.onEditStage(position, stage));
    }

    @Override
    public int getItemCount() {
        return stages.size();
    }

    static class StageViewHolder extends RecyclerView.ViewHolder {
        final ItemStageCardBinding binding;

        StageViewHolder(@NonNull ItemStageCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
