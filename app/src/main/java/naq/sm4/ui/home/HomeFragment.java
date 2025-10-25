package naq.sm4.ui.home;

import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import naq.sm4.R;
import naq.sm4.data.MeditationConfig;
import naq.sm4.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment implements HomeConfigAdapter.ConfigCardListener {

    private FragmentHomeBinding binding;
    private HomeConfigAdapter configAdapter;
    private HomeViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        configAdapter = new HomeConfigAdapter(this);

        binding.configRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.configRecyclerView.setAdapter(configAdapter);

        viewModel.getConfigs().observe(getViewLifecycleOwner(), this::renderConfigs);
        viewModel.isLoading().observe(getViewLifecycleOwner(), this::renderLoading);
        viewModel.getMessages().observe(getViewLifecycleOwner(), this::renderMessage);
        viewModel.getErrors().observe(getViewLifecycleOwner(), this::renderError);

        binding.addConfigFab.setOnClickListener(v -> showAddConfigDialog());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onStartClicked(@NonNull MeditationConfig config) {
        viewModel.setActiveSession(config);
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_homeFragment_to_meditationTimerFragment);
    }

    @Override
    public void onEditClicked(@NonNull MeditationConfig config) {
        viewModel.setPendingEdit(config);
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_homeFragment_to_configEditorFragment);
    }

    @Override
    public void onOptionsRequested(@NonNull View anchor, @NonNull MeditationConfig config) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), anchor);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.home_config_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_edit_config) {
                onEditClicked(config);
                return true;
            } else if (id == R.id.menu_delete_config) {
                confirmDeleteConfig(config);
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void renderConfigs(List<MeditationConfig> configs) {
        configAdapter.updateConfigs(configs);
        if (binding != null) {
            binding.emptyMessage.setVisibility(configs.isEmpty() ? View.VISIBLE : View.GONE);
            binding.progressBar.setVisibility(View.GONE);
        }
    }

    private void renderLoading(Boolean loading) {
        if (binding == null) {
            return;
        }
        boolean show = loading != null && loading;
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.addConfigFab.setEnabled(!show);
    }

    private void renderMessage(@Nullable String message) {
        if (binding == null || TextUtils.isEmpty(message)) {
            return;
        }
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
        viewModel.clearMessage();
    }

    private void renderError(@Nullable String error) {
        if (binding == null || TextUtils.isEmpty(error)) {
            return;
        }
        Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG).show();
        viewModel.clearError();
    }

    private void showAddConfigDialog() {
        if (binding == null) {
            return;
        }
        final EditText input = new EditText(requireContext());
        input.setHint(R.string.dialog_add_config_hint);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        int padding = getResources().getDimensionPixelSize(R.dimen.appbar_padding);
        input.setPadding(padding, padding / 2, padding, padding / 2);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_add_config_title)
                .setView(input)
                .setPositiveButton(R.string.dialog_add_config_positive, null)
                .setNegativeButton(R.string.button_cancel, (d, which) -> d.dismiss())
                .create();

        dialog.setOnShowListener(dlg -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(btn -> {
            String name = input.getText() == null ? "" : input.getText().toString();
            viewModel.addConfig(name);
            viewModel.setPendingEdit(null);
            dialog.dismiss();
        }));

        dialog.show();
    }

    private void confirmDeleteConfig(@NonNull MeditationConfig config) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.home_delete_title)
                .setMessage(getString(R.string.home_delete_message, config.getName()))
                .setNegativeButton(R.string.button_cancel, null)
                .setPositiveButton(R.string.menu_delete, (dialog, which) -> {
                    viewModel.deleteConfig(config);
                })
                .show();
    }
}
