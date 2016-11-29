# Android Manual Installation

```bash
$ npm install git+https://git@github.com:transistorsoft/react-native-background-geolocation-android.git --save
```

## Gradle Configuration

* In `android/settings.gradle`

```diff
+include ':react-native-background-geolocation'
+project(':react-native-background-geolocation').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-background-geolocation-android/android')
```

* In `android/app/build.gradle`

```diff
+repositories {
+    flatDir {
+        dirs "../../node_modules/react-native-background-geolocation-android/android/libs"
+    }
+}
dependencies {
+  compile project(':react-native-background-geolocation')
+  compile(name: 'tslocationmanager', ext: 'aar')
}
```

## MainApplication.java

* **`MainApplication.java`** (`android/app/main/java/com/.../MainApplication.java`)

```diff
+import com.transistorsoft.rnbackgroundgeolocation.*;
public class MainApplication extends ReactApplication {
  @Override
  protected List<ReactPackage> getPackages() {
    return Arrays.<ReactPackage>asList(
+     new RNBackgroundGeolocation(),
      new MainReactPackage()
    );
  }
}
```

## AndroidManifest.xml

* In your `AndroidManifest.xml` (`android/app/src/AndroidManifest.xml`)

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
    <uses-permission android:name="android.permission.VIBRATE" />
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

## Proguard Config
* In your `proguard-rules.pro` (`android/app/proguard-rules.pro`)

```proguard
# react-native-background-geolocation uses EventBus 3.0
# ref http://greenrobot.org/eventbus/documentation/proguard/
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# logback-android
-keep class ch.qos.** { *; }
-keep class org.slf4j.** { *; }
-dontwarn ch.qos.logback.core.net.*
```
