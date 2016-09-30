package com.github.yamill.orientation;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.util.Log;

import com.facebook.common.logging.FLog;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.ReactConstants;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class OrientationModule extends ReactContextBaseJavaModule implements LifecycleEventListener {

    private final BroadcastReceiver receiver;

    public OrientationModule(ReactApplicationContext reactContext) {
        super(reactContext);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final Configuration newConfig = intent.getParcelableExtra("newConfig");
                Log.d("receiver", String.valueOf(newConfig.orientation));

                final String orientationValue = newConfig.orientation == 1 ? "PORTRAIT" : "LANDSCAPE";

                final WritableMap params = Arguments.createMap();
                params.putString("orientation", orientationValue);

                final ReactApplicationContext reactApplicationContext = getReactApplicationContext();
                if (reactApplicationContext.hasActiveCatalystInstance()) {
                    reactApplicationContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit("orientationDidChange", params);
                }
            }
        };

        reactContext.addLifecycleEventListener(this);
    }

    @Override
    public String getName() {
        return "Orientation";
    }

    @ReactMethod
    public void getOrientation(Promise promise) {
        final int orientationInt = getReactApplicationContext().getResources().getConfiguration().orientation;
        final String orientation = this.getOrientationString(orientationInt);

        promise.resolve(orientation);
    }

    @ReactMethod
    public void lockToPortrait() {
        setRequestedOrientation(getCurrentActivity(), ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @ReactMethod
    public void lockToLandscape() {
        setRequestedOrientation(getCurrentActivity(), ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
    }

    @ReactMethod
    public void lockToLandscapeLeft() {
        setRequestedOrientation(getCurrentActivity(), ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @ReactMethod
    public void lockToLandscapeRight() {
        setRequestedOrientation(getCurrentActivity(), ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
    }

    @ReactMethod
    public void unlockAllOrientations() {
        setRequestedOrientation(getCurrentActivity(), ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    private void setRequestedOrientation(Activity activity, int orientation) {
        if(null != activity) {
            activity.setRequestedOrientation(orientation);
        }
    }

    @Override
    public @Nullable Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<String, Object>();
        final int orientationInt = getReactApplicationContext().getResources().getConfiguration().orientation;

        final String orientation = this.getOrientationString(orientationInt);
        constants.put("initialOrientation", orientation);

        return constants;
    }

    private String getOrientationString(int orientation) {
        switch(orientation) {
            case Configuration.ORIENTATION_LANDSCAPE: return "LANDSCAPE";
            case Configuration.ORIENTATION_PORTRAIT: return "PORTRAIT";
            case Configuration.ORIENTATION_UNDEFINED:
            default:
                return "UNKNOWN";
        }
    }

    @Override
    public void onHostResume() {
        final Activity activity = getCurrentActivity();
        if(null != activity) {
            activity.registerReceiver(receiver, new IntentFilter("onConfigurationChanged"));
        } else {
            FLog.e(ReactConstants.TAG, "onHostResume activity was NULL");
        }
    }

    @Override
    public void onHostPause() {
        final Activity activity = getCurrentActivity();
        if (null != activity) {
            try {
                activity.unregisterReceiver(receiver);
            }
            catch (IllegalArgumentException e) {
                FLog.e(ReactConstants.TAG, "receiver already unregistered", e);
            }
        } else {
            FLog.e(ReactConstants.TAG, "onHostPause activity was NULL");
        }
    }

    @Override
    public void onHostDestroy() {
        final Activity activity = getCurrentActivity();
        if (null != activity) {
            try {
                activity.unregisterReceiver(receiver);
            } catch (IllegalArgumentException e) {
                FLog.e(ReactConstants.TAG, "receiver already unregistered", e);
            }
        } else {
            FLog.e(ReactConstants.TAG, "onHostDestroy activity was NULL");
        }
    }
}
