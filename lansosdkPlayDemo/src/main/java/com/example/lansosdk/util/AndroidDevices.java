/*****************************************************************************
 * AndroidDevices.java
 *****************************************************************************/

package com.example.lansosdk.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.view.InputDevice;
import android.view.MotionEvent;


import com.LanSoSdk.Play.AndroidVersion;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

public class AndroidDevices {
    public final static String TAG = "AndroidDevices";
    public final static String EXTERNAL_PUBLIC_DIRECTORY = Environment.getExternalStorageDirectory().getPath();

    final static boolean hasNavBar;
    final static boolean hasTsp;

    static {
        HashSet<String> devicesWithoutNavBar = new HashSet<String>();
        devicesWithoutNavBar.add("HTC One V");
        devicesWithoutNavBar.add("HTC One S");
        devicesWithoutNavBar.add("HTC One X");
        devicesWithoutNavBar.add("HTC One XL");
        hasNavBar = AndroidVersion.isICSOrLater()
                && !devicesWithoutNavBar.contains(android.os.Build.MODEL);
        hasTsp = LanSoDemoApplication.getAppContext().getPackageManager().hasSystemFeature("android.hardware.touchscreen");
    }

    public static boolean hasExternalStorage() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static boolean hasNavBar()
    {
        return hasNavBar;
    }

    /** hasCombBar test if device has Combined Bar : only for tablet with Honeycomb or ICS */
    public static boolean hasCombBar() {
        return (!AndroidDevices.isPhone()
                && ((VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) &&
                    (VERSION.SDK_INT <= VERSION_CODES.JELLY_BEAN)));
    }

    public static boolean isPhone(){
        TelephonyManager manager = (TelephonyManager)LanSoDemoApplication.getAppContext().getSystemService(Context.TELEPHONY_SERVICE);
        return manager.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE;
    }

    public static boolean hasTsp(){
        return hasTsp;
    }
    @TargetApi(VERSION_CODES.HONEYCOMB_MR1)
    public static float getCenteredAxis(MotionEvent event,
            InputDevice device, int axis) {
        final InputDevice.MotionRange range =
                device.getMotionRange(axis, event.getSource());

        // A joystick at rest does not always report an absolute position of
        // (0,0). Use the getFlat() method to determine the range of values
        // bounding the joystick axis center.
        if (range != null) {
            final float flat = range.getFlat();
            final float value = event.getAxisValue(axis);

            // Ignore axis values that are within the 'flat' region of the
            // joystick axis center.
            if (Math.abs(value) > flat) {
                return value;
            }
        }
        return 0;
    }
}
