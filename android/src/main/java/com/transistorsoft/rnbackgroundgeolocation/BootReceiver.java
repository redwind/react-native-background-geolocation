package com.transistorsoft.rnbackgroundgeolocation;

import com.transistorsoft.locationmanager.*;
import com.transistorsoft.locationmanager.adapter.BackgroundGeolocation;
import com.transistorsoft.locationmanager.settings.*;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import org.json.JSONArray;

/**
 * This boot receiver is meant to handle the case where device is first booted after power up.  
 * This boot the headless BackgroundGeolocationService as configured by this class.
 * @author chris scott
 *
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "TSLocationManager";

    @Override
    public void onReceive(Context context, Intent intent) {
        TSLog.box("BootReceiver");

        BackgroundGeolocation adapter = BackgroundGeolocation.getInstance(context, intent);

        // Start scheduler service?
        JSONArray schedule = Settings.getSchedule();
        if (schedule.length() > 0) {
            adapter.startSchedule();
        }

        // startOnBoot?
        if (Settings.getStartOnBoot() && Settings.getEnabled()) {
            adapter.startOnBoot();
        }
    }
}