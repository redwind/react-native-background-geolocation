declare module "react-native-background-geolocation-android" {
	interface WatchPositionRequest {
    interval?: number;
    desiredAccuracy?: number;
    persist?: boolean;
    extras?: Object;
    timeout?: number;
  }
}