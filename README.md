Background Geolocation for React Native (Android)
==============================

Cross-platform background geolocation module for React Native with battery-saving **"circular stationary-region monitoring"** and **"stop detection"**.

![Home](https://www.dropbox.com/s/4cggjacj68cnvpj/screenshot-iphone5-geofences-framed.png?dl=1)
![Settings](https://www.dropbox.com/s/mmbwgtmipdqcfff/screenshot-iphone5-settings-framed.png?dl=1)

Follows the [React Native Modules spec](https://facebook.github.io/react-native/docs/native-modules-ios.html#content).

## Installing the Plugin

```
$ npm install git+https://git@github.com:transistorsoft/react-native-background-geolocation-android.git --save

```

[See Installation Guide](docs/INSTALL.md)


## Configure your license

1. Login to Customer Dashboard to generate an application key:
[www.transistorsoft.com/shop/customers](http://www.transistorsoft.com/shop/customers)
![](https://gallery.mailchimp.com/e932ea68a1cb31b9ce2608656/images/b2696718-a77e-4f50-96a8-0b61d8019bac.png)

2. Add your license-key to the module's params when executing `#configure`
```Javascript
BackgroundGeolocation.configure({
  license: <YOUR LICENSE KEY>,  // <-- Add your license key here
  desiredAccuracy: 0,
  distanceFilter: 50,
  .
  .
  .
}, function(state) {
  console.log('- Configure success, state: ', state);
  // Module is now ready to use.
});
```

## Using the plugin ##

```Javascript
import BackgroundGeolocation from 'react-native-background-geolocation-android';
```

## Documentation
- [API Documentation](docs)
- [Location Data Schema](../../wiki/Location-Data-Schema)
- [Error Codes](../../wiki/Error-Codes)
- [Debugging Sounds](../../wiki/Debug-Sounds)
- [Geofence Features](../../wiki/Geofence-Features)
  
## Help

[See the Wiki](../..//wiki)

## Example

```Javascript

import BackgroundGeolocation from 'react-native-background-geolocation-android';

BackgroundGeolocation.configure({
  // License validations
  license: '<your license key>',
  
  // Geolocation config
  desiredAccuracy: 0,
  distanceFilter: 50,
  locationUpdateInterval: 5000,
  fastestLocationUpdateInterval: 5000,
  
  // Activity Recognition config
  minimumActivityRecognitionConfidence: 80,   // 0-100%.  Minimum activity-confidence for a state-change 
  activityRecognitionInterval: 10000,
  stopDetectionDelay: 1,  // <--  minutes to delay after motion stops before engaging stop-detection system
  stopTimeout: 2, // 2 minutes

  // Application config
  debug: true, // <-- enable this hear sounds for background-geolocation life-cycle.
  forceReloadOnLocationChange: false,  // <-- [Android] If the user closes the app **while location-tracking is started** , reboot app when a new location is recorded (WARNING: possibly distruptive to user) 
  forceReloadOnMotionChange: false,    // <-- [Android] If the user closes the app **while location-tracking is started** , reboot app when device changes stationary-state (stationary->moving or vice-versa) --WARNING: possibly distruptive to user) 
  forceReloadOnGeofence: false,        // <-- [Android] If the user closes the app **while location-tracking is started** , reboot app when a geofence crossing occurs --WARNING: possibly distruptive to user) 
  stopOnTerminate: false,              // <-- [Android] Allow the background-service to run headless when user closes the app.
  startOnBoot: true,                   // <-- [Android] Auto start background-service in headless mode when device is powered-up.
    
  // HTTP / SQLite config
  url: 'http://posttestserver.com/post.php?dir=cordova-background-geolocation',
  batchSync: false,       // <-- [Default: false] Set true to sync locations to server in a single HTTP request.
  maxBatchSize: 100,      // <-- If using batchSync: true, specifies the max number of records send with each HTTP request.
  autoSync: true,         // <-- [Default: true] Set true to sync each location to server as it arrives.
  maxDaysToPersist: 1,    // <-- Maximum days to persist a location in plugin's SQLite database when HTTP fails
  headers: {
    "X-FOO": "bar"
  },
  params: {
    "auth_token": "maybe_your_server_authenticates_via_token_YES?"
  }
}, function(state) {
  console.log('- BackgroundGeolocation is ready to use', state);
  if (!state.enabled) {
    BackgroundGeolocation.start();
  }
});
    
var Foo = React.createClass({
  getInitialState() {
    
    // This handler fires whenever bgGeo receives a location update.
    BackgroundGeolocation.on('location', function(location) {
      console.log('- [js]location: ', JSON.stringify(location));
    });
    
    // This handler fires whenever bgGeo receives an error
    BackgroundGeolocation.on('error', function(error) {
      var type = error.type;
      var code = error.code;
      alert(type + " Error: " + code);
    });

    // This handler fires when movement states changes (stationary->moving; moving->stationary)
    BackgroundGeolocation.on('motionchange', function(location) {
      var isMoving = location.is_moving;
      console.log('- [js]motionchanged: ', JSON.stringify(location));
    });
    
    // This handler fires after every success HTTP response
    BackgroundGeolocation.on('http', function(response) {
      var statusCode = response.status;
      var responseText = response.responseText;
      console.log('- [js]http: ', statusCode, responseText);
    });
    
    BackgroundGeolocation.configure({
      desiredAccuracy: 0,
      distanceFilter: 50,
      locationUpdateInterval: 5000,
      activityRecognitionInterval: 10000
    } function(state) {
      console.log('- [js] BackgroundGeolocation successfully configure', state);

      if (!state.enabled) {
        BackgroundGeolocation.start();
      }
      // Call #stop to halt all tracking
      // BackgroundGeolocation.stop();
    
    });
  }
});

```

## [Advanced Demo Application for Field-testing](https://github.com/transistorsoft/rn-background-geolocation-demo)

A fully-featured [Demo App](https://github.com/transistorsoft/rn-background-geolocation-demo) is available in its own public repo.  After first cloning that repo, follow the installation instructions in the **README** there.  This demo-app includes a settings-screen allowing you to quickly experiment with all the different settings available for each platform.

![Home](https://www.dropbox.com/s/4cggjacj68cnvpj/screenshot-iphone5-geofences-framed.png?dl=1)
![Settings](https://www.dropbox.com/s/mmbwgtmipdqcfff/screenshot-iphone5-settings-framed.png?dl=1)

## Help!  It doesn't work!

Yes it does.  [See the Wiki](https://github.com/transistorsoft/react-native-background-geolocation/wiki)

- on iOS, background tracking won't be engaged until you travel about **2-3 city blocks**, so go for a walk or car-ride (or use the Simulator with ```Debug->Location->City Drive```)
- Android is much quicker detecting movements; typically several meters of walking will do it.
- When in doubt, **nuke everything**:  First delete the app from your device (or simulator)

## Behaviour

The plugin has features allowing you to control the behaviour of background-tracking, striking a balance between accuracy and battery-usage.  In stationary-mode, the plugin attempts to descrease its power usage and accuracy by setting up a circular stationary-region of configurable #stationaryRadius.  

iOS has a nice system  [Significant Changes API](https://developer.apple.com/library/ios/documentation/CoreLocation/Reference/CLLocationManager_Class/CLLocationManager/CLLocationManager.html#//apple_ref/occ/instm/CLLocationManager/startMonitoringSignificantLocationChanges), which allows the os to suspend your app until a cell-tower change is detected (typically 2-3 city-block change) 

The plugin will execute your configured event-listener subscribed to by the `onLocation(callback)` method.  Both iOS & Android use a SQLite database to persist **every** recorded geolocation so you don't have to worry about persistence when no network is detected.  The plugin provides a Javascript API to fetch and destroy the records in the database.  In addition, the plugin has an optional HTTP layer allowing allowing you to automatically HTTP POST recorded geolocations to your server.

The function `changePace({Boolean})` is provided to force the plugin to enter `moving` or `stationary` state.

## iOS

The plugin uses iOS Significant Changes API, and starts triggering your configured `callback` only when a cell-tower switch is detected (i.e. the device exits stationary radius). 

When the plugin detects the device has moved beyond its configured `#stationaryRadius` (typically 2-3 city blocks), it engages the native location API for **aggressive monitoring** according to the configured `#desiredAccuracy`, `#distanceFilter`.

The plugin will continue tracking the device's location even after the user closes the app (`stopOnTerminate: false`) or reboots the device.

# License

The MIT License (MIT)

Copyright (c) 2015 Chris Scott, Transistor Software

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.


