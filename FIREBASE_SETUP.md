# Firebase setup for WayWayApp

## Authentication

Enable these providers in Firebase Console > Authentication > Sign-in method:

- Email/password
- Google
- Facebook

For Google login:

1. Add the app SHA-1 and SHA-256 fingerprints in Firebase Console.
2. Download a fresh `google-services.json` into `app/google-services.json`.
3. Create or copy the Web client ID from Google Cloud OAuth clients.
4. Add it to `local.properties`:

```properties
GOOGLE_WEB_CLIENT_ID=your-web-client-id.apps.googleusercontent.com
```

To create the first admin account, add the Firebase Auth email to `local.properties`:

```properties
ADMIN_EMAILS=admin@example.com
```

You can add multiple admins with commas:

```properties
ADMIN_EMAILS=admin@example.com,owner@example.com
```

After rebuilding, sign in with one of those emails. The app will create/update
`users/{uid}` in Firestore with `role = ADMIN` and open the admin driver screen.

For Facebook login:

1. Create a Facebook app in Meta for Developers.
2. Add the Firebase OAuth redirect URI to Facebook Login settings.
3. Add these values to `local.properties`:

```properties
FACEBOOK_APP_ID=fb_your_app_id_or_your_app_id
FACEBOOK_CLIENT_TOKEN=your_facebook_client_token
```

## Firestore

The app writes:

- `users/{uid}` after login/register
- `drivers/{uid}` when a driver goes online/offline
- `driver_locations/{uid}` every few seconds while the driver is online

Upload `firebase/firestore.rules` to Firestore Rules before testing with real users.

## Driver location flow

Driver side:

```text
DriverHomeScreen -> DriverViewModel -> DriverLocationRepository
-> driver_locations/{driverId}
```

User side can listen with:

```kotlin
DriverLocationRepository()
    .observeDriverLocation(driverId)
```

Use that flow to update the driver marker on a booking/tracking map.
