/*****************************************************************
 *Email: support@lansongtech.com
 * AndroidUtil.java
 *
 *
 *这个程序仅仅是演示版本，仅是功能上的呈现，不保证性能和适用性。如正好满足您的项目，我们深感荣幸。
 *我们有更专业稳定强大的发行版本，期待和您进一步的合作。
 *
 *Email: support@lansongtech.com
 *****************************************************************/


package com.LanSoSdk.Play;

import android.net.Uri;
import android.os.Build;
import android.util.Log;

import java.io.File;

public class AndroidVersion {
    public static boolean isFroyoOrLater() {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO;
    }

    public static boolean isGingerbreadOrLater() {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD;
    }

    public static boolean isHoneycombOrLater() {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean isHoneycombMr1OrLater() {
        return android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    public static boolean isICSOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    public static boolean isJellyBeanOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean isJellyBeanMR1OrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    }

    public static boolean isJellyBeanMR2OrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    public static boolean isKitKatOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static boolean isLolliPopOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static File UriToFile(Uri uri) {
        return new File(uri.getPath().replaceFirst("file://", ""));
    }

    /**
     * @param path The path to be converted.
     * @return A URI representation of path
     */
    public static Uri PathToUri(String path) {
        return Uri.fromFile(new File(path));
    }

    public static Uri LocationToUri(String location) {
        Uri uri = Uri.parse(location);
        if (uri.getScheme() == null)
            throw new IllegalArgumentException("location has no scheme");
        return uri;
    }

    public static Uri FileToUri(File file) {
        return Uri.fromFile(file);
    }
}