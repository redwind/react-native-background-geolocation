# CHANGELOG

## 3.4.2 - 2019-12-03
- [Fixed] iOS crash when launching first time `-[__NSDictionaryM setObject:forKey:]: object cannot be nil (key: authorization)'`
- [Changed] Remove Android warning `In order to enable encryption, you must provide the com.transistorsoft.locationmanager.ENCRYPTION_PASSWORD` when using `encrypt: false`.
- [Fixed] Added headless implementation for `geofenceschange` event.

## 3.4.1 - 2019-12-03
- [Fixed] Android bug rendering `Authorization.toJson` when no `Config.authorization` defined.

## 3.4.0 - 2019-12-02
- [Added] New `Config.authorization` option for automated authorization-token support.  If the SDK receives an HTTP response status `401 Unauthorized` and you've provided an `authorization` config, the plugin will automatically send a request to your configured `refreshUrl` to request a new token.  The SDK will take care of adding the required `Authorization` HTTP header with `Bearer accessToken`.  In the past, one would manage token-refresh by listening to the SDK's `onHttp` listener for HTTP `401`.  This can now all be managed by the SDK by providing a `Config.authorization`.
- [Added] Implemented strong encryption support via `Config.encrypt`.  When enabled, the SDK will encrypt location data in its SQLite datbase, as well as the payload in HTTP requests.  See API docs `Config.encrypt` for more information, including the configuration of encryption password.
- [Added] New JSON Web Token API for the Demo server at http://tracker.transistorsoft.com.  It's now easier than ever to configure the plugin to post to the demo server.  See API docs `Config.transistorAuthorizationToken`.  The old method using `BackgroundGeolocation.transistorTrackerParams` is now deprecated.
- [Added] New `DeviceInfo` module for providing simple device-info (`model`, `manufacturer`, `version`, `platform`).

## 3.3.2 - 2019-10-25
- [Fixed] Android bug in params order to getLog.  Thanks @mikehardy.
- [Fixed] Typo in Javascript API callback in `destroyLog`.  Thanks @mikehardy.

## 3.3.1 - 2019-10-23
- [Fixed] Android NPE
```
Caused by: java.lang.NullPointerException:
  at com.transistorsoft.locationmanager.service.TrackingService.b (TrackingService.java:172)
  at com.transistorsoft.locationmanager.service.TrackingService.onStartCommand (TrackingService.java:135)
```
- [Added] new `uploadLog` feature for uploading logs directly to a server.  This is an alternative to `emailLog`.
- [Changed] Migrated logging methods `getLog`, `destroyLog`, `emailLog` to new `Logger` module available at `BackgroundGeolocation.logger`.  See docs for more information.  Existing log methods on `BackgroundGeolocation` are now `@deprecated`.
- [Changed] All logging methods (`getLog`, `emailLog` and `uploadLog`) now accept an optional `SQLQuery`.  Eg:
```javascript
let query = {
  start: Date.parse('2019-10-23 09:00'),
  end: Date.parse('2019-10-23 19:00'),
  limit: 1000,
  order: Logger.ORDER_ASC
};
let Logger = BackgroundGeolocation.logger;

let log = await Logger.getLog(query)
Logger.emailLog('foo@bar.com', query);
Logger.uploadLoad('http://your.server.com/logs', query);
```

