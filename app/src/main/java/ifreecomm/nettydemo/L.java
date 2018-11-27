package ifreecomm.nettydemo;

import android.util.Log;


public class L {

    public static final String TAG = "xingluo";//"netty";
    private static final boolean DEBUG = true;//BuildConfig.DEBUG;

    public static void i(String msg) {
        if (DEBUG)
            Log.i(TAG, msg);
    }

    public static void e(String msg) {
        if (DEBUG)
            Log.e(TAG, msg);
    }

    public static void d(String msg) {
        if (DEBUG)
            Log.d(TAG, msg);
    }
    public static void v(String msg) {
        if (DEBUG)
            Log.v(TAG, msg);
    }
    public static void w(String msg) {
        if (DEBUG)
            Log.w(TAG, msg);
    }

    public static void e(String msg, Throwable e) {
        if (DEBUG)
            Log.e(TAG, msg, e);
    }
}
