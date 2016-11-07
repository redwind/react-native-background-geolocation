# Android RNPM Installation

```shell
npm install git+https://git@github.com:transistorsoft/react-native-background-geolocation-android.git --save
```

#### With React Native 0.27+

```shell
react-native link react-native-background-geolocation-android
```

#### With older versions of React Native

You need [`rnpm`](https://github.com/rnpm/rnpm) (`npm install -g rnpm`)

```shell
rnpm link react-native-background-geolocation-android
```

## Gradle Configuration

RNPM does a nice job, but we need to do a bit of manual setup.

* In **`android/app/build.gradle`**

```diff
...
+repositories {
+    flatDir {
+        dirs "../../node_modules/react-native-background-geolocation-android/android/libs"
+    }
+}
dependencies {
+  compile(name: 'tslocationmanager', ext: 'aar')
}
```

## AndroidManifest.xml

* In your **`AndroidManifest.xml`** (`android/app/src/AndroidManifest.xml`)

```xml
<manifest>

    <application>
      .
      .
      .
      <!-- background-location services (1 of 2) -->
      <service android:name="com.transistorsoft.locationmanager.BackgroundGeolocationService" />
      <service android:name="com.transistorsoft.locationmanager.LocationService" />
      <service android:name="com.transistorsoft.locationmanager.ActivityRecognitionService" />
      <service android:name="com.transistorsoft.locationmanager.geofence.GeofenceService" />
      <service android:name="com.transistorsoft.locationmanager.scheduler.ScheduleAlarmService" />
      <receiver android:enabled="true" android:exported="false" android:name="com.transistorsoft.locationmanager.BootReceiver">
        <intent-filter>
          <action android:name="android.intent.action.BOOT_COMPLETED" />
        </intent-filter>
      </receiver>

      <!-- /background-location -->
    </application>

    <!-- background-geolocation permissions (2 of 2) -->
    <uses-feature android:name="android.hardware.location.gps" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_LOCATION_EXTRA_COMMANDS" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="com.google.android.gms.permission.ACTIVITY_RECOGNITION" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
    <uses-permission android:name="android.permission.GET_TASKS" />

    <!-- optional: for method #emailLog during debug mode; allows the log-file to be attached to email -->
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />

    <!-- /background-geolocation -->
</manifest>

```

## Proguard

* In your `proguard-rules.pro` (`android/app/proguard-rules.pro`)

```proguard
# react-native-background-geolocation uses EventBus 3.0
# ref http://greenrobot.org/eventbus/documentation/proguard/
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
```
