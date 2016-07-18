# Installation

## Android

```bash
$ npm install git+https://git@github.com:transistorsoft/react-native-background-geolocation-android.git --save
```

* In `android/settings.gradle`

```gradle
...
include ':react-native-background-geolocation'
project(':react-native-background-geolocation').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-background-geolocation-android/android')
```

* In `android/app/build.gradle`

```gradle
...
repositories {
    flatDir {
        dirs "../../node_modules/react-native-background-geolocation-android/android/libs"
    }
}
dependencies {
  ...
  compile project(':react-native-background-geolocation')
  compile(name: 'tslocationmanager', ext: 'aar')
}
```

* MainActivity.java (1 of 2):  Import the module.

```java
import com.transistorsoft.rnbackgroundgeolocation.*;
```

* **react-native >= `0.17.0`**: MainActivity.java (2 of 2)  Register the module:

```java
public class MainActivity extends ReactActivity {
    .
    .
    .
    @Override
    protected List<ReactPackage> getPackages() {
        return Arrays.<ReactPackage>asList(
            new RNBackgroundGeolocation(),      // <-- for background-geolocation
            new MainReactPackage()
        );
    }
```


* In your `AndroidManifest.xml` (`android/app/src/AndroidManifest.xml`)

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.transistorsoft.rnbackgroundgeolocationsample">

    <application
      android:allowBackup="true"
      android:label="@string/app_name"
      android:icon="@mipmap/ic_launcher"
      android:theme="@style/AppTheme">
      .
      .
      .
      <!-- background-location services (1 of 2) -->
      <service android:name="com.transistorsoft.locationmanager.BackgroundGeolocationService" />
      <service android:name="com.transistorsoft.locationmanager.LocationService" />
      <service android:name="com.transistorsoft.locationmanager.ActivityRecognitionService" />
      <service android:name="com.transistorsoft.locationmanager.geofence.GeofenceService" />
      <service android:name="com.transistorsoft.locationmanager.scheduler.ScheduleService" />
      <service android:name="com.transistorsoft.locationmanager.scheduler.ScheduleAlarmService" />
      <receiver android:enabled="true" android:exported="false" android:name="com.transistorsoft.locationmanager.BootReceiver">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
            </intent-filter>
        </receiver>

      <!-- /background-location -->
    </application>

    <!-- background-geolocation permissions (2 of 2) -->
    
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