## [3.3.0] - 2019-10-17
- [Fixed] Android: Fixed issue executing `#changePace` immediately after `#start`.
- [Fixed] Android:  Add guard against NPR in `calculateMedianAccuracy`
- [Added] Add new Geofencing methods: `#getGeofence(identifier)` and `#geofenceExists(identifier)`.
- [Fixed] iOS issue using `disableMotionActivityUpdates: false` with `useSignificantChangesOnly: true` and `reset: true`.  Plugin will accidentally ask for Motion Permission.  Fixes #1992.
- [Fixed] Resolved a number of Android issues exposed by booting the app in [StrictMode](https://developer.android.com/reference/android/os/StrictMode).  This should definitely help alleviate ANR issues related to `Context.startForegroundService`.
- [Added] Android now supports `disableMotionActivityUpdates` for Android 10 which now requires run-time permission for "Physical Activity".  Setting to `true` will not ask user for this permission.  The plugin will fallback to using the "stationary geofence" triggering, like iOS.
- [Changed] Android:  Ensure all code that accesses the database is performed in background-threads, including all logging (addresses `Context.startForegroundService` ANR issue).
- [Changed] Android:  Ensure all geofence event-handling is performed in background-threads (addresses `Context.startForegroundService` ANR issue).
- [Added] Android: implement logic to handle operation without Motion API on Android 10.  v3 has always used a "stationary geofence" like iOS as a fail-safe, but this is now crucial for Android 10 which now requires run-time permission for "Physical Activity".  For those users who [Deny] this permission, Android will trigger tracking in a manner similar to iOS (ie: requiring movement of about 200 meters).  This also requires handling to detect when the device has become stationary.


## [3.2.2] - 2019-09-18
- [Changed] Android:  move more location-handling code into background-threads to help mitigate against ANR referencing `Context.startForegroundService`
- [Changed] Android:  If BackgroundGeolocation adapter is instantiated headless and is enabled, force ActivityRecognitionService to start.
- [Added] Add `mock` to `locationTemplate` data.

## [3.2.1] - 2019-09-05
- [Changed] Android now hosts its own `proguard-rules.pro`.  See Android setup docs for new integration of plugin's required Proguard Rules into your app.
- [Changed] Rebuild iOS `TSLocationManager.framework` with XCode 10 (previous build used XCode 11-beta6).  Replace `@available` macro with `SYSTEM_VERSION_GREATER_THAN_OR_EQUAL_TO`.
- [Fixed] iOS 13 preventSuspend was not working with iOS 13.  iOS has once again decreased the max time for UIApplication beginBackgroundTask from 180s down to 30s.
- [Changed] Upgrade `android-logback` dependency to `2.0.0`
- [Changed] Android: move some plugin initialization into background-threads (eg: `performLogCleanup`) to help mitigate against ANR "`Context.startForegroundService` did not then call `Service.startForeground`".
- [Fixed] Android Initial headless events can be missed when app booted due to motion transition event.
- [Fixed] Android crash with EventBus `Subscriber already registered error`.
- [Fixed] iOS `Crash: [TSHttpService postBatch:error:] + 6335064 (TSHttpService.m:253)`
- [Changed] Minor changes to `build.gradle`: fetch `minSdkVersion` from `ext` instead of hard-coding, use `implementation` with `react` instead of `compileOnly`.

## [3.2.0] - 2019-08-16
- [Added] iOS 13 support.
- [Added] Auto-linking support.  Do not use `react-native link` anymore.  See the Setup docs for Android and iOS.  Before installing `3.2.0`, first `react-native unlink` both `background-geolocation` and `background-fetch`.

:warning: If you have a previous version of **`react-native-background-geolocation-android < 3.2.0`** installed into **`react-native >= 0.60`**, you should first `unlink` your previous version as `react-native link` is no longer required.

```bash
$ react-native unlink react-native-background-geolocation-android
```

## [3.1.0] - 2019-08-07
- [Fixed] Android Geofence `DWELL` transition (`notifyOnDwell: true`) not firing.
- [Fixed] iOS `logMaxDays` was hard-coded to `7`; Config option not being respected.
- [Added] Android `Q` support (API 29) with new iOS-like location permission model which now requests `When In Use` or `Always`.  Android now supports the config option `locationAuthorizationRequest` which was traditionally iOS-only.  Also, Android Q now requires runtime permission from user for `ACTIVITY_RECOGNITION`.
- [Changed] Another Android tweak to mitigate against error `Context.startForegroundService() did not then call Service.startForeground()`.
- [Added] Add new Android gradle config parameter `appCompatVersion` to replace `supportLibVersion` for better AndroidX compatibility.  If `appCompatVersion` is not found, the plugin's gradle file falls back to `supportLibVersion`.  For react-native@0.60, you should start using the new `appCompatVersion`.

## [3.0.9] - 2019-07-04
- [Changed] Implement `react-native.config.js` ahead of deprecated `rnpm` config.
- [Fixed] Added logic to detect when app is configured to use **AndroidX** dependencies, adjusting the required `appcompat` dependency accordingly.  If the gradle config `ext.supportLibVersion` corresponds to an AndroidX version (eg: `1.0.0`), the plugin assumes AndroidX.
- [Changed] Remove `cocoa-lumberjack` as a dependency since `react-native >= 0.60.0` iOS apps are all Cocoapods now.  There's no longer a need for this dependency now since the plugin's podspec can import `CocoaLumberjack` pod directly.  This is going to cause short-term pain for those using `< 0.60.0`, forcing one to manually install `cocoa-lumberjack`.

## [3.0.8] - 2019-06-28
- [Fixed] iOS / Android issues with odometer and `getCurrentPosition` when used with `maximumAge` constraint.  Incorrect, old location was being returned instead of latest available.
- [Fixed] Some Android methods were executing the callback in background-thread, exposed when using flutter dev channel (`#insertLocation`, `#getLocations`, `#getGeofences`, `#sync`).
- [Fixed] Add `intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)` to `DeviceSettings` request for Android 9 compatibility.
- [Changed] Tweaks to Android services code to help guard against error `Context.startForegroundService() did not then call Service.startForeground()`.
- [Fixed] iOS manual `sync` crash in simulator while executing callback when error response is returned from server.

## [3.0.7] - 2019-06-17
- [Fixed] iOS & Android:  Odometer issues:  clear odometer reference location on `#stop`, `#startGeofences`.
- [Fixed] Odometer issues: Android must persist its odometer reference location since the foreground-service is no longer long-lived and the app may be terminated between motionchange events.
- [Fixed] Return `Service.START_REDELIVER_INTENT` from `HeartbeatService` to prevent `null` Intent being delivered to `HeartbeatService`, causing a crash.
- [Added] Implement Android [LocationSettingsRequest](https://developer.android.com/training/location/change-location-settings#get-settings).  Determines if device settings is currently configured according to the plugin's desired-settings (eg: gps enabled, location-services enabled).  If the device settings differs, an automatic dialog will perform the required settings changes automatically when user clicks [OK].
- [Fixed] Android `triggerActivities` was not implemented refactor of `3.x`.

## [3.0.6] - 2019-06-04
- [Fixed] Android `destroyLocations` callback was being executed in background-thread.
- [Fixed] When Android geofence API receives a `GEOFENCE_NOT_AVAILABLE` error (can occur if Wifi is disabled), geofences must be re-registered.
- [Fixed] Android `Config.disableStopDetection` was not implemented.
- [Added] Add new Android Config options `scheduleUseAlarmManager` for forcing scheduler to use `AlarmManager` insead of `JobService` for more precise scheduling.

## [3.0.5] - 2019-05-10
- [Changed] Rollback `android-permissions` version back to `0.1.8`.  It relies on `support-annotations@28`.  This isn't a problem if one simply upgrades their `targetSdkVersion` but the support calls aren't worth the hassle, since the latest version doesn't offer anything the plugin needs.

## [3.0.4] - 2019-05-08
- [Fixed] iOS: changing `pauseslocationUpdatesAutomatically` was not being applied.
- [Changed] `reset` parameter provided to `#ready` has now been default to `true`.  This causes too many support issues for people using the plugin the first time.
- [Fixed] Android threading issue where 2 distinct `SingleLocationRequest` were issued the same id.  This could result in the foreground service quickly starting/stopping until `locationTimeout` expired.
- [Fixed] Android issue where geofences could fail to query for new geofences-in-proximity after a restart.
- [Fixed] Android issues re-booting device with location-services disabled or location-authorization revoked.
- [Added] Implement support for [Custom Android Notification Layouts](/../../wiki/Android-Custom-Notification-Layout).

## [3.0.3] - 2019-04-25
- [Fixed] Android bug where service repeatedly starts/stops after rebooting the device with plugin in *moving* state.
- [Fixed] Android headless `heartbeat` events were failing (incorrect `Context` was supplied to the event).

## [3.0.2] - 2019-04-18
- [Fixed] Windows bug in new `react-native link` script.
- [Fixed] Android scheduler bug.  When app is terminated & restarted during a scheduled ON period, tracking-service does not restart.

## [3.0.1] - 2019-04-15
- [Added] Added android implementation for `react-native link` script to automatically add the required `maven url` and `ext.googlePlayServicesLocationVersion`.

## [3.0.0] - 2019-04-10
- [Fixed] Fixed `NullPointerException BackgroundTaskManager$Task.start`

## [3.0.0-rc.5] - 2019-03-31
- [Fixed] [Fixed] Another case of Android `NullPointerException` with `Bundle#getExtras` (#674).

## [3.0.0-rc.4] - 2019-03-29
- [Fixed] Android `NullPointerException` with `Bundle#getExtras` (#674).
- [Fixed] Android not persisting `providerchange` location when location-services re-enabled.

## [3.0.0-rc.3] - 2019-03-27
- [Fixed] An Android foreground-service is launched on first install and fails to stop.

## [3.0.0-rc.2] - 2019-03-27
- [Updated] Re-generate docs.  Forgot to import my new typedoc plugin `typedoc-plugin-mediaplayer` to generate audio player for debug sounds in the docs.
![](https://dl.dropbox.com/s/zomejlm9egm1ujl/Screenshot%202019-03-26%2023.10.50.png?dl=1)

## [3.0.0-rc.1] - 2019-03-26

------------------------------------------------------------------------------
### :warning: Breaking Changes

#### [Changed] The license format has changed.  New `3.0.0` licenses are now available for customers in the [product dashboard](https://www.transistorsoft.com/shop/customers).
![](https://dl.dropbox.com/s/3ohnvl9go4mi30t/Screenshot%202019-03-26%2023.07.46.png?dl=1)

- For versions `< 3.0.0`, use *old* license keys.
- For versions `>= 3.0.0`, use *new* license keys.

#### [Changed] Proguard Rules.
- For those minifying their generated Android APK by enabling the following in your `app/build.gradle`,

```
/**
 * Run Proguard to shrink the Java bytecode in release builds.
 */
def enableProguardInReleaseBuilds = true
```

You must add the following line to your `proguard-rules.pro`:
```
-keepnames class com.facebook.react.ReactActivity
```

See the full required Proguard config in the Android Setup Doc.


------------------------------------------------------------------------------

### Fixes
- [Fixed] Logic bugs in MotionActivity triggering between *stationary* / *moving* states.
- [Fixed] Remove iOS react-native `#include` cruft `if __has_include(“XXX.h”)` for support RN libs during their transition to using Frameworks.  Fixes #669.
- [Fixed] iOS crash with configured `schedule`: `index 2 beyond bounds [0 .. 1]' was thrown`.  Fixes #666.

### New Features

- [Added] Android implementation for `useSignificantChangesOnly` Config option.  Will request Android locations **without the persistent foreground service**.  You will receive location updates only a few times per hour:

#### `useSignificantChangesOnly: true`:
![](https://dl.dropboxusercontent.com/s/wdl9e156myv5b34/useSignificantChangesOnly.png?dl=1)

#### `useSignificantChangesOnly: false`:
![](https://dl.dropboxusercontent.com/s/hcxby3sujqanv9q/useSignificantChangesOnly-false.png?dl=1)

- [Added] Android now implements a "stationary geofence", just like iOS.  It currently acts as a secondary triggering mechanism along with the current motion-activity API.  You will hear the "zap" sound effect when it triggers.  This also has the fortunate consequence of allowing mock-location apps (eg: Lockito) of being able to trigger tracking automatically.

- [Added] The SDK detects mock locations and skips trigging the `stopTimeout` system, improving location simulation workflow.
- [Added] Android-only Config option `geofenceModeHighAccuracy` for more control over geofence triggering responsiveness.  Runs a foreground-service during geofences-only mode (`#startGeofences`).  This will, of course, consume more power.
```dart
await BackgroundGeolocation.ready({
  geofenceModeHighAccuracy: true,
  desiredAccuracy: BackgroundGeolocation.DESIRED_ACCURACY_MEDIUM,
  locationUpdateInterval: 5000,
  distanceFilter: 50
));

BackgroundGeolocation.startGeofences();
```

#### `geofenceModeHighAccuracy: false` (Default)

- Transition events are delayed in favour of lower power consumption.

![](https://dl.dropboxusercontent.com/s/6nxbuersjcdqa8b/geofenceModeHighAccuracy-false.png?dl=1)

#### `geofenceModeHighAccuracy: true`

- Transition events are nearly instantaneous at the cost of higher power consumption.

![](https://dl.dropbox.com/s/w53hqn7f7n1ug1o/geofenceModeHighAccuracy-true.png?dl=1)

- [Added] Android implementation of `startBackgroundTask` / `stopBackgroundTask`.
```dart
  int taskId = await BackgroundGeolocation.startBackgroundTask();

  // Do any work you like -- it's guaranteed to run, regardless of background/terminated.
  // Your task has exactly 30s to do work before the service auto-stops itself.

  getDataFromServer('https://foo.bar.com').then((result) => {
    // Be sure to always signal completion of your taskId.
    BackgroundGeolocation.stopBackgroundTask(taskId);
  }).catch((error) => {
    // Be sure to always signal completion of your taskId.
    BackgroundGeolocation.stopBackgroundTask(taskId);
  });
```
Logging for Android background-tasks looks like this (when you see an hourglass, a foreground-service is active)
```
 [BackgroundTaskManager onStartJob] ⏳ startBackgroundTask: 6
 .
 .
 .
 [BackgroundTaskManager$Task stop] ⏳ stopBackgroundTask: 6
```
- [Added] New custom Android debug sound FX.  See the [Config.debug](https://transistorsoft.github.io/cordova-background-geolocation/interfaces/_cordova_background_geolocation_.config.html#debug) for a new decription of iOS / Android sound FX **including a media player to play each.**
![](https://dl.dropbox.com/s/zomejlm9egm1ujl/Screenshot%202019-03-26%2023.10.50.png?dl=1)

:warning: These debug sound FX consume about **1.4MB** in the plugin's `tslocationmanager.aar`.  These assets can easily be stripped in your `release` builds by adding the following gradle task to your `app/build.gradle` (I'm working on an automated solution within the context of the plugin's `build.gradle`; so far, no luck).  [Big thanks](https://github.com/transistorsoft/react-native-background-geolocation-android/issues/667#issuecomment-475928108) to @mikehardy.
```gradle
/**
 * Purge Background Geolocation debug sounds from release build.
 */
def purgeBackgroundGeolocationDebugResources(applicationVariants) {
    applicationVariants.all { variant ->
        if (variant.buildType.name == 'release') {
            variant.mergeResources.doLast {
                delete(fileTree(dir: variant.mergeResources.outputDir, includes: ['raw_tslocationmanager*']))

            }
        }
    }
}

android {
    //Remove debug sounds from BackgroundGeolocation plugin
    purgeBackgroundGeolocationDebugResources(applicationVariants)

    compileSdkVersion rootProject.ext.compileSdkVersion
    .
    .
    .
}
```

### Removed
- [Changed] Removed Android config option **`activityRecognitionInterval`** and **`minimumActivityRecognitionConfidence`**.  The addition of the new "stationary geofence" for Android should alleviate issues with poor devices failing to initiate tracking.  The Android SDK now uses the more modern [ActivityTransistionClient](https://medium.com/life360-engineering/beta-testing-googles-new-activity-transition-api-c9c418d4b553) API which is a higher level wrapper for the traditional [ActivityReconitionClient](https://developers.google.com/android/reference/com/google/android/gms/location/ActivityRecognitionClient).  `AcitvityTransitionClient` does not accept a polling `interval`, thus `actiivtyRecognitionInterval` is now unused.  Also, `ActivityTransitionClient` emits similar `on_foot`, `in_vehicle` events but no longer provides a `confidence`, thus `confidence` is now reported always as `100`.  If you've been implementing your own custom triggering logic based upon `confidence`, it's now pointless.  The `ActivityTransitionClient` will open doors for new features based upon transitions between activity states.

```
╔═════════════════════════════════════════════
║ Motion Transition Result
╠═════════════════════════════════════════════
╟─ 🔴  EXIT: walking
╟─ 🎾  ENTER: still
╚═════════════════════════════════════════════
```

### Maintenance
- [Changed] Update `android-permissions` dependency to `0.1.8`.

## [3.0.0-beta.5] - 2019-03-20
- [Fixed] Logic bugs in MotionActivity triggering between *stationary* / *moving* states.
- [Added] Android-only Config option `geofenceModeHighAccuracy` for more control over geofence triggering accuracy.  Runs a foreground-service during geofences-only mode (`#startGeofences`).
- [Added] Android implementation for `useSignificantChangesOnly` Config option.  Will request Android locations **without the persistent foreground service**.  You will receive location updates only a few times per hour.
- [Changed] Update `android-permissions` dependency to `0.1.8`.

## [3.0.0-beta.4] - 2019-03-02
- [Fixed] Android bug in Config dirty-fields mechanism.

## [3.0.0-beta.3] - 2019-03-02
- [Changed] Improve trackingMode state-changes between location -> geofences-only.
- [Changed] Improvements to geofences-only tracking.
- [Changed] Improvements to stationary-geofence monitoring, detecting mock locations to prevent stopTimeout triggering.

## [3.0.0-beta.2] - 2019-02-28
- [Changed] Tweaking stationary region monitoring.
- [Changed] Tweaking bad vendor detection to force stopTimeout timer when device is stationary for long periods and motion api hasn't respon
ded.

## [3.0.0-beta.1] - 2019-02-27
- [Changed] Major refactor of Android Service architecture.  The SDK no longer requires a foreground-service active at all times.  The foreground-service (and cooresponding persistent notification) will only be active while the SDK is in the *moving* state.  No breaking dart api changes.
- [Changed] Improved Android debug notifications.

## [2.15.1] 2019-02-20
- [Added] Added new Config options `persistMode` for specifying exactly which events get persisted: location | geofence | all | none.
- [Added] Experimental Android-only Config option `speedJumpFilter (default 300 meters/second)` for detecting location anomalies.  The plugin will measure the distance and apparent speed of the current location relative to last location.  If the apparent speed is > `speedJumpFilter`, the location will be ignored.  Some users, particularly in Australia, curiously, have had locations suddenly jump hundreds of kilometers away, into the ocean.
- [Changed] iOS and Android will not perform odometer updates when the calculated distance is less than the average accuracy of the current and previous location.  This is to prevent small odometer changes when the device is lingering around the same position.
- [Fixed] Added Android gradle dependency `appcompat-v7`
- [Fixed] Minor change to Android `HeadlessTask` to fix possible issue with `Looper`.

## [2.15.0] 2019-02-07
- [Added] New `DeviceSettings` API for redirecting user to Android Settings screens, including vendor-specific screens (eg: Huawei, OnePlus, Xiaomi, etc).  This is an attempt to help direct the user to appropriate device-settings screens for poor Android vendors as detailed in the site [Don't kill my app](https://dontkillmyapp.com/).
- [Added] `schedule` can now be configured to optionally execute geofences-only mode (ie: `#startGeofences`) per schedule entry.  See `schedule` docs.
- [Changed] Update Gradle config to use `implementation` instead of deprecated `compile`
- **[BREAKING]** Change Gradle `ext` configuration property `googlePlayServicesVersion` -> `googlePlayServicesLocationVersion`.  Now that Google has decoupled all their libraries, `play-services:location` now has its own version, independant of all other libs.

`android/build.gradle`:
```diff
buildscript {
    ext {
        buildToolsVersion = "28.0.3"
        minSdkVersion = 16
        compileSdkVersion = 28
        targetSdkVersion = 27
        supportLibVersion = "28.0.0"
-       googlePlayServicesVersion = "16.0.0"
+       googlePlayServicesLocationVersion = "16.0.0"
    }
}
```

## [2.14.6] 2019-01-11
- [Changed] Android Service: Return `START_STICKY` instead of `START_REDELIVER_INTENT`.
- [Changed] Android: `setShowBadge(false)` on Android `NotificationChannel`.  Some users reporting that Android shows a badge-count on app icon when service is started / stopped.

## [2.14.5] 2018-12-18
- [Fixed] Android `extras` provided to `watchPosition` were not being appended to location data.

## [2.14.4] 2018-12-13
- [Fixed] Android NPE in `watchPosition`
- [Added] Added method `getProviderState` for querying current state of location-services.
- [Added] Added method `requestPermission` for manually requesting location-permission (`#start`, `#getCurrentPosition`, `#watchPosition` etc, will already automatically request permission.

## [2.14.3] 2018-12-06
- [Changed] Upgrade Android logger dependency to latest version (`logback`).
- [Fixed] Prevent Android foreground-service from auto-starting when location permission is revoked via Settings screen.
- [Fixed] NPE in Android HTTP Service when manual sync is called.  Probably a threading issue with multiple sync operations executed simultaneously.

## [2.14.2] 2018-11-20
- [Added] Android SDK 28 requires new permission to use foreground-service.
- [Fixed] Do not calculate odometer with geofence events.  Android geofence event's location timestamp does not correspond with the actual time the geofence fires since Android is performing some heuristics in the background to ensure the potential geofence event is not a false positive.  The actual geofence event can fire some minutes in the future (ie: the location timestamp will be some minutues in the past).  Using geofence location in odometer calculations will corrupt the odometer value.
- [Fixed] Android could not dynamically update the `locationTemplate` / `geofenceTemplate` without `#stop` and application restart.
- [Fixed] Android `startGeofences` after revoking & re-granting permission would fail to launch the plugin's Service.
- [Fixed] iOS HTTP crash when using `batchSync: true`.  At application boot, there was a threading issue if the server returned an HTTP error, multiple instances of the HTTP service could run, causing a crash.

## [2.14.1] 2018-10-29
- [Fixed] react-native link on Windows.

## [2.14.0] 2018-10-29
- [Fixed] Android `NullPointerException` on `WatchPositionCallback` with `watchPosition`.

## [2.14.0-beta.2] 2018-10-23
- [Fixed] Documentation issue with method signature `getCurrentPosition`.
- [iOS] Catch `NSInvalidArgumentException` when decoding `TSConfig`.
- [Added] `.npmignore`:  ignore `/docs`, `node_modules`.

## [2.14.0-beta.1] 2018-10-16

- [Added] Implement [Typescript API](https://facebook.github.io/react-native/blog/2018/05/07/using-typescript-with-react-native)
- [Added] Refactor documentation.  Now auto-generated from Typescript api with [Typedoc](https://typedoc.org/) and served from https://transistorsoft.github.io/react-native-background-geolocation-android
- [Breaking] Change event-signature of `location` event:  location-errors are now received in new `failure` callback rather than separate `error` event.  The `error` event has been removed.
```javascript
BackgroundGeolocation.onLocation((location) => {
  console.log('[location] -', location);
}, (errorCode) => {
  // Location errors received here.
  console.log('[location] ERROR -', errorCode);
});
```
- [Added] With the new Typescript API, it's necessary to add dedicated listener-methods for each method (in order for code-assist to work).
```javascript
// Old:  No code-assist for event-signature with new Typescript API
BackgroundGeolocation.on('location', (location) => {}, (error) => {});
// New:  use dedicated listener-method #onLocation
BackgroundGeolocation.onLocation((location) => {}, (error) => {});
// And every other event:
BackgroundGeolocation.onMotionChange(callback);
BackgroundGeolocation.onMotionProviderChange(callback);
BackgroundGeolocation.onActivityChange(callback);
BackgroundGeolocation.onHttp(callback);
BackgroundGeolocation.onGeofence(callback);
BackgroundGeolocation.onGeofencesChange(callback);
BackgroundGeolocation.onSchedule(callback);
BackgroundGeolocation.onConnectivityChange(callback);
BackgroundGeolocation.onPowerSaveChange(callback);
BackgroundGeolocation.onEnabledChange(callback);
```
- [Breaking] Change event-signature of `enabledchange` event to return simple `boolean` instead of `{enabled: true}`:  It was pointless to return an `{}` for this event.
```javascript
// Old
BackgroundGeolocation.onEnabledChange((enabledChangeEvent) => {
  console.log('[enabledchange] -' enabledChangeEvent.enabled);
})
// New
BackgroundGeolocation.onEnabledChange((enabled) => {
  console.log('[enabledchange] -' enabled);
})
```
- [Breaking] Change signature of `#getCurrentPosition` method:  Options `{}` is now first argument rather than last:
```javascript
// Old (Options as 3rd argument)
BackgroundGeolocation.getCurrentPosition((location) => {
  console.log('[getCurrentPosition] -', location);
}, (error) => {
  console.log('[getCurrentPosition] ERROR -', error);
}, {  // <-- Options as 3rd argument
  samples: 3,
  extras: {foo: 'bar'}
})

// New (Options as 1st argument)
BackgroundGeolocation.getCurrentPosition({
  samples: 3,
  extras: {foo: 'bar'}
}, (location) => {
  console.log('[getCurrentPosition] -', location);
}, (error) => {
  console.log('[getCurrentPosition] ERROR -', error);
})
```
## [2.13.4] - 2018-10-01
- [Fixed] iOS was missing Firebase adapter hook for persisting geofences.
- [Changed] Android headless events are now posted with using `EventBus` instead of `JobScheduler`.  Events posted via Android `JobScheduler` are subject to time-slicing by the OS so events could arrive late.
- [Fixed] Missing `removeListeners` for `connectivitychange`, `enabledchange` events.
- [Fixed] iOS was not calling `success` callback for `removeListeners`.
- [Fixed] iOS plugin was not parsing schedule in `#ready` event.

## [2.13.3] - 2018-08-30
- [Fixed] Minor error plugin's `build.gradle`.  `DEFAULT_BUILD_TOOLS_VERSION` was set incorrectly.

## [2.13.2] - 2018-08-29
- [Fixed] iOS scheduler not being initialized in `#ready` after reboot.

## [2.13.1] - 2018-08-17
- [Fixed] Android firebase plugin bug in release build.

## [2.13.0] - 2018-08-14
- [Added] New Android config-option `notificationChannelName` for configuring the notification-channel required by the foreground-service notification.  See *Settings->Apps & Notificaitions->Your App->App Notifications*.
- [Added] Support for new [Firebase Adapter](https://github.com/transistorsoft/react-native-background-geolocation-firebase)
- [Added] iOS support for HTTP method `PATCH` (Android already supports it).
- [Fixed] Android was not using `httpTimeout` with latest `okhttp3`.
- [Fixed] Android issue not firing `providerchange` on boot when configured with `stopOnTerminate: true`
- [Fixed] Android `headlessJobService` class could fail to be applied when upgrading from previous version.  Ensure always applied.
- [Fixed] Android `httpTimeout` was not being applied to new `okhttp3.Client#connectionTimeout`
- [Fixed] Apply recommended XCode build settings.
- [Fixed] XCode warnings 'implicity retain self in block'
- [Changed] Android Removed unnecessary attribute `android:supportsRtl="true"` from `AndroidManifest`
- [Fixed] iOS `preventSuspend` was not working with `useSignificantChangesOnly`
- [Changed] iOS disable encryption on SQLite database file when "Data Protection" capability is enabled with `NSFileProtectionNone` so that plugin can continue to insert records while device is locked.

## [2.12.2] - 2018-05-25

- [Fixed] Fix `react-native link` error when iOS and npm project name are diferent.
- [Fixed] iOS issue when plugin is booted in background in geofences-only mode, could engage location-tracking mode.
- [Fixed] Android `getCurrentPosition` was not respecting `persist: true` when executed in geofences-only mode.

## [2.12.1] - 2018-05-17
- [Fixed] iOS geofence exit was being ignored in a specific case where (1) geofence was configured with `notifyOnDwell: true` AND (2) the app was booted in the background *due to* a geofence exit event.

## [2.12.0] - 2018-05-11
- [Fixed] Android bug where plugin could fail to translate iOS desiredAccuracy value to Android value, resulting in incorrect `desiredAccuracy` value for Android, probably defaulting to `DESIRED_ACCURACY_LOWEST`.

## [2.12.0-beta.15] - 2018-05-02
- [Fixed] iOS was not persiting odometer.
- [Fixed] iOS geofence exit event not being executed due to a condition where a stationary event occurs while geofence exit events are awaiting their location to be returned.
- [Added] iOS config `disableLocationAuthorizationAlert` for disabling automatic location-authorization alert when location-services are disabled or user changes toggles location access (eg: `Always` -> `WhenInUse`).
- [Fixed] Android was not executing `#getCurrentPosition` `failure` callback.
- [Fixed] Fixed issue executing `#getCurrentPosition` from Headless mode while plugin is current disabled.
- [Added] Add new iOS `locationAuthorizationRequest: "Any"` for allowing the plugin to operate in either `Always` or `WhenInUse` without being spammed by location-authorization dialog.
- [Changed] Repackage android lib `tslocationmanager.aar` as a Maven Repositoroy.  :warning: Installation procedure has changed slightly.  Please review Android installation docs for your chosen install method (Manual or react-native link).
- [Added] Added new initialization method `#ready`, desigend to replace `#configure` (which is now deprectated).  The new `#ready` method operates in the same manner as `#configure` with a crucial difference -- the plugin will only apply the supplied configuration `{}` at the first launch of your app &mdash; thereafter, it will automatically load the last-known config from persistent storage.
- [Added] Add new method `#reset` for resetting the plugin configuration to documented defaults.
- [Added] Refactor Javascript API to use Promises.  Only `#watchPosition` and adding event-listeners with `#on` will not use promises.
- [Fixed] iOS issue not turning of "keepAlive" system when `#stop` method is executed while stop-detection system is engaged.
- [Added] Android will fire `providerchange` event after the result of user location-authorization (accept/deny).  The result will be available in the `status` key of the event-object.
- [Changed] Refactor native configuration system for both iOS and Android with more traditional Obj-c / Java API.
- [Changed] Create improved Obj-c / Java APIs for location-requests (`#getCurrentPosition`, `#watchPosition`) and geofencing.
- [Added] Added new event `connectivitychange` for detecting network connectivity state-changes.
- [Added] Added new event `enabledchange`, fired with the plugin enabled state changes.  Executing `#start` / `#stop` will cause this event to fire.  This is primarily designed for use with `stopAfterElapsedMinutes`.

## [2.11.0] - 2018-02-03
- [Fixed] Guard usage of `powersavechange` event for iOS < 9
- [Added] Android permissions are now handled completely within `tslocationmanager` library rather than within Cordova Activity.
- [Fixed] iOS `emailLog` issues:  sanity check existence of email client, ensure we have reference to topMost `UIViewController`.
- [Added] New Android "Headless" mechanism allowing you provide a simple custom Java class to receive all events from the plugin when your app is terminated (with `stopOnTerminate: false`).  The headless mechanism is enabled with new `@config {Boolean} enableHeadless`.  See the Wiki "Headless Mode" for details.
- [Fixed] iOS `getCurrentPosition` was applying entire options `{}` as `extras`.
- [Fixed] iOS `watchPosition` / `getCurrentPosition` `@option persist` was being ignored when plugin was disabled (ie: `#stop`ped).
- [Fixed] Implement Android `JobScheduler` API for scheduler (where API_LEVEL) allows it.  Will fallback to existing `AlarmManager` implementation where API_LEVEL doesn't allow `JobScheduler`.  This fixes issues scheduler issues with strict new Android 8 background-operation rules.
- [Added] Added new Android `@config {Boolean} allowIdenticalLocations [false]` for overriding the default behaviour of ignoring locations which are identical to the last location.
- [Added] Add iOS `CLFloor` attribute to `location.coordinate` for use in indoor-tracking when required RF hardware is present in the environment (specifies which floor the device is on).

## [2.10.1] - 2017-11-12
- [Fixed] Rare issue with iOS where **rapidly** toggling executing `start` with `changePace(true)` in the callback followed by `stop`, over and over again, would lock up the main thread.
- [Changed] Android `GEOFENCE_INITIAL_TRIGGER_DWELL` defaulted to `true`.
- [Fixed] `Proguard-Rules` were not ignoring the new `LogFileProvider` used for `#emailLog` method.
- [Fixed] Android issue on some device where callback to `#configure` would not be executed in certain cases.

## [2.10.0] - 2017-11-09
- [Fixed] Android NPE on `Settings.getForegroundService()` when using `foregroundService: false`
- [Fixed] Android 8 error with `emailLog`.  Crash due to `SecurityException` when writing the log-file.  Fixed by implementing `FileProvider` (storage permissions no longer necessary).
- [Fixed] iOS bug when providing non-string `#header` values.  Ensure casted to String.
- [Changed] Android minimum required play-services version is `11.2.0` (required for new `play-services` APis.  Anything less and plugin will crash.
- [Changed] Update Android to use new [`FusedLocationProviderClient`](https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderClient) instead of now-deprectated `FusedLocationProviderAPI`.  It's the same underlying play-services location API -- just with a much simpler, less error-prone interface to implement.
- [Fixed] On Android, when `changePace(true)` is executed while device is currently `still` (and remains `still`), `stopTimeout` timer would never initiate until device movement is detected.
- [Fixed] iOS manual `#sync` was not executing *any* callback if database was empty.
- [Added] Implement new Android 8 `NotificationChannel` which is now required for displaying the `foregroundService` notification.
- [Added] Android foreground-service notification now uses `id: 9942585`.  If you wish to interact with the foreground-service notification in native code, this is the `id`.
- [Fixed] iOS not always firing location `failure` callback.
- [Fixed] iOS was not forcing an HTTP flush on `motionchange` event when `autoSyncThreshold` was used.
- [Fixed] iOS Add sanity-check for Settings `boolean` type.  It was possible to corrupt the Settings when a `boolean`-type setting was provided with a non-boolean value (eg: `{}`, `[]`).
- [Fixed] Android `getState` could cause an NPE if executed before `#configure`.
- [Fixed] Work around iOS 11 bug with `CLLocationManager#stopMonitoringSignificantLocationChanges` (SLC):  When this method is called upon *any* single `CLLocationManager` instance, it would cause *all* instances to `#stopMonitoringSignificantLocationChanges`.  This caused problems with Scheduler evaluation, since SLC is required to periodically evaluate the schedule.

## [2.9.4] - 2017-09-25
- [Added] Re-build for iOS 11, XCode 9
- [Added] Implement new `powersavechange` event in addition to `isPowerSaveMode` method for determining if OS "Power saving" mode is enabled.
- [Added] New config `elasticityMultiplier` for controlling the scale of `distanceFilter` elasticity calculation.
- [Fixed] Android bug not firing `schedule` Javascript listeners
- [Fixed] Android crash `onGooglePlayServicesConnectdError` when Google Play Services needs to be updated on device.

## [2.9.3] - 2017-09-15
- [Changed] Refactor Android `onDestroy` mechanism attempting to solve nagging and un-reproducible null pointer exceptions.
- [Added] Implement Android location permissions handling using `PermissionsAndroid` API.  You no longer need to use 3rd-party permissions module to obtain Android location permission.
- [Fixed] Fixed bug not where `stopAfterElapsedMinutes` is not evaluated when executing `#getCurrentPosition`.
- [Fixed] Modifications for Android O.  For now, `foregroundService: true` will be enforced when running on Android O (api 26).

## [2.9.2] - 2017-08-28
- [Changed] iOS `motionchange` position will be fetch by `CLLocationManager#startUpdatingLocation` rather than `#requestLocation`, since `#requestLocation` cannot keep the app alive in the background.  This could cause the app to be suspended when `motionchange` position was requested due to a background-fetch event.
- [Fixed] Android `stopOnTerminate` was not setting the `enabled` value to `false` when terminated.  This caused the plugin to automatically `#start` the first time the app was booted (it would work correctly every boot thereafter).
- [Changed] Change Android HTTP layer to use more modern library `OkHttp3` instead of `Volley`.  Some users reported weird issues with some devices on some servers.  `OkHttp` seems to have solved it for them.  `OkHttp` is a much simpler library to use than `Volley`

## [2.9.1] - 2017-08-21
- [Changed] Update `react-native-background-fetch` to `~2.1.0`
- [Added] Javascript API to plugin's logging system.
- [Fixed] Minor issue with iOS flush where multiple threads might create multiple background-tasks, leaving some unfinished.

## [2.9.0] - 2017-08-16
- [Changed] Refactor iOS / Android core library event-subscription API.
- [Added] Removing single event-listeners with `#removeListener` (alias `#un`) is snow fully supported!  There will no longer be warnings "No listeners for event X", since the plugin completely removes event-listeners from the core library.  You will no longer have to create `noop` event-listeners on events you're not using simply to suppress these warnings.

## [2.8.5] - 2017-07-27
- [Changed] Improve iOS/Android acquisition of `motionchange` location to ensure a recent location is fetched.
- [Changed] Implement `#getSensors` method for both iOS & Android.  Returns an object indicating the presense of *accelerometer*, *gyroscope* and *magnetometer*.  If any of these sensors are missing, the motion-detection system for that device will poor.
- [Changed] The `activitychange` success callback method signature has been changed from `{String} activityName` -> `{Object}` containing both `activityName` as well as `confidence`.  This event only used to fire after the `activityName` changed (eg: `on_foot` -> `in_vehicle`), regardless of `confidence`.  This event will now fire for *any* change in activity, including `confidence` changes.
- [Changed] iOS `emailLog` will gzip the attached log file.
- [Added] Implement new Android config `notificationPriority` for controlling the behaviour of the `foregroundService` notification and notification-bar icon.
- [Changed] Tweak iOS Location Authorization to not show locationAuthorizationAlert if user initially denies location permission.
- [Fixed] Android:  Remove isMoving condition from geofence proximity evaluator.
- [Fixed] Android was creating a foreground notification even when `foregroundService: false`
- [Fixed] iOS 11 fix:  Added new location-authorization string `NSLocationAlwaysAndWhenInUseUsageDescription`.  iOS 11 now requires location-authorization popup to allow user to select either `Always` or `WhenInUse`.

## [2.8.4] -  2017-07-10
- [Fixed] Android & iOS will ensure old location samples are ignored with `getCurrentPosition`
- [Fixed] Android `providerchange` event would continue to persist a providerchange location even when plugin was disabled for the case where location-services is disabled by user.
- [Fixed] Don't mutate iOS `url` to lowercase.  Just lowercase the comparison when checking for `301` redirects.
- [Changed] Android will attempt up to 5 `motionchange` samples instead of 3.  Cheaper devices can take longer to lock onto GPS.
- [Changed] Android foregroundService notification priority set to `PRIORITY_MIN` so that notification doesn't always appear on top.
- [Fixed] Android plugin was not nullifying the odometer reference location when `#stop` method is executed, resulting in erroneous odometer calculations if plugin was stopped, moved some distance then started again.
- [Added] Android plugin will detect presense of Sensors `ACCELEROMETER`, `GYROSCOPE`, `MAGNETOMETER` and `SIGNIFICANT_MOTION`.  If any of these sensors are missing, the Android `ActivityRecognitionAPI` is considered non-optimal and plugin will add extra intelligence to assist determining when device is moving.
- [Fixed] Bug in broadcast event `GEOFENCE` not being fired when `MainActivity` is terminated (only applies to those use `HeadlessJS`).
- [Added] Implement Javascript API for `removeAllListeners` for...you guessed it:  removing all event-listeners.
- [Fixed] Android scheduler issue when device is rebooted and plugin is currently within a scheduled ON period (fails to start)
- [Fixed] (Android) Fix error calling `stopWatchPosition` before `#configure` callback has executed.  Also add support for executing `#getCurrentPosition` before `#configure` callback has fired.
- [Added] (Android) Listen to LocationResult while stopTimeout is engaged and perform manual motion-detection by checking if location distance from stoppedAtLocation is > stationaryRadius
- [Fixed] Bug in literal schedule parsing for both iOS and Android
- [Fixed] Bug in Android scheduler after app terminated.  Configured schedules were not having their `onTime` and `offTime` zeroed, resulting in incorrect time comparison.

## [2.8.3] - 2017-06-15
- [Fixed] Bug in Android scheduler after app terminated.  Configured schedules were not having their `SECOND` and `MILLISECOND` zeroed resulting in incorrect time comparison.

## [2.8.2] - 2017-06-14
- [Added] New config `stopOnStationary` for both iOS and Android.  Allows you to automatically `#stop` tracking when the `stopTimeout` timer elapses.
- [Added] Support for configuring the "Large Icon" (`notificationLargeIcon`) on Android `foregroundService` notification.  `notificationIcon` has now been aliased -> `notificationSmallIcon`.
- [Fixed] iOS timing issue when fetching `motionchange` position after initial `#start` -- since the significant-location-changes API (SLC) is engaged in the `#stop` method and eagerly returns a location ASAP, that first SLC location could sometimes be several minutes old and come from cell-tower triangulation (ie: ~1000m accuracy).  The plugin could mistakenly capture this location as the `motionchange` location instead of waiting for the highest possible accuracy location that was requested.  SLC API will be engaged only after the `motionchange` location has been received.
- [Fixed] Headless JS `RNBackgroundGeolocationEventReceiver` was broken in `react-native 0.45.0` -- they removed a mechanism for fetching the `ReactApplication`.  Changed to using a much simpler, backwards-compatible mechansim using simple `context.getApplicationContext()`.
- [Fixed] On Android, when adding a *massive* number of geofences (ie: *thousands*), it can take several minutes to perform all `INSERT` queries.  There was a threading issue which could cause the main-thread to be blocked while waiting for the database lock from the geofence queries to be released, resulting in an ANR (app isn't responding) warning.
- [Changed] Changing the Android foreground-service notification is now supported (you no longer need to `#stop` / `#start` the plugin for changes to take effect).
- [Added] New config option `httpTimeout` (milliseconds) for configuring the timeout where the plugin will give up on sending an HTTP request.
- [Added] Improved `react-native link` automation for iOS.  XCode setup is now completely handled!
- [Fixed] Android bug in `RNBackgroundGeolocationEventReceiver`.  Catch errors when `ReactNativeApplication` can not be referenced (this can happen during `startOnBoot` when the react native App has not yet booted, thus no `registerHeadlessTask` has been executed yet.  It can also occur if the plugin has not been configured with `foregroundService: true` -- Headless JS **requires** `foregroundService: true`)
- [Fixed] When iOS engages the `stopTimeout` timer, the OS will pause background-execution if there's no work being performed, in spite of `startBackgroundTask`, preventing the `stopTimeout` timer from running.  iOS will now keep location updates running at minimum accuracy during `stopTimeout` to prevent this.
- [Fixed] Ensure iOS background "location" capability is enabled before asking `CLLocationManager` to `setBackgroundLocationEnabled`.
- [Added] Implement ability to provide literal dates to schedule (eg: `2017-06-01 09:00-17:00`)
- [Added] When Android motion-activity handler detects `stopTimeout` has expired, it will initiate a `motionchange` without waiting for the `stopTimeout` timer to expire (there were cases where the `stopTimeout` timer could be delayed from firing due likely to vendor-based battery-saving software)

## [2.8.1] - 2017-05-12
- [Fixed] iOS has a new hook to execute an HTTP flush when network reachability is detected.  However, it was not checking if `autoSync: true` or state of `autoSyncThreshold`.

## [2.8.0] - 2017-05-08
- [Added] When iOS detects a network connection with `autoSync: true`, an HTTP flush will be initiated.
- [Fixed] Improve switching between tracking-mode location and geofence.  It's not necessary to call `#stop` before executing `#start` / `#startGeofences`.
- Fixed] iOS `maximumAge` with `getCurrentPosition` wasn't clearing the callbacks when current-location-age was `<= maximumAge`
- [Fixed] iOS when `#stop` is executed, nullify the odometer reference location.
- [Fixed] iOS issue with `preventSuspend: true`:  When a `motionchange` event with `is_moving: false` occurred, the event was incorrectly set to `heartbeat` instead of `motionchange`.
- [Fixed] Android null pointer exception when using `startOnBoot: true, forceReloadOnBoot: true`:  there was a case where last known location failed to return a location.  The lib will check for null location in this case.
- [Changed] iOS minimum version is now `8.4`.  Plugin will log an error when used on versions of iOS that don't implement the method `CLLocationManager#requestLocation`
- [Fixed] iOS bug executing `#setConfig` multiple times too quickly can crash the plugin when multiple threads attempt to modify an `NSMutableDictionary`

## [2.7.1] - 2017-04-18
- [Fixed] Android was rounding `battery_level` to 1 decimal place.
- [Fixed] iOS geofences-only mode was not using significant-location-change events to evaluate geofences within proximity.
- [Changed] iOS now uses `CLLocationManager requestLocation` to request the `motionchange` position, rather than counting samples.  This is a more robust way to get a single location
- [Fixed] iOS crash when providing `null` values in `Object` config options (ie: `#extras`, `#params`, `#headers`, etc)
- [Fixed] iOS was creating `backgroundTask` in `location` listener even if no listeners were registered, resulting in growing list of background-tasks which would eventually be `FORCE KILLED`.
- [Added] New config option `locationsOrderDirection [ASC|DESC]` for controlling the order that locations are selected from the database (and synced to your server).  Defaults to `ASC`.
- [Added] Support for React Native "Headless JS"
- [Added] Support for iOS geofence `DWELL` transitions.

## [2.7.0] - 2017-03-09
- [Changed] Updated **proguard config** to ignore `com.transistorsoft.**` -- `tslocationmanager.aar` is *already* pro-guarded.
- [Fixed] iOS bug when composing geofence data for peristence.  Sometimes it appended a `location.geofence.location` due to a shared `NSDictionary`
- [Fixed] Android issue with applying default settings the first time an app boots.  If you execute `#getState` before `#configure` is called, `#getState` would return an empty `{}`.
- [Changed] The licensing model of Android now enforces license only for **release** builds.  If an invalid license is configured while runningin **debug** mode, a Toast warning will appear **"BackgroundGeolocation is running in evaluation mode."**, but the plugin *will* work.
- [Fixed] iOS bug with HTTP `401` handling.
- [Added] The Android plugin now broadcasts all its events using the Android `BroadcastReceiver` mechanism.  You're free to implement your own native Android handler to receive and react to these events as you wish.

## [2.6.1] - 2017-03-01
- [Changed] Refactor Settings Management for both iOS and Android.
- [Fixed] Android database migration issue when upgrading from very old version to latest.  Was missing "geofences" table migration.

## [2.6.0] - 2017-02-22
- [Fixed] Issue #186: `geofence` event not passing Geofence `#extras`.
- [Fixed] iOS geofence identifiers containing ":" character were split and only the last chunk returned.  The plugin itself prefixes all geofences it creates with the string `TSGeofenceManager:` and the string-splitter was too naive.  Uses a `RegExp` replace to clear the plugin's internal prefix.
- [Changed] Refactored API Documentation
- [Added] HTTP JSON template features.  See [HTTP Features](./docs/http.md).  You can now template your entire JSON request data sent to the server by the plugin's HTTP layer.

## [2.5.0] - 2017-02-08
- [Changed] **BREAKING** License key is no longer provided to `BackgroundGeolocation#configure` -- You will now add your license key to `AndroidManifest`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.transistorsoft.backgroundgeolocation.react">

    <application
      android:name=".MainApplication"
      android:allowBackup="true"
      android:label="@string/app_name"
      android:icon="@mipmap/ic_launcher"
      android:theme="@style/AppTheme">

      <!-- react-native-background-geolocation licence -->
      <meta-data android:name="com.transistorsoft.locationmanager.license" android:value="YOUR_LICENSE_KEY" />
      .
      .
      .
    </application>
</manifest>
```

## [2.4.2] - 2017-02-07
- [Changed] Make iOS plugin work for both pre and post `0.40.0`.
- [Fixed] Fix issue with Location Authorization alert popup up when not desired.  Fixes Issue #190

## [2.4.1] - 2017-01-12
- [Fixed] Rounded location attribute Floats rendered as string (eg: `speed`, `odometer`, `heading`).  Fixes issue #162

## [2.4.0] - 2017-01-11
- [Fixed] Support for `react-native-0.40.0`

## [2.3.0] - 2017-01-09
- [Fixed] Locale issue when formatting Floats.  Some locale use "," as decimal separator.  Force Locale -> US when performing rounding.  Proper locale will be applied during the JSON encoding.
- [Added] Add `mock` attribute to Location data; present when the location comes from a mock provider (ie: when location is being simulated).
- [Added] Ability to provide optional arbitrary meta-data `extras` on geofences.
- [Changed] Location parameters `heading`, `accuracy`, `odometer`, `speed`, `altitude`, `altitudeAccuracy` are now fixed at 2 decimal places.
- [Fixed] Bug reported with `EventBus already registered` error.  Found a few cases where `EventBus.isRegistered` was not being used.
- [Added] Android will attempt to auto-sync on heartbeat events.
- [Changed] permission `android.hardware.location.gps" **android:required="false"**`
- [Added] Implement `IntentFilter` to capture `MY_PACKAGE_REPLACED`, broadcast when user upgrades the app.  If you've configured `startOnBoot: true, stopOnTerminate: false` and optionally `foreceRelaodOnBoot: true`, the plugin will automatically restart when user upgrades the app.
- [Changed] When adding a geofence (either `#addGeofence` or `#addGeofences`), if a geofence already exists with the provided `identifier`, the plugin will first destroy the existing one before creating the new one.
- [Changed] Merge iOS platform.  You no longer have to install both projects.
- [Fixed] Use more precise Alarm mechanism for `stopTimeout`.  Was using `AlarmManager#set`.  Changed `AlaramManager#setExact` (only works for API >= `19`).  Some were reporting that `stopTimeout` could run several minutes beyond what was configured.
- [Fixed] Improve odometer accuracy.  Introduce `desiredOdometerAccuracy` for setting a threshold of location accuracy for calculating odometer.  Any location having `accuracy > desiredOdometerAccuracy` will not be used for odometer calculation.
- [Fixed] When configured with a schedule, the Schedule parser wasn't ordering the schedule entries by start-time.  Since the scheduler seeks the first matching schedule-entry, it could fail to pick the latest entry.
- [Changed] Add ability to set odometer to any arbitrary value.  Before, odometer could only be reset to `0` via `resetOdometer`.  The plugin now uses `setOdometer(Float, successFn, failureFn`.  `resetOdometer` is now just an alias for `setOdometer(0)`.  `setOdometer` will now internally perform a `#getCurrentPosition`, so it can know the exact location where the odometer was set at.  As a result, using `#setOdometer` is exactly like performing a `#getCurrentPosition` and the `success` / `failure` callbacks use the same method-signature, where the `success` callback is provided the `location`.

## [2.2.0] - 2016-11-21
- [Changed] The plugin will ignore `autoSyncThreshold` when a `motionchange` event occurs.
- [Fixed] Bug with Android geofences not posting `event: geofence` and the actual `geofence` data was missing (The data sent to Javascript callback was ok, just the data sent to HTTP.
- [Added] Geofences-only mode.  Start geofences-only mode with method `#startGeofences`.
- [Fixed] Bug in Android Scheduler, failing to `startOnBoot`.  Issue #985

## [2.1.3] - 2016-10-26
- [Added] Add new `@config {Boolean} geofenceInitialTriggerEntry [true]`, allowing you to control the behaviour of initially triggering a geofence when the device is already within the newly added geofence, defaults to `true`.

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
