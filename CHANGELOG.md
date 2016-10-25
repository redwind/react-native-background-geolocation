# Change Log

## [Unreleased]
- [Changed] Implement a mechanism for removing listeners `removeListener` (@alias `un`).  This is particularly important for Android when using `stopOnTerminate: false`.  Listeners on `BackgroundGeolocation` should be removed in `componentDidUnmount`:
```Javascript
  componentDidMount() {
    BackgroundGeolocation.on('location', this.onLocation);
  }
  onLocation(location) {
    console.log('- Location: ', location);
  }
  componentDidUnmount() {
    BackgroundGeolocation.un('location', this.onLocation);
  }
```

## [2.1.2] - 2016-10-19
- [Changed] Introduce database-logging for Android.  Like iOS, the Android module's logs are now stored in the database!  By default, logs are stored for 3 days, but is configurable with `logMaxDays`.  Logs can now be filtered by logLevel:

| logLevel | Label |
|---|---|
|`0`|`LOG_LEVEL_OFF`|
|`1`|`LOG_LEVEL_ERROR`|
|`2`|`LOG_LEVEL_WARNING`|
|`3`|`LOG_LEVEL_INFO`|
|`4`|`LOG_LEVEL_DEBUG`|
|`5`|`LOG_LEVEL_VERBOSE`|

These constants are available on the `BackgroundGeolocation` module:
```Javascript
import {BackgroundGeolocation} from "react-native-background-geolocation-android";

console.log(BackgroundGeolocation.LOG_LEVEL_ERROR);
```

fetch logs with `#getLog` or `#emailLog` methods.  Destroy logs with `#destroyLog`.
- [Fixed] Implement `onHostDestroy` to signal to `BackgroundGeolocation` when `MainActivity` is terminated (was using only `onCatalystInstanceDestroy` previously)
- [Fixed] Issues with Scheduler.
- [Fixed] Issues with `startOnBoot`
- [Added] Added `#removeListener` (@alias `#un`) method to Javascript API.  This is particularly important when using `stopOnTerminate: false`.  You should remove listeners on `BackgroundGeolocation` in `componentWillUnmount`:
```Javascript
  componentWillUnmount: function() {
    // Unregister BackgroundGeolocation event-listeners!
    BackgroundGeolocation.un("location", this.onLocation);
    BackgroundGeolocation.un("http", this.onHttp);
    BackgroundGeolocation.un("geofence", this.onGeofence);
    BackgroundGeolocation.un("heartbeat", this.onHeartbeat);
    BackgroundGeolocation.un("error", this.onError);
    BackgroundGeolocation.un("motionchange", this.onMotionChange);
    BackgroundGeolocation.un("schedule", this.onSchedule);
    BackgroundGeolocation.un("geofenceschange", this.onGeofencesChange);
  }
```

## [2.1.1] - 2016-10-17
- [Changed] Android will filter-out received locations detected to be same-as-last by comparing `latitude`, `longitude`, `speed` & `bearing`.

