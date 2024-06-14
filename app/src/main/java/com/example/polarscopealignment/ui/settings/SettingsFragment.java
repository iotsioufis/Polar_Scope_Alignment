package com.example.polarscopealignment.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.polarscopealignment.R;
import com.example.polarscopealignment.SharedViewModel;
import com.example.polarscopealignment.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {
    private RadioGroup themeRadioGroup;
    private RadioButton radioLight;
    private RadioButton radioDark;
    SharedViewModel viewModel;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    private FragmentSettingsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        viewModel = new ViewModelProvider(getActivity()).get(SharedViewModel.class);
        themeRadioGroup = root.findViewById(R.id.theme_radio_group);
        radioLight = root.findViewById(R.id.radio_light);
        radioDark = root.findViewById(R.id.radio_dark);
        if(viewModel.getDark_mode_enabled().getValue()){radioDark.setChecked(true);}
        else if(!viewModel.getDark_mode_enabled().getValue()){radioLight.setChecked(true);}
        sharedPreferences = getActivity().getSharedPreferences("com.example.polarscopealignment", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();


        themeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_light) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    viewModel.setDark_mode_enabled(false);
                } else if (checkedId == R.id.radio_dark) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    viewModel.setDark_mode_enabled(true);
                }

                editor.putBoolean("dark_mode_enabled", (boolean) viewModel.getDark_mode_enabled().getValue());
                editor.apply();

            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}