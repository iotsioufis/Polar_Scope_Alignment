package com.example.polarscopealignment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.polarscopealignment.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    SharedPreferences sharedPreferences;
    SharedViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_polar_view, R.id.navigation_location, R.id.navigation_settings)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
        viewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        sharedPreferences = this.getSharedPreferences("com.example.polarscopealignment", Context.MODE_PRIVATE);
        viewModel.setLatitude(sharedPreferences.getFloat("latitude", 0.0f));
        viewModel.setLongitude(sharedPreferences.getFloat("longitude", 0.0f));
        viewModel.setDark_mode_enabled(sharedPreferences.getBoolean("dark_mode_enabled",true));
        if(!viewModel.getDark_mode_enabled().getValue()){AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);}
        if(viewModel.getDark_mode_enabled().getValue()){AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);}
    }

}