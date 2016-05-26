package com.transistorsoft.rnbackgroundgeolocation;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
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
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.transistorsoft.locationmanager.BackgroundGeolocationService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.DetectedActivity;

import com.transistorsoft.locationmanager.*;
import com.transistorsoft.locationmanager.settings.*;
import com.transistorsoft.locationmanager.scheduler.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;



/**
 * Created by chris on 2015-10-30.
 */
public class RNBackgroundGeolocationModule extends ReactContextBaseJavaModule {
    private static final String TAG = "TSLocationManager";

    public static final String ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final String ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;

    public static final int REQUEST_ACTION_START = 1;
    public static final int REQUEST_ACTION_GET_CURRENT_POSITION = 2;

    private static final String EVENT_LOCATION = "location";
    private static final String EVENT_MOTIONCHANGE = "motionchange";
    private static final String EVENT_ERROR = "error";
    private static final String EVENT_GEOFENCE = "geofence";
    private static final String EVENT_HTTP = "http";
    private static final String EVENT_HEARTBEAT = "heartbeat";
    private static final String EVENT_SCHEDULE = "schedule";

    private static final long GET_CURRENT_POSITION_TIMEOUT = 30000;

    private ReadableMap mConfig;
    private Boolean isEnabled           = false;
    private Boolean stopOnTerminate     = true;
    private Boolean isMoving            = false;
    private Boolean isAcquiringCurrentPosition = false;
    private Boolean forceReload         = false;
    private long isAcquiringCurrentPositionSince;
    private Intent backgroundServiceIntent;

    private DetectedActivity currentActivity;

    private HashMap<String, Callback> startCallback;
    private HashMap<String, Callback> getLocationsCallback;
    private HashMap<String, Callback> syncCallback;
    private HashMap<String, Callback> getOdometerCallback;
    private HashMap<String, Callback> paceChangeCallback;
    private HashMap<String, Callback> clearDatabaseCallback;
    private HashMap<String, HashMap> addGeofenceCallbacks = new HashMap();
    private List<HashMap> getCountCallbacks = new ArrayList<>();
    private HashMap<String, HashMap> insertLocationCallbacks = new HashMap();
    private List<HashMap> getLogCallbacks = new ArrayList<>();

    private List<HashMap<String, Callback>> currentPositionCallbacks = new ArrayList<HashMap<String, Callback>>();
    private ReadableMap currentPositionOptions;

    private ToneGenerator toneGenerator;

    ReactApplicationContext reactContext;
    Activity activity;

    public RNBackgroundGeolocationModule(ReactApplicationContext reactContext, Activity activity) {
        super(reactContext);
        this.reactContext   = reactContext;
        this.activity       = activity;

        EventBus eventBus = EventBus.getDefault();
        synchronized(eventBus) {
            if (!eventBus.isRegistered(this)) {
                eventBus.register(this);
            }
        }

        // Load settings.
        SharedPreferences settings = activity.getSharedPreferences("TSLocationManager", 0);
        Settings.init(settings);
        Settings.load();

        Intent launchIntent = activity.getIntent();
        if (launchIntent.hasExtra("forceReload")) {
            forceReload = true;
        }
        backgroundServiceIntent = new Intent(reactContext, BackgroundGeolocationService.class);
        backgroundServiceIntent.putExtra("enabled", isEnabled);
    }

    @Override
    public String getName() {
        return "RNBackgroundGeolocation";
    }

    @ReactMethod
    public void configure(ReadableMap config, Callback callback) {
        mConfig = config;
        Settings.reset();
        applyConfig();

        Boolean willEnable = false;
        if (Settings.values.containsKey("schedule")) {
            willEnable = Settings.getEnabled() && Settings.getSchedulerEnabled();
        } else {
            Settings.setSchedulerEnabled(false);
            willEnable = Settings.getEnabled();
        }
        if (willEnable) {
            start(null, null);
        }
        callback.invoke(getState());
    }

