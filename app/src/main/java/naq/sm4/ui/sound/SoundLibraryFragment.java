package naq.sm4.ui.sound;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import naq.sm4.R;
import naq.sm4.core.storage.StorageHelper;
import naq.sm4.databinding.FragmentSoundLibraryBinding;

public class SoundLibraryFragment extends Fragment implements SoundLibraryAdapter.SelectionListener {

    private FragmentSoundLibraryBinding binding;
    private SoundLibraryViewModel viewModel;
    private SoundLibraryAdapter adapter;
    private ActivityResultLauncher<String[]> pickAudioLauncher;
    private final Deque<android.net.Uri> pendingImportQueue = new ArrayDeque<>();
    private AlertDialog renameDialog;
    private AlertDialog deleteDialog;
    private boolean loading;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickAudioLauncher = registerForActivityResult(new ActivityResultContracts.OpenMultipleDocuments(), uris -> {
            if (uris == null || uris.isEmpty()) {
                return;
            }
            pendingImportQueue.clear();
            pendingImportQueue.addAll(uris);
            processNextImport();
        });
    }

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

        binding.addSoundButton.setOnClickListener(v -> launchAudioPicker());
        binding.selectSoundButton.setOnClickListener(v -> toggleSelectionMode());
        binding.deleteSelectedButton.setOnClickListener(v -> confirmDeleteSelected());

        binding.libraryActionToggle.addOnButtonCheckedListener(this::handleToggleChecked);

        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            loading = isLoading != null && isLoading;
            if (binding != null) {
                binding.libraryLoadingIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
                binding.addSoundButton.setEnabled(!loading);
                binding.selectSoundButton.setEnabled(!loading || adapter.isSelectionMode());
                binding.deleteSelectedButton.setEnabled(!loading && adapter.getSelectionCount() > 0);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (binding == null || TextUtils.isEmpty(message)) {
                return;
            }
            Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
            viewModel.clearError();
        });

        viewModel.getMessages().observe(getViewLifecycleOwner(), message -> {
            if (binding == null || TextUtils.isEmpty(message)) {
                return;
            }
            Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
            viewModel.clearMessage();
        });

        viewModel.getDeletePreview().observe(getViewLifecycleOwner(), this::handleDeletePreview);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (renameDialog != null) {
            renameDialog.dismiss();
            renameDialog = null;
        }
        if (deleteDialog != null) {
            deleteDialog.dismiss();
            deleteDialog = null;
        }
        pendingImportQueue.clear();
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
        binding.deleteSelectedButton.setEnabled(!loading && count > 0);
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
        binding.selectSoundButton.setEnabled(!loading || enabled);
        binding.addSoundButton.setEnabled(!loading);
        binding.deleteSelectedButton.setEnabled(!loading && adapter.getSelectionCount() > 0);
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
        viewModel.prepareDelete(selected);
    }

    private void launchAudioPicker() {
        if (pickAudioLauncher != null) {
            pickAudioLauncher.launch(new String[]{"audio/*"});
        }
    }

    private void processNextImport() {
        if (pendingImportQueue.isEmpty() || binding == null || !isAdded()) {
            return;
        }
        android.net.Uri uri = pendingImportQueue.pollFirst();
        showRenameDialog(uri);
    }

    private void showRenameDialog(@NonNull android.net.Uri uri) {
        if (renameDialog != null) {
            renameDialog.dismiss();
        }
        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        int padding = getResources().getDimensionPixelSize(R.dimen.appbar_padding);
        input.setPadding(padding, padding / 2, padding, padding / 2);
        String defaultName = resolveDefaultFileName(uri);
        input.setText(defaultName);
        input.setSelection(defaultName.length());
        renameDialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.library_add_title)
                .setMessage(R.string.library_saf_hint)
                .setView(input)
                .setNegativeButton(R.string.button_cancel, (dialog, which) -> {
                    pendingImportQueue.clear();
                    viewModel.clearError();
                })
                .setPositiveButton(R.string.library_add, null)
                .create();
        renameDialog.setOnShowListener(dlg -> renameDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(btn -> {
            String entered = input.getText() == null ? "" : input.getText().toString();
            String trimmed = entered.trim();
            if (trimmed.isEmpty()) {
                input.setError(getString(R.string.library_add_error));
                return;
            }
            viewModel.importSound(uri, trimmed);
            renameDialog.dismiss();
            renameDialog = null;
            processNextImport();
        }));
        renameDialog.setOnCancelListener(dialog -> {
            pendingImportQueue.clear();
        });
        renameDialog.show();
    }

    private String resolveDefaultFileName(@NonNull android.net.Uri uri) {
        String name = queryDisplayName(uri);
        if (TextUtils.isEmpty(name)) {
            name = "sound";
        }
        String sanitized = StorageHelper.sanitizeFileName(name);
        if (TextUtils.isEmpty(sanitized)) {
            sanitized = "sound";
        }
        if (!StorageHelper.isSupportedAudioFile(sanitized)) {
            sanitized += ".mp3";
        }
        return sanitized;
    }

    private String queryDisplayName(@NonNull android.net.Uri uri) {
        Cursor cursor = null;
        try {
            cursor = requireContext().getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    private void handleDeletePreview(@Nullable SoundLibraryViewModel.DeletePreview preview) {
        if (binding == null) {
            return;
        }
        if (preview == null) {
            if (deleteDialog != null) {
                deleteDialog.dismiss();
                deleteDialog = null;
            }
            return;
        }
        if (deleteDialog != null) {
            deleteDialog.dismiss();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.library_delete_title)
                .setNegativeButton(R.string.button_cancel, (dialog, which) -> viewModel.clearDeletePreview());
        if (preview.getInUse().isEmpty()) {
            builder.setMessage(getString(R.string.library_delete_message, preview.getFiles().size()))
                    .setPositiveButton(R.string.menu_delete, (dialog, which) -> {
                        viewModel.executeDelete(true);
                        adapter.clearSelection();
                        adapter.setSelectionMode(false);
                    });
        } else {
            String warning = TextUtils.join(", ", preview.getInUse());
            builder.setMessage(getString(R.string.library_delete_in_use_warning, warning))
                    .setPositiveButton(R.string.menu_delete, (dialog, which) -> {
                        viewModel.executeDelete(true);
                        adapter.clearSelection();
                        adapter.setSelectionMode(false);
                    });
        }
        deleteDialog = builder.setOnDismissListener(dialog -> viewModel.clearDeletePreview()).show();
    }
}
