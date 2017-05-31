# iOS Installation with rnpm

```shell
$ npm install git+https://git@github.com:transistorsoft/react-native-background-geolocation-android.git --save
```

#### With React Native 0.27+

```shell
react-native link react-native-background-geolocation-android
react-native link react-native-background-fetch
react-native link cocoa-lumberjack
```

#### With older versions of React Native

You need [`rnpm`](https://github.com/rnpm/rnpm) (`npm install -g rnpm`)

```shell
rnpm link react-native-background-geolocation-android
rnpm link react-native-background-fetch
rnpm link cocoa-lumberjack
```

## XCode Configuration

- Edit **`Info.plist`**.  The plugin adds default values for the following `plist` elements.  You will need to change these values as desired.

| Key | Value | Description |
|---|---|---|
| NSLocationAlwaysUsageDescription | CHANGEME: Location Always Usage Description |
| NSMotionUsageDescription | CHANGEME: Motion updates increase battery efficiency by intelligently toggling location-services when device is detected to be moving |

![](https://www.dropbox.com/s/j7udsab7brlj4yk/Screenshot%202016-09-22%2008.33.53.png?dl=1)



