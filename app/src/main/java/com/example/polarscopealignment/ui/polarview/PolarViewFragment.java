package com.example.polarscopealignment.ui.polarview;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.polarscopealignment.CalculateHourAngle;
import com.example.polarscopealignment.R;
import com.example.polarscopealignment.SharedViewModel;

import java.text.DecimalFormat;

public class PolarViewFragment extends Fragment {


    private ConstraintLayout polar_alignment_scope_frame;
    double polaris_HA = 0.0d;
    CalculateHourAngle calculateHourAngle = new CalculateHourAngle();
    TextView text_long_lat;

    private TextView text_polaris_ha;
    private TextView text_position_in_polar_scope;
    private SharedViewModel viewModel;
    private Handler handler;
    double position_in_scope=0;
    private Runnable runnable;
    String[] dms ;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_polar_view, container, false);
        viewModel = new ViewModelProvider(getActivity()).get(SharedViewModel.class);
        text_long_lat=v.findViewById(R.id.text_long_lat);
        text_polaris_ha = v.findViewById(R.id.text_polaris_ha);
        text_position_in_polar_scope = v.findViewById(R.id.text_position_in_scope);
        polar_alignment_scope_frame = v.findViewById(R.id.outer_circle);


        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                String[] dms = convert_coords_to_dms(viewModel.getLatitute().getValue(), viewModel.getLongitude().getValue());
                text_long_lat.setText("Latitude    : "+ dms[0] + "\nLongitude : "+ dms[1]  );
                polaris_HA = get_polaris_HA();
                polar_alignment_scope_frame.setRotation(((-((float) mod(polaris_HA,24))) * 15.0f));
                text_polaris_ha.setText(decimal_hours_to_dms(polaris_HA));
                position_in_scope=polar_alignment_scope_frame.getRotation()/30;
                if(position_in_scope<-6){text_position_in_polar_scope.setText(decimal_hours_to_dms(position_in_scope+18));}
                else{text_position_in_polar_scope.setText(decimal_hours_to_dms(position_in_scope+6));}
                handler.postDelayed(this, 1000);
            }
        };

        // Start executing the runnable
        handler.post(runnable);






        return v;
    }


    public double get_polaris_HA() {
        double[] polaris_current_coords = calculateHourAngle.calculate_coords_with_precession(2.5303040666666665d, 89.264109d, viewModel);
        return polaris_current_coords[0];
    }

    public String decimal_hours_to_dms(double decimal_coord) {
        Integer degrees_int_part = Integer.valueOf((int) decimal_coord);
        double decimal_part = Math.abs((decimal_coord - ((double) degrees_int_part.intValue())) * 60.0d);
        Integer mm = Integer.valueOf((int) decimal_part);
        Double ss = Double.valueOf((decimal_part - ((double) mm.intValue())) * 60.0d);
        String mm_str = new DecimalFormat("00").format(mm);
        String ss_str = new DecimalFormat("00.0").format(ss);
        return degrees_int_part.toString() + "h " + mm_str + "m " + ss_str + "s";
    }

    public static String[] convert_coords_to_dms(double latitude, double longitude) {
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
    private double mod(double number, int divider) {
              double modulo = 0;
        if (number > 0) {
            modulo = number % divider;
        }
        if (number < 0) {
            modulo = (number % divider + divider) % divider;
        }
        return modulo;
    }
}