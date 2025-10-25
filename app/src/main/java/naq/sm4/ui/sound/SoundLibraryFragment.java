package naq.sm4.ui.sound;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import naq.sm4.R;
import naq.sm4.databinding.FragmentSoundLibraryBinding;

public class SoundLibraryFragment extends Fragment implements SoundLibraryAdapter.SelectionListener {

    private FragmentSoundLibraryBinding binding;
    private SoundLibraryViewModel viewModel;
    private SoundLibraryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSoundLibraryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(SoundLibraryViewModel.class);
        adapter = new SoundLibraryAdapter(this);

        binding.soundRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.soundRecyclerView.setAdapter(adapter);

        viewModel.getSounds().observe(getViewLifecycleOwner(), this::renderSounds);

        binding.addSoundButton.setOnClickListener(v -> showAddSoundDialog());
        binding.selectSoundButton.setOnClickListener(v -> toggleSelectionMode());
        binding.deleteSelectedButton.setOnClickListener(v -> confirmDeleteSelected());

        binding.libraryActionToggle.addOnButtonCheckedListener(this::handleToggleChecked);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onSelectionCountChanged(int count) {
        if (binding == null) {
            return;
        }
        binding.selectionInfoText.setText(getString(R.string.library_selection_count, count));
        binding.selectionInfoText.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        binding.deleteSelectedButton.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onSelectionModeChanged(boolean enabled) {
        if (binding == null) {
            return;
        }
        MaterialButtonToggleGroup toggleGroup = binding.libraryActionToggle;
        binding.selectSoundButton.setText(enabled ? R.string.library_select_cancel : R.string.library_select_mode);
        binding.selectionInfoText.setVisibility(enabled && adapter.getSelectionCount() > 0 ? View.VISIBLE : View.GONE);
        binding.deleteSelectedButton.setVisibility(enabled && adapter.getSelectionCount() > 0 ? View.VISIBLE : View.GONE);
        if (!enabled) {
            toggleGroup.clearChecked();
        } else {
            toggleGroup.check(R.id.selectSoundButton);
        }
    }

    private void renderSounds(List<String> sounds) {
        adapter.updateSounds(sounds);
        if (binding != null) {
            boolean empty = sounds.isEmpty();
            binding.emptyLibraryText.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.soundRecyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
        }
    }

    private void showAddSoundDialog() {
        if (binding == null) {
            return;
        }
        final EditText input = new EditText(requireContext());
        input.setHint(R.string.library_add_hint);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        int padding = getResources().getDimensionPixelSize(R.dimen.appbar_padding);
        input.setPadding(padding, padding / 2, padding, padding / 2);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.library_add_title)
                .setView(input)
                .setPositiveButton(R.string.library_add, null)
                .setNegativeButton(R.string.button_cancel, (d, which) -> d.dismiss())
                .create();

        dialog.setOnShowListener(dlg -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(btn -> {
            String name = input.getText() == null ? "" : input.getText().toString();
            String trimmed = name.trim();
            if (trimmed.isEmpty()) {
                input.setError(getString(R.string.library_add_error));
                return;
            }
            String addedName = viewModel.addSound(trimmed);
            if (binding != null) {
                Snackbar.make(binding.getRoot(),
                        getString(R.string.library_added_message, addedName),
                        Snackbar.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        }));

        dialog.show();
    }

    private void toggleSelectionMode() {
        boolean enable = !adapter.isSelectionMode();
        adapter.setSelectionMode(enable);
    }

    private void handleToggleChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
        if (checkedId == R.id.selectSoundButton && !isChecked && adapter.isSelectionMode()) {
            adapter.setSelectionMode(false);
        } else if (checkedId == R.id.selectSoundButton && isChecked && !adapter.isSelectionMode()) {
            adapter.setSelectionMode(true);
        }
    }

    private void confirmDeleteSelected() {
        List<String> selected = adapter.getSelectedSounds();
        if (selected.isEmpty()) {
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.library_delete_title)
                .setMessage(getString(R.string.library_delete_message, selected.size()))
                .setNegativeButton(R.string.button_cancel, null)
                .setPositiveButton(R.string.menu_delete, (dialog, which) -> {
                    viewModel.removeSounds(selected);
                    adapter.clearSelection();
                    if (binding != null) {
                        Snackbar.make(binding.getRoot(),
                                getString(R.string.library_deleted_message, selected.size()),
                                Snackbar.LENGTH_SHORT).show();
                    }
                })
                .show();
    }
}
