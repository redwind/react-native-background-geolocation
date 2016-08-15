# Change Log
## [Unreleased]
- [Fixed] GoogleApiClient null pointer exeception
- [Changed] Remove `android-compat-v7` from dependencies
- [Changed] Change error message when `#removeGeofence` fails.
- [Fixed] Implement `http` error callback.

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
