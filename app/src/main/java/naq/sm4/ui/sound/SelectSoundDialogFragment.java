package naq.sm4.ui.sound;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import naq.sm4.R;
import naq.sm4.core.storage.StorageHelper;
import naq.sm4.ui.sound.SoundLibraryViewModel;
import android.media.MediaPlayer;
import android.net.Uri;

/**
 * DialogFragment that lets the user pick one or more sound files from the
 * library to associate with a meditation stage.
 */
public class SelectSoundDialogFragment extends DialogFragment {

    public static final String ARG_SELECTED = "arg_selected_sounds";
    public static final String RESULT_KEY = "select_sound_result";
    public static final String RESULT_SELECTED = "result_selected_sounds";

    private SoundLibraryViewModel viewModel;
    private ArrayAdapter<String> adapter;
    private final Set<String> selected = new HashSet<>();
    private MaterialButton previewButton;
    private String lastHighlightedSound;
    private MediaPlayer mediaPlayer;

    /**
     * Builds the multi-choice dialog, pre-selecting any sounds passed via arguments
     * and observing the sound library for updates while the dialog is visible.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Context context = requireContext();
        viewModel = new ViewModelProvider(requireActivity()).get(SoundLibraryViewModel.class);

        View root = LayoutInflater.from(context).inflate(R.layout.dialog_select_sound, null, false);
        ListView listView = root.findViewById(android.R.id.list);
        TextView emptyView = root.findViewById(R.id.selectSoundEmptyText);
        listView.setEmptyView(emptyView);
        previewButton = root.findViewById(R.id.previewButton);

        adapter = new ArrayAdapter<>(context, R.layout.item_sound_choice, android.R.id.text1, new ArrayList<>());
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        ArrayList<String> preselected = getArguments() != null ? getArguments().getStringArrayList(ARG_SELECTED) : null;
        if (preselected != null && !preselected.isEmpty()) {
            selected.clear();
            selected.addAll(preselected);
            lastHighlightedSound = selected.iterator().next();
        } else {
            selected.clear();
            lastHighlightedSound = null;
        }

        previewButton.setOnClickListener(v -> playSelectedPreview());
        updatePreviewButtonState();

        viewModel.getSounds().observe(this, sounds -> {
            adapter.clear();
            adapter.addAll(sounds);
            adapter.notifyDataSetChanged();
            selected.retainAll(sounds);
            if (lastHighlightedSound != null && !sounds.contains(lastHighlightedSound)) {
                lastHighlightedSound = selected.isEmpty() ? null : selected.iterator().next();
            }
            updateSelections(listView, sounds);
            updatePreviewButtonState();
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String sound = adapter.getItem(position);
            if (TextUtils.isEmpty(sound)) {
                return;
            }
            if (selected.contains(sound)) {
                selected.remove(sound);
            } else {
                selected.add(sound);
            }
            lastHighlightedSound = sound;
            listView.setItemChecked(position, selected.contains(sound));
            updatePreviewButtonState();
        });

        return new AlertDialog.Builder(context)
                .setTitle(R.string.dialog_select_sound_title)
                .setView(root)
                .setNegativeButton(R.string.button_cancel, (dialog, which) -> dismiss())
                .setPositiveButton(R.string.button_done, (dialog, which) -> {
                    Bundle result = new Bundle();
                    result.putStringArrayList(RESULT_SELECTED, new ArrayList<>(selected));
                    getParentFragmentManager().setFragmentResult(RESULT_KEY, result);
                })
                .create();
    }

    @Override
    public void onStop() {
        super.onStop();
        releasePlayer();
    }

    /**
     * Synchronises the ListView check states with the current in-memory selection.
     */
    private void updateSelections(@NonNull ListView listView, @NonNull List<String> sounds) {
        for (int i = 0; i < sounds.size(); i++) {
            String sound = sounds.get(i);
            listView.setItemChecked(i, selected.contains(sound));
        }
    }

    private void updatePreviewButtonState() {
        if (previewButton == null) {
            return;
        }
        boolean enabled = (lastHighlightedSound != null) || !selected.isEmpty();
        previewButton.setEnabled(enabled);
        if (!enabled || mediaPlayer == null) {
            previewButton.setText(getString(R.string.button_preview_sound));
        }
    }

    private void playSelectedPreview() {
        String target = lastHighlightedSound;
        if (TextUtils.isEmpty(target) && !selected.isEmpty()) {
            target = selected.iterator().next();
        }
        if (TextUtils.isEmpty(target)) {
            return;
        }
        Context context = getContext();
        if (context == null) {
            return;
        }
        File workingDir;
        try {
            workingDir = StorageHelper.ensureWorkingDirectory();
        } catch (IOException e) {
            Toast.makeText(context, R.string.library_import_error, Toast.LENGTH_SHORT).show();
            return;
        }
        File targetFile = new File(workingDir, target);
        if (!targetFile.exists()) {
            Toast.makeText(context, getString(R.string.timer_missing_sound, target), Toast.LENGTH_SHORT).show();
            return;
        }

        releasePlayer();
        Uri uri = Uri.fromFile(targetFile);
        mediaPlayer = MediaPlayer.create(context, uri);
        if (mediaPlayer == null) {
            Toast.makeText(context, R.string.library_import_error, Toast.LENGTH_SHORT).show();
            updatePreviewButtonState();
            return;
        }
        previewButton.setText(getString(R.string.button_preview_sound_playing, target));
        mediaPlayer.setOnCompletionListener(mp -> {
            releasePlayer();
            updatePreviewButtonState();
        });
        mediaPlayer.start();
    }

    private void releasePlayer() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (previewButton != null) {
            previewButton.setText(getString(R.string.button_preview_sound));
        }
    }
}
