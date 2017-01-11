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
