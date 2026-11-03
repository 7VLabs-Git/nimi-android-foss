# Nimi Android - repair pass

## Changes in this pass
- Removed five hardcoded fictional nearby members.
- Removed seeded Connaught Place map reports. The safety map now starts empty.
- Removed synthetic straight-line route and fabricated "safer detour" generation. Routes are returned only when the configured road-routing service returns an actual route.
- Prevented a failed typed source geocode from silently falling back to the device's current location.
- Removed the hardcoded Connaught Place GPS fallback. The app now starts with an unknown (0,0 sentinel) location until a real location fix arrives.
- Added persistent Light/Dark theme preference and a Home-screen toggle.
- Removed misleading claims that local state changes alert remote community members.

## Important limitations that remain
This is not a production emergency-response system. There is no user authentication, remote backend, push-notification service, server-side community map, or verified report moderation. SOS to nearby Nimi users therefore cannot work yet. The trusted-contact flow can open the phone's SMS composer; the user must review and send it. The fake-call screen is a local simulation, not a carrier call.

The public OSRM routing endpoint is a demo service and may rate-limit or be unavailable. No route is shown when it fails. A route with no known reports is NOT guaranteed safe. OpenStreetMap community reports are not included unless the user adds them locally.

## Build / verification
This repair archive has not been compiled or run on a physical Android device in this environment. Open the project in Android Studio, allow Gradle sync, build the debug variant, and test GPS/map behavior outdoors before relying on it. Treat all emergency functions as unverified until end-to-end tested.
