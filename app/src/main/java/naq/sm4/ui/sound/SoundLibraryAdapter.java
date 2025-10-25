package naq.sm4.ui.sound;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import naq.sm4.databinding.ItemSoundFileBinding;

class SoundLibraryAdapter extends RecyclerView.Adapter<SoundLibraryAdapter.SoundViewHolder> {

    interface SelectionListener {
        void onSelectionCountChanged(int count);

        void onSelectionModeChanged(boolean enabled);
    }

    private final List<String> sounds = new ArrayList<>();
    private final Set<String> selected = new LinkedHashSet<>();
    private final SelectionListener selectionListener;
    private boolean selectionMode;

    SoundLibraryAdapter(@NonNull SelectionListener selectionListener) {
        this.selectionListener = selectionListener;
    }

    void updateSounds(@NonNull List<String> newSounds) {
        sounds.clear();
        sounds.addAll(newSounds);
        selected.retainAll(newSounds);
        notifyDataSetChanged();
        notifySelectionChanged();
    }

    void setSelectionMode(boolean enabled) {
        if (!enabled && selectionMode) {
            selectionMode = false;
            selected.clear();
            notifyModeChanged();
            notifyDataSetChanged();
            notifySelectionChanged();
        } else if (enabled && !selectionMode) {
            selectionMode = true;
            notifyModeChanged();
            notifyDataSetChanged();
            notifySelectionChanged();
        }
    }

    boolean isSelectionMode() {
        return selectionMode;
    }

    List<String> getSelectedSounds() {
        return new ArrayList<>(selected);
    }

    int getSelectionCount() {
        return selected.size();
    }

    void clearSelection() {
        if (selected.isEmpty()) {
            return;
        }
        selected.clear();
        notifyDataSetChanged();
        notifySelectionChanged();
    }

    @NonNull
    @Override
    public SoundViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemSoundFileBinding binding = ItemSoundFileBinding.inflate(inflater, parent, false);
        return new SoundViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SoundViewHolder holder, int position) {
        String sound = sounds.get(position);
        holder.bind(sound);
    }

    @Override
    public int getItemCount() {
        return sounds.size();
    }

    private void toggleSelection(@NonNull String sound) {
        if (!selectionMode) {
            return;
        }
        if (selected.contains(sound)) {
            selected.remove(sound);
        } else {
            selected.add(sound);
        }
        notifySelectionChanged();
    }

    private void notifySelectionChanged() {
        selectionListener.onSelectionCountChanged(selected.size());
    }

    private void notifyModeChanged() {
        selectionListener.onSelectionModeChanged(selectionMode);
    }

    class SoundViewHolder extends RecyclerView.ViewHolder {

        private final ItemSoundFileBinding binding;

        SoundViewHolder(@NonNull ItemSoundFileBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(String soundName) {
            binding.soundNameText.setText(soundName);

            boolean checked = selected.contains(soundName);
            binding.soundSelectionCheck.setOnCheckedChangeListener(null);
            binding.soundSelectionCheck.setChecked(checked);
            binding.soundSelectionCheck.setVisibility(selectionMode ? View.VISIBLE : View.GONE);
            binding.soundSelectionCheck.setOnCheckedChangeListener(createCheckedChangeListener(soundName));

            binding.getRoot().setOnClickListener(v -> {
                if (selectionMode) {
                    binding.soundSelectionCheck.toggle();
                }
            });

            binding.getRoot().setOnLongClickListener(v -> {
                if (!selectionMode) {
                    selectionMode = true;
                    binding.soundSelectionCheck.setVisibility(View.VISIBLE);
                    binding.soundSelectionCheck.setChecked(true);
                    selected.clear();
                    selected.add(soundName);
                    notifyModeChanged();
                    notifyDataSetChanged();
                    notifySelectionChanged();
                    return true;
                }
                return false;
            });
        }

        private CompoundButton.OnCheckedChangeListener createCheckedChangeListener(String soundName) {
            return (buttonView, isChecked) -> {
                if (!selectionMode) {
                    buttonView.setChecked(false);
                    return;
                }
                if (isChecked) {
                    selected.add(soundName);
                } else {
                    selected.remove(soundName);
                }
                notifySelectionChanged();
            };
        }
    }
}
