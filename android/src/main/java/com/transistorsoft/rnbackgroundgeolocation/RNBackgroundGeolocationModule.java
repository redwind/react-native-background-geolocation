package com.transistorsoft.rnbackgroundgeolocation;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

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
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
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
    private static final String EVENT_LOCATION = "location";
    private static final String EVENT_MOTIONCHANGE = "motionchange";
    private static final String EVENT_ERROR = "error";
    private static final String EVENT_GEOFENCE = "geofence";
    private static final String EVENT_HTTP = "http";

    private static final long GET_CURRENT_POSITION_TIMEOUT = 30000;

    private Boolean isEnabled           = false;
    private Boolean stopOnTerminate     = false;
    private Boolean isMoving            = false;
    private Boolean isAcquiringCurrentPosition = false;
    private long isAcquiringCurrentPositionSince;
    private Intent backgroundServiceIntent;

    private DetectedActivity currentActivity;

    private HashMap<String, Callback> startCallback;
    private HashMap<String, Callback> getLocationsCallback;
    private HashMap<String, Callback> syncCallback;
    private HashMap<String, Callback> getOdometerCallback;
    private HashMap<String, Callback> resetOdometerCallback;
    private HashMap<String, Callback> paceChangeCallback;

    private List<HashMap<String, Callback>> currentPositionCallbacks = new ArrayList<HashMap<String, Callback>>();
    private ReadableMap currentPositionOptions;

    private ToneGenerator toneGenerator;

    ReactApplicationContext reactContext;
    Activity activity;

    public RNBackgroundGeolocationModule(ReactApplicationContext reactContext, Activity activity) {
        super(reactContext);
        this.reactContext   = reactContext;
        this.activity       = activity;
        backgroundServiceIntent = new Intent(reactContext, BackgroundGeolocationService.class);
    }

    @Override
    public String getName() {
        return "RNBackgroundGeolocation";
    }

    @ReactMethod
    public void start(Callback success, Callback failure) {
        startCallback = new HashMap();
        startCallback.put("success", success);
        startCallback.put("failure", failure);
        setEnabled(true);
    }

    @ReactMethod
    public void stop() {
        setEnabled(false);
    }

    @ReactMethod
    public void configure(ReadableMap config) {
        applyConfig(config);
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
        applyConfig(config);

    }

    @ReactMethod
    public void getState() {

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
    public void sync(Callback success, Callback failure) {
        if (syncCallback != null) {
            // Outstanding sync-action is currently running.  Wait for it to complete
            return;
        }
        syncCallback = new HashMap();
        syncCallback.put("success", success);
        syncCallback.put("failure", failure);

        Bundle event = new Bundle();
        event.putString("name", BackgroundGeolocationService.ACTION_SYNC);
        event.putBoolean("request", true);
        EventBus.getDefault().post(event);
    }

    @ReactMethod
    public void getCurrentPosition(ReadableMap options, Callback successCallback, Callback failureCallback) {
        currentPositionOptions          = options;
        isAcquiringCurrentPosition      = true;
        isAcquiringCurrentPositionSince = System.nanoTime();

        HashMap<String, Callback> callback = new HashMap();
        callback.put("success", successCallback);
        callback.put("failure", failureCallback);
        currentPositionCallbacks.add(callback);

        if (!isEnabled) {
            EventBus eventBus = EventBus.getDefault();
            if (!eventBus.isRegistered(this)) {
                eventBus.register(this);
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
        getOdometerCallback = new HashMap();
        getOdometerCallback.put("success", success);
        getOdometerCallback.put("failure", failure);

        Bundle event = new Bundle();
        event.putString("name", BackgroundGeolocationService.ACTION_GET_ODOMETER);
        event.putBoolean("request", true);
        EventBus.getDefault().post(event);
    }
    @ReactMethod
    public void resetOdometer(Callback success, Callback failure) {
        resetOdometerCallback = new HashMap();
        resetOdometerCallback.put("success", success);
        resetOdometerCallback.put("failure", failure);

        Bundle event = new Bundle();
        event.putString("name", BackgroundGeolocationService.ACTION_RESET_ODOMETER);
        event.putBoolean("request", true);
        EventBus.getDefault().post(event);
    }
    @ReactMethod
    public void addGeofence(ReadableMap options, Callback success, Callback failure) {
        Bundle event = new Bundle();
        event.putString("name", BackgroundGeolocationService.ACTION_ADD_GEOFENCE);
        event.putBoolean("request", true);
        event.putFloat("radius", (float) options.getDouble("radius"));
        event.putDouble("latitude", options.getDouble("latitude"));
        event.putDouble("longitude", options.getDouble("longitude"));
        event.putString("identifier", options.getString("identifier"));
        if (options.hasKey("notifyOnEntry")) {
            event.putBoolean("notifyOnEntry", options.getBoolean("notifyOnEntry"));
        }
        if (options.hasKey("notifyOnExit")) {
            event.putBoolean("notifyOnExit", options.getBoolean("notifyOnExit"));
        }
        EventBus.getDefault().post(event);

        // TODO no error checking here
        success.invoke();
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
                // TODO Auto-generated catch block
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
    public void onEventMainThread(ActivityRecognitionResult result) {
        currentActivity = result.getMostProbableActivity();

        if (isAcquiringCurrentPosition) {
            long elapsedMillis = (System.nanoTime() - isAcquiringCurrentPositionSince) / 1000000;
            if (elapsedMillis > GET_CURRENT_POSITION_TIMEOUT) {
                isAcquiringCurrentPosition = false;
                Log.i(TAG, "- getCurrentPosition timeout, giving up");
                /* TODO
                for (CallbackContext callback : currentPositionCallbacks) {
                    callback.error(408); // aka HTTP 408 Request Timeout
                }
                currentPositionCallbacks.clear();
                */
            }
        }
    }

    @Subscribe
    public void onEventMainThread(Location location) {
        this.onLocationChange(locationToMap(location));

        // Fire "location" event on React EventBus
        sendEvent(EVENT_LOCATION, locationToMap(location));
    }

    @Subscribe
    public void onEventMainThread(GeofencingEvent geofencingEvent) {
        Log.i(TAG, "- Rx GeofencingEvent: " + geofencingEvent);
        sendEvent(EVENT_GEOFENCE, geofencingEventToMap(geofencingEvent));
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
        } else if (BackgroundGeolocationService.ACTION_GET_ODOMETER.equalsIgnoreCase(name)) {
            this.onGetOdometer(event);
        } else if (BackgroundGeolocationService.ACTION_RESET_ODOMETER.equalsIgnoreCase(name)) {
            this.onResetOdometer(event);
        } else if (BackgroundGeolocationService.ACTION_CHANGE_PACE.equalsIgnoreCase(name)) {
            this.onChangePace(event);
        } else if (BackgroundGeolocationService.ACTION_ON_MOTION_CHANGE.equalsIgnoreCase(name)) {
            this.onMotionChange(event);
        } else if (BackgroundGeolocationService.ACTION_GOOGLE_PLAY_SERVICES_CONNECT_ERROR.equalsIgnoreCase(name)) {
            GoogleApiAvailability.getInstance().getErrorDialog(activity, event.getInt("errorCode"), 1001).show();
        } else if (BackgroundGeolocationService.ACTION_GET_CURRENT_POSITION.equalsIgnoreCase(name)) {
            this.onCurrentPositionTimeout(event);
        } else if (BackgroundGeolocationService.ACTION_LOCATION_ERROR.equalsIgnoreCase(name)) {
            this.onLocationError(event);
        } else if (BackgroundGeolocationService.ACTION_HTTP_RESPONSE.equalsIgnoreCase(name)) {
            this.onHttpResponse(event);
        }
    }

    private void applyConfig(ReadableMap config) {
        Log.i(TAG, "- configure: " + config.toString());

        SharedPreferences settings = reactContext.getSharedPreferences("TSLocationManager", 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putBoolean("activityIsActive", true);

        if (config.hasKey("stopOnTerminate")) {
            stopOnTerminate = config.getBoolean("stopOnTerminate");
            editor.putBoolean("stopOnTerminate", stopOnTerminate);
        }

        editor.putString("config", mapToJson(config).toString());
        editor.commit();
    }

    private JSONObject mapToJson(ReadableMap map) {
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
                        break;

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    private void setEnabled(boolean value) {
        // Don't set a state that we're already in.
        Log.i(TAG, "- setEnabled:  " + value + ", current value: " + isEnabled);
        if (value == isEnabled) {
            return;
        }
        isEnabled = value;

        Intent launchIntent = activity.getIntent();
        if (launchIntent.hasExtra("forceReload") && launchIntent.hasExtra("location")) {
            try {
                JSONObject location = new JSONObject(launchIntent.getStringExtra("location"));
                onLocationChange(locationToMap(location));
                sendEvent(EVENT_LOCATION, locationToMap(location));

                launchIntent.removeExtra("forceReload");
                launchIntent.removeExtra("location");
            } catch (JSONException e) {
                Log.w(TAG, e);
            }

        }

        SharedPreferences settings = reactContext.getSharedPreferences("TSLocationManager", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("enabled", isEnabled);
        editor.commit();

        if (isEnabled) {
            EventBus eventBus = EventBus.getDefault();
            if (!eventBus.isRegistered(this)) {
                eventBus.register(this);
            }
            if (!BackgroundGeolocationService.isInstanceCreated()) {
                reactContext.startService(backgroundServiceIntent);
            }
        } else {
            EventBus.getDefault().unregister(this);
            reactContext.stopService(backgroundServiceIntent);
        }
    }

    private void onStart(Bundle event) {
        if (startCallback == null) {
            return;
        }
        Callback success = startCallback.get("success");
        success.invoke();
        startCallback = null;

        Intent launchIntent = activity.getIntent();
        if (launchIntent.hasExtra("forceReload") && launchIntent.hasExtra("geofencingEvent")) {
            try {
                JSONObject geofencingEvent  = new JSONObject(launchIntent.getStringExtra("geofencingEvent"));
                sendEvent(EVENT_GEOFENCE, geofencingEventToMap(geofencingEvent));

            } catch (JSONException e) {
                e.printStackTrace();
                Log.w(TAG, e);
            }
        }
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
    private void onGetOdometer(Bundle event) {
        Callback success    = getOdometerCallback.get("success");
        Double distance     = (double) event.getFloat("data");
        success.invoke(distance);
        getOdometerCallback = null;
    }
    public void onResetOdometer(Bundle event) {
        Callback success = resetOdometerCallback.get("success");
        success.invoke();
        resetOdometerCallback = null;
    }
    public void onChangePace(Bundle event) {
        Callback success    = paceChangeCallback.get("success");
        success.invoke(event.getBoolean("isMoving"));
        paceChangeCallback = null;
    }

    private void onLocationChange(WritableMap data) {
        Log.i(TAG, "- RNackgroundGeolocation Rx Location: " + isEnabled);

        if (isAcquiringCurrentPosition) {
            finishAcquiringCurrentPosition(true);
            // Execute callbacks.
            for (HashMap<String, Callback> callback : currentPositionCallbacks) {
                Callback success = callback.get("success");
                success.invoke(data);
            }
            currentPositionCallbacks.clear();
        }
    }

    private void onMotionChange(Bundle event) {
        isMoving = event.getBoolean("isMoving");
        WritableMap params = new WritableNativeMap();
        try {
            params.putMap("location", locationToMap(new JSONObject(event.getString("location"))));
            params.putBoolean("isMoving", isMoving);
            sendEvent(EVENT_MOTIONCHANGE, params);
        } catch (JSONException e) {
            e.printStackTrace();
            params.putString("message", e.getMessage());
            sendEvent(EVENT_ERROR, params);
        }
    }

    public void onHttpResponse(Bundle event) {
        WritableMap params = new WritableNativeMap();
        params.putInt("status", event.getInt("status"));
        params.putString("responseText", event.getString("responseText"));
        sendEvent(EVENT_HTTP, params);
    }

    private void onCurrentPositionTimeout(Bundle event) {
        this.finishAcquiringCurrentPosition(false);
        for (HashMap<String, Callback> callback : currentPositionCallbacks) {
            Callback failure = callback.get("failure");
            failure.invoke(event.getInt("code"));
        }
        currentPositionCallbacks.clear();

        WritableMap params = new WritableNativeMap();
        params.putString("type", "location");
        params.putInt("code", event.getInt("code"));
        params.putString("message", event.getString("message"));
        sendEvent(EVENT_ERROR, params);
    }

    private void onLocationError(Bundle event) {
        Integer code = event.getInt("code");
        if (code == BackgroundGeolocationService.LOCATION_ERROR_DENIED) {
            if (isDebugging()) {
                Toast.makeText(activity, "Location services disabled!", Toast.LENGTH_SHORT).show();
            }
        }

        if (isAcquiringCurrentPosition) {
            finishAcquiringCurrentPosition(true);
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

    private void finishAcquiringCurrentPosition(boolean success) {
        // Current position has arrived:  release the hounds.
        isAcquiringCurrentPosition = false;
        // When currentPosition is explicitly requested while plugin is stopped, shut Service down again and stop listening to EventBus
        if (!isEnabled) {
            backgroundServiceIntent.removeExtra("command");
            EventBus.getDefault().unregister(this);
            reactContext.stopService(backgroundServiceIntent);
        }
    }

    private Boolean isDebugging() {
        SharedPreferences settings = activity.getSharedPreferences("TSLocationManager", 0);
        return settings.contains("debug") && settings.getBoolean("debug", false);
    }

    private WritableMap locationToMap(JSONObject json) {
        WritableMap data = new WritableNativeMap();

        try {
            data.putString("timestamp", json.getString("timestamp"));
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
    private void sendEvent(String eventName, WritableMap params) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(TAG + ":" + eventName, params);
    }
    @Override
    public void onCatalystInstanceDestroy() {
        Log.i(TAG, "- RNBackgroundGeolocationModule#destroy");
        EventBus eventBus = EventBus.getDefault();
        if (eventBus.isRegistered(this)) {
            eventBus.unregister(this);
        }
    }
}
