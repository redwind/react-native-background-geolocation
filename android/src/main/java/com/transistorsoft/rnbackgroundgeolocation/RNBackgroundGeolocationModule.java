package com.transistorsoft.rnbackgroundgeolocation;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.GoogleApiAvailability;

import com.transistorsoft.locationmanager.*;
import com.transistorsoft.locationmanager.adapter.BackgroundGeolocation;
import com.transistorsoft.locationmanager.adapter.TSCallback;
import com.transistorsoft.locationmanager.settings.*;
import com.transistorsoft.locationmanager.scheduler.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by chris on 2015-10-30.
 */
public class RNBackgroundGeolocationModule extends ReactContextBaseJavaModule implements ActivityEventListener, LifecycleEventListener {
    private static final String TAG = "TSLocationManager";

    public static final String ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final String ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;

    public static final int REQUEST_ACTION_START                = 1;
    public static final int REQUEST_ACTION_GET_CURRENT_POSITION = 2;
    public static final int REQUEST_ACTION_START_GEOFENCES      = 3;

    private boolean initialized = false;
    private Intent launchIntent;
    private Context context;

    private static final String EVENT_WATCHPOSITION = "watchposition";

    private HashMap<String, Callback> startCallback;
    public RNBackgroundGeolocationModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addLifecycleEventListener(this);
    }

    @Override
    public void initialize() {
        // do nothing
    }
    @Override
    public String getName() {
        return "RNBackgroundGeolocation";
    }
    @Override
    public void onHostResume() {
        TSLog.d("- RNBackgroundGeolocation#onHostResume");
        if (!initialized) {
            initializeLocationManager();
        }
    }
    @Override
    public void onHostPause() {

    }
    @Override
    public void onHostDestroy() {

    }
    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        TSLog.i("- onActivityResult: " + requestCode + ", " + resultCode + ", intent: " + intent);
    }

    @ReactMethod
    public void configure(ReadableMap config, final Callback success, final Callback failure) {
        TSCallback callback = new TSCallback() {
            public void success(Object state) {
                success.invoke(getState());
            }
            public void error(Object error) {
                TSLog.e("RNBackgroundGeolocation#configure failed");
                failure.invoke("Unknown error");
            }
        };
        getAdapter().configure(mapToJson(config), callback);
    }

    @ReactMethod
    public void start(Callback success, Callback failure) {
        if (startCallback != null) {
            failure.invoke("Waiting for a previous start action to complete");
            return;
        }
        startCallback = new HashMap();
        startCallback.put("success", success);
        startCallback.put("failure", failure);

        if (hasPermission(ACCESS_COARSE_LOCATION) && hasPermission(ACCESS_FINE_LOCATION)) {
            setEnabled(true);
        } else {
            String[] permissions = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION};
            requestPermissions(REQUEST_ACTION_START, permissions);
        }
    }

    @ReactMethod
    public void startSchedule(Callback success, Callback failure) {
        if (getAdapter().startSchedule()) {
            success.invoke();
        } else {
            failure.invoke("Failed to start schedule.  Did you configure a #schedule?");
        }
    }
    @ReactMethod
    public void stopSchedule(Callback success, Callback failure) {
        getAdapter().stopSchedule();
        success.invoke();
    }
    @ReactMethod
    public void startGeofences(Callback success, Callback failure) {
        /*
        startCallback = new HashMap();
        startCallback.put("success", success);
        startCallback.put("failure", failure);

        backgroundServiceIntent = new Intent(getCurrentActivity(), BackgroundGeolocationService.class);
        backgroundServiceIntent.putExtra("command", BackgroundGeolocation.ACTION__START_GEOFENCES);
        if (hasPermission(ACCESS_COARSE_LOCATION) && hasPermission(ACCESS_FINE_LOCATION)) {
            setEnabled(true);
        } else {
            String[] permissions = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION};
            requestPermissions(REQUEST_ACTION_START_GEOFENCES, permissions);
        }
        */
    }

    @ReactMethod
    public void stop() {
        startCallback = null;
        getAdapter().stop();
    }

    @ReactMethod
    public void changePace(Boolean moving, final Callback success, final Callback failure) {
        TSCallback callback = new TSCallback() {
            public void success(Object result) {
                success.invoke(getState());
            }
            public void error(Object result) {
                failure.invoke(result);
            }
        };
        getAdapter().changePace(moving, callback);
    }

    @ReactMethod
    public void setConfig(ReadableMap config, final Callback success, final Callback failure) {
        TSCallback callback = new TSCallback() {
            @Override
            public void success(Object o) {
                TSLog.d("#setConfig success");
                success.invoke(getState());
            }

            @Override
            public void error(Object o) {
                TSLog.e("#setConfig failed");
                failure.invoke("Unknown error");
            }
        };
        getAdapter().setConfig(mapToJson(config), callback);
    }

    @ReactMethod
    public void getState(Callback success, Callback failure) {
        WritableMap state = getState();
        if (!state.hasKey("error")) {
            success.invoke(state);
        } else {
            failure.invoke(state);
        }
    }

    private WritableMap getState() {
        try {
            return jsonToMap(Settings.getState());
        } catch (JSONException e) {
            TSLog.e("Failed to parse Settings#getState");
            e.printStackTrace();
            return null;
        }
    }
    @ReactMethod
    public void getLocations(final Callback success, final Callback failure) {
        TSCallback callback = new TSCallback() {
            public void success(Object result) {
                try {
                    success.invoke(convertJsonToArray((JSONArray) result));
                } catch (JSONException e) {
                    e.printStackTrace();
                    failure.invoke(e.getMessage());
                }
            }
            public void error(Object error) {
                failure.invoke((String) error);
            }
        };
        getAdapter().getLocations(callback);
    }

    @ReactMethod
    public void getCount(final Callback success, final Callback failure) {
        TSCallback callback = new TSCallback() {
            public void success(Object result) {
                success.invoke((int) result);
            }
            public void error(Object error) {
                failure.invoke((String) error);
            }
        };
        getAdapter().getCount(callback);
    }

    @ReactMethod
    public void insertLocation(ReadableMap params, final Callback success, final Callback failure) {
        TSCallback callback = new TSCallback() {
            public void success(Object result) {
                success.invoke((String)result);
            }
            public void error(Object error) {
                failure.invoke((String)error);
            }
        };
        getAdapter().insertLocation(mapToJson(params), callback);
    }

    @ReactMethod
    public void clearDatabase(final Callback success, final Callback failure) {
        TSCallback callback = new TSCallback() {
            public void success(Object result) {
                success.invoke((Boolean) result);
            }
            public void error(Object error) {
                failure.invoke((String)error);
            }
        };
        getAdapter().clearDatabase(callback);
    }

    @ReactMethod
    public void sync(final Callback success, final Callback failure) {
        TSCallback callback = new TSCallback() {
            public void success(Object result) {
                try {
                    success.invoke(convertJsonToArray((JSONArray) result));
                } catch (JSONException e) {
                    failure.invoke(e.getMessage());
                }
            }
            public void error(Object error) {
                failure.invoke((String)error);
            }
        };
        getAdapter().sync(callback);
    }

    @ReactMethod
    public void getCurrentPosition(ReadableMap options, final Callback success, final Callback failure) {
        TSCallback callback = new TSCallback() {
            public void success(Object location) {
                try {
                    success.invoke(jsonToMap((JSONObject) location));
                } catch (JSONException e) {
                    failure.invoke(e.getMessage());
                }
            }
            public void error(Object error) {
                failure.invoke(error);
            }
        };
        getAdapter().getCurrentPosition(mapToJson(options), callback);
    }
    @ReactMethod
    public void watchPosition(ReadableMap options, final Callback success, final Callback failure) {        
        TSCallback callback = new TSCallback() {
            public void success(Object location) {
                try {
                    TSLog.d("************ watchPosition Rx: " + location);
                    sendEvent(EVENT_WATCHPOSITION, jsonToMap((JSONObject)location));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            public void error(Object error) {
                failure.invoke(error);
            }
        };
        getAdapter().watchPosition(mapToJson(options), callback);
        success.invoke();
    }
    @ReactMethod
    public void stopWatchPosition(final Callback success, final Callback failure) {
        TSCallback callback = new TSCallback() {
            public void success(Object result) {
                success.invoke();
            }
            public void error(Object error) {
                failure.invoke(error);
            }
        };
        getAdapter().stopWatchPosition(callback);
    }
    @ReactMethod
    public void getOdometer(Callback success, Callback failure) {
        success.invoke(getAdapter().getOdometer());
    }
    @ReactMethod
    public void resetOdometer(Callback success, Callback failure) {
        getAdapter().resetOdometer();
        success.invoke();
    }
    @ReactMethod
    public void addGeofence(ReadableMap options, final Callback success, final Callback failure) {
        TSCallback callback = new TSCallback() {
            @Override
            public void success(Object o) {
                success.invoke((String) o);
            }

            @Override
            public void error(Object o) {
                failure.invoke((String) o);
            }
        };
        getAdapter().addGeofence(mapToJson(options), callback);
    }

    @ReactMethod
    public void addGeofences(ReadableArray geofences, final Callback success, final Callback failure) {
        JSONArray json = new JSONArray();
        for (int n=0;n<geofences.size();n++) {
            json.put(mapToJson(geofences.getMap(n)));
        }
        TSCallback callback = new TSCallback() {
            @Override
            public void success(Object o) {
                success.invoke((Boolean) o);
            }
            @Override
            public void error(Object o) {
                failure.invoke((String) o);
            }
        };
        getAdapter().addGeofences(json, callback);
    }

    @ReactMethod
    public void removeGeofence(String identifier, final Callback success, final Callback failure) {
        TSCallback callback = new TSCallback() {
            @Override
            public void success(Object identifier) {
                success.invoke((String) identifier);
            }
            @Override
            public void error(Object o) {
                failure.invoke((String) o);
            }
        };
        getAdapter().removeGeofence(identifier, callback);
    }

    @ReactMethod
    public void removeGeofences(final Callback success, final Callback failure) {
        TSCallback callback = new TSCallback() {
            @Override
            public void success(Object o) {
                success.invoke((Boolean) o);
            }

            @Override
            public void error(Object o) {
                failure.invoke((String) o);
            }
        };
        getAdapter().removeGeofences(callback);
    }

    @ReactMethod
    public void getGeofences(final Callback success, final Callback failure) {
        TSCallback callback = new TSCallback() {
            @Override
            public void success(Object geofences) {
                try {
                    success.invoke(convertJsonToArray((JSONArray) geofences));
                } catch (JSONException e) {
                    e.printStackTrace();
                    failure.invoke(e.getMessage());
                }
            }
            @Override
            public void error(Object error) {

            }
        };
        //success.invoke(convertJsonToArray(getAdapter().getGeofences()));
        getAdapter().getGeofences(callback);
    }

    @ReactMethod
    public void playSound( int soundId) {
        getAdapter().startTone(soundId);
    }

    @ReactMethod
    public void getLog(Callback successCallback, Callback failureCallback) {

        String log = readLog();
        if (log == null) {
            failureCallback.invoke("Could not read log");
            return;
        }
        successCallback.invoke(log);
    }

    @ReactMethod
    public void emailLog(String email, Callback success, Callback error) {
        String log = readLog();
        if (log == null) {
            error.invoke(500);
            return;
        }

        Intent mailer = new Intent(Intent.ACTION_SEND);
        mailer.setType("message/rfc822");
        mailer.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        mailer.putExtra(Intent.EXTRA_SUBJECT, "BackgroundGeolocation log");

        try {
            JSONObject state = mapToJson(getState());

            if (state.has("license")) {
                state.put("license", "<SECRET>");
            }
            if (state.has("orderId")) {
                state.put("orderId", "<SECRET>");
            }

            mailer.putExtra(Intent.EXTRA_TEXT, state.toString(2));
        } catch (JSONException e) {
            Log.w(TAG, "- Failed to write state to email body");
            e.printStackTrace();
        }
        File file = new File(Environment.getExternalStorageDirectory(), "background-geolocation.log");
        try {
            FileOutputStream stream = new FileOutputStream(file);
            try {
                stream.write(log.getBytes());
                stream.close();
                mailer.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                file.deleteOnExit();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            Log.i(TAG, "FileNotFound");
            e.printStackTrace();
        }
        Activity activity = getCurrentActivity();
        try {
            activity.startActivityForResult(Intent.createChooser(mailer, "Send log: " + email + "..."), 1);
            success.invoke();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(activity, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            error.invoke("There are no email clients installed");
        }
    }

    private void setEnabled(boolean value) {
        Log.i(TAG, "- setEnabled:  " + value + ", current value: " + Settings.getEnabled());

        BackgroundGeolocation adapter = getAdapter();
        if (value) {
            TSCallback callback = new TSCallback() {
                public void success(Object state) {
                    if (startCallback != null) {
                        Callback success = startCallback.get("success");
                        success.invoke(getState());
                        startCallback = null;
                    }
                }
                public void error(Object error) {
                    TSLog.d("BackgroundGeolocation#start FAILED");
                    startCallback = null;
                }
            };
            adapter.start(callback);
        } else {
            adapter.stop();
        }
    }

    // Event-handlers
    private void onLocationChange(JSONObject location) {
        try {
            sendEvent(BackgroundGeolocation.EVENT_LOCATION, jsonToMap(location));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void onMotionChange(JSONObject params) {
        try {
            sendEvent(BackgroundGeolocation.EVENT_MOTIONCHANGE, jsonToMap(params));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onHttpResponse(JSONObject params) {
        try {
            sendEvent(BackgroundGeolocation.EVENT_HTTP, jsonToMap(params));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void onHeartbeat(JSONObject params) {
        try {
            sendEvent(BackgroundGeolocation.EVENT_HEARTBEAT, jsonToMap(params));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void onGeofence(JSONObject params) {
        try {
            sendEvent(BackgroundGeolocation.EVENT_GEOFENCE, jsonToMap(params));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void onSchedule(JSONObject params) {
        try {
            sendEvent(BackgroundGeolocation.EVENT_SCHEDULE, jsonToMap(params));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void onActivityChange(String activityName) {
        sendEvent(BackgroundGeolocation.EVENT_ACTIVITYCHANGE, activityName);
    }

    private void onProviderChange(JSONObject provider) {
        try {
            sendEvent(BackgroundGeolocation.EVENT_PROVIDERCHANGE, jsonToMap(provider));
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    private void onLocationError(Integer code) {
        WritableMap params = new WritableNativeMap();
        params.putInt("code", code);
        params.putString("type", "location");
        sendEvent(BackgroundGeolocation.EVENT_ERROR, params);
    }

    private void onPlayServicesConnectError(Integer errorCode) {
        Activity activity = getCurrentActivity();
        if (activity == null) {
            TSLog.e("onPlayServicesConnectError could not find current Activity");
            return;
        }
        GoogleApiAvailability.getInstance().getErrorDialog(getCurrentActivity(), errorCode, 1001).show();
    }

    private String readLog() {
        try {
            Process process = Runtime.getRuntime().exec("logcat -d");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            StringBuilder log = new StringBuilder();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line + "\n");
            }
            return log.toString();
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void sendEvent(String eventName, WritableMap params) {
        getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(TAG + ":" + eventName, params);
    }
    private void sendEvent(String eventName, String result) {
        getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(TAG + ":" + eventName, result);
    }

    private static WritableMap jsonToMap(JSONObject jsonObject) throws JSONException {
        WritableMap map = new WritableNativeMap();

        Iterator<String> iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value = jsonObject.get(key);
            if (value instanceof JSONObject) {
                map.putMap(key, jsonToMap((JSONObject) value));
            } else if (value instanceof  JSONArray) {
                map.putArray(key, convertJsonToArray((JSONArray) value));
            } else if (value instanceof  Boolean) {
                map.putBoolean(key, (Boolean) value);
            } else if (value instanceof  Integer) {
                map.putInt(key, (Integer) value);
            } else if (value instanceof  Double) {
                map.putDouble(key, (Double) value);
            } else if (value instanceof String)  {
                map.putString(key, (String) value);
            } else {
                map.putString(key, value.toString());
            }
        }
        return map;
    }

    private static WritableArray convertJsonToArray(JSONArray jsonArray) throws JSONException {
        WritableArray array = new WritableNativeArray();

        for (int i = 0; i < jsonArray.length(); i++) {
            Object value = jsonArray.get(i);
            if (value instanceof JSONObject) {
                array.pushMap(jsonToMap((JSONObject) value));
            } else if (value instanceof  JSONArray) {
                array.pushArray(convertJsonToArray((JSONArray) value));
            } else if (value instanceof  Boolean) {
                array.pushBoolean((Boolean) value);
            } else if (value instanceof  Integer) {
                array.pushInt((Integer) value);
            } else if (value instanceof  Double) {
                array.pushDouble((Double) value);
            } else if (value instanceof String)  {
                array.pushString((String) value);
            } else {
                array.pushString(value.toString());
            }
        }
        return array;
    }

    private static JSONObject mapToJson(ReadableMap map) {
        ReadableMapKeySetIterator iterator = map.keySetIterator();
        JSONObject json = new JSONObject();

        try {
            while (iterator.hasNextKey()) {
                String key = iterator.nextKey();
                ReadableType type = map.getType(key);
                switch (map.getType(key)) {
                    case String:
                        json.put(key, map.getString(key));
                        break;
                    case Boolean:
                        json.put(key, map.getBoolean(key));
                        break;
                    case Number:
                        json.put(key, map.getDouble(key));
                        break;
                    case Map:
                        json.put(key, mapToJson(map.getMap(key)));
                        break;
                    case Array:
                        json.put(key, arrayToJson(map.getArray(key)));
                        break;

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    private static JSONArray arrayToJson(ReadableArray readableArray) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for(int i=0; i < readableArray.size(); i++) {
            ReadableType valueType = readableArray.getType(i);
            switch (valueType){
                case Null:
                    jsonArray.put(JSONObject.NULL);
                    break;
                case Boolean:
                    jsonArray.put(readableArray.getBoolean(i));
                    break;
                case Number:
                    jsonArray.put(readableArray.getInt(i));
                    break;
                case String:
                    jsonArray.put(readableArray.getString(i));
                    break;
                case Map:
                    jsonArray.put(mapToJson(readableArray.getMap(i)));
                    break;
                case Array:
                    jsonArray.put(arrayToJson(readableArray.getArray(i)));
                    break;
            }
        }
        return jsonArray;
    }

    // TODO placehold for implementing Android M permissions request.  Just return true for now.
    private Boolean hasPermission(String permission) {
        return true;
    }
    // TODO placehold for implementing Android M permissions request.  Just return true for now.
    private void requestPermissions(int requestCode, String[] action) {

    }

    private void initializeLocationManager() {
        Activity activity = getCurrentActivity();
        if (activity == null) {
            TSLog.e("RNBackgroundGeolocationModule failed to create BackgroundGeolocation adapter!");
            return;
        }
        launchIntent    = activity.getIntent();
        context         = activity.getApplicationContext();

        if (launchIntent.hasExtra("forceReload")) {
            activity.moveTaskToBack(true);
        }
        BackgroundGeolocation adapter = getAdapter();

        adapter.on(BackgroundGeolocation.EVENT_LOCATION, (new TSCallback() {
            @Override
            public void success(Object o) {
                onLocationChange((JSONObject) o);
            }
            @Override
            public void error(Object o) {
                onLocationError((Integer) o);
            }
        }));

        adapter.on(BackgroundGeolocation.EVENT_MOTIONCHANGE, (new TSCallback() {
            @Override
            public void success(Object o) {
                onMotionChange((JSONObject) o);
            }
            @Override
            public void error(Object o) {
                TSLog.e(BackgroundGeolocation.EVENT_LOCATION + " error: " + o);
            }
        }));

        adapter.on(BackgroundGeolocation.EVENT_ACTIVITYCHANGE, (new TSCallback() {
            @Override
            public void success(Object o) {
                onActivityChange((String) o);
            }
            @Override
            public void error(Object o) {
                TSLog.e(BackgroundGeolocation.EVENT_ACTIVITYCHANGE + " error: " + o);
            }
        }));

        adapter.on(BackgroundGeolocation.EVENT_HTTP, (new TSCallback() {
            @Override
            public void success(Object o) {
                onHttpResponse((JSONObject) o);
            }
            @Override
            public void error(Object o) {
                TSLog.e(BackgroundGeolocation.EVENT_HTTP + " error: " + o);
            }
        }));

        adapter.on(BackgroundGeolocation.EVENT_HEARTBEAT, (new TSCallback() {
            @Override
            public void success(Object o) {
                onHeartbeat((JSONObject) o);
            }
            @Override
            public void error(Object o) {
                TSLog.e(BackgroundGeolocation.EVENT_HEARTBEAT + " error: " + o);
            }
        }));

        adapter.on(BackgroundGeolocation.EVENT_GEOFENCE, (new TSCallback() {
            @Override
            public void success(Object o) {
                onGeofence((JSONObject) o);
            }

            @Override
            public void error(Object o) {
                TSLog.e(BackgroundGeolocation.EVENT_GEOFENCE + " error: " + o);
            }
        }));

        adapter.on(BackgroundGeolocation.EVENT_GEOFENCE, (new TSCallback() {
            @Override
            public void success(Object o) {
                onSchedule((JSONObject) o);
            }
            @Override
            public void error(Object o) {
                TSLog.e(BackgroundGeolocation.EVENT_GEOFENCE + " error: " + o);
            }
        }));

        adapter.on(BackgroundGeolocation.EVENT_PLAY_SERVICES_CONNECT_ERROR, (new TSCallback() {
            @Override
            public void success(Object o) {
                onPlayServicesConnectError((Integer)o);
            }
            @Override
            public void error(Object o) {

            }
        }));

        adapter.on(BackgroundGeolocation.EVENT_PROVIDERCHANGE, (new TSCallback() {
            @Override
            public void success(Object provider) { onProviderChange((JSONObject) provider); }
            @Override
            public void error(Object o) {}
        }));
        initialized = true;
    }

    private BackgroundGeolocation getAdapter() {
        return BackgroundGeolocation.getInstance(context, launchIntent);
    }

    @Override
    public void onCatalystInstanceDestroy() {
        TSLog.box("RNBackgroundGeolocationModule#destroy");
        initialized = false;
        getAdapter().onActivityDestroy();
        context = null;
    }
}
