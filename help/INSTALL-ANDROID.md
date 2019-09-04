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

```

### :open_file_folder: **`android/app/build.gradle`**

```diff
apply from: "../../node_modules/react-native/react.gradle"

+Project background_geolocation = project(':react-native-background-geolocation-android')
+apply from: "${background_geolocation.projectDir}/app.gradle"
.
.
.
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

If you've enabled **`def enableProguardInReleaseBuilds = true`** in your `app/build.gradle`, be sure to add the BackgroundGeolocation SDK's `proguard-rules.pro` to your **`proguardFiles`**:

### :open_file_folder: `android/app/build.gradle`)

```diff
/**
 * Run Proguard to shrink the Java bytecode in release builds.
 */
def enableProguardInReleaseBuilds = true
.
.
.
android {
    .
    .
    .
    buildTypes {
        release {
            minifyEnabled enableProguardInReleaseBuilds
            proguardFiles getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro"
            // Add following proguardFiles (leave existing one above untouched)
+           proguardFiles "${background_geolocation.projectDir}/proguard-rules.pro"
            signingConfig signingConfigs.release
        }
    }
}
```

:warning: If you get error `"ERROR: Could not get unknown property 'background_geolocation' for project ':app'"`, see [above](https://github.com/transistorsoft/react-native-background-geolocation-android/blob/integrate-proguard-rules/help/INSTALL-ANDROID-AUTO.md#open_file_folder-androidappbuildgradle) and make sure to define the `Project background_geolocation`.


