package com.example.polarscopealignment.ui.location;

import static android.view.View.GONE;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.polarscopealignment.R;
import com.example.polarscopealignment.SharedViewModel;
import com.example.polarscopealignment.databinding.FragmentLocationBinding;
import com.google.android.material.button.MaterialButton;


public class LocationFragment extends Fragment implements LocationListener {

    private SharedViewModel viewModel;
    private TextView text_saved_longitude;
    private TextView text_saved_latitude;
     private TextView text_gps_info;
      private MaterialButton button_get_gps;
    private MaterialButton button_set_manually;
    private MaterialButton button_save_location;
    private MaterialButton button_cancel;
    private ProgressBar progressBar;
    private EditText latitudeDegrees, latitudeMinutes, latitudeSeconds;
    private Spinner latitudeDirection;
    private EditText longitudeDegrees, longitudeMinutes, longitudeSeconds;
    private Spinner longitudeDirection;
    private LinearLayout coords_enter_layout;
    String latitude_str = "";
    String longitude_str = "";
      double gps_latitude = 0.0;
    double gps_longitude = 0.0;
    LocationManager lm;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private ActivityResultLauncher<String> requestPermissionLauncher;


    private FragmentLocationBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(getActivity()).get(SharedViewModel.class);

        binding = FragmentLocationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        sharedPreferences = getActivity().getSharedPreferences("com.example.polarscopealignment", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        button_set_manually = root.findViewById(R.id.button_set_manually);
        text_saved_latitude = root.findViewById(R.id.textView_saved_latitude);
        text_saved_longitude = root.findViewById(R.id.textView_saved_longitude);
        text_gps_info = root.findViewById(R.id.textView_info_gps);
        button_get_gps = root.findViewById(R.id.button_get_gps);
        button_save_location = root.findViewById(R.id.button_save_location);
        button_cancel = root.findViewById(R.id.button_cancel);
        progressBar = root.findViewById(R.id.gps_progress_bar);
        latitudeDegrees = root.findViewById(R.id.latitude_degrees);
        latitudeMinutes = root.findViewById(R.id.latitude_minutes);
        latitudeSeconds = root.findViewById(R.id.latitude_seconds);
        latitudeDirection = root.findViewById(R.id.latitude_direction);
        String[] latitudeDirections = getResources().getStringArray(R.array.latitude_directions);
        // Create an ArrayAdapter using the custom layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner, latitudeDirections);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(R.layout.spinner);
        // Apply the adapter to the spinner
        latitudeDirection.setAdapter(adapter);
        longitudeDirection = root.findViewById(R.id.longitude_direction);
        String[] longitudeDirections = getResources().getStringArray(R.array.longitude_directions);
        // Create an ArrayAdapter using the custom layout
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(getActivity(), R.layout.spinner, longitudeDirections);
        // Specify the layout to use when the list of choices appears
        adapter2.setDropDownViewResource(R.layout.spinner);
        if( AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES){
            button_get_gps.setIconTint(ColorStateList.valueOf(Color.parseColor("#151515")));
            button_set_manually.setIconTint(ColorStateList.valueOf(Color.parseColor("#151515")));
            button_save_location.setIconTint(ColorStateList.valueOf(Color.parseColor("#151515")));
        }
        // Apply the adapter to the spinner
        longitudeDirection.setAdapter(adapter2);
        longitudeDegrees = root.findViewById(R.id.longitude_degrees);
        longitudeMinutes = root.findViewById(R.id.longitude_minutes);
        longitudeSeconds = root.findViewById(R.id.longitude_seconds);

        coords_enter_layout = root.findViewById(R.id.coords_enter_layout);

        loadLocation();
        String[] dms = convertToDMS(viewModel.getLatitute().getValue(), viewModel.getLongitude().getValue());
        text_saved_latitude.setText("Saved Latitude :    \n" + dms[0]);
        text_saved_longitude.setText("Saved Longitude : \n" + dms[1]);

        button_set_manually.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                text_saved_latitude.setVisibility(GONE);
                text_saved_longitude.setVisibility(GONE);
                progressBar.setVisibility(GONE);
                button_cancel.setVisibility(GONE);
                text_gps_info.setVisibility(GONE);
                coords_enter_layout.setVisibility(View.VISIBLE);
                button_save_location.setVisibility(View.VISIBLE);
            }
        });


        button_get_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                root.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                coords_enter_layout.setVisibility(GONE);
                checkAndRequestLocationPermission();
            }
        });


        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                root.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                if (progressBar.getVisibility() == View.VISIBLE) {
                    progressBar.setVisibility(GONE);
                    lm.removeUpdates(LocationFragment.this);
                } else if (progressBar.getVisibility() == GONE) {
                    /*open the gps settings*/
                    Intent settingintent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(settingintent);

                }
                button_cancel.setVisibility(GONE);
                text_gps_info.setVisibility(GONE);


            }
        });


        button_save_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                root.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);


                String latDeg = latitudeDegrees.getText().toString();
                String latMin = latitudeMinutes.getText().toString();
                String latSec = latitudeSeconds.getText().toString();
                String latDir = latitudeDirection.getSelectedItem().toString();

                String lonDeg = longitudeDegrees.getText().toString();
                String lonMin = longitudeMinutes.getText().toString();
                String lonSec = longitudeSeconds.getText().toString();
                String lonDir = longitudeDirection.getSelectedItem().toString();