    @ReactMethod
    public void start(Callback success, Callback failure) {
        if (startCallback != null) {
            Callback callback = startCallback.get("failure");
            callback.invoke("Waiting for a previous start action to complete");
            return;
        }
        if (success != null) {
            startCallback = new HashMap();
            startCallback.put("success", success);
            startCallback.put("failure", failure);
        }
        //setEnabled(true);
        backgroundServiceIntent = new Intent(activity, BackgroundGeolocationService.class);
        if (hasPermission(ACCESS_COARSE_LOCATION) && hasPermission(ACCESS_FINE_LOCATION)) {
            setEnabled(true);
        } else {
            String[] permissions = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION};
            requestPermissions(REQUEST_ACTION_START, permissions);
        }

    }
    // TODO placehold for implementing Android M permissions request.  Just return true for now.
    private Boolean hasPermission(String permission) {
        return true;
    }
    // TODO placehold for implementing Android M permissions request.  Just return true for now.
    private void requestPermissions(int requestCode, String[] action) {

    }

    @ReactMethod
    public void startSchedule(Callback success, Callback failure) {
        if (Settings.values.containsKey("schedule")) {
            Settings.setSchedulerEnabled(true);
            EventBus eventBus = EventBus.getDefault();
            synchronized(eventBus) {
                if (!eventBus.isRegistered(this)) {
                    eventBus.register(this);
                }
            }
            startScheduleService();
            success.invoke();
        } else {
            failure.invoke("No schedule defined");
        }
    }
    @ReactMethod
    public void stopSchedule(Callback success, Callback failure) {
        Settings.setSchedulerEnabled(false);
        stop();
        stopScheduleService();
        success.invoke();
    }
    @ReactMethod
    public void stop() {
        setEnabled(false);
    }

    @ReactMethod
    public void changePace(Boolean moving, Callback success, Callback failure) {
        if (!isEnabled) {
            Log.w(TAG, "- Cannot change pace while disabled");
            failure.invoke("Cannot #changePace while disabled");
        } else {
            paceChangeCallback = new HashMap();
            paceChangeCallback.put("success", success);
            paceChangeCallback.put("failure", failure);

            isMoving = moving;

            Bundle event = new Bundle();
            event.putString("name", BackgroundGeolocationService.ACTION_CHANGE_PACE);
            event.putBoolean("request", true);
            event.putBoolean("isMoving", isMoving);
            EventBus.getDefault().post(event);
        }
    }

    @ReactMethod
    public void setConfig(ReadableMap config) {
        JSONObject currentConfig = mapToJson(mConfig);
        JSONObject newConfig = mapToJson(config);
        try {
            JSONObject merged = new JSONObject();
            JSONObject[] objs = new JSONObject[] { currentConfig, newConfig };
            for (JSONObject obj : objs) {
                Iterator it = obj.keys();
                while (it.hasNext()) {
                    String key = (String)it.next();
                    merged.put(key, obj.get(key));
                }
            }
            mConfig = jsonToMap(merged);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        applyConfig();

        // Need to take special care with some params are updated.  Here we need reload the ScheduleService
        if (config.hasKey("schedule")) {
            startScheduleService();
        }

        Bundle event = new Bundle();
        event.putString("name", BackgroundGeolocationService.ACTION_SET_CONFIG);
        event.putBoolean("request", true);
        EventBus.getDefault().post(event);
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
        SharedPreferences settings = activity.getSharedPreferences("TSLocationManager", 0);

        try {
            if (settings.contains("config")) {
                JSONObject config = new JSONObject(settings.getString("config", "{}"));
                WritableMap state = jsonToMap(config);
                state.putBoolean("enabled", isEnabled);
                state.putBoolean("isMoving", isMoving);
                return state;
            } else {
                WritableMap state = Arguments.createMap();
                state.putString("error", "Could not location config from SharedPreferences");
                return state;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            WritableMap state = Arguments.createMap();
            state.putString("error", e.getMessage());
            return state;
        }
    }
    @ReactMethod
    public void getLocations(Callback successCallback, Callback failureCallback) {
        getLocationsCallback = new HashMap();
        getLocationsCallback.put("success", successCallback);
        getLocationsCallback.put("failure", failureCallback);

        Bundle event = new Bundle();
        event.putString("name", BackgroundGeolocationService.ACTION_GET_LOCATIONS);
        event.putBoolean("request", true);
        EventBus.getDefault().post(event);
    }

    @ReactMethod
    public void getCount(Callback successCallback, Callback failureCallback) {
        Log.i(TAG, "- getCount");
        Bundle event = new Bundle();
        event.putString("name", BackgroundGeolocationService.ACTION_GET_COUNT);
        event.putBoolean("request", true);

        HashMap<String, Callback> callbacks = new HashMap();
        callbacks.put("success", successCallback);
        callbacks.put("failure", failureCallback);
        getCountCallbacks.add(callbacks);

        EventBus.getDefault().post(event);
    }

    @ReactMethod
    public void insertLocation(ReadableMap params, Callback successCallback, Callback failureCallback) {
        Log.i(TAG, "- insertLocation" + params.toString());

        if (!BackgroundGeolocationService.isInstanceCreated()) {
            Log.i(TAG, "Cannot insertLocation when the BackgroundGeolocationService is not running.  Plugin must be started first");
            return;
        }
        if (!params.hasKey("uuid")) {
            failureCallback.invoke("insertLocation params must contain uuid");
            return;
        }
        if (!params.hasKey("timestamp")) {
            failureCallback.invoke("insertLocation params must contain timestamp");
            return;
        }
        if (!params.hasKey("coords")) {
            failureCallback.invoke("insertLocation params must contains a coords {}");
            return;
        }
        String uuid = params.getString("uuid");

        Bundle event = new Bundle();
        event.putString("name", BackgroundGeolocationService.ACTION_INSERT_LOCATION);
        event.putBoolean("request", true);
        event.putString("location", mapToJson(params).toString());

        HashMap<String, Callback> callbacks = new HashMap();
        callbacks.put("success", successCallback);
        callbacks.put("failure", failureCallback);
        insertLocationCallbacks.put(uuid, callbacks);

        EventBus.getDefault().post(event);
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

        try {
            activity.startActivityForResult(Intent.createChooser(mailer, "Send log: " + email + "..."), 1);
            success.invoke();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(activity, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            error.invoke("There are no email clients installed");
        }
    }

    @ReactMethod
    public void clearDatabase(Callback successCallback, Callback failureCallback) {
        if (clearDatabaseCallback != null) {
            Log.i(TAG, "- a clearDatabase action is already outstanding");
            return;
        }
        clearDatabaseCallback = new HashMap();
        clearDatabaseCallback.put("success", successCallback);
        clearDatabaseCallback.put("failure", failureCallback);

        Bundle event = new Bundle();
        event.putString("name", BackgroundGeolocationService.ACTION_CLEAR_DATABASE);
        event.putBoolean("request", true);
        EventBus.getDefault().post(event);
    }

    @ReactMethod
    public void sync(Callback success, Callback failure) {
        if (syncCallback != null) {
            // Outstanding sync-action is currently running.  Wait for it to complete
            return;
        }
        syncCallback = new HashMap();
        syncCallback.put("success", success);
        syncCallback.put("failure", failure);

        EventBus eventBus = EventBus.getDefault();
        synchronized(eventBus) {
            if (!eventBus.isRegistered(this)) {
                eventBus.register(this);
            }
        }
        if (!BackgroundGeolocationService.isInstanceCreated()) {
            Intent syncIntent = new Intent(activity, BackgroundGeolocationService.class);
            syncIntent.putExtra("command", BackgroundGeolocationService.ACTION_SYNC);
            reactContext.startService(syncIntent);
        } else {
            Bundle event = new Bundle();
            event.putString("name", BackgroundGeolocationService.ACTION_SYNC);
            event.putBoolean("request", true);
            eventBus.post(event);
        }
    }

    @ReactMethod
    public void getCurrentPosition(ReadableMap options, Callback successCallback, Callback failureCallback) {
        currentPositionOptions          = options;
        isAcquiringCurrentPositionSince = System.nanoTime();

        HashMap<String, Callback> callback = new HashMap();
        callback.put("success", successCallback);
        callback.put("failure", failureCallback);

        synchronized(currentPositionCallbacks) {
            currentPositionCallbacks.add(callback);
        }

        if (isAcquiringCurrentPosition) {
            return;
        }
        isAcquiringCurrentPosition = true;
        if (!isEnabled) {
            EventBus eventBus = EventBus.getDefault();
            synchronized(eventBus) {
                if (!eventBus.isRegistered(this)) {
                    eventBus.register(this);
                }
            }
            if (!BackgroundGeolocationService.isInstanceCreated()) {
                backgroundServiceIntent.putExtra("command", BackgroundGeolocationService.ACTION_GET_CURRENT_POSITION);
                reactContext.startService(backgroundServiceIntent);
            }
        } else {
            JSONObject currentPositionOptions = new JSONObject();
            try {
                if (options.hasKey("timeout")) {
                    currentPositionOptions.put("timeout", options.getInt("timeout"));
                }
                if (options.hasKey("maximumAge")) {
                    currentPositionOptions.put("maximumAge", options.getInt("maximumAge"));
                }
                if (options.hasKey("persist")) {
                    currentPositionOptions.put("persist", options.getBoolean("persist"));
                }
                if (options.hasKey("extras")) {
                    // TODO
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Bundle event = new Bundle();
            event.putString("name", BackgroundGeolocationService.ACTION_GET_CURRENT_POSITION);
            event.putBoolean("request", true);
            event.putString("options", currentPositionOptions.toString());
            EventBus.getDefault().post(event);
        }
    }
    @ReactMethod
    public void getOdometer(Callback success, Callback failure) {
        SharedPreferences settings = reactContext.getSharedPreferences("TSLocationManager", 0);
        Float value = settings.getFloat("odometer", 0);
        success.invoke(value);
    }
    @ReactMethod
    public void resetOdometer(Callback success, Callback failure) {
        SharedPreferences settings = reactContext.getSharedPreferences("TSLocationManager", 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putFloat("odometer", 0);
        editor.apply();

        if (BackgroundGeolocationService.isInstanceCreated()) {
            Bundle event = new Bundle();
            event.putString("name", BackgroundGeolocationService.ACTION_RESET_ODOMETER);
            event.putBoolean("request", true);
            EventBus.getDefault().post(event);
        }
        success.invoke();
    }
    @ReactMethod
    public void addGeofence(ReadableMap options, Callback success, Callback failure) {
        Bundle event = new Bundle();
        String identifier = options.getString("identifier");
        event.putString("name", BackgroundGeolocationService.ACTION_ADD_GEOFENCE);
        event.putBoolean("request", true);
        event.putFloat("radius", (float) options.getDouble("radius"));
        event.putDouble("latitude", options.getDouble("latitude"));
        event.putDouble("longitude", options.getDouble("longitude"));
        event.putString("identifier", identifier);
        if (options.hasKey("notifyOnEntry")) {
            event.putBoolean("notifyOnEntry", options.getBoolean("notifyOnEntry"));
        }
        if (options.hasKey("notifyOnExit")) {
            event.putBoolean("notifyOnExit", options.getBoolean("notifyOnExit"));
        }
        EventBus.getDefault().post(event);

        HashMap<String, Callback> callbacks = new HashMap();
        callbacks.put("success", success);
        callbacks.put("failure", failure);

        addGeofenceCallbacks.put(identifier, callbacks);
    }

    @ReactMethod
    public void addGeofences(ReadableArray geofences, Callback success, Callback failure) {
        Bundle event = new Bundle();
        event.putString("name", BackgroundGeolocationService.ACTION_ADD_GEOFENCES);
        event.putBoolean("request", true);

        JSONArray json = new JSONArray();
        for (int n=0;n<geofences.size();n++) {
            json.put(mapToJson(geofences.getMap(n)));
        }
        event.putString("geofences", json.toString());

        HashMap<String, Callback> callbacks = new HashMap();
        callbacks.put("success", success);
        callbacks.put("failure", failure);
        addGeofenceCallbacks.put(BackgroundGeolocationService.ACTION_ADD_GEOFENCES, callbacks);

        EventBus.getDefault().post(event);
    }

    @ReactMethod
    public void removeGeofence(String identifier, Callback success, Callback failure) {
        Bundle event = new Bundle();
        event.putString("name", BackgroundGeolocationService.ACTION_REMOVE_GEOFENCE);
        event.putBoolean("request", true);
        event.putString("identifier", identifier);
        EventBus.getDefault().post(event);

        // TODO no error checking here
        success.invoke();
    }

    @ReactMethod
    public void removeGeofences(Callback success, Callback failure) {
        Bundle event = new Bundle();
        event.putString("name", BackgroundGeolocationService.ACTION_REMOVE_GEOFENCES);
        event.putBoolean("request", true);
        EventBus.getDefault().post(event);

        success.invoke();
    }

    @ReactMethod
    public void getGeofences(Callback success, Callback failure) {
        WritableArray rs = new WritableNativeArray();

        SharedPreferences settings = reactContext.getSharedPreferences(TAG, 0);
        if (settings.contains("geofences")) {
            try {
                JSONArray json = new JSONArray(settings.getString("geofences", "[]"));
                for (int i = 0; i < json.length(); i++) {
                    JSONObject geofenceJson = json.getJSONObject(i);
                    WritableMap geofence = new WritableNativeMap();
                    geofence.putString("identifier", geofenceJson.getString("identifier"));
                    geofence.putDouble("radius", geofenceJson.getDouble("radius"));
                    geofence.putDouble("latitude", geofenceJson.getDouble("latitude"));
                    geofence.putDouble("longitude", geofenceJson.getDouble("longitude"));
                    rs.pushMap(geofence);
                }
                success.invoke(rs);
            } catch (JSONException e) {
                e.printStackTrace();
                failure.invoke(e.getMessage());
            }
        } else {
            success.invoke(rs);
        }
    }

    @ReactMethod
    public void playSound( int soundId) {
        int duration = 1000;

        if (toneGenerator == null) {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        }
        toneGenerator.startTone(soundId, duration);
    }

    @Subscribe
    public void onEventMainThread(ScheduleEvent event) {
        if (!Settings.getSchedulerEnabled()) {
            TSLog.i("Ignored a Schedule event because Scheduler is disabled: " + event);
            return;
        }
        isEnabled = event.getEnabled();
        sendEvent(EVENT_SCHEDULE, getState());
    }


    @Subscribe
    public void onEventMainThread(Location location) {
        Bundle meta = location.getExtras();
        if (meta != null) {
            if (meta.containsKey("action")) {
                String action = meta.getString("action");
                boolean motionChanged = action.equalsIgnoreCase(BackgroundGeolocationService.ACTION_ON_MOTION_CHANGE);
                if (motionChanged) {
                    boolean nowMoving = meta.getBoolean("isMoving");
                    onMotionChange(nowMoving, locationToMap(location));
                }
            }
        }

        this.onLocationChange(location);

        // Fire "location" event on React EventBus
        sendEvent(EVENT_LOCATION, locationToMap(location));
    }

    @Subscribe
    public void onEventMainThread(GeofencingEvent geofencingEvent) {
        Log.i(TAG, "- Rx GeofencingEvent: " + geofencingEvent);
        sendEvent(EVENT_GEOFENCE, geofencingEventToMap(geofencingEvent));
    }

    private void startScheduleService() {
        Intent intent = new Intent(activity, ScheduleService.class);
        stopScheduleService();
        activity.startService(intent);
    }

    private void stopScheduleService() {
        // First kill if it's already running.
        if (ScheduleService.isInstanceCreated()) {
            Intent intent = new Intent(activity, ScheduleService.class);
            activity.stopService(intent);
        }
    }

    /**
     * EventBus listener for Event Bundle
     * @param {Bundle} event
     */
    @Subscribe
    public void onEventMainThread(Bundle event) {
        if (event.containsKey("request")) {
            return;
        }
        String name = event.getString("name");

        if (BackgroundGeolocationService.ACTION_START.equalsIgnoreCase(name)) {
            onStart(event);
        } else if (BackgroundGeolocationService.ACTION_GET_LOCATIONS.equalsIgnoreCase(name)) {
            onGetLocations(event);
        } else if (BackgroundGeolocationService.ACTION_SYNC.equalsIgnoreCase(name)) {
            onSync(event);
        } else if (BackgroundGeolocationService.ACTION_RESET_ODOMETER.equalsIgnoreCase(name)) {
            this.onResetOdometer(event);
        } else if (BackgroundGeolocationService.ACTION_CHANGE_PACE.equalsIgnoreCase(name)) {
            this.onChangePace(event);
        } else if (BackgroundGeolocationService.ACTION_GOOGLE_PLAY_SERVICES_CONNECT_ERROR.equalsIgnoreCase(name)) {
            GoogleApiAvailability.getInstance().getErrorDialog(activity, event.getInt("errorCode"), 1001).show();
        } else if (BackgroundGeolocationService.ACTION_LOCATION_ERROR.equalsIgnoreCase(name)) {
            this.onLocationError(event);
        } else if (BackgroundGeolocationService.ACTION_GET_CURRENT_POSITION.equalsIgnoreCase(name)) {
            this.onLocationError(event);
        } else if (BackgroundGeolocationService.ACTION_HTTP_RESPONSE.equalsIgnoreCase(name)) {
            this.onHttpResponse(event);
        } else if (BackgroundGeolocationService.ACTION_CLEAR_DATABASE.equalsIgnoreCase(name)) {
            this.onClearDatabase(event);
        } else if (BackgroundGeolocationService.ACTION_ADD_GEOFENCE.equalsIgnoreCase(name)) {
            this.onAddGeofence(event);
        } else if (BackgroundGeolocationService.ACTION_GET_COUNT.equalsIgnoreCase(name)) {
            this.onGetCount(event);
        } else if (BackgroundGeolocationService.ACTION_INSERT_LOCATION.equalsIgnoreCase(name)) {
            this.onInsertLocation(event);
        } else if (name.equalsIgnoreCase(BackgroundGeolocationService.ACTION_HEARTBEAT)) {
            this.onHeartbeat(event);
        }
    }

    private boolean applyConfig() {
        if (mConfig.hasKey("stopOnTerminate")) {
            stopOnTerminate = mConfig.getBoolean("stopOnTerminate");
        }
        SharedPreferences settings = activity.getSharedPreferences("TSLocationManager", 0);
        SharedPreferences.Editor editor = settings.edit();


        if (mConfig.hasKey("isMoving")) {
            editor.putBoolean("isMoving", mConfig.getBoolean("isMoving"));
        }
        editor.putString("config", mapToJson(mConfig).toString());
        editor.apply();
        Settings.load();
        return true;
    }

    private void setEnabled(boolean value) {
        Log.i(TAG, "- setEnabled:  " + value + ", current value: " + isEnabled);
        isEnabled = value;

        Intent launchIntent = activity.getIntent();
        if (forceReload) {
            Integer eventCode = launchIntent.getIntExtra("eventCode", -1);
            if (eventCode == BackgroundGeolocationService.FORCE_RELOAD_HEARTBEAT) {
                onHeartbeat(launchIntent.getExtras());
            }
            if (launchIntent.hasExtra("location")) {
                try {
                    JSONObject location = new JSONObject(launchIntent.getStringExtra("location"));
                    //onLocationChange(locationToMap(location));
                    sendEvent(EVENT_LOCATION, locationToMap(location));
                    launchIntent.removeExtra("location");
                } catch (JSONException e) {
                    Log.w(TAG, e);
                }
            }
            if (launchIntent.hasExtra("geofencingEvent")) {
                try {
                    JSONObject geofencingEvent  = new JSONObject(launchIntent.getStringExtra("geofencingEvent"));
                    sendEvent(EVENT_GEOFENCE, geofencingEventToMap(geofencingEvent));

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.w(TAG, e);
                }
                launchIntent.removeExtra("geofencingEvent");
            }
            launchIntent.removeExtra("forceReload");
            activity.moveTaskToBack(true);
        }

        SharedPreferences settings = reactContext.getSharedPreferences("TSLocationManager", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("enabled", isEnabled);
        editor.commit();

        EventBus eventBus = EventBus.getDefault();
        if (isEnabled) {
            synchronized(eventBus) {
                if (!eventBus.isRegistered(this)) {
                    eventBus.register(this);
                }
            }
            if (!BackgroundGeolocationService.isInstanceCreated()) {
                reactContext.startService(backgroundServiceIntent);
            } else {
                Bundle event = new Bundle();
                event.putString("name", BackgroundGeolocationService.ACTION_GET_CURRENT_POSITION);
                event.putBoolean("request", true);
                EventBus.getDefault().post(event);

                if (startCallback != null) {
                    Callback success = startCallback.get("success");
                    success.invoke();
                    startCallback = null;
                }
            }
        } else {
            reactContext.stopService(backgroundServiceIntent);
        }
    }

    private void onStart(Bundle event) {
        if (event.getBoolean("response") && !event.getBoolean("success")) {
            Toast.makeText(activity, event.getString("message"), Toast.LENGTH_LONG).show();
            if (startCallback != null) {
                startCallback.get("failure").invoke(event.getString("message"));
            }
        } else if (startCallback != null) {
            startCallback.get("success").invoke();
        }
        if (forceReload) {
            forceReload = false;
        }
        startCallback = null;
    }
    private void onGetLocations(Bundle event) {
        try {
            JSONArray data      = new JSONArray(event.getString("data"));
            WritableArray rs    = new WritableNativeArray();

            for (int i = 0; i < data.length(); i++) {
                rs.pushMap(locationToMap(data.getJSONObject(i)));
            }
            Callback success = getLocationsCallback.get("success");
            success.invoke(rs);
        } catch (JSONException e) {
            e.printStackTrace();
            Callback failure = getLocationsCallback.get("failure");
            failure.invoke(e.getMessage());
        }
        getLocationsCallback = null;
    }
    private void onClearDatabase(Bundle event) {
        Callback success    = clearDatabaseCallback.get("success");
        success.invoke(true);
        clearDatabaseCallback = null;
    }
    private void onSync(Bundle event) {
        Boolean success = event.getBoolean("success");
        if (success) {
            try {
                JSONArray data      = new JSONArray(event.getString("data"));
                WritableArray rs    = new WritableNativeArray();

                for (int i = 0; i < data.length(); i++) {
                    rs.pushMap(locationToMap(data.getJSONObject(i)));
                }
                Callback cb = syncCallback.get("success");
                cb.invoke(rs);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Callback cb = syncCallback.get("failure");
                cb.invoke(e.getMessage());
            }
        } else {
            Callback cb = syncCallback.get("failure");
            cb.invoke(event.getString("message"));
        }
        syncCallback = null;
    }    
    public void onResetOdometer(Bundle event) {
        // Do Nothing.  Callback already called.
    }
    public void onChangePace(Bundle event) {
        Callback success    = paceChangeCallback.get("success");
        success.invoke(event.getBoolean("isMoving"));
        paceChangeCallback = null;
    }

    private void onLocationChange(Location location) {
        if (isAcquiringCurrentPosition) {
            finishAcquiringCurrentPosition(true);
            // Execute callbacks.
            synchronized(currentPositionCallbacks) {
                for (HashMap<String, Callback> callback : currentPositionCallbacks) {
                    Callback success = callback.get("success");
                    success.invoke(locationToMap(location));
                }
                currentPositionCallbacks.clear();
            }
        }
    }

    private void onMotionChange(boolean nowMoving, WritableMap locationData) {
        isMoving = nowMoving;
        WritableMap params = new WritableNativeMap();

        params.putMap("location", locationData);
        params.putBoolean("isMoving", isMoving);
        sendEvent(EVENT_MOTIONCHANGE, params);
    }

    public void onHttpResponse(Bundle event) {
        WritableMap params = new WritableNativeMap();
        params.putInt("status", event.getInt("status"));
        params.putString("responseText", event.getString("responseText"));
        sendEvent(EVENT_HTTP, params);
    }

    private void onHeartbeat(Bundle event) {
        WritableMap params = new WritableNativeMap();
        try {
            JSONObject json = new JSONObject(event.getString("location"));
            params.putMap("location", locationToMap(json));
            params.putInt("shakes", -1);
            sendEvent(EVENT_HEARTBEAT, params);
        } catch (JSONException e) {
            e.printStackTrace();

        }
    }
    private void onLocationError(Bundle event) {
        Integer code = event.getInt("code");
        if (code == BackgroundGeolocationService.LOCATION_ERROR_DENIED) {
            if (isDebugging()) {
                Toast.makeText(activity, "Location services disabled!", Toast.LENGTH_SHORT).show();
            }
        }
        if (isAcquiringCurrentPosition) {
            finishAcquiringCurrentPosition(false);
            for (HashMap<String, Callback> callback : currentPositionCallbacks) {
                Callback failure = callback.get("failure");
                failure.invoke(code);
            }
            currentPositionCallbacks.clear();
        }
        WritableMap params = new WritableNativeMap();
        params.putInt("code", code);
        params.putString("type", "location");
        sendEvent(EVENT_ERROR, params);
    }

    private void onAddGeofence(Bundle event) {
        boolean success = event.getBoolean("success");
        String identifier = event.getString("identifier");
        if (addGeofenceCallbacks.containsKey(identifier)) {
            HashMap<String, Callback> callbacks = addGeofenceCallbacks.get(identifier);
            if (success) {
                Callback callback = callbacks.get("success");
                callback.invoke();
            } else {
                Callback callback = callbacks.get("failure");
                callback.invoke(event.getString("message"));
            }
            addGeofenceCallbacks.remove(identifier);
        }
    }

    private void onGetCount(Bundle event) {
        int count = event.getInt("count");
        for (HashMap<String,Callback> callback : getCountCallbacks) {
            Callback success = callback.get("success");
            success.invoke(count);
        }
        getCountCallbacks.clear();
    }

    private void onInsertLocation(Bundle event) {
        String uuid = event.getString("uuid");
        Log.i(TAG, "- onInsertLocation: " + uuid);
        if (insertLocationCallbacks.containsKey(uuid)) {
            HashMap<String, Callback> callbacks = insertLocationCallbacks.get(uuid);
            Callback success = callbacks.get("success");
            success.invoke();
            insertLocationCallbacks.remove(uuid);
        } else {
            Log.i(TAG, "- onInsertLocation failed to find its success-callback for " + uuid);
        }
    }

    private void finishAcquiringCurrentPosition(boolean success) {
        // Current position has arrived:  release the hounds.
        isAcquiringCurrentPosition = false;
        backgroundServiceIntent.removeExtra("command");
        // When currentPosition is explicitly requested while plugin is stopped, shut Service down again and stop listening to EventBus
    }

    private Boolean isDebugging() {
        SharedPreferences settings = activity.getSharedPreferences("TSLocationManager", 0);
        return settings.contains("debug") && settings.getBoolean("debug", false);
    }

    private WritableMap geofencingEventToMap(JSONObject event) {
        WritableMap params = new WritableNativeMap();
        try {
            params.putString("identifier", event.getString("identifier"));
            params.putString("action", event.getString("action"));
            params.putMap("location", locationToMap(event.getJSONObject("location")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return params;
    }
    private WritableMap geofencingEventToMap(GeofencingEvent event) {
        WritableMap params = new WritableNativeMap();

        Location location = event.getTriggeringLocation();
        Geofence geofence = event.getTriggeringGeofences().get(0);

        String action = "";
        int transitionType = event.getGeofenceTransition();
        if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
            action = BackgroundGeolocationService.GEOFENCE_ACTION_ENTER;
        } else if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
            action = BackgroundGeolocationService.GEOFENCE_ACTION_EXIT;
        } else {
            action = BackgroundGeolocationService.GEOFENCE_ACTION_DWELL;
        }
        params.putMap("location", locationToMap(location));
        params.putString("identifier", geofence.getRequestId());
        params.putString("action", action);

        return params;
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
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(TAG + ":" + eventName, params);
    }

    private WritableMap locationToMap(JSONObject json) {
        WritableMap data = new WritableNativeMap();

        try {
            data.putString("timestamp", json.getString("timestamp"));

            if (json.has("odometer")) {
                data.putDouble("odometer", json.getDouble("odometer"));
            }
            data.putBoolean("is_moving", isMoving);
            data.putString("uuid", json.getString("uuid"));

            // Coords
            JSONObject coordsJson = json.getJSONObject("coords");
            WritableMap coords = new WritableNativeMap();
            coords.putDouble("latitude", coordsJson.getDouble("latitude"));
            coords.putDouble("longitude", coordsJson.getDouble("longitude"));
            coords.putDouble("accuracy", coordsJson.getDouble("accuracy"));
            coords.putDouble("speed", coordsJson.getDouble("speed"));
            coords.putDouble("heading", coordsJson.getDouble("heading"));
            coords.putDouble("altitude", coordsJson.getDouble("altitude"));
            data.putMap("coords", coords);

            if (json.has("activity")) {
                JSONObject activityJson = json.getJSONObject("activity");
                WritableNativeMap activity = new WritableNativeMap();
                activity.putString("type", activityJson.getString("type"));
                activity.putInt("confidence", activityJson.getInt("confidence"));
                data.putMap("activity", activity);
            }
            if (json.has("battery")) {
                JSONObject batteryJson = json.getJSONObject("battery");
                WritableMap battery = new WritableNativeMap();
                battery.putBoolean("is_charging", batteryJson.getBoolean("is_charging"));
                battery.putDouble("level", batteryJson.getLong("level"));
                data.putMap("battery", battery);
            }
            if (json.has("geofence")) {
                JSONObject geofenceJson = json.getJSONObject("geofence");
                WritableMap geofence = new WritableNativeMap();
                geofence.putString("identifier", geofenceJson.getString("identifier"));
                geofence.putString("action", geofenceJson.getString("action"));
                data.putMap("geofence", geofence);
            }
            if (json.has("extras")) {
                // TODO
            }
        } catch(JSONException e) {
            e.printStackTrace();
        }

        return data;
    }
    private WritableMap locationToMap(Location location) {
        WritableMap data = Arguments.createMap();

        TimeZone tz = TimeZone.getTimeZone("UTC");
        SimpleDateFormat dateFormatter = new SimpleDateFormat(BackgroundGeolocationService.DATE_FORMAT, Locale.getDefault());
        dateFormatter.setTimeZone(tz);

        WritableMap coordData = Arguments.createMap();
        WritableMap activityData = Arguments.createMap();

        coordData.putDouble("latitude", location.getLatitude());
        coordData.putDouble("longitude", location.getLongitude());
        coordData.putDouble("accuracy", location.getAccuracy());
        coordData.putDouble("speed", location.getSpeed());
        coordData.putDouble("heading", location.getBearing());
        coordData.putDouble("altitude", location.getAltitude());

        if (currentActivity != null) {
            activityData.putString("type", BackgroundGeolocationService.getActivityName(currentActivity.getType()));
            activityData.putInt("confidence", currentActivity.getConfidence());
            data.putMap("activity", activityData);
        }
        // Meta-data
        Bundle meta = location.getExtras();
        if (meta != null) {
            if (meta.containsKey("odometer")) {
                data.putDouble("odometer", (double)meta.getFloat("odometer"));
            }
            if (meta.containsKey("action")) {
                if (meta.getString("action").equalsIgnoreCase(BackgroundGeolocationService.ACTION_ON_MOTION_CHANGE)) {
                    data.putString("event", EVENT_MOTIONCHANGE);
                }
            }
            if (meta.containsKey("uuid")) {
                data.putString("uuid", meta.getString("uuid"));
            }
            // Battery data?
            if (meta.containsKey("is_charging")) {
                WritableMap battery = Arguments.createMap();
                battery.putBoolean("is_charging", meta.getBoolean("is_charging"));
                battery.putDouble("level", meta.getFloat("battery_level"));
                data.putMap("battery", battery);
            }
            // Triggered Geofence?
            if (meta.containsKey("geofence_identifier")) {
                WritableMap geofence = Arguments.createMap();
                geofence.putString("identifier", meta.getString("geofence_identifier"));
                geofence.putString("action", meta.getString("geofence_action"));
                data.putMap("geofence", geofence);
                data.putString("event", EVENT_GEOFENCE);
            }
        }
        if (isAcquiringCurrentPosition && currentPositionOptions.hasKey("extras")) {
            //data.putMap(currentPositionOptions.getMap("extras"));
        }
        data.putBoolean("is_moving", isMoving);
        data.putMap("coords", coordData);
        data.putString("timestamp", dateFormatter.format(location.getTime()));

        return data;
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

    @Override
    public void onCatalystInstanceDestroy() {
        Log.i(TAG, "- RNBackgroundGeolocationModule#destroy");
        EventBus eventBus = EventBus.getDefault();
        synchronized(eventBus) {
            if (eventBus.isRegistered(this)) {
                eventBus.unregister(this);
            }
        }
        currentPositionCallbacks.clear();

        if(stopOnTerminate) {
            if (isEnabled) {
                Intent intent = new Intent(activity, BackgroundGeolocationService.class);
                reactContext.stopService(intent);
            }
            stopScheduleService();
        }
    }
}