## [2.1.0] - 2016-10-10
- [Changed] Refactored Geolocation system.  The plugin is no longer bound by native platform limits on number of geofences which can be monitored (iOS: 20; Android: 100).  You may now monitor infinite geofences.  The plugin now stores geofences in its SQLite db and performs a geospatial query, activating only those geofences in proximity of the device (@config #geofenceProximityRadius, @event `geofenceschange`).  See the new [Geofencing Guide](./docs/geofencing.md)

## [2.0.4] - 2016-09-22
- [Added] New required android permission `<uses-feature android:name="android.hardware.location.gps" />`.  See Setup docs.
- [Changed] Changed API of `watchPosition` to match iOS.  See docs.
- [Changed] Refactor setup guides.  Implement `Cocoapod` and `RNPM` guides.
- [Changed] Render `-1`if location has no speed, bearing or altitude
- [Fixed] Fix Android crash when resetOdometer called before `#configure`. 
- [Fixed] found bug where `TSLocationManager` doesn't initialize its odometer from Settings when first instantiated.

## [2.0.3] - 2016-08-29
- [Fixed] Issue where Android pukes when configured with an empty schedule `[]`- [Fixed] Android when configured with `batchSync: true, autoSync: true` was failing because the plugin automatically tweaked `autoSync: false` but failed to reset it to the configured value.  This behaviour was obsolete and has been removed.
- [Added] Add new config `@param {Integer} autoSyncThreshold [0]`.  Allows you to specify a minimum number of persisted records to trigger an auto-sync action.
- [Fixed] `SimpleDateFormat` used to format timestamps was not being used in a thread-safe manner, resulting in corrupted timestamps for some
- [Fixed] React module was setting up listeners on `BackgroundGeolocation` adapter before the app javascript code had a change to subscribe to events, resulting in lost events when MainActivity was launched due to a forceReload event
- [Changed] Accept callbacks to `#stop` method

## [2.0.2] - 2016-08-17
- [Fixed] GoogleApiClient null pointer exeception
- [Changed] Remove `android-compat-v7` from dependencies
- [Changed] Change error message when `#removeGeofence` fails.
- [Fixed] Implement `http` error callback.
- [Fixed] Android heartbeat location wasn't having its meta-data updated (ie: `event: 'heartbeat', battery:<current-data>, uuid: <new uuid>`)
- [Changed] Reduce Android `minimumActivityRecognitionConfidence` default from `80` to `75`
- [Changed] Android will catch `java.lang.SecurityException` when attempting to request location-updates without "Location Permission"

## [2.0.1] - 2016-08-08
- [Fixed] Parse error in Scheduler.
- [Fixed] Sending wrong event `EVENT_GEOFENCE` for schedule-event.  Should have been `EVENT_SCHEDULE` (copy/paste bug).

## [2.0.0] - 2016-08-01
- [Changed] Major Android refactor with significant architectural changes.  Introduce new `adapter.BackgroundGeolocation`, a proxy between the Cordova plugin and `BackgroundGeolocationService`.  Up until now, the functionality of the plugin has been contained within a large, monolithic Android Service class.  This monolithic functionality has mostly been moved into the proxy object now, in addition to spreading among a number of new Helper classes.  The functionality of the HTTP, SQLite, Location and ActivityRecognition layers are largely unchanged, just re-orgnanized.  This new structure will make it much easier going forward with adding new features.
- [Changed] SQLite & HTTP layers have been moved from the BackgroundGeolocationService -> the proxy.  This means that database & http operations can now be performed without enabling the plugin with start() method.
- [Changed] Upgrade EventBus to latest.
- [Changed] Implement new watchPosition method (Android-only), allowing you to get a continues stream of locations without using Javascript setInterval (Android-only currently)
- [Added] Implement new event "providerchange" allowing you to listen to Location-services change events (eg: user turns off GPS, user turns off location services).  Whenever a "providerchange" event occurs, the plugin will automatically fetch the current position and persist the location adding the event: "providerchange" as well as append the provider state-object to the location.
- [Changed] Significantly simplified ReactNativeModule by moving boiler-plate code into the Proxy object.  This significantly simplifies the Cordova plugin, making it much easier to support all the different frameworks the plugin has been ported to (ie: Cordova, NativeScript).
- [Fixed] Breaking changes with react-native-0.29.0

## [1.3.0] - 2016-06-10
- [Changed] `Scheduler` will use `Locale.US` in its Calendar operations, such that the days-of-week correspond to Sunday=1..Saturday=6.
- [Fixed] Bug in `start` method, invoking incorrect Callback reference, which can be null.
- [Changed] Refactor odometer calculation for both iOS and Android.  No longer filters out locations based upon average location accuracy of previous 10 locations; instead, it will only use the current location for odometer calculation if it has accuracy < 100.
- [Changed] Updata binary build to use latest `play-services-location:9.0.1`
- [Added] **Android-only currently, beta** New geofence-only tracking-mode, where the plugin will not actively track location, only geofences.  This mode is engaged with the new API method `#startGeofences` (instead of `#start`, which engages the usual location-tracking mode).  `#getState` returns a new param `#trackingMode [location|geofences]`
- [Changed] **Android** Refactor the Android "Location Request" system to be more robust and better handle "single-location" requests / asynchronous requests, such as `#getCurrentPosition` and stationary-position to fetch multiple samples (3), selecting the most accurate (ios has always done this, heard with the debug sound `tick`).  In debug mode, you'll hear these location-samples with new debug sound "US dialtone".  Just like iOS, these location-samples will be returned to your `#onLocation` event-listener with the property `{sample: true}`.  If you're doing your own HTTP in Javascript, you should **NOT** send these locations to your server.  Use them instead to simply update the current position on the Map, if you're using one.
- [Added] new `#getCurrentPosition` options `#samples` and `#desiredAccuracy`. `#samples` allows you to configure how many location samples to fetch before settling sending the most accurate to your `callbackFn`.  `#desiredAccuracy` will keep sampling until an location having accuracy `<= desiredAccuracy` is achieved (or `#timeout` elapses).
- [Added] new `#event` type `heartbeat` added to `location` params (`#is_heartbeat` is **@deprecated**).
- [Fixed] Issue #676.  Don't engage foreground-service when executing `#getCurrentPosition` while plugin is in disabled state.
- [Fixed] When enabling iOS battery-state monitoring, use setter method `setBatteryMonitoringEnabled` rather than setting property.  This seems to have changed with latest iOS

- [Added] Implement `disableStopDetection` for Android
- [Changed] `android.permission.GET_TASKS` changed to `android.permission.GET_REAL_TASKS`.  Hoping this removes deprecation warning.  This permission is required for Android `#forceReload` configs.
- [Added] New Anddroid config `#notificationIcon`, allowing you to customize the icon shown on notification when using `foregroundServcie: true`.
- [Changed] Take better care with applying `DEFAULT` settings.
- [Changed] Default settings: `startOnBoot: false`, `stopOnTerminate: true`, `distanceFilter: 10`.
- [Added] Allow setting `isMoving` as a config param to `#configure`.  Allows the plugin to automatically do a `#changePace` to your desired value when the plugin is first `#configure`d.
- [Added] New event `activitychange` for listening to changes from the Activit Recognition system.  See **Events** section in API docs for details.  Fixes issue #703.
- [Added] Allow Android `foregroundService` config to be changed dynamically with `#setConfig` (used to have to restart the start to apply this).

## [1.2.3] - 2016-05-25
- [Fixed] Rebuild binary `tslocationmanager.aar` excluding dependencies `appcompat-v7` and `play-services`.  I was experiencing build-failures with latest react-native since other libs may include these dependencies:
- [Fixed] App will no longer crash when license-validation fails.
- [Fixed] Android `GeofenceService` namespace was changed from `com.transistorsoft.locationmanager.GeofenceService` to `com.transistorsoft.locationmanager.geofence.GeofenceService`.  Your `AndroidManifest.xml` will have to be modified.  See installation guide in [Wiki](https://github.com/transistorsoft/react-native-background-geolocation-android/wiki/Installation)

## [1.2.2] - 2016-05-07
- [Changed] Refactor HTTP Layer to stop spamming server when it returns an error (used to keep iterating through the entire queue).  It will now stop syncing as soon as server returns an error (good for throttling servers).
- [Fixed] bugs in Scheduler

## [1.2.1] - 2016-05-02
- [Fixed] Wrap `getCurrentPositionCallbacks` in `synchronized` block to prevent `ConcurrentModificationException` if `getCurrentPosition` is called in quick succession.

## [1.2.0] - 2016-05-01
- [Added] Introduce new [Scheduling feature](http://shop.transistorsoft.com/blogs/news/98537665-background-geolocation-scheduler)

## [1.1.1] - 2016-04-15
- [Fixed] Refactor `startOnBoot` system.  Added missing instruction to configure `BootReceiver` in `AndroidManifest`.  See [Installation Guide](https://github.com/transistorsoft/react-native-background-geolocation-android/wiki/Installation)
- [Added] Android `heartbeat` event, configured with `heartbeatInterval`.

## [1.1.0] - 2016-04-04
- [Fixed] Android 5 geofence limit.  Refactored Geofence system.  Was using a `PendingIntent` per geofence; now uses a single `PendingIntent` for all geofences.
- [Fixed] Edge-case issue when executing `#getCurrentPosition` followed immediately by `#start` in cases where location-timeout occurs.
- [Changed] Volley dependency to official version `com.android.volley`
- [Changed] When plugin is manually stopped, update state of `isMoving` to `false`.
- [Fixed] If location-request times-out while acquiring stationary-location, try to use last-known-location
- [Added] `maxRecordsToPersist` to limit the max number of records persisted in plugin's SQLite database.
- [Added] API methods `#addGeofences` (for adding a list-of-geofences), `#removeGeofences`
- [Changed] The plugin will no longer delete geofences when `#stop` is called; it will merely stop monitoring them.  When the plugin is `#start`ed again, it will start monitoringt any geofences it holds in memory.  To completely delete geofences, use new method `#removeGeofences`.
- [Fixed] Issue with `forceReloadOnX` params. These were not forcing the activity to reload on device reboot when configured with `startOnBoot: true`
- [Fixed] `stopOnTerminate: false` was not working.

## [1.0.1] - 2016-03-14
- [Changed] Standardize the Javascript API methods to send both a `success` as well as `failure` callbacks.
- [Changed] Upgrade plugin for react-native `v0.21.0`
- [Changed] Document new installation steps
- [Changed] Upgrade `emailLog` method to attach log as email-attachment rather than rendering to email-body.  The result of `#getState` is now rendered to the email-body
- [Changed] Android `getState` method was only returning the value of `isMoving` and `isEnabled`.  It now returns the entire current-configuration as supplied to the `#configure` method.
- [Added] Intelligence for `stopTimeout`.  When stop-timer is initiated, save a reference to the current-location.  If another location is recorded while during stop-timer, calculate the distance from location when stop-timer initiated:  if `distance > stationaryRadius`, cancel the stop-timer and stay in "moving" state.
- [Added] New debug sound **"booooop"**:  Signals the initiation of stop-timer of `stopTimeout` minutes.
- [Added] New debug sound **"boop-boop-boop"**: Signals the cancelation of stop-timer due to movement beyond `stationaryRadius`.
- [Added] `mapToJson`, `jsonToMap` methods.
- [Added] CHANGELOG
- [Added] Document `#getLog`, `#emailLog`
- [Fixed] When using setConfig to change `distanceFilter` while location-updates are already engaged, was mistakenly using value for `desiredAccuracy`

## [1.0.0] - 2016-03-08
- [Changed] Introduce new per-app licensing scheme.  I'm phasing out the unlimited 'god-mode' license in favour of generating a distinct license-key for each bundle ID.  This cooresponds with new Customer Dashboard for generating application keys and managing team-members.
- [Changed] Upgrade plugin for react-native v `0.20.0`.

## [0.4.0] - 2016-02-28

- [Fixed] Fix bug transmitting `motionchange` event.
- [Fixed] HTTP listener not being called on server error
- [Fixed] Fix location-timeout; wasn't being fired in right thread.
- [Added] `#insertLocation` method for manually adding locations to plugin's SQLite db
- [Added] `#getCount` retrieve count of records in plugin's SQLite db.
- [Changed] Upgrade react-native dep to 0.19.0
- [Changed] Significant refactor of `LocationManager`
- [Changed] SQLite multi-threading support.
- [Changed] Native HTTP layer will not follow HTTP `301` (redirect) response.
- [Changed] Cache `odometer` between app restart / device reboot.
- [Added] `@param {Boolean} foregroundService` Allow service to run as "foreground service".  This makes the service more impervious to closure by OS due to memory pressure.  Since a foreground service must display a persistent notification in Android notifiction-bar, config-params for modifying the `@param {String} notificaitonTitle`, `@param {String} notificationText` and `@param {String} notificationColor`.
- [Added] Methods `getLog` and `emailLog` for fetching the current applicaiton log at run-time.

## [0.3.1] - 2016-02-20
