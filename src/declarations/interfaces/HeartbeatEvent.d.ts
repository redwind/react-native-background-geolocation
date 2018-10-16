declare module "react-native-background-geolocation-android" {
  /**
  * The event-object provided to [[BackgroundGeolocation.onHeartbeat]]
  *
  * @example
  * ```typescript
  * BackgroundGeolocation.onHeartbeat(heartbeatEvent => {
  *   console.log('[heartbeat] ', heartbeatEvent);
  * });
  * ```
  */
  interface HeartbeatEvent {
    location: Location;
  }
}