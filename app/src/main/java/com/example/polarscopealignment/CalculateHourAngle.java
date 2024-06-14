package com.example.polarscopealignment;

import static android.content.ContentValues.TAG;
import android.util.Log;
import static java.lang.Math.*;
import Jama.Matrix;
import java.time.LocalDateTime;
import java.time.ZoneId;



public class CalculateHourAngle {
    double latitude_degrees = 0;
    double longitude_degrees = 0;
    double RA_with_precession;




    public double[] calculate_coords_with_precession(double RA, double DEC, SharedViewModel viewModel) {


        // calculations for Local Sidereal Time(LST), needed for the Hour Angle(HA) of an object:
        double J2000 = 2451545.0;
        //Calculations of current Julian day:

        LocalDateTime UTCtime = LocalDateTime.now(ZoneId.of("GMT"));
        Log.e(TAG, "UTCtime: " + UTCtime);
        int year = UTCtime.getYear();
        int month = UTCtime.getMonthValue();
        int day = UTCtime.getDayOfMonth();
        int hours = UTCtime.getHour();
        int min = UTCtime.getMinute();
        int sec = UTCtime.getSecond();
        double JD = julianday(year, month, day, hours, min, sec);
        Log.e(TAG, "Current Julian Day: " + JD);
        //UT is the Universal Time at this moment
        double UT = hours + min / 60.0 + sec / 3600.0;
        //local_time is the local to the user time


        // LocalTime ltime = LocalTime.now();
        //double hour = ((double) ltime.getHour()) + (((double) ltime.getMinute()) / 60) + (((double) ltime.getSecond()) / 3600);
        //Conversion of Universal Time (UT) to Greenwich mean sidereal time(GST)
        double S = julianday(year, month, day, 0, 0, 0) - J2000;

        double T = S / 36525.0;

        double T0 = 6.697374558 + 2400.051336 * T + (0.000025862 * T * T);
        T0 = T0 % 24.0;
        UT = UT * 1.002737909;
        double GST_decimal = (T0 + UT) % 24.0;

        //latidute of the user .should be defined at location.
        latitude_degrees = viewModel.getLatitute().getValue();
        // longitude of the user .should be defined at location.
        longitude_degrees = viewModel.getLongitude().getValue();
         //Convert longitude difference in degrees to
        //difference in time by dividing by 15.
        double longitude_in_time = longitude_degrees / 15.0;

        /*calculation of Local Sidereal Time (LST)*/

        double GST_plus_longitude = GST_decimal + longitude_in_time;
        if (GST_plus_longitude > 24) {
            GST_plus_longitude = GST_plus_longitude - 24;
        }

        if (GST_plus_longitude < 0) {
            GST_plus_longitude = GST_plus_longitude + 24;
        }

        double LST_decimal = GST_plus_longitude;

        /*TODO next line only for testing :*/
//LST_decimal=22.513337;

        Log.e(TAG, "LST is : " + LST_decimal);

         /* %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%        PART 3            %%%%%%%%%%%%%%%%%%%%%%%%%%%
         %%%%%%%%%%%%%%%%%%%%%%% Calculation of precession  of the RA and DEC coordinates%%%%%%%%%%%%%%
          %%%%%% using the rigorous method%%%%%%%%%%%%%%*/
        double JD1 = 2451545.5;
        T = (JD1 - 2451545) / 36525;
        double zeta = 0.6406161 * T + 0.0000839 * T * T + 0.0000050 * T * T * T;
        double z = 0.6406161 * T + 0.0003041 * T * T + 0.0000051 * T * T * T;
        double theta = 0.5567530 * T - 0.0001184 * T * T - 0.0000116 * T * T * T;
        //convert to radians
        zeta = zeta * PI / 180;
        z = z * PI / 180;
        theta = theta * PI / 180;

        //matrix P_t converts the coordinates from epoch 1(epoch 1=date the database was created) to epoch0(J2000).

        double[][] P_t_array = {
                {cos(zeta) * cos(theta) * cos(z) - sin(zeta) * sin(z), cos(zeta) * cos(theta) * sin(z) + sin(zeta) * cos(z), cos(zeta) * sin(theta)},
                {-sin(zeta) * cos(theta) * cos(z) - cos(zeta) * sin(z), -sin(zeta) * cos(theta) * sin(z) + cos(zeta) * cos(z), -sin(zeta) * sin(theta)},
                {-sin(theta) * cos(z), -sin(theta) * sin(z), cos(theta)},
        };
        // convert RA from decimal hours to radian degrees
        double RA_rad = RA * PI / 180;
        double a1 = RA_rad * 15;
        //convert DEC from decimal degrees to radian degrees(already in degrees so no need to multiply*15)
        double DEC_rad = DEC * PI / 180;
        double d1 = DEC_rad;
        //vector v corresponds to the coordinates at epoch 1, a1(RA) and d1(DEC)
        double[] v_array = {cos(a1) * cos(d1), sin(a1) * cos(d1), sin(d1)};

        //vectol s corresponds to the coordinates at epoch 0(J2000)
        //double s=P_t * v;
        Matrix P_t = new Matrix(P_t_array);
        Matrix v = new Matrix(v_array, 1);
        v = v.transpose();
        Matrix s = P_t.times(v);
        // Log.e(TAG, "s is :\n " + strung(s));


        //Second part of the precession calculation :J0(Julian day 0) to J2(current Julian day)
        //P = [CX*CT*CZ-SX*SZ -SX*CT*CZ-CX*SZ -ST*CZ; CX*CT*SZ+SX*CZ -SX*CT*SZ+CX*CZ -ST*SZ;CX*ST -SX*ST CT]
        // JD=2459223.52527914;
        double JD2 = JD;
        T = (JD2 - 2451545) / 36525;
        zeta = 0.6406161 * T + 0.0000839 * T * T + 0.0000050 * T * T * T;
        z = 0.6406161 * T + 0.0003041 * T * T + 0.0000051 * T * T * T;
        theta = 0.5567530 * T - 0.0001184 * T * T - 0.0000116 * T * T * T;
        //convert to radians
        zeta = zeta * PI / 180;
        z = z * PI / 180;
        theta = theta * PI / 180;

        double[][] P_array = {
                {cos(zeta) * cos(theta) * cos(z) - sin(zeta) * sin(z), -sin(zeta) * cos(theta) * cos(z) - cos(zeta) * sin(z), -sin(theta) * cos(z)},
                {cos(zeta) * cos(theta) * sin(z) + sin(zeta) * cos(z), -sin(zeta) * cos(theta) * sin(z) + cos(zeta) * cos(z), -sin(theta) * sin(z)},
                {cos(zeta) * sin(theta), -sin(zeta) * sin(theta), cos(theta)},
        };
        Matrix P = new Matrix(P_array);
        Matrix w = P.times(s);
        //  Log.e(TAG, "w is :\n " + strung(w));

        double a2 = atan2(w.get(1, 0), w.get(0, 0));
        Log.e(TAG, "a2 is :\n " + a2);
        double d2 = asin(w.get(2, 0));

        //convert to degrees
        a2 = a2 * 180 / PI;
        d2 = d2 * 180 / PI;
        if (w.get(1, 0) < 0 && w.get(0, 0) > 0) {
            a2 = a2 + 180;
        }


        if (w.get(1, 0) < 0 && w.get(0, 0) < 0) {
            a2 = a2 + 360;
        }


        if (w.get(0, 0) > 0 && (w.get(1, 0) < 0)) {
            a2 = a2 + 180;
        }
        //convert a2 from degrees to hours
        a2 = a2 / 15;

        Log.e(TAG, "a2 is :\n " + a2);
        Log.e(TAG, "d2 is :\n " + d2);

        this.RA_with_precession = a2;
        viewModel.setRA_decimal(this.RA_with_precession);
        DEC = d2;


   /* HA=LST-RA. Calculation of the Hour Angle(HA) of a target object, based on its Right Ascension (RA) coordinate and the
    Local Sidereal Time (LST).*/

        double HA_decimal = LST_decimal - RA_with_precession;
        if (HA_decimal < 0) {

            HA_decimal = HA_decimal + 24;
        }


        double[] coords_with_precession = {HA_decimal, DEC};

        return coords_with_precession;

    }
    private double julianday(int year, int month, int day, int hour, int minute, int second) {
        if (month <= 2) { // January & February
            year = year - 1;
            month = month + 12;
        }
        double dayFraction = (hour + minute / 60.0 + second / 3600.0) / 24.0;
        double day2 = floor(365.25 * (year + 4716.0)) + floor(30.6001 * (month + 1.0)) + 2.0 - floor(year / 100.0) + floor(floor(year / 100.0) / 4.0) + day - 1524.5;
        double JD = dayFraction + day2;
        return JD;
    }



}
