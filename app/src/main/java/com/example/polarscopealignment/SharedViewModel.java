package com.example.polarscopealignment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


public class SharedViewModel extends ViewModel {
    private MutableLiveData<Double> RA_decimal = new MutableLiveData<>(0.0);
    private MutableLiveData<Double> latitude = new MutableLiveData<>(0.0);
    private MutableLiveData<Double> longitude = new MutableLiveData<>(0.0);
    private MutableLiveData<Boolean> dark_mode_enabled = new MutableLiveData<>(true);

    public void setRA_decimal(double ra_after_precession) { RA_decimal.setValue(ra_after_precession); }
    public void setLatitude(double lat) {
        latitude.setValue(lat);
    }

    public void setLongitude(double lon) {
        longitude.setValue(lon);
    }

    public LiveData<Double> getLatitute() {
        return latitude;
    }

    public LiveData<Double> getLongitude() {
        return longitude;
    }
    public void setDark_mode_enabled(boolean enabled){dark_mode_enabled.setValue(enabled);}
    public LiveData<Boolean> getDark_mode_enabled(){return dark_mode_enabled;}



}