if((!latDeg.equals("")) && (!latMin.equals("")) && (!latSec.equals("")) && (!lonDeg.equals("")) && (!lonMin.equals(""))  && (!lonSec.equals(""))){
            text_saved_latitude.setVisibility(View.VISIBLE);
                text_saved_longitude.setVisibility(View.VISIBLE);
                coords_enter_layout.setVisibility(GONE);
                double latitude = convertDMSToDecimal(latDeg, latMin, latSec, latDir);
                double longitude = convertDMSToDecimal(lonDeg, lonMin, lonSec, lonDir);
                viewModel.setLatitude(latitude);
                viewModel.setLongitude(longitude);
                String[] dms = convertToDMS(latitude , longitude);
                text_saved_latitude.setText("Saved Latitude :    \n" + dms[0]);
                text_saved_longitude.setText("Saved Longitude : \n" + dms[1]);
                button_save_location.setVisibility(GONE);
                saveLocation();}
else Toast.makeText(getContext(), "Missing fields ", Toast.LENGTH_LONG).show();
            }

        });




// Registers the permissions callback
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean isGranted) {
                        if (isGranted) {
                            LocationManager lm = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
                            boolean isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

                            if (isGPSEnabled) {
                                progressBar.setVisibility(View.VISIBLE);
                            }
                            getLocation();
                            text_saved_latitude.setVisibility(GONE);
                            text_saved_longitude.setVisibility(GONE);
                            button_save_location.setVisibility(GONE);
                            coords_enter_layout.setVisibility(GONE);
                        } else {
                            if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                                Toast.makeText(getContext(), "Permission denied permanently. Please allow location permission from settings.", Toast.LENGTH_LONG).show();
                                openAppSettings();
                            } else {
                                Toast.makeText(getContext(), "Please allow location permission to get GPS coordinates", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );
        return root;
    }














    private void checkAndRequestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationManager lm = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
            boolean isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (isGPSEnabled) {
                progressBar.setVisibility(View.VISIBLE);
            }
            getLocation();
            text_saved_latitude.setVisibility(GONE);
            text_saved_longitude.setVisibility(GONE);
            button_save_location.setVisibility(GONE);
            coords_enter_layout.setVisibility(GONE);
        } else {
            requestLocationPermission();
        }
    }


    private void requestLocationPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            showPermissionRationale();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void showPermissionRationale() {
        new AlertDialog.Builder(getContext())
                .setTitle("Location Permission Needed")
                .setMessage("This app needs location permission to provide GPS coordinates.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }


    public Location getLocation() {

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            progressBar.setVisibility(GONE);
            /*if persmission is not granded getlocation does not continue its execution and returns null*/
            /*for newer versions of android it is mandatory to stop if permission is not granded*/
            return null;
        }
        lm = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isGPSEnabled) {
            /*if permission IS granded and gps is ENABLED get location*/
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, this);
            text_gps_info.setText("Receiving GPS coordinates ...");
            button_cancel.setText(" cancel ");
            button_cancel.setVisibility(View.VISIBLE);
            text_gps_info.setVisibility(View.VISIBLE);


        } else {
            /*open the gps settings*/
            Intent settingintent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(settingintent, 0);


        }
        /*if GPS is NOT ENABLED return null*/

        return null;
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        gps_latitude = location.getLatitude();
        gps_longitude = location.getLongitude();
        viewModel.setLongitude(gps_longitude);
        viewModel.setLatitude(gps_latitude);
        double gps_accuracy = location.getAccuracy();
        Toast.makeText(getContext(), "Accuracy" + ((int) gps_accuracy) + " meters", Toast.LENGTH_LONG).show();
        progressBar.setVisibility(View.GONE);
        String[] dms = convertToDMS(gps_latitude ,gps_longitude);
        text_saved_latitude.setText("Saved Latitude :    \n" + dms[0]);
        text_saved_longitude.setText("Saved Longitude : \n" + dms[1]);
        text_saved_latitude.setVisibility(View.VISIBLE);
        text_saved_longitude.setVisibility(View.VISIBLE);
        text_gps_info.setText("  GPS coordinates successfully received.\n\n    Device's GPS can now be disabled");
        button_cancel.setText("disable GPS");
        saveLocation();
        lm.removeUpdates(this);

    }


    @Override
    public void onProviderEnabled(@NonNull String provider) {
        if (getContext() != null) {
            Toast.makeText(getContext(), "GPS is turned ON ", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        if (getContext() != null) {
            Toast.makeText(getContext(), "GPS is turned OFF ", Toast.LENGTH_LONG).show();
        }
        progressBar.setVisibility(View.GONE);
        button_cancel.setVisibility(GONE);
        text_gps_info.setVisibility(GONE);
    }


    public void saveLocation() {
        editor.putFloat("latitude", (float) viewModel.getLatitute().getValue().doubleValue());
        editor.putFloat("longitude", (float) viewModel.getLongitude().getValue().doubleValue());
        editor.apply();
    }


    public void loadLocation() {
        viewModel.setLatitude(sharedPreferences.getFloat("latitude", 0.0f));
        viewModel.setLongitude(sharedPreferences.getFloat("longitude", 0.0f));
    }
    public static String[] convertToDMS(double latitude, double longitude) {
        // Convert latitude
        String latDirection = latitude >= 0 ? "N" : "S";
        latitude = Math.abs(latitude);
        int latDegrees = (int) latitude;
        int latMinutes = (int) ((latitude - latDegrees) * 60);
        double latSecondsDouble = (latitude - latDegrees - (latMinutes / 60.0)) * 3600;
        int latSeconds = (int) latSecondsDouble;

        // Convert longitude
        String lonDirection = longitude >= 0 ? "E" : "W";
        longitude = Math.abs(longitude);
        int lonDegrees = (int) longitude;
        int lonMinutes = (int) ((longitude - lonDegrees) * 60);
        double lonSecondsDouble = (longitude - lonDegrees - (lonMinutes / 60.0)) * 3600;
        int lonSeconds = (int) lonSecondsDouble;

        // Formatting the output
        String latDMS = String.format("%d°%d'%d\"%s", latDegrees, latMinutes, latSeconds, latDirection);
        String lonDMS = String.format("%d°%d'%d\"%s", lonDegrees, lonMinutes, lonSeconds, lonDirection);

        return new String[]{latDMS, lonDMS};
    }

    private double convertDMSToDecimal(String degrees, String minutes, String seconds, String direction) {
        double deg = Double.parseDouble(degrees);
        double min = Double.parseDouble(minutes);
        double sec = Double.parseDouble(seconds);
        double decimal = deg + (min / 60) + (sec / 3600);
        if (direction.equals("S") || direction.equals("W")) {
            decimal = -decimal;
        }
        return decimal;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            button_get_gps.performClick();
        }

    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}