# Android Installation with `react-native link`

### With `yarn`

```shell
yarn add https://github.com/transistorsoft/react-native-background-geolocation-android.git
```

### With `npm`
```shell
npm install git+https://git@github.com:transistorsoft/react-native-background-geolocation-android.git --save
```

### `react-native link`
```shell
react-native link react-native-background-geolocation-android
react-native link react-native-background-fetch
```

### `react-native < 0.60.0`

If you're using react-native **`< 0.60.0`**, you must also install and link the following dependency:

```shell
// with yarn
yarn add cocoa-lumberjack
// with npm
npm install cocoa-lumberjack

react-native link cocoa-lumberjack
```

:warning: If you're *upgrading* to `0.60.0`, you must **unlink and remove** `cocoa-lumberjack`.

## Gradle Configuration

The `react-native link` command has automatically added a new Gradle `ext` parameter **`googlePlayServicesLocationVersion`**.  You can Use this to control the version of `play-services:location` used by the Background Geolocation SDK.

:information_source: You should always strive to use the latest available Google Play Services libraries.  You can determine the latest available version [here](https://developers.google.com/android/guides/setup).

### :open_file_folder: **`android/build.gradle`**

```diff
buildscript {
    ext {
+       googlePlayServicesLocationVersion = "17.0.0"  # or latest
        buildToolsVersion = "28.0.3"
        minSdkVersion = 16
        compileSdkVersion = 28
        targetSdkVersion = 27
        supportLibVersion = "28.0.0"

    }
    .
    .
    .
}
```

## AndroidManifest.xml

Paste the following `<meta-data />` element into the `AndroidManifest`, replaced with your license key:

```diff
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.transistorsoft.backgroundgeolocation.react">

  <application
    android:name=".MainApplication"
    android:allowBackup="true"
    android:label="@string/app_name"
    android:icon="@mipmap/ic_launcher"
    android:theme="@style/AppTheme">

    <!-- react-native-background-geolocation licence -->
+   <meta-data android:name="com.transistorsoft.locationmanager.license" android:value="YOUR_LICENCE_KEY_HERE" />
    .
    .
    .
  </application>
</manifest>

```


## Proguard Config

If you've enabled **`def enableProguardInReleaseBuilds = true`** in your `app/build.gradle`, be sure to add the following items to your `proguard-rules.pro`:

### :open_file_folder: `proguard-rules.pro` (`android/app/proguard-rules.pro`)

```proguard
-keepnames class com.transistorsoft.rnbackgroundgeolocation.RNBackgroundGeolocation
-keepnames class com.facebook.react.ReactActivity

# BackgroundGeolocation lib tslocationmanager.aar is *already* proguarded
-keep class com.transistorsoft.** { *; }
-dontwarn com.transistorsoft.**

# BackgroundGeolocation (EventBus)
-keepclassmembers class * extends de.greenrobot.event.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

# logback
-keep class ch.qos.** { *; }
-keep class org.slf4j.** { *; }
-dontwarn ch.qos.logback.core.net.*

# OkHttp3
-dontwarn okio.**
```

