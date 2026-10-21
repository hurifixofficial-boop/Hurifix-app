# Hurifix Google Drive media integration

This build uses the Android OAuth client configured for package `com.example` and the release SHA-1 supplied by the project owner. Media is uploaded using the `drive.file` scope to a Drive folder named `Hurifix Order Media` in `hurifixservices@gmail.com`.

## In-app behavior
- Dispatch Order supports selecting multiple customer images/videos; selected media uploads before the order is saved.
- Mark Complete opens a full-screen completion page with optional Before/After photos, rating, and review.
- Save opens a confirmation dialog. Confirm uploads selected technician photos and records completion; the existing customer WhatsApp prompt is then shown.
- Completed Order history shows separate links for customer media and technician Before/After photos.
- Media URLs are stored with the order in Room and Firestore. A Room 11→12 migration adds the new nullable URL columns without deleting existing local order data.

## Notes
- The app prompts for Google authorization when needed and reuses the signed-in account when Drive permission is already granted.
- The Drive account must be `hurifixservices@gmail.com` when the authorization flow appears.
- OAuth consent-screen publishing/testing settings are managed in Google Cloud Console. If the consent screen remains in Testing, refresh-token lifetime limitations may require reauthorization later.
- Build and test this project in Android Studio before installing a release APK. This environment did not have Gradle/Android SDK tooling available, so a successful APK build could not be verified here.
