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
