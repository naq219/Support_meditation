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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import naq.sm4.R;
import naq.sm4.ui.sound.SoundLibraryViewModel;

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

        adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_multiple_choice, new ArrayList<>());
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        ArrayList<String> preselected = getArguments() != null ? getArguments().getStringArrayList(ARG_SELECTED) : null;
        if (preselected != null && !preselected.isEmpty()) {
            selected.clear();
            selected.addAll(preselected);
        } else {
            selected.clear();
        }

        viewModel.getSounds().observe(this, sounds -> {
            adapter.clear();
            adapter.addAll(sounds);
            adapter.notifyDataSetChanged();
            selected.retainAll(sounds);
            updateSelections(listView, sounds);
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
        });

        return new AlertDialog.Builder(context)
                .setTitle(R.string.dialog_select_sound_title)
                .setMessage(R.string.sound_library_title_selection)
                .setView(root)
                .setNegativeButton(R.string.button_cancel, (dialog, which) -> dismiss())
                .setPositiveButton(R.string.button_done, (dialog, which) -> {
                    Bundle result = new Bundle();
                    result.putStringArrayList(RESULT_SELECTED, new ArrayList<>(selected));
                    getParentFragmentManager().setFragmentResult(RESULT_KEY, result);
                })
                .create();
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
}
