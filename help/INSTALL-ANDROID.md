# Android Manual Installation

### With `yarn`

```shell
yarn add https://github.com/transistorsoft/react-native-background-geolocation-android.git
```

### With `npm`
```shell
npm install git+https://git@github.com:transistorsoft/react-native-background-geolocation-android.git --save
```

## Gradle Configuration

### :open_file_folder: **`android/settings.gradle`**

```diff
+include ':react-native-background-geolocation-android'
+project(':react-native-background-geolocation-android').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-background-geolocation-android/android')

+include ':react-native-background-fetch'
+project(':react-native-background-fetch').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-background-fetch/android')

```

-------------------------------------------------------------------------------


### :open_file_folder: **`android/build.gradle`**

Add the `googlePlayServicesLocation` Gradle variable.  This controls the version of `play-services:location` the SDK will use.

:information_source: You should always strive to use the latest available Google Play Services libraries.  You can determine the latest available version [here](https://developers.google.com/android/guides/setup).

```diff
buildscript {
    ext {
        buildToolsVersion = "28.0.3"
        minSdkVersion = 16
        compileSdkVersion = 28
        targetSdkVersion = 27
        supportLibVersion = "28.0.0"
        // You can control the SDK's version of play-services:location
        // You should always use the latest available version.
+       googlePlayServicesLocationVersion = "16.0.0"
    }
    .
    .
    .
}

allprojects {
    repositories {
        mavenLocal()
        google()
        jcenter()
        maven {
            // All of React Native (JS, Obj-C sources, Android binaries) is installed from npm
            url "$rootDir/../node_modules/react-native/android"
        }
+       maven {
+           url "$rootDir/../node_modules/react-native-background-geolocation-android/android/libs"
+       }
+       maven {
+           url "$rootDir/../node_modules/react-native-background-fetch/android/libs"
+       }
    }
}
```

### :open_file_folder: **`android/app/build.gradle`**

```diff

dependencies {
+   implementation project(':react-native-background-geolocation-android')
+   implementation project(':react-native-background-fetch')
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


## MainApplication.java

### :open_file_folder: `android/app/main/java/com/.../MainApplication.java`

```diff
+import com.transistorsoft.rnbackgroundgeolocation.*;
+import com.transistorsoft.rnbackgroundfetch.RNBackgroundFetchPackage;
public class MainApplication extends ReactApplication {
  @Override
  protected List<ReactPackage> getPackages() {
    return Arrays.<ReactPackage>asList(
+     new RNBackgroundGeolocation(),
+     new RNBackgroundFetchPackage(),
      new MainReactPackage()
    );
  }
}
```

## Proguard Config

If you've enabled **`def enableProguardInReleaseBuilds = true`** in your `app/build.gradle`, be sure to add the following items to your `proguard-rules.pro`:

### :open_file_folder: `proguard-rules.pro` (`android/app/proguard-rules.pro`)

```proguard
-keepnames class com.transistorsoft.rnbackgroundgeolocation.RNBackgroundGeolocation
-keepnames class com.facebook.react.ReactActivity

# BackgroundGeolocation
-keep class com.transistorsoft.** { *; }
-dontwarn com.transistorsoft.**

# BackgroundGeolocation (EventBus)
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